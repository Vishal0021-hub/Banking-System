# BankOps — Configuration Management & Deployment Automation (Ansible)

This directory contains the production-ready **Ansible** automation playbooks and roles for configuring target Linux hosts, installing container runtimes, securely managing secrets, and orchestrating the multi-service **Enterprise Banking System**.

---

## Architecture & Responsibilities

In accordance with the BankOps architecture design:
* **Procedural Host Setup:** Installs system prerequisites, configures kernel parameters, and hardens UFW firewall rules.
* **Runtime Preparation:** Installs official Docker Engine and Compose plugin with automatic JSON-file log rotation.
* **Secrets Management:** Injects database credentials, JWT secrets, and Eureka tokens via Ansible Vault into an isolated `.env` file with `0600` permissions.
* **Phased Container Rollout:** Starts backing databases and message brokers first, waits for health checks, boots Eureka, launches all 7 Spring Boot microservices and API Gateway, and finally exposes the React Frontend.
* **Automated Smoke Testing:** Interrogates Spring Boot Actuator `/actuator/health` endpoints before declaring deployment success.

---

## Directory Structure

```
ansible/
├── ansible.cfg                # Global Ansible configuration (pipelining, YAML callback, SSH tuning)
├── inventory/
│   └── hosts.ini              # Server inventory (updated with Terraform output IP)
├── group_vars/
│   ├── all.yml                # Non-sensitive global variables (ports, endpoints, packages)
│   └── vault.yml.example      # Example encrypted secrets template
├── roles/
│   ├── common/                # OS packages, bankingapp user, UFW firewall rules
│   │   └── tasks/main.yml
│   ├── docker/                # Docker CE, CLI, Compose, daemon.json log rotation
│   │   ├── tasks/main.yml
│   │   └── handlers/main.yml
│   └── deploy/                # Repo pull, .env template, phased container rollout & health verification
│       ├── tasks/main.yml
│       ├── templates/env.j2
│       └── handlers/main.yml
├── site.yml                   # Master full-stack deployment playbook
├── deploy-app.yml             # Fast app-only deployment playbook (skips OS setup)
├── rollback.yml               # Graceful container shutdown & teardown playbook
└── README.md                  # This documentation guide
```

---

## Step-by-Step Execution Guide

### Step 1: Update Server Inventory
After running `terraform apply` in `../terraform/`, copy the public IP from the output:
```bash
# Example from terraform output:
# app_server_public_ip = "54.210.120.45"
```
Edit `inventory/hosts.ini`:
```ini
[app_servers]
banking-node-01 ansible_host=54.210.120.45 ansible_user=ubuntu ansible_ssh_private_key_file=~/.ssh/bankops-key.pem
```

### Step 2: Test Host Connectivity
Verify that Ansible can communicate with the provisioned EC2 instance:
```bash
ansible -i inventory/hosts.ini app_servers -m ping
```
*Expected response: `"ping": "pong"`*

### Step 3: (Optional) Encrypt Secrets with Ansible Vault
For production-grade security, copy and encrypt the vault secrets file:
```bash
cp group_vars/vault.yml.example group_vars/vault.yml
ansible-vault encrypt group_vars/vault.yml
# Enter a vault password when prompted
```

### Step 4: Execute Full Production Deployment
Run the master playbook:
```bash
# Standard deployment:
ansible-playbook -i inventory/hosts.ini site.yml

# Or with Ansible Vault password:
ansible-playbook -i inventory/hosts.ini site.yml --ask-vault-pass
```

The playbook will execute in sequential phases:
1. **Host Baseline:** Updates apt, creates `bankingapp` user, hardens UFW firewall.
2. **Docker Engine:** Installs Docker CE & Compose plugin with log rotation limits.
3. **Backing Infra:** Boots MySQL, Redis, Zookeeper, and Kafka. Waits for port availability.
4. **Service Discovery:** Starts Eureka Server and polls `/actuator/health` until UP.
5. **Microservices:** Launches API Gateway, Auth, User, Account, Transaction, Notification, Loan, and Admin services.
6. **Frontend:** Starts Nginx web server on Port 80.
7. **Verification:** Validates all health endpoints and prints the deployment summary.

---

## Useful Operational Commands

### Fast App-Only Update (Skip OS & Docker Setup)
When code or environment variables change, use the lightweight rollout playbook:
```bash
ansible-playbook -i inventory/hosts.ini deploy-app.yml
```

### Clean Stack Shutdown / Rollback
To stop all 14 banking containers cleanly:
```bash
ansible-playbook -i inventory/hosts.ini rollback.yml
```

### Manual Verification
Once deployed, verify running containers on the remote host:
```bash
ssh -i ~/.ssh/bankops-key.pem ubuntu@YOUR_SERVER_IP "docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'"
```
Access points:
* **Frontend UI:** `http://YOUR_SERVER_IP`
* **API Gateway Actuator:** `http://YOUR_SERVER_IP:8088/actuator/health`
* **Eureka Registry Dashboard:** `http://YOUR_SERVER_IP:8761`
