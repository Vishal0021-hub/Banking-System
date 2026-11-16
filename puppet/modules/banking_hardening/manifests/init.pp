# ==============================================================================
#  Puppet Module: banking_hardening
#  Master class coordinating host security, permissions, users, and daemons
# ==============================================================================

class banking_hardening {

  # Execute classes in explicit sequential dependency order
  contain banking_hardening::user
  contain banking_hardening::permissions
  contain banking_hardening::services
  contain banking_hardening::security

  Class['banking_hardening::user']
    -> Class['banking_hardening::permissions']
    -> Class['banking_hardening::services']
    -> Class['banking_hardening::security']
}
