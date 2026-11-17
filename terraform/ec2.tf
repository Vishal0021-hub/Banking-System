# ==============================================================================
#  BankOps — EC2 Compute Instance & SSH Key Configuration
# ==============================================================================

# Query latest official Ubuntu 22.04 LTS (Jammy Jellyfish) AMI
data "aws_ami" "ubuntu_2204" {
  most_recent = true
  owners      = ["099720109477"] # Canonical official owner ID

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# Optional Key Pair creation if ssh_public_key is provided
resource "aws_key_pair" "bankops_key" {
  count      = (var.ssh_key_name == "" && var.ssh_public_key != "") ? 1 : 0
  key_name   = "bankops-key-${var.environment}"
  public_key = var.ssh_public_key

  tags = {
    Name = "bankops-ssh-key-${var.environment}"
  }
}

locals {
  selected_key_name = var.ssh_key_name != "" ? var.ssh_key_name : (
    length(aws_key_pair.bankops_key) > 0 ? aws_key_pair.bankops_key[0].key_name : null
  )
}

# Bootstrap User Data: prepares Linux packages and installs Docker prerequisites
locals {
  app_user_data = <<-EOF
              #!/bin/bash
              set -e

              # System update & Swapfile configuration for Free-Tier memory expansion
              export DEBIAN_FRONTEND=noninteractive
              fallocate -l 8G /swapfile || dd if=/dev/zero of=/swapfile bs=1M count=8192
              chmod 600 /swapfile
              mkswap /swapfile
              swapon /swapfile
              echo '/swapfile none swap sw 0 0' >> /etc/fstab
              sysctl vm.swappiness=20

              apt-get update -y
              apt-get upgrade -y
              apt-get install -y curl wget git jq ca-certificates gnupg lsb-release

              # Install official Docker Engine
              install -m 0755 -d /etc/apt/keyrings
              curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
              chmod a+r /etc/apt/keyrings/docker.asc

              echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

              apt-get update -y
              apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

              # Enable Docker service and add ubuntu user to docker group
              systemctl enable docker
              systemctl start docker
              usermod -aG docker ubuntu

              # Create application directory
              mkdir -p /opt/banking
              chown -R ubuntu:ubuntu /opt/banking

              echo "BankOps host initialization complete at $(date)" > /var/log/bankops-init.log
              EOF
}

# Primary Banking Application Server (Runs Dockerized Microservices & Backing Infra)
resource "aws_instance" "app_server" {
  ami                         = data.aws_ami.ubuntu_2204.id
  instance_type               = var.app_instance_type
  subnet_id                   = var.deploy_in_public_subnet ? aws_subnet.public.id : aws_subnet.private.id
  vpc_security_group_ids      = [aws_security_group.app_sg.id]
  key_name                    = local.selected_key_name
  associate_public_ip_address = var.deploy_in_public_subnet
  user_data                   = local.app_user_data

  root_block_device {
    volume_size           = var.root_volume_size
    volume_type           = "gp3"
    encrypted             = true
    delete_on_termination = true

    tags = {
      Name = "bankops-app-root-${var.environment}"
    }
  }

  tags = {
    Name = "bankops-app-server-${var.environment}"
    Role = "Application-Cluster-Host"
  }
}
