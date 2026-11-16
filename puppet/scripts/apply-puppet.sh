#!/bin/bash
# ==============================================================================
#  BankOps — Apply Puppet Hardening Manifest
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PUPPET_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=================================================================="
echo "  BankOps: Applying Puppet Configuration & Drift Enforcement"
echo "=================================================================="

# Run puppet in standalone apply mode
puppet apply \
  --modulepath="$PUPPET_ROOT/modules" \
  --detailed-exitcodes \
  "$PUPPET_ROOT/manifests/site.pp" || EXIT_CODE=$?

case "$EXIT_CODE" in
  0)
    echo "[OK] Puppet run completed. System is fully compliant (no changes needed)."
    ;;
  2)
    echo "[SUCCESS] Puppet detected configuration drift and successfully enforced compliant state!"
    ;;
  *)
    echo "[ERROR] Puppet encountered an issue during execution (code: $EXIT_CODE)."
    exit $EXIT_CODE
    ;;
esac
