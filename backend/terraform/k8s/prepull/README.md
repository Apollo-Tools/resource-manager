# K8s Pre Pull

This Terraform Module can be used to create a daemon set that pre pulls images and keeps them for 
later usage.

## What's a Terraform Module?

A Terraform Module refers to a self-contained packages of Terraform configurations that are managed as a group. This repo
is a Terraform Module and contains many "submodules" which can be composed together to create useful infrastructure patterns.

## How do you use this module?

This directory defines a [Terraform module](https://www.terraform.io/docs/modules/usage.html), which you can use in your
code by adding a `module` configuration and setting its `source` parameter to the path of this directory:

```hcl
module "k8s_pre_pull" {
  source = "../../terraform/k8s/prepull"
  reservation_id = 1
  config_path = "~/.kube/config"
  namespace = "default"
  config_context = "context"
  images = ["nginx:latest"]
  timeout = "2m"
}
```

<!-- BEGIN_TF_DOCS -->
## Requirements

| Name       | Version   |
|------------|-----------|
| terraform  | \>= 1.2.0 |

## Providers

| Name       | Version  |
|------------|----------|
| kubernetes | = 2.20.0 |

## Resources

| Name                                                                                                                        | Type     |
|-----------------------------------------------------------------------------------------------------------------------------|----------|
| [manifest.kubernetes_manifest](https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/manifest) | resource |

## Inputs

| Name           | Description                                                             | Type                                                           | Default            | Required |
|----------------|-------------------------------------------------------------------------|----------------------------------------------------------------|--------------------|:--------:|
| name           | The name of the service                                                 | `string`                                                       | `"service"`        |    no    |
| reservation_id | The id of the reservation.                                              | `number`                                                       | n/a                |   yes    |
| namespace      | The k8s namespace that should be used.                                  | `string`                                                       | `"default"`        |    no    |
| config_path    | The path to the k8s config                                              | `string`                                                       | `"~/.kube/config"` |    no    |
| config_context | The selected k8s context                                                | `string`                                                       | n/a                |   yes    |
| image          | The image to use                                                        | `string`                                                       | n/a                |   yes    |
| replicas       | The amount of replicas for this service                                 | `number`                                                       | `1`                |    no    |
| cpu            | The amount of requested and limited compute processing units.           | `string`                                                       | `"50m"`            |    no    |
| memory         | The amount of requested and limited memory                              | `string`                                                       | `"50Mi"`           |    no    |
| ports          | The ports to expose                                                     | `list(object({container_port: number, service_port: number}))` | `[]`               |    no    |
| service_type   | The type of the service. Valid values are `NodePort` and `LoadBalancer` | `string`                                                       | `"NodePort"`       |    no    |
| external_ip    | The external ip used for the load balancer if present.                  | `string`                                                       | `""`               |    no    |

<!-- END_TF_DOCS -->
