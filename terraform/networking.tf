# ==============================================================================
#  BankOps — VPC and Subnet Networking
# ==============================================================================

# Dedicated Banking VPC
resource "aws_vpc" "banking_vpc" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "bankops-vpc-${var.environment}"
  }
}

# Internet Gateway for public subnet traffic
resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.banking_vpc.id

  tags = {
    Name = "bankops-igw-${var.environment}"
  }
}

# Public Subnet (Reverse Proxy / Ingress / Bastion)
resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.banking_vpc.id
  cidr_block              = var.public_subnet_cidr
  availability_zone       = var.availability_zone
  map_public_ip_on_launch = true

  tags = {
    Name = "bankops-public-subnet-${var.environment}"
    Tier = "Public"
  }
}

# Private Subnet (Internal Services / Isolated Databases)
resource "aws_subnet" "private" {
  vpc_id                  = aws_vpc.banking_vpc.id
  cidr_block              = var.private_subnet_cidr
  availability_zone       = var.availability_zone
  map_public_ip_on_launch = false

  tags = {
    Name = "bankops-private-subnet-${var.environment}"
    Tier = "Private"
  }
}

# Public Route Table -> Internet Gateway
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.banking_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }

  tags = {
    Name = "bankops-public-rt-${var.environment}"
  }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

# Elastic IP for NAT Gateway (only created if NAT Gateway enabled)
resource "aws_eip" "nat_eip" {
  count  = (!var.deploy_in_public_subnet && var.enable_nat_gateway) ? 1 : 0
  domain = "vpc"

  tags = {
    Name = "bankops-nat-eip-${var.environment}"
  }

  depends_on = [aws_internet_gateway.igw]
}

# NAT Gateway for private subnet outbound access (optional)
resource "aws_nat_gateway" "nat_gw" {
  count         = (!var.deploy_in_public_subnet && var.enable_nat_gateway) ? 1 : 0
  allocation_id = aws_eip.nat_eip[0].id
  subnet_id     = aws_subnet.public.id

  tags = {
    Name = "bankops-nat-gw-${var.environment}"
  }

  depends_on = [aws_internet_gateway.igw]
}

# Private Route Table
resource "aws_route_table" "private" {
  vpc_id = aws_vpc.banking_vpc.id

  dynamic "route" {
    for_each = (!var.deploy_in_public_subnet && var.enable_nat_gateway) ? [1] : []
    content {
      cidr_block     = "0.0.0.0/0"
      nat_gateway_id = aws_nat_gateway.nat_gw[0].id
    }
  }

  tags = {
    Name = "bankops-private-rt-${var.environment}"
  }
}

resource "aws_route_table_association" "private" {
  subnet_id      = aws_subnet.private.id
  route_table_id = aws_route_table.private.id
}
