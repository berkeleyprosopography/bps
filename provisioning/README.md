# BPS Provisioning

## Overview

This folder contains the provisioning mechanisms for BPS in the following scenarios:

 * Baremetal (native) 
 * Development on Vagrant
 * Development on Docker

Provisioning is done through a set of Ansible scripts. in particular:

 * `playbook-*` takes care of deploying an entire BPS instance starting from the selected platform.
 * `deploy-*` scripts allow to have a quick shortcut to deploy parts of the BPS system at will. These are especially useful in development. 


### Variables

You may need to adjust the values in `variables.yml` to match your use-case:

    ---
    project_name: bps
    project_root: [directory where the tree will be copied | e.g. /srv/bps]
    project_repo: [repo url, only needed in playbook-native | e.g. https://github.com/berkeleyprosopography/bps.git]
    branch: [repo branch, only needed in playbook-native | e.g. master]
    user: [unix user | e.g. bps-user ]
    user_password: [bps-user password | e.g. password123]
    db_user: bps-database-user
    db_password: [bps-database-user password | e.g. password123]
    db_name: [database name | e.g. bpsdev]
    tomcat_admin: [tomcat admin user | e.g. tomcat]
    tomcat_admin_password: [tomcat admin user password | e.g. password123]


## Deploying natively

Clone the BPS git repo in a directory of your choice:

    git clone git@github.com:berkeleyprosopography/bps.git
    
and start the provisioning process with

	cd bps
    ansible-playbook -i provisioning/hosts provisioning/playbook-native.yml

When done, BPS will be running on port 80.

## Deploying via Vagrant

### Selecting a Vagrantfile
Three `Vagrantfile` files are made available:
 * `Vagrantfile.dev`: is what you will use most of the time. It starts from the `bpsteam/bps-bare` image, which is meant to bootstrap the development process and comes mostly pre-configured. It's a good choice if you just want to get a BPS instance going quickly. **This is the default choice if you want to develop for BPS**
 * `Vagrantfile.prod`: starts from a `hashicorp/precise32` image, and requires more time to deploy, as all packages need to be installed afresh. It's a good choice if you are developing/testing deployment components, or just prefer to build your system from scratch. *This vagrantfile uses the native provisioning script*.
 
There is also a third `Vagrantfile.boot2docker`, which is a convenience image to be used on non-linux native systems, should you want to use docker. Usage is described in the Docker section and not applicable to this section.

Before starting, you will need to copy or symlink the `Vagrantfile` of your choice in the project root. For example, to use the default `Vagrantfile.dev`:
    
    ln -s provisioning/Vagrantfile.dev Vagrantfile
    
or 

	cp provisioning/Vagrantfile.dev Vagrantfile
    
right before you `vagrant up`. 

### Configuration

#### Port mappings
The following port mappings are available: 

| Service  | Host Port | Guest port  | Enabled in dev  | Enabled in prod |
|---|---|---|---|---|
| HTTP (Apache)  | 7000 | 80  |  Yes | Yes
| Tomcat (Management) |  8080 | 8080  | Yes  | No
| Mysql | 3306  | 3306  | Yes  | No
| Java debug  | 9876  | 9876  | Yes  | No

#### Shared folders

The main difference between the `dev` and `prod` configurations has to do with where the tree is sourced from: in `dev`, it's mounted in read-only mode as the `/bps` folder on the vagrant filesystem. in `prod`, it's pulled from the git `project_repo` and `branch` variables specified in `variables.yml`.

In case you wish to build the warfile on your host machine, you will want to comment out the following two lines from `Vagrantfile.dev`:

    # Map the folder for a deploy target for the War files 
    config.vm.synced_folder "/var/local/tomcat/webapps", "/var/lib/tomcat6/webapps"

And adjust the host paths to where your build system is deploying the war file

The same applies for running maven to build the webcontent host-side:

    # Map a folder for a deploy target for the web files
    config.vm.synced_folder "/var/www/bps", "/var/www/bps"

### Running
Clone the BPS git repo in a directory of your choice in your host machine:

    git clone git@github.com:berkeleyprosopography/bps.git

Enter the directory and copy the Vagrantfile of your choice as detailed above

	cd bps
    ln -s provisioning/Vagrantfile.dev Vagrantfile
    vagrant up

and start the provisioning process with

	cd bps
    ansible-playbook -i provisioning/hosts provisioning/playbook-vagrant.yml

BPS will be available on `localhost:7000`

## Deploying on Docker

### Quickstart 

Supposing you have the BPS tree at `/home/user/bps`, Just run:

    docker pull bpsteam/bps:initial
	docker run -i -t -p 80:7000 -v /home/user/bps:/bps bpsteam/bps:latest /bin/bash
    ansible-playbook -i /bps/provisioning/hosts /bps/provisioning/playbook-docker.yml

### Building the Docker image
Clone the BPS git repo in a directory of your choice in your host machine:

    git clone git@github.com:berkeleyprosopography/bps.git

Enter the directory and copy the Dockerfile of your choice as detailed above

	cd bps
    ln -s provisioning/Vagrantfile.dev Vagrantfile
    docker build .

### Using Boot2Docker
A convenience image is provided if you are running Windows or OSX. This will runn a lightweight ubuntu system, on which you can then do all the above. To use:

	git clone git@github.com:berkeleyprosopography/bps.git
    cd bps
    ln -s provisioning/Vagrantfile.boot2docker Vagrantfile
    vagrant up
    vagrant ssh 
    docker pull bpsteam/bps:initial
	docker run -i -t -p 80:7000 -v /home/user/bps:/bps bpsteam/bps:latest /bin/bash
    ansible-playbook -i /bps/provisioning/hosts /bps/provisioning/playbook-docker.yml
    
BPS will be available on port 7000 on your host machine. 
