# BankOps — State & Configuration Drift Enforcement (Puppet)

This directory contains the production-ready **Puppet** standalone manifests and modules designed to govern operating-system security baselines, user permissions, service daemons, and automated configuration drift remediation for the **Enterprise Banking System**.

---

## Separation of Concerns: Puppet vs. Ansible

A foundational requirement of the BankOps architecture is eliminating functional overlap between tools:

| Dimension | Ansible (Phase 3) | Puppet (Phase 4) |
|---|---|---|
| **Paradigm** | Procedural orchestration (Push) | Declarative state management (Pull/Apply) |
| **Primary Role** | Bootstrapping, repo pulling, secret templating, container startup | Ongoing OS compliance, security baselines, drift correction |
| **Execution Trigger** | Run once during deployments or release updates | Scheduled via cron or trigger on detected file/system drift |
| **Core Deliverable** | Live Dockerized microservices stack running on ports 80/8088 | Self-healing OS files, hardened SSH, 0600 on secrets, Docker daemon running |

---

## Directory Structure

```
puppet/
├── manifests/
│   └── site.pp                 # Main node entrypoint manifest
├── modules/
│   └── banking_hardening/      # Complete host hardening module
│       └── manifests/
│           ├── init.pp         # Master class with dependency containment
│           ├── user.pp         # Dedicated 'bankingapp' service user & group
│           ├── permissions.pp  # Strict 0600 on .env, 0750 on /opt/banking
│           ├── services.pp     # Docker, SSH, and Cron daemon state enforcement
│           └── security.pp     # SSH hardening (PermitRootLogin=no) & fail2ban
├── scripts/
│   ├── apply-puppet.sh         # Convenience wrapper to apply manifests
│   └── test-drift.sh           # Automated drift & recovery test script for viva evaluation!
└── README.md                  # This documentation guide
```

---

## Installation & Execution on Ubuntu Host

### Step 1: Install Puppet Standalone
On the target EC2 Linux instance:
```bash
sudo apt-get update
sudo apt-get install -y puppet
```

### Step 2: Apply Hardening Manifests
Run the apply script:
```bash
sudo bash /opt/banking/puppet/scripts/apply-puppet.sh
```
Or directly via the puppet binary:
```bash
sudo puppet apply --modulepath=/opt/banking/puppet/modules /opt/banking/puppet/manifests/site.pp
```

---

## Live Evaluation Demonstration: The Drift Test

During your project defense / teacher review, run the provided demonstration script:

```bash
sudo bash /opt/banking/puppet/scripts/test-drift.sh
```

### What this script proves:
1. **Initial Baseline:** Displays `/opt/banking/.env` with safe `0600` permissions.
2. **Drift Injection:** Intentionally tampers with the file using `chmod 777 /opt/banking/.env`, exposing critical database and JWT secrets.
3. **Automated Remediation:** Runs Puppet, which audits the filesystem, detects the security violation, and outputs:
   ```text
   Notice: /Stage[main]/Banking_hardening::Permissions/File[/opt/banking/.env]/mode: mode changed '0777' to '0600'
   ```
4. **Verified Recovery:** Checks the file mode again to confirm permissions have been automatically restored to `0600`.
