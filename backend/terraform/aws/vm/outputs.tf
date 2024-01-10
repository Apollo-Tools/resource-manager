output "vm_props" {
  description = "A map containing the base urls, ports and auth passwords of the deployed resources"
  value       = {for i, resource in var.names : resource =>
    {
      base_url = format("http://%s", aws_eip.vm[i].public_ip)
      metrics_port = var.metrics_port,
      openfaas_port = 8080,
      auth_password = random_password.vm[i].result
    }
  }
}
