locals {
  tags = {
    Deployment = var.deployment_id
    System = "Apollo Resource Manager"
  }
}

data "aws_iam_role" "iam_role" {
  count = length(var.deployment_roles)
  name  = var.deployment_roles[count.index]
}

resource "aws_lambda_function" "lambda" {
  count            = length(var.names)
  filename         = var.paths[count.index]
  function_name    = var.names[count.index]
  role             = data.aws_iam_role.iam_role[count.index].arn
  handler          = var.handlers[count.index]
  timeout          = var.timeouts[count.index]
  memory_size      = var.memory_sizes[count.index]
  layers           = var.layers[count.index]
  runtime          = var.runtimes[count.index]
  source_code_hash = filebase64sha256(var.paths[count.index])

  tags = merge(local.tags, {Name = var.names[count.index]})
}
resource "aws_lambda_function_url" "function_url" {
  count              = length(var.names)
  function_name      = aws_lambda_function.lambda[count.index].function_name
  authorization_type = "NONE"
}
