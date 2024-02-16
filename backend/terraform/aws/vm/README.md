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
module "ec2" {
  source         = "./path-to-module"
  deployment_id  = 1
  name           = ["resource_1",]
  vpc_id         = "vpc-1234"
  subnet_id      = "subnet-1234"
  instance_types = ["t2.micro",]
  metrics_port   = 8082
}
```

<!-- BEGIN_TF_DOCS -->
## Requirements

| Name      | Version   |
|-----------|-----------|
| terraform | >= 1.0.0  |
| aws       | >= 3.30.0 |
| random    | >= 3.1.0  |

## Providers

| Name   | Version   |
|--------|-----------|
| aws    | >= 3.30.0 |
| random | >= 3.1.0  |

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

| Name            | Description                                  | Type     | Default | Required |
|-----------------|----------------------------------------------|----------|---------|:--------:|
| deployment_id   | The id of the deployment.                    | `number` | n/a     |   yes    |
| names           | The names of the ec2 instances.              | `list`   | n/a     |   yes    |
| basic_auth_user | The basic auth user                          | `string` | "admin" |    no    |
| vpc\_id         | VPC ID.                                      | `string` | n/a     |   yes    |
| subnet\_id      | VPC Subnet ID to launch in.                  | `string` | n/a     |   yes    |
| instance\_types | The instance types to use for the instances. | `list`   | n/a     |   yes    |
| metrics\_port   | The metrics port of the node exporter.       | `number` | 8082    |    no    |

## Outputs

THe output of the module is vm_props which is a list of objects with the following schema:

| Name           | Description                            |
|----------------|----------------------------------------|
| auth\_password | The basic auth password.               |
| base\_url      | The base url of the resource           |
| metrics\_port  | The metrics port of the node exporter. |
| openfaas\_port | The port of the OpenFaaS gateway.      |
<!-- END_TF_DOCS -->
