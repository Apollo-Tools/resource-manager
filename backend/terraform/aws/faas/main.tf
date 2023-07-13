locals {
  tags = {
    Deployment = var.deployment_id
    System     = "Apollo Resource Manager"
  }
  function_layer_map =  {for name, layer in zipmap(var.names, var.layers.layers): name => layer if layer != ""}
  layer_map = {for i, layer in toset(var.layers.layers): layer => "${var.layers.path}/${layer}.zip" if layer != ""}
  function_runtime_map = zipmap(var.names, var.runtimes)
}

data "aws_iam_role" "iam_role" {
  count = length(var.deployment_roles)
  name  = var.deployment_roles[count.index]
}

resource "aws_lambda_layer_version" "layer" {
  for_each = local.layer_map
  filename = each.value
  layer_name = "${each.key}_${var.deployment_id}"
  compatible_runtimes = each.value == "python38" ? ["python3.8"] : []
}

resource "aws_lambda_function" "lambda" {
  count            = length(var.names)
  filename         = var.paths[count.index]
  function_name    = var.names[count.index]
  role             = data.aws_iam_role.iam_role[count.index].arn
  handler          = var.handlers[count.index]
  timeout          = var.timeouts[count.index]
  memory_size      = var.memory_sizes[count.index]
  layers           = try([aws_lambda_layer_version.layer[local.function_layer_map[var.names[count.index]]].arn], [])
  runtime          = var.runtimes[count.index]
  source_code_hash = filebase64sha256(var.paths[count.index])

  tags = merge(local.tags, {Name = var.names[count.index]})
}
resource "aws_lambda_function_url" "function_url" {
  count              = length(var.names)
  function_name      = aws_lambda_function.lambda[count.index].function_name
  authorization_type = "NONE"
}
