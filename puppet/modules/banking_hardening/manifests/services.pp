# ==============================================================================
#  Puppet Class: banking_hardening::services
#  Enforces required daemon states to ensure container engine & SSH availability
# ==============================================================================

class banking_hardening::services {

  # Ensure Docker service is running and starts at boot
  service { 'docker':
    ensure     => running,
    enable     => true,
    hasrestart => true,
    hasstatus  => true,
  }

  # Ensure cron service is active for scheduled tasks
  service { 'cron':
    ensure     => running,
    enable     => true,
    hasrestart => true,
    hasstatus  => true,
  }

  # Ensure SSH daemon remains running for secure administration
  service { 'ssh':
    ensure     => running,
    enable     => true,
    hasrestart => true,
    hasstatus  => true,
  }
}
