output "gateway_url" {
  description = "The url of the faasd gateway"
  value       = format("http:/%s:8080", aws_eip.vm.public_ip)
}

output "basic_auth_password" {
  description = "The basic auth password."
  value       = random_password.vm[0].result
  sensitive = true
}
