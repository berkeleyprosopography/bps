FROM contrafactus/bps:latest

RUN if [ -f /bps/provisioning/hosts ]; then ansible-playbook -i /bps/provisioning/hosts /bps/provisioning/docker-deploy-war.yml; fi