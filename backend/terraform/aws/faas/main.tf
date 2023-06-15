locals {
  tags = {
    Deployment = var.deployment_id
    System     = "Apollo Resource Manager"
  }
  function_layer_map = zipmap(var.names, var.layers)
  function_runtime_map = zipmap(var.names, var.runtimes)
}

data "aws_iam_role" "iam_role" {
  count = length(var.deployment_roles)
  name  = var.deployment_roles[count.index]
}

resource "aws_lambda_layer_version" "layer" {
  for_each = local.function_layer_map
  filename = each.value
  layer_name = each.key
  compatible_runtimes = [local.function_runtime_map[each.key]]
}

resource "aws_lambda_function" "lambda" {
  count            = length(var.names)
  filename         = var.paths[count.index]
  function_name    = var.names[count.index]
  role             = data.aws_iam_role.iam_role[count.index].arn
  handler          = var.handlers[count.index]
  timeout          = var.timeouts[count.index]
  memory_size      = var.memory_sizes[count.index]
  layers           = aws_lambda_layer_version.layer != null ? [lookup(lookup(aws_lambda_layer_version.layer,
    var.names[count.index], null), "arn", null)] : []
  runtime          = var.runtimes[count.index]
  source_code_hash = filebase64sha256(var.paths[count.index])

  tags = merge(local.tags, {Name = var.names[count.index]})
}
resource "aws_lambda_function_url" "function_url" {
  count              = length(var.names)
  function_name      = aws_lambda_function.lambda[count.index].function_name
  authorization_type = "NONE"
}
