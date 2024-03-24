output "function_output" {
  value = {
    base_url = var.vm_props.base_url
    openfaas_port = var.vm_props.openfaas_port
    metrics_port = var.vm_props.metrics_port
    path = local.path
    full_url = "${local.gateway_url}${local.path}"
  }
}
