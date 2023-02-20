output "vm_props" {
  description = "A map containing the gateway urls and auth passwordss of the deployed resources"
  value       = {for i, resource in var.names : resource =>
    {
      gateway_url   = format("http:/%s:8080", aws_eip.vm[i].public_ip),
      auth_password = random_password.vm[i].result
    }
  }
}