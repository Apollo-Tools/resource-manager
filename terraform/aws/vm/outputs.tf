output "gateway_urls" {
  description = "The url of the faasd gateway"
  value       = format("http:/%s:8080", aws_eip.vm[*].public_ip)
}

output "auth_passwords" {
  description = "The basic auth passwords."
  value       = random_password.vm[*].result
  sensitive = true
}
