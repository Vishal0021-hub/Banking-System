# ==============================================================================
#  BankOps — Master Puppet Node Manifest
#  Target: Ubuntu 22.04 LTS Application and Database Host
#
#  Usage:
#    puppet apply --modulepath=/opt/banking/puppet/modules /opt/banking/puppet/manifests/site.pp
# ==============================================================================

node default {
  # Include complete banking host hardening and baseline configuration
  include banking_hardening
}
