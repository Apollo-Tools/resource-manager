locals {
  tags = {
    Deployment = var.deployment_id
    System = "Apollo Resource Manager"
  }
}

# Generate passwords to access the vm
resource "random_password" "vm" {
  count   = length(var.names)
  length  = 16
  special = false
}

# Load image
data "aws_ami" "ubuntu" {
  most_recent = true

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-focal-20.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  owners = ["099720109477"] # Canonical
}

# Create a security group
resource "aws_security_group" "vm" {
  name        = "Apollo ${var.deployment_id}"
  description = "Allow all incoming traffic"
  vpc_id      = var.vpc_id

  ingress {
    description = "OpenFaaS"
    cidr_blocks = [
      "0.0.0.0/0"
    ]
    from_port = 8080
    to_port   = 8080
    protocol  = "tcp"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = local.tags
}

# Deploy instances
resource "aws_instance" "vm" {
  count                  = length(var.names)
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.instance_types[count.index]
  user_data_base64       = base64encode(templatefile("${path.module}/templates/startup.sh", {
                            basic_auth_user     = var.basic_auth_user
                            basic_auth_password = random_password.vm[count.index].result
                          }))
  vpc_security_group_ids = [aws_security_group.vm.id]
  subnet_id              = var.subnet_id
  tags = merge(local.tags, {Name = var.names[count.index]})
}

# Create elastic ip addresses
resource "aws_eip" "vm" {
  count = length(var.names)
  vpc = true
  tags = merge(local.tags, {Name = var.names[count.index]})
}

# Create associations between eips and instances
resource "aws_eip_association" "vm" {
  count         = length(var.names)
  instance_id   = aws_instance.vm[count.index].id
  allocation_id = aws_eip.vm[count.index].id
}

data "http-wait" "check_vm" {
  depends_on = [aws_eip_association.vm]
  count = length(var.names)
  provider = http
  url = format("http://%s:8080", aws_eip.vm[count.index].public_ip)
  max_elapsed_time = 999
  initial_interval = 10000
  multiplier       = "1.0"
  max_interval     = 10000
  randomization_factor = 3
}
