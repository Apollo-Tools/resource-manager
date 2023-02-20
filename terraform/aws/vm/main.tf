locals {
  tags = {
    Reservation = var.reservation
    System = "Apollo Resource Manager"
  }
  user_data_vars = {
    basic_auth_user     = var.basic_auth_user
    basic_auth_password = random_password.vm[0].result
  }
}

# Generate password to access the vm
resource "random_password" "vm" {
  count   = 1
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
  name = "Apollo ${var.reservation}"
  description = "Allow all incoming traffic"
  vpc_id      = var.vpc_id

  ingress {
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
  user_data_base64       = base64encode(templatefile("${path.module}/templates/startup.sh", local.user_data_vars))
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
