# -*- mode: ruby -*-
# vi: set ft=ruby :


Vagrant.configure(2) do |config|

  config.vm.synced_folder "/Users/davide/Documents/Projects/bps/bps", "/bps"

  config.vm.box = "yungsang/boot2docker"
  config.vm.network "forwarded_port", guest: 80, host: 7000
  config.vm.network "forwarded_port", guest: 8080, host: 7001

  config.vm.provision "shell", inline: <<-SHELL

  alias deploy="ls "

  SHELL

end
