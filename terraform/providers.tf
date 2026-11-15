# ==============================================================================
#  BankOps — Terraform AWS Provider Configuration
# ==============================================================================

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.40"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "BankOps"
      System      = "Enterprise-Banking-System"
      ManagedBy   = "Terraform"
      Environment = var.environment
    }
  }
}
