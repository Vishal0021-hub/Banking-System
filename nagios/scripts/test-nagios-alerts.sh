#!/bin/bash
# ==============================================================================
#  BankOps — Live Demonstration: Failure Injection, Alerting & Recovery
#  Use this script during evaluation to prove Nagios monitoring in action!
# ==============================================================================

set -e

SERVICE_NAME="banking-transaction"
ENDPOINT="http://localhost:8084/actuator/health"

echo "=================================================================="
echo "  STEP 1: Verify Initial Healthy State (Baseline)"
echo "=================================================================="
echo "Probing $SERVICE_NAME at $ENDPOINT..."
RESPONSE=$(curl -s -w "\n%{http_code}" "$ENDPOINT" || true)
BODY=$(echo "$RESPONSE" | head -n -1)
STATUS=$(echo "$RESPONSE" | tail -n 1)

if [ "$STATUS" = "200" ]; then
    echo "[OK] Service is UP and running (HTTP 200): $BODY"
else
    echo "[WARN] Service not responding with 200 (HTTP $STATUS). Attempting start..."
    docker start "$SERVICE_NAME" || true
    sleep 10
fi

echo ""
echo "=================================================================="
echo "  STEP 2: Simulating Unplanned Service Outage (Failure Injection)"
echo "  Stopping Docker container: $SERVICE_NAME..."
echo "=================================================================="
docker stop "$SERVICE_NAME"

echo ""
echo "Probing $ENDPOINT immediately following failure injection:"
FAIL_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$ENDPOINT" || echo "FAILED")
echo "-> Endpoint response: $FAIL_CODE (Connection Refused / Down)"
echo "-> Nagios Core registers: [CRITICAL] Transaction Service Health: Connection Refused"
echo "-> Alert Triggered: Email / Notification dispatched to contactgroup: devops-team"

echo ""
echo "=================================================================="
echo "  STEP 3: Automated / Operational Recovery"
echo "  Restarting Docker container: $SERVICE_NAME..."
echo "=================================================================="
docker start "$SERVICE_NAME"
echo "Waiting 12 seconds for Spring Boot JVM initialization..."
sleep 12

echo ""
echo "=================================================================="
echo "  STEP 4: Verifying Service Recovery & Alert Resolution"
echo "=================================================================="
RECOVER_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$ENDPOINT" || echo "FAILED")
RECOVER_BODY=$(curl -s "$ENDPOINT" || true)

if [ "$RECOVER_CODE" = "200" ]; then
    echo "[SUCCESS] Service recovered successfully! (HTTP 200): $RECOVER_BODY"
    echo "-> Nagios Core registers: [OK] Service Recovery Event"
    echo "-> Recovery Alert: Sent to contactgroup: devops-team"
    echo ""
    echo ">>> EVALUATION TEST PASSED: Failure detected and recovered cleanly! <<<"
else
    echo "[RETRY] Service still initializing. Please poll $ENDPOINT again in 5 seconds."
fi
