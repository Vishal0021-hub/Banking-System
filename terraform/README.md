# BankOps — Infrastructure as Code (Terraform AWS)

This directory contains the production-ready **Terraform** configuration for provisioning the cloud infrastructure required by the **Enterprise Banking System** on Amazon Web Services (AWS).

---

## Architecture Overview

```
                      +------------------------------------------+
                      |         AWS Cloud (us-east-1)            |
                      |                                          |
                      |     Dedicated VPC (10.0.0.0/16)          |
                      |                                          |
   Internet --------->|    Internet Gateway (IGW)                |
                      |               |                          |
                      |      Public Subnet (10.0.1.0/24)         |
                      |               |                          |
                      |    [Banking App Server (t3.large)]       |
                      |    - Docker Engine & Compose             |
                      |    - React Frontend (Port 80)            |
                      |    - API Gateway (Port 8088)             |
                      |    - Eureka Server (Port 8761)           |
                      |    - 7 Core Microservices                |
                      |    - MySQL, Redis, Kafka, Zookeeper      |
                      |                                          |
                      |      Private Subnet (10.0.2.0/24)        |
                      |      (Optional for Multi-Tier RDS)       |
                      +------------------------------------------+
```

### Security Group Ingress Rules
* **Port 80 (HTTP) & 443 (HTTPS):** Public ingress for the React Frontend.
* **Port 8088 (API Gateway):** Ingress for banking REST API calls.
* **Port 22 (SSH):** Administrative shell restricted to `admin_ip`.
* **Port 8761 (Eureka):** Service registry dashboard restricted to `admin_ip`.
* **Ports 3306 (MySQL), 6379 (Redis), 9092 (Kafka):** Completely isolated from the internet; accepting connections only within the application security group.

---

## File Structure

```
terraform/
├── providers.tf               # Terraform AWS provider declaration (~> 5.40)
├── variables.tf               # Input parameter definitions with defaults
├── networking.tf              # VPC, Subnets, Internet Gateway, Route Tables
├── security.tf                # Security Groups with firewall access rules
├── ec2.tf                     # EC2 Instance, KeyPair, and User Data bootstrap
├── outputs.tf                 # Public/private IPs, URLs, and Ansible inventory snippet
├── terraform.tfvars.example   # Sample variable file for easy customization
└── README.md                  # This documentation guide
```

---

## Quick Start Guide

### Step 1: Configure AWS CLI Credentials
Ensure your AWS credentials are configured on your machine:
```bash
aws configure
# AWS Access Key ID: [Your Key]
# AWS Secret Access Key: [Your Secret]
# Default region name: us-east-1
# Default output format: json
```

### Step 2: Initialize Configuration
Copy the example variables file:
```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
```
Edit `terraform.tfvars` to customize:
* `ssh_key_name` or `ssh_public_key`
* `admin_ip` (set your current public IP, e.g. `$(curl -s https://checkip.amazonaws.com)/32`)

### Step 3: Initialize Terraform
Download the AWS provider plugins:
```bash
terraform init
```

### Step 4: Preview Infrastructure (Dry Run)
Check the execution plan:
```bash
terraform plan
```

### Step 5: Provision Infrastructure
Deploy the resources to AWS:
```bash
terraform apply
# Type 'yes' when prompted
```

Upon completion, Terraform will output:
* `app_server_public_ip`
* `frontend_url`
* `api_gateway_url`
* `eureka_dashboard_url`
* `ssh_login_command`
* `ansible_inventory_block` (ready for Phase 3)

---

## Tearing Down (Cost Optimization)
When you finish testing or demonstration, destroy all created AWS resources with a single command to avoid unnecessary cloud costs:
```bash
terraform destroy
# Type 'yes' when prompted
```
