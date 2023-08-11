# OpenFaas

This Terraform Module can be used to deploy a function to a running [faasd](https://github.com/openfaas/faasd) instance 
using [Terraform](https://www.terraform.io/).

__faasd__, a lightweight & portable faas engine, is [OpenFaaS](https://github.com/openfaas/) reimagined, but without the cost and complexity of Kubernetes. It runs on a single host with very modest requirements, making it fast and easy to manage. Under the hood it uses [containerd](https://containerd.io/) and [Container Networking Interface (CNI)](https://github.com/containernetworking/cni) along with the same core OpenFaaS components from the main project.

## What's a Terraform Module?

A Terraform Module refers to a self-contained packages of Terraform configurations that are managed as a group. This repo
is a Terraform Module and contains many "submodules" which can be composed together to create useful infrastructure patterns.

## How do you use this module?

This directory defines a [Terraform module](https://www.terraform.io/docs/modules/usage.html), which you can use in your
code by adding a `module` configuration and setting its `source` parameter to the path of this directory:

```hcl
module "openfaas" {
  source = "../../../terraform/openfaas"
  deployment_id = 1
  name = "add"
  image = "duser/add"
  basic_auth_user = "admin"
  vm_props = {gateway_url= "http://localhost:8080", auth_password= "11111"}
  timeout = 30
}
```

<!-- BEGIN_TF_DOCS -->
## Requirements

| Name      | Version  |
|-----------|----------|
| terraform | >= 1.0.0 |
| openfaas  | >= 0.0.4 |

## Providers

| Name     | Version  |
|----------|----------|
| openfaas | >= 0.0.4 |

## Resources

| Name                                                                                                                     | Type     |
|--------------------------------------------------------------------------------------------------------------------------|----------|
| [openfaas_function.function](https://registry.terraform.io/providers/Waterdrips/openfaas/latest/docs/resources/function) | resource |

## Inputs

| Name                | Description                                                                 | Type     | Default | Required |
|---------------------|-----------------------------------------------------------------------------|----------|---------|:--------:|
| deployment_id       | The id of the deployment.                                                   | `number` | n/a     |   yes    |
| name                | The name of the function.                                                   | `string` | n/a     |   yes    |
| image               | The docker image to use.                                                    | `string` | n/a     |   yes    |
| vm\_props           | A map containing the gateway url and auth password of the deployed resource | `object` | n/a     |   yes    |
| basic_auth_user     | The basic auth user name.                                                   | `string` | "admin" |    no    |
| openfaas_depends_on | A dependency to wait for. This can be used to wait for a vm startup.        | `any`    | 0       |    no    |
| timeout             | The timeout of the function in seconds                                      | `number` | 5       |    no    |

## Outputs

| Name         | Description                      |
|--------------|----------------------------------|
| function_url | The trigger url of the function. |
<!-- END_TF_DOCS -->
