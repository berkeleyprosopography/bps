# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  
  config.vm.box = "bpsteam/bps-bare"

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.

  # Web
  config.vm.network "forwarded_port", guest: 80, host: 7000
  # Tomcat
  config.vm.network "forwarded_port", guest: 8080, host: 8080
  # MySql port for debugging
  config.vm.network "forwarded_port", guest: 3306, host: 3306
  # java debugger port (defined in /etc/default/tomcat6)
  config.vm.network "forwarded_port", guest: 9876, host: 9876  

  config.vm.synced_folder ".", "/vagrant"
  # Need to map the folder properly for a deploy target for the War files
  config.vm.synced_folder "/var/local/tomcat/webapps", "/var/lib/tomcat6/webapps"
  # Need to map a folder for a deploy target for the web files
  config.vm.synced_folder "/var/www/bps", "/var/www/bps"

  # Enable provisioning with a shell script. Additional provisioners such as
  # Puppet, Chef, Ansible, Salt, and Docker are also available. Please see the
  # documentation for more information about their specific syntax and use.
  # config.vm.provision "shell", inline: <<-SHELL
  #   sudo apt-get update
  #   sudo apt-get install -y apache2
  # SHELL
end
