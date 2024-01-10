# faas deployment for Amazon Web Services

This Terraform Module can be used to deploy a lambda function on
[AWS](https://aws.amazon.com/) using [Terraform](https://www.terraform.io/).

## What's a Terraform Module?

A Terraform Module refers to a self-contained packages of Terraform configurations that are managed as a group. This repo
is a Terraform Module and contains many "submodules" which can be composed together to create useful infrastructure patterns.

## How do you use this module?

This directory defines a [Terraform module](https://www.terraform.io/docs/modules/usage.html), which you can use in your
code by adding a `module` configuration and setting its `source` parameter to the path of this directory:

```hcl
module "faas" {
  source           = "./path-to-module"
  deployment_id    = 1
  names            = ["add",]
  paths            = ["absolute/path/add.zip",]
  handlers         = ["main.handler",]
  timeouts         = [600,]
  memory_sizes     = [512,]
  layers           = {layers=["python38",], path="path/to/layers/dir"}
  runtimes         = ["python3.8"]
  deployment_roles = ["LabRole",]
}
```

<!-- BEGIN_TF_DOCS -->
## Requirements

| Name      | Version   |
|-----------|-----------|
| terraform | >= 1.0.0  |
| aws       | >= 4.16.0 |
| random    | >= 3.1.0  |

## Providers

| Name | Version   |
|------|-----------|
| aws  | >= 4.16.0 |

## Resources

| Name                                                                                                                                    | Type     |
|-----------------------------------------------------------------------------------------------------------------------------------------|----------|
| [aws_lambda_function_url.function_url](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lambda_function_url) | resource |
| [aws_lambda_function.lambda](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lambda_function)               | resource |
| [aws_iam_role.vm](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role)                                 | data     |

## Inputs

| Name             | Description                            | Type     | Default | Required |
|------------------|----------------------------------------|----------|---------|:--------:|
| deployment_id    | The id of the deployment.              | `number` | n/a     |   yes    |
| names            | The names of the functions to deploy.  | `list`   | n/a     |   yes    |
| paths            | The absolute paths to the source code. | `list`   | n/a     |   yes    |
| handlers         | The function handlers.                 | `list`   | n/a     |   yes    |
| timeouts         | The function timeouts.                 | `list`   | n/a     |   yes    |
| memory\_sizes    | The memory size for each function.     | `list`   | n/a     |   yes    |
| layers           | The layers for each function.          | `object` | n/a     |   yes    |
| runtimes         | The function runtimes.                 | `list`   | n/a     |   yes    |
| deployment_roles | The user roles to use.                 | `list`   | n/a     |   yes    |

## Outputs

| Name             | Description                     |
|------------------|---------------------------------|
| resource\_output | The list of full function urls. |
<!-- END_TF_DOCS -->
