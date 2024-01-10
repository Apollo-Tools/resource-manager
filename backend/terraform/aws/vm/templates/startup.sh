#! /bin/bash

# Set up openfaas authentication
mkdir -p /var/lib/faasd/secrets/
echo "${basic_auth_user}" > /var/lib/faasd/secrets/basic-auth-user
echo "${basic_auth_password}" > /var/lib/faasd/secrets/basic-auth-password

# Setup timeout of service
sudo mkdir /etc/systemd/system/faasd-provider.service.d
sudo echo $'[Service]\nEnvironment="service_timeout=5m5s"' | sudo tee /etc/systemd/system/faasd-provider.service.d/override.conf

# Install node exporter
wget https://github.com/prometheus/node_exporter/releases/download/v1.7.0/node_exporter-1.7.0.linux-amd64.tar.gz
tar -xvzf node_exporter-1.7.0.linux-amd64.tar.gz
sudo cp node_exporter-1.7.0.linux-amd64/node_exporter /usr/local/bin
sudo chmod +x /usr/local/bin/node_exporter
sudo useradd -m -s /bin/bash node_exporter
sudo mkdir /var/lib/node_exporter
sudo chown -R node_exporter:node_exporter /var/lib/node_exporter
sudo echo $'[Unit]
Description=Node Exporter

[Service]
ExecStart=/usr/local/bin/node_exporter --web.listen-address=:{{PORT}} --collector.disable-defaults \
        --web.disable-exporter-metrics --collector.cpu --collector.meminfo --collector.filesystem \
        --collector.thermal_zone --collector.hwmon --collector.stat

[Install]
WantedBy=multi-user.target' | sudo tee /etc/systemd/system/node_exporter.service
sudo sed -i "s/{{PORT}}/${metrics_port}/g" /etc/systemd/system/node_exporter.service

# Install openfaas
curl -sfL https://raw.githubusercontent.com/openfaas/faasd/master/hack/install.sh | bash -s -

# Set up timeouts to support timeouts of max. 5 minutes
sudo sed -i 's/read_timeout=60s/read_timeout=5m/g' /var/lib/faasd/docker-compose.yaml
sudo sed -i 's/write_timeout=60s/write_timeout=5m/g' /var/lib/faasd/docker-compose.yaml
sudo sed -i 's/upstream_timeout=65s/upstream_timeout=5m2s/g' /var/lib/faasd/docker-compose.yaml

# Start services
sudo systemctl daemon-reload
sudo systemctl enable node_exporter.service
sudo systemctl start node_exporter.service
sudo systemctl restart faasd
