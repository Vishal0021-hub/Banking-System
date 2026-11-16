#!/bin/bash
# ==============================================================================
#  BankOps — Live Demonstration: Configuration Drift & Automated Remediation
#  Use this script during evaluation to prove Puppet drift enforcement!
# ==============================================================================

set -e

ENV_FILE="/opt/banking/.env"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=================================================================="
echo "  STEP 1: Verify Current Compliant State"
echo "=================================================================="
if [ -f "$ENV_FILE" ]; then
    ls -la "$ENV_FILE"
else
    echo "Creating demo .env file at $ENV_FILE..."
    touch "$ENV_FILE"
    chmod 0600 "$ENV_FILE"
    ls -la "$ENV_FILE"
fi

echo ""
echo "=================================================================="
echo "  STEP 2: Simulating Intentional Configuration Drift (Tampering)"
echo "  Changing .env permissions to world-readable (0777)!"
echo "=================================================================="
chmod 777 "$ENV_FILE"
echo "Current corrupted state:"
ls -la "$ENV_FILE"
echo "-> VULNERABILITY DETECTED: File is now world-readable and writable!"

echo ""
echo "=================================================================="
echo "  STEP 3: Triggering Puppet Desired-State Enforcement"
echo "=================================================================="
bash "$SCRIPT_DIR/apply-puppet.sh"

echo ""
echo "=================================================================="
echo "  STEP 4: Verifying Automated Remediation"
echo "=================================================================="
ls -la "$ENV_FILE"

PERMS=$(stat -c "%a" "$ENV_FILE" 2>/dev/null || stat -f "%Lp" "$ENV_FILE")
if [ "$PERMS" = "600" ]; then
    echo ""
    echo ">>> DEMO PASSED: Puppet automatically detected the drift and restored permissions to 0600! <<<"
else
    echo ">>> UNEXPECTED PERMISSION: $PERMS <<<"
fi
