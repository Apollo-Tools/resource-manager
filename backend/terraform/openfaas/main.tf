provider "openfaas" {
  uri = var.vm_props.gateway_url
  user_name = var.basic_auth_user
  password = var.vm_props.auth_password
}

resource "openfaas_function" "function" {
  depends_on = [var.openfaas_depends_on]
  name      = var.name
  image     = "${var.image}:latest"
  env_vars  = {
      write_timeout: "${var.timeout}s"
      read_timeout: "${var.timeout}s"
      exec_timeout: "${var.timeout}s"
  }

  labels = {
    System = "Apollo Resource Manager"
    Deployment = var.deployment_id
    Name = var.name
  }
}
