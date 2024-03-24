output "function_output" {
  value = {for i, function in var.names: function => {full_url: aws_lambda_function_url.function_url[i].function_url}}
}
