# faasd for Amazon Web Services

This module is a modified version of [this](https://github.com/jsiebens/terraform-aws-faasd) repository.

This Terraform Module can be used to deploy a [faasd](https://github.com/openfaas/faasd) instance on the
[AWS](https://aws.amazon.com/) using [Terraform](https://www.terraform.io/).

__faasd__, a lightweight & portable faas engine, is [OpenFaaS](https://github.com/openfaas/) reimagined, but without the cost and complexity of Kubernetes. It runs on a single host with very modest requirements, making it fast and easy to manage. Under the hood it uses [containerd](https://containerd.io/) and [Container Networking Interface (CNI)](https://github.com/containernetworking/cni) along with the same core OpenFaaS components from the main project.

## What's a Terraform Module?

A Terraform Module refers to a self-contained packages of Terraform configurations that are managed as a group. This repo
is a Terraform Module and contains many "submodules" which can be composed together to create useful infrastructure patterns.

## How do you use this module?

This directory defines a [Terraform module](https://www.terraform.io/docs/modules/usage.html), which you can use in your
code by adding a `module` configuration and setting its `source` parameter to the path of this directory:

```hcl
module "faasd" {
  source = "./path-to-module"
  name      = var.name
  vpc_id    = var.vpc_id
  subnet_id = var.subnet_id
  key_name  = var.key_name
}
```

<!-- BEGIN_TF_DOCS -->
## Requirements

| Name | Version |
|------|---------|
| terraform | >= 1.0.0 |
| aws | >= 3.30.0 |
| random | >= 3.1.0 |

## Providers

| Name | Version |
|------|---------|
| aws | >= 3.30.0 |
| random | >= 3.1.0 |

## Resources

| Name                                                                                                                  | Type        |
|-----------------------------------------------------------------------------------------------------------------------|-------------|
| [aws_eip.vm](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/eip)                         | resource    |
| [aws_eip_association.vm](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/eip_association) | resource    |
| [aws_iam_role.vm](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role)               | data        |
| [aws_instance.vm](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/instance)               | resource    |
| [aws_security_group.vm](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/security_group)   | resource    |
| [random_password.vm](https://registry.terraform.io/providers/hashicorp/random/latest/docs/resources/password)         | resource    |
| [aws_ami.ubuntu](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/ami)                  | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| instance\_type | The instance type to use for the instance. | `string` | `"t2.micro"` | no |
| name | The name of the faasd instance. | `string` | n/a | yes |
| subnet\_id | VPC Subnet ID to launch in. | `string` | n/a | yes |
| vpc\_id | VPC ID. | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| basic\_auth\_password | The basic auth password. |
| gateway\_url | The url of the faasd gateway |
<!-- END_TF_DOCS -->
