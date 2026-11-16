# BankOps — Continuous Observability & Health Monitoring (Nagios Core)

This directory contains the production-ready **Nagios Core** monitoring specifications, check commands, contact notification groups, and Dockerized dashboard configurations for the **Enterprise Banking System**.

---

## Role in the BankOps Architecture

Nagios Core serves as the continuous observability layer:
* **Active Probing:** Periodically interrogates Spring Boot Actuator endpoints (`/actuator/health`) across all 9 microservices, confirming `status: "UP"`.
* **Infrastructure Tracking:** Probes TCP reachability for MySQL (3306), Redis (6379), Kafka (9092), and Zookeeper (2181).
* **Host Resource Auditing:** Monitors CPU load, memory utilization, and disk storage via NRPE.
* **Automated Alerting:** Dispatches notifications upon `WARNING` or `CRITICAL` state transitions and sends recovery confirmations when services return to `OK`.

---

## Service Monitoring Matrix

| Target Component | Port / Endpoint | Check Type | Warning Threshold | Critical Threshold |
|---|---|---|---|---|
| **Host CPU Load** | Host OS | NRPE `check_load` | Load > 70% | Load > 90% |
| **Host Memory** | Host OS | NRPE `check_mem` | Memory > 80% | Memory > 95% |
| **Host Disk Space** | `/` filesystem | NRPE `check_disk` | Disk > 75% | Disk > 90% |
| **MySQL Database** | Port 3307 / 3306 | `check_tcp` | Timeout > 3s | Connection Refused / Down |
| **Redis Cache** | Port 6379 | `check_tcp` | Timeout > 2s | Connection Refused / Down |
| **Kafka Broker** | Port 9092 | `check_tcp` | Timeout > 3s | Connection Refused / Down |
| **Zookeeper** | Port 2181 | `check_tcp` | Timeout > 2s | Connection Refused / Down |
| **Eureka Discovery** | Port 8761 `/actuator/health` | `check_http` | Status != UP | HTTP 5xx / Refused |
| **API Gateway** | Port 8088 `/actuator/health` | `check_http` | Status != UP | HTTP 5xx / Refused |
| **Auth Service** | Port 8081 `/actuator/health` | `check_http` | Status != UP | HTTP 5xx / Refused |
| **User Service** | Port 8082 `/actuator/health` | `check_http` | Status != UP | HTTP 5xx / Refused |
| **Account Service** | Port 8083 `/actuator/health` | `check_http` | Status != UP | HTTP 5xx / Refused |
| **Transaction Service** | Port 8084 `/actuator/health` | `check_http` | Status != UP | HTTP 5xx / Refused |
| **Notification Service** | Port 8085 `/actuator/health` | `check_http` | Status != UP | HTTP 5xx / Refused |
| **Loan Service** | Port 8086 `/actuator/health` | `check_http` | Status != UP | HTTP 5xx / Refused |
| **Admin Service** | Port 8087 `/actuator/health` | `check_http` | Status != UP | HTTP 5xx / Refused |
| **React Frontend** | Port 80 `/` | `check_http` | Response > 3s | HTTP 5xx / Refused |

---

## Directory Structure

```
nagios/
├── conf.d/
│   ├── banking_contacts.cfg     # Alert notification recipients and devops-team group
│   ├── banking_commands.cfg     # check_actuator_health, check_tcp_port, check_http_endpoint
│   ├── banking_host.cfg         # Banking server host definition
│   └── banking_services.cfg     # 16 active service checks
├── docker-compose.nagios.yml    # Single-command startup for Nagios Web Dashboard
├── scripts/
│   └── test-nagios-alerts.sh    # Evaluation script for live failure injection and recovery
└── README.md                    # This documentation guide
```

---

## Launching the Nagios Web Dashboard

To run the full Nagios Core web console locally or on the cloud instance:

```bash
docker compose -f nagios/docker-compose.nagios.yml up -d
```

### Accessing the Dashboard:
* **URL:** `http://localhost:8089` (or `http://YOUR_SERVER_IP:8089`)
* **Username:** `nagiosadmin`
* **Password:** `bankops123`

Click on **"Services"** in the left sidebar to see the real-time status grid showing all 16 banking components with green `OK` status indicators.

---

## Live Evaluation Demonstration: Failure Injection & Recovery Test

During project presentation, run the provided script to demonstrate real-time fault detection and recovery:

```bash
bash nagios/scripts/test-nagios-alerts.sh
```

### What this script proves:
1. **Healthy Baseline:** Shows `banking-transaction` responding with HTTP 200 `{"status":"UP"}`.
2. **Failure Injection:** Intentionally stops the container (`docker stop banking-transaction`).
3. **Detection & Alerting:** Probes the endpoint to show the failure. Nagios immediately flags the service as `[CRITICAL]` and dispatches an alert.
4. **Automated Recovery:** Restarts the container (`docker start banking-transaction`).
5. **State Restoration:** Confirms the endpoint returns to HTTP 200 and Nagios transitions the service back to `[OK]` with a recovery notification.
