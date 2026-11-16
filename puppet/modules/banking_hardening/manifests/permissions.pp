# ==============================================================================
#  Puppet Class: banking_hardening::permissions
#  Enforces strict filesystem permissions and remediates configuration drift
# ==============================================================================

class banking_hardening::permissions {

  # Base deployment directory
  file { '/opt/banking':
    ensure => directory,
    owner  => 'bankingapp',
    group  => 'banking',
    mode   => '0750',
  }

  # Secrets and runtime environment file — STRICT 0600 mode (Owner only)
  file { '/opt/banking/.env':
    ensure  => file,
    owner   => 'bankingapp',
    group   => 'banking',
    mode    => '0600',
    require => File['/opt/banking'],
  }

  # Database initialization SQL script
  file { '/opt/banking/init-db.sql':
    ensure  => file,
    owner   => 'bankingapp',
    group   => 'banking',
    mode    => '0640',
    require => File['/opt/banking'],
  }

  # Docker Compose orchestration file
  file { '/opt/banking/docker-compose.yml':
    ensure  => file,
    owner   => 'bankingapp',
    group   => 'banking',
    mode    => '0644',
    require => File['/opt/banking'],
  }
}
