#! /bin/bash

# Set up openfaas authentication
mkdir -p /var/lib/faasd/secrets/
echo "${basic_auth_user}" > /var/lib/faasd/secrets/basic-auth-user
echo "${basic_auth_password}" > /var/lib/faasd/secrets/basic-auth-password

# Setup timeout of service
sudo mkdir /etc/systemd/system/faasd-provider.service.d
sudo echo $'[Service]\nEnvironment="service_timeout=5m5s"' | sudo tee /etc/systemd/system/faasd-provider.service.d/override.conf


# Install openfaas
curl -sfL https://raw.githubusercontent.com/openfaas/faasd/master/hack/install.sh | bash -s -

# Set up timeouts to support timeouts of max. 5 minutes
sudo sed -i 's/read_timeout=60s/read_timeout=5m/g' /var/lib/faasd/docker-compose.yaml
sudo sed -i 's/write_timeout=60s/write_timeout=5m/g' /var/lib/faasd/docker-compose.yaml
sudo sed -i 's/upstream_timeout=65s/upstream_timeout=5m2s/g' /var/lib/faasd/docker-compose.yaml
sudo systemctl restart faasd