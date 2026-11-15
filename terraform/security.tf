# ==============================================================================
#  BankOps — Security Groups (Firewall Rules)
# ==============================================================================

# Application & Gateway Security Group
resource "aws_security_group" "app_sg" {
  name        = "bankops-app-sg-${var.environment}"
  description = "Security rules for Banking App server and edge routing"
  vpc_id      = aws_vpc.banking_vpc.id

  # SSH Management Access
  ingress {
    description = "SSH access restricted to administrator IP"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.admin_ip]
  }

  # Frontend HTTP (Nginx Web UI)
  ingress {
    description = "Public HTTP web traffic to React frontend"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Frontend HTTPS (Optional TLS)
  ingress {
    description = "Public HTTPS secure web traffic"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # API Gateway Endpoint
  ingress {
    description = "API Gateway edge traffic"
    from_port   = 8088
    to_port     = 8088
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Eureka Server Dashboard (Admin Only)
  ingress {
    description = "Eureka Service Registry Dashboard (Admin restricted)"
    from_port   = 8761
    to_port     = 8761
    protocol    = "tcp"
    cidr_blocks = [var.admin_ip]
  }

  # Nagios NRPE / Monitoring Port
  ingress {
    description = "Nagios NRPE monitoring agent"
    from_port   = 5666
    to_port     = 5666
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  # Self-referencing ingress: all internal communication between containerized services
  ingress {
    description = "Allow internal inter-service communication within security group"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    self        = true
  }

  # Outbound rule: allow all outbound internet access for packages and container downloads
  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "bankops-app-sg-${var.environment}"
  }
}

# Dedicated Database & Backing Services Security Group (Least-Privilege Isolation)
resource "aws_security_group" "db_sg" {
  name        = "bankops-db-sg-${var.environment}"
  description = "Strict isolation for MySQL, Redis, and Kafka"
  vpc_id      = aws_vpc.banking_vpc.id

  # MySQL Port (3306) restricted exclusively to application instances
  ingress {
    description     = "MySQL access restricted to App Server SG only"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.app_sg.id]
  }

  # Redis Port (6379)
  ingress {
    description     = "Redis cache access restricted to App Server SG only"
    from_port       = 6379
    to_port         = 6379
    protocol        = "tcp"
    security_groups = [aws_security_group.app_sg.id]
  }

  # Kafka Broker Port (9092)
  ingress {
    description     = "Kafka event broker restricted to App Server SG only"
    from_port       = 9092
    to_port         = 9092
    protocol        = "tcp"
    security_groups = [aws_security_group.app_sg.id]
  }

  egress {
    description = "Allow outbound to VPC"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [var.vpc_cidr]
  }

  tags = {
    Name = "bankops-db-sg-${var.environment}"
  }
}
