data "aws_iam_role" "awsRole" {
  name = var.aws_role
}

resource "aws_lambda_function" "lambda" {
  count            = length(var.names)
  filename         = "${path.module}/${var.paths[count.index]}"
  function_name    = var.names[count.index]
  role             = data.aws_iam_role.awsRole.arn
  handler          = var.handlers[count.index]
  timeout          = var.timeouts[count.index]
  memory_size      = var.memory_sizes[count.index]
  layers           = var.layers[count.index]
  runtime          = var.runtimes[count.index]
  source_code_hash = filebase64sha256("${path.module}/${var.paths[count.index]}")
}
resource "aws_lambda_function_url" "function_url" {
  count              = length(var.names)
  function_name      = aws_lambda_function.lambda[count.index].function_name
  authorization_type = "NONE"
}