# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  config.vm.synced_folder ".", "/bps"

  config.vm.box = "yungsang/boot2docker"
  config.vm.network "forwarded_port", guest: 80, host: 7000
  config.vm.network "forwarded_port", guest: 8080, host: 7001

  config.vm.provision "shell", inline: <<-SHELL

  # Enable provisioning with a shell script. Additional provisioners such as
  # Puppet, Chef, Ansible, Salt, and Docker are also available. Please see the
  # documentation for more information about their specific syntax and use.
  # config.vm.provision "shell", inline: <<-SHELL
  #   sudo apt-get update
  #   sudo apt-get install -y apache2
  # SHELL
end
