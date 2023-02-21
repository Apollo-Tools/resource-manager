provider "openfaas" {
  uri = var.vm_props.gateway_url
  user_name = var.basic_auth_user
  password = var.vm_props.auth_password
}

resource "openfaas_function" "function" {
  name      = var.name
  image     = "${var.image}:latest"

  labels = {
    System = "Apollo"
    Name = var.name
  }
}