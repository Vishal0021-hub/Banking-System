# Enterprise Digital Banking System

Inspired by JPMorgan Chase internal banking platforms. A full-stack, microservices-based enterprise banking application.

## Tech Stack
- **Backend**: Java 21, Spring Boot 3.2, Spring Cloud (Eureka, Gateway, Feign), Spring Security, JWT, MySQL, Redis, Kafka, Maven.
- **Frontend**: React + Vite, Tailwind CSS, Shadcn/UI, Recharts, Framer Motion.

## Microservices Architecture
| Service | Port | Description |
|---|---|---|
| `eureka-server` | 8761 | Service Discovery |
| `api-gateway` | 8088 | Edge Server & Routing |
| `auth-service` | 8081 | Identity Management & JWT |
| `user-service` | 8082 | Profile & KYC |
| `account-service` | 8083 | Accounts & Beneficiaries |
| `transaction-service` | 8084 | ACID Transfers & Fraud Detection |
| `notification-service` | 8085 | Notifications & Emails |
| `loan-service` | 8086 | Loans & EMI Calculator |
| `admin-service` | 8087 | Admin Dashboard Operations |

## Prerequisites
- Java 21
- Node.js 18+
- MySQL 8.0
- Redis
- Apache Kafka (Zookeeper + Kafka Broker)

## Environment Configuration (`.env`)
Create a `.env` file in the root directory with the following variables (or run `./run-backend.ps1` which automatically configures defaults):
```env
# Infrastructure
DB_HOST=localhost
DB_PORT=3307
DB_USERNAME=root
DB_PASSWORD=root

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Security
JWT_SECRET=bXlTdXBlclNlY3JldEtleUZvckJhbmtpbmdTeXN0ZW0xMjM0NTY3ODk=
JWT_EXPIRATION_MS=86400000
JWT_REFRESH_EXPIRATION_MS=604800000

# Email (Optional)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Eureka
EUREKA_USERNAME=eureka
EUREKA_PASSWORD=eureka123
```

## Setup Instructions
1. Clone the repository.
2. Start infrastructure containers using Docker Compose:
   ```bash
   docker compose up -d
   ```
3. Build all microservices:
   ```bash
   mvn clean install -DskipTests
   ```
4. Start backend services:
   ```powershell
   ./run-backend.ps1
   ```
5. Start frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## Core Features
- **Security**: JWT-based auth with refresh token rotation.
- **Transfers**: ACID-compliant internal transfers with daily limits.
- **Fraud**: Real-time velocity and amount checks.
- **Statements**: PDF generation for transaction history.
- **Loans**: EMI calculation and application workflow.
- **Admin**: Dashboard for KYC, user lock, and monitoring.
