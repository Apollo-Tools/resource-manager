locals {
  basic_auth_user = var.basic_auth_user
  tags = {
    Name = var.name
    System = "Apollo Resource Manager"
  }
  user_data_vars = {
    basic_auth_user     = local.basic_auth_user
    basic_auth_password = random_password.vm[0].result
  }
}

resource "random_password" "vm" {
  count   = 1
  length  = 16
  special = false
}

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

resource "aws_security_group" "vm" {
  name        = var.name
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

data "aws_iam_role" "awsRole" {
  name = "LabRole"
}

resource "aws_instance" "vm" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.instance_type
  user_data_base64       = base64encode(templatefile("${path.module}/templates/startup.sh", local.user_data_vars))
  vpc_security_group_ids = [aws_security_group.vm.id]
  subnet_id              = var.subnet_id
  tags = local.tags
}

resource "aws_eip" "vm" {
  vpc = true
  tags = local.tags
}

resource "aws_eip_association" "vm" {
  instance_id   = aws_instance.vm.id
  allocation_id = aws_eip.vm.id
}
