# -*- mode: ruby -*-
# vi: set ft=ruby :


Vagrant.configure(2) do |config|

  config.vm.box = "hashicorp/precise32"
  config.vm.network "forwarded_port", guest: 80, host: 7000
  config.vm.network "forwarded_port", guest: 8080, host: 7001

  config.vm.provision "ansible" do |ansible|
        ansible.playbook = "provisioning/playbook.yml"
        ansible.verbose = 'vvvv'
#        ansible.inventory_path = ".vagrant/provisioners/ansible/inventory/vagrant_ansible_inventory"
  end

end
