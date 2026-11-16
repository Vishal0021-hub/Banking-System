# ==============================================================================
#  Puppet Class: banking_hardening::security
#  Enforces host OS security baselines and SSH hardening policies
# ==============================================================================

class banking_hardening::security {

  # Ensure essential security packages are present
  $security_packages = ['ufw', 'fail2ban', 'auditd', 'libpam-pwquality']

  package { $security_packages:
    ensure => present,
  }

  # SSH daemon hardening configuration snippet
  file { '/etc/ssh/sshd_config.d/99-bankops-hardening.conf':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => "# BankOps Automated Security Hardening via Puppet\nPermitRootLogin prohibit-password\nPasswordAuthentication no\nX11Forwarding no\nMaxAuthTries 4\nClientAliveInterval 300\nClientAliveCountMax 2\n",
    notify  => Service['ssh'],
  }

  # Ensure fail2ban intrusion prevention daemon is running
  service { 'fail2ban':
    ensure     => running,
    enable     => true,
    require    => Package['fail2ban'],
  }
}
