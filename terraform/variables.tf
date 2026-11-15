# ==============================================================================
#  BankOps — Terraform Input Variables
# ==============================================================================

variable "aws_region" {
  description = "Target AWS deployment region"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Environment identifier tag (e.g. dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "vpc_cidr" {
  description = "CIDR block for the dedicated Banking VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  description = "CIDR block for the public ingress subnet"
  type        = string
  default     = "10.0.1.0/24"
}

variable "private_subnet_cidr" {
  description = "CIDR block for the private application/database subnet"
  type        = string
  default     = "10.0.2.0/24"
}

variable "availability_zone" {
  description = "Availability zone for subnet placement"
  type        = string
  default     = "us-east-1a"
}

variable "admin_ip" {
  description = "CIDR block permitted for SSH administrative access (e.g. YOUR_IP/32 or 0.0.0.0/0)"
  type        = string
  default     = "0.0.0.0/0"
}

variable "app_instance_type" {
  description = "EC2 instance type for the banking application host (t3.large recommended for 8GB RAM)"
  type        = string
  default     = "t3.large"
}

variable "root_volume_size" {
  description = "Size in GB of the root EBS volume (gp3) for Docker images and containers"
  type        = number
  default     = 50
}

variable "deploy_in_public_subnet" {
  description = "Set to true to assign a public IP directly to the app server for cost-effective single-node prototype deployment (avoids AWS NAT Gateway hourly cost). Set to false to keep strictly in private subnet behind NAT."
  type        = bool
  default     = true
}

variable "enable_nat_gateway" {
  description = "Whether to provision an AWS NAT Gateway for private subnet outbound internet access. (Only active if deploy_in_public_subnet is false)."
  type        = bool
  default     = false
}

variable "ssh_key_name" {
  description = "Name of an existing AWS EC2 Key Pair. If blank, ssh_public_key will be used to create one."
  type        = string
  default     = ""
}

variable "ssh_public_key" {
  description = "Public SSH key content (e.g. contents of ~/.ssh/id_rsa.pub or id_ed25519.pub)"
  type        = string
  default     = ""
}
