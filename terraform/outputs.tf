# ==============================================================================
#  BankOps — Terraform Output Values
# ==============================================================================

output "vpc_id" {
  description = "ID of the created Banking VPC"
  value       = aws_vpc.banking_vpc.id
}

output "public_subnet_id" {
  description = "ID of the public subnet"
  value       = aws_subnet.public.id
}

output "private_subnet_id" {
  description = "ID of the private subnet"
  value       = aws_subnet.private.id
}

output "app_server_instance_id" {
  description = "EC2 Instance ID of the Banking Application Server"
  value       = aws_instance.app_server.id
}

output "app_server_public_ip" {
  description = "Public IPv4 address of the Banking Application Server"
  value       = aws_instance.app_server.public_ip
}

output "app_server_private_ip" {
  description = "Private IPv4 address of the Banking Application Server"
  value       = aws_instance.app_server.private_ip
}

output "frontend_url" {
  description = "Web address for the React Banking UI"
  value       = aws_instance.app_server.public_ip != "" ? "http://${aws_instance.app_server.public_ip}" : "N/A (Private Subnet)"
}

output "api_gateway_url" {
  description = "Endpoint for Spring Cloud API Gateway"
  value       = aws_instance.app_server.public_ip != "" ? "http://${aws_instance.app_server.public_ip}:8088" : "N/A (Private Subnet)"
}

output "eureka_dashboard_url" {
  description = "Eureka Service Discovery Dashboard"
  value       = aws_instance.app_server.public_ip != "" ? "http://${aws_instance.app_server.public_ip}:8761" : "N/A (Private Subnet)"
}

output "ssh_login_command" {
  description = "Direct SSH login command"
  value       = aws_instance.app_server.public_ip != "" ? "ssh -i <path-to-private-key> ubuntu@${aws_instance.app_server.public_ip}" : "ssh -i <path-to-private-key> -J ubuntu@<bastion-ip> ubuntu@${aws_instance.app_server.private_ip}"
}

output "ansible_inventory_block" {
  description = "Pre-formatted snippet ready to paste into ansible/inventory/hosts.ini for Phase 3"
  value       = <<-EOT
  [app_servers]
  banking-node ansible_host=${aws_instance.app_server.public_ip != "" ? aws_instance.app_server.public_ip : aws_instance.app_server.private_ip} ansible_user=ubuntu
  EOT
}
