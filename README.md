# Enterprise Digital Banking System

Inspired by JPMorgan Chase internal banking platforms. A full-stack, microservices-based enterprise banking application.

## Tech Stack
- **Backend**: Java 21, Spring Boot 3.2, Spring Cloud (Eureka, Gateway, Feign), Spring Security, JWT, MySQL, Redis, Kafka, Maven.
- **Frontend**: React + Vite, Tailwind CSS, Shadcn/UI, Recharts, Framer Motion.

## Microservices Architecture
| Service | Port | Description |
|---|---|---|
| `eureka-server` | 8761 | Service Discovery |
| `api-gateway` | 8080 | Edge Server & Routing |
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

## Setup Instructions
1. Clone the repository.
2. Create the following MySQL databases: `banking_auth`, `banking_users`, `banking_accounts`, `banking_transactions`, `banking_notifications`, `banking_loans`.
3. Configure `.env` based on `.env.example`.
4. Start infrastructure services: MySQL, Redis, Kafka.
5. Build the project: `mvn clean install`.
6. Start services in order:
   - `eureka-server`
   - `api-gateway`
   - Remaining services.
7. Start frontend: `cd frontend && npm install && npm run dev`.

## Core Features
- **Security**: JWT-based auth with refresh token rotation.
- **Transfers**: ACID-compliant internal transfers with daily limits.
- **Fraud**: Real-time velocity and amount checks.
- **Statements**: PDF generation for transaction history.
- **Loans**: EMI calculation and application workflow.
- **Admin**: Dashboard for KYC, user lock, and monitoring.
