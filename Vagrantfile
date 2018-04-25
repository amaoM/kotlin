# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  config.vm.box = "centos7"
  config.vm.network :private_network, ip: "192.168.170.170"

  config.vm.synced_folder ".", "/home/vagrant/kotlin", :create => true, :owner=> 'vagrant', :group=>'vagrant', :mount_options => ['dmode=777,fmode=777']
  config.vm.synced_folder "../ansible", "/vagrant", :create => true, :owner=> 'vagrant', :group=>'vagrant', :mount_options => ['dmode=777,fmode=666']

  config.vm.provider :virtualbox do |vb|
    vb.customize ["modifyvm", :id, "--memory", "1024"]
    vb.customize ["modifyvm", :id, "--usb", "on"]
  end

  if ARGV[0] == 'up'
    config.vm.provision "ansible_local" do |ansible|
      ansible.playbook = "kotlin.yml"
      ansible.limit = "server"
      ansible.inventory_path = 'inventory/localhost'
    end
  end
end
