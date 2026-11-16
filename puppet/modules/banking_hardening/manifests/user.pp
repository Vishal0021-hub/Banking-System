# ==============================================================================
#  Puppet Class: banking_hardening::user
#  Enforces service account and group isolation for the banking platform
# ==============================================================================

class banking_hardening::user {

  # Enforce system group exists
  group { 'banking':
    ensure => present,
    system => true,
  }

  # Enforce dedicated non-root application service user exists
  user { 'bankingapp':
    ensure     => present,
    gid        => 'banking',
    groups     => ['docker'],
    home       => '/opt/banking',
    shell      => '/bin/bash',
    system     => true,
    managehome => false,
    require    => Group['banking'],
  }
}
