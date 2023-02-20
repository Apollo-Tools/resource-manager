output "function_urls" {
  value = {for i, function in var.names: function => aws_lambda_function_url.function_url[i]}
}
