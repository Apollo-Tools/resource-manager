# K8s Deployment

This Terraform Module can be used to create a deployment of a container image.

## What's a Terraform Module?

A Terraform Module refers to a self-contained packages of Terraform configurations that are
managed as a group. This repo is a Terraform Module and contains many "submodules" which can be
composed together to create useful infrastructure patterns.

## How do you use this module?

This directory defines a [Terraform module](https://www.terraform.io/docs/modules/usage.html),
which you can use in your code by adding a `module` configuration and setting its `source`
parameter to the path of this directory:

```hcl
module "k8s_deployment" {
  source = "../../terraform/k8s/deployment"
  config_context = "context"
  namespace = "default"
  name = "servicename"
  image = "nginx:latest"
  deployment_id = 1
  resource_deployment_id = 1
  service_id = 1
  ports = [{
    container_port = 80
    service_port = 8000
  }]
  service_type = "LoadBalancer"
  external_ip = "0.0.0.0"
  hostname = "node1"
  image_pull_secrets = ["regcred"]
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

| Name                                                                                                                                 | Type     |
|--------------------------------------------------------------------------------------------------------------------------------------|----------|
| [core/v1.kubernetes_service_v1](https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/service_v1)       | resource |
| [apps/v1.kubernetes_deployment_v1](https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/deployment_v1) | resource |

## Inputs

| Name                   | Description                                                            | Type           | Default            | Required |
|------------------------|------------------------------------------------------------------------|----------------|--------------------|:--------:|
| deployment_id          | The id of the deployment.                                              | `number`       | n/a                |   yes    |
| resource_deployment_id | The id of the resource deployment.                                     | `number`       | n/a                |   yes    |
| service_id             | The id of the service.                                                 | `number`       | n/a                |   yes    |
| namespace              | The k8s namespace that should be used.                                 | `string`       | `"default"`        |    no    |
| config_path            | The path to the k8s config.                                            | `string`       | `"~/.kube/config"` |    no    |
| config_context         | The selected k8s context.                                              | `string`       | n/a                |   yes    |
| images                 | The images to pre pull.                                                | `list(string)` | n/a                |   yes    |
| timeout                | The timeout for the creation, update and deletion of the pre pull pod. | `string`       | `"5m"`             |    no    |
| hostname               | The value of the hostname label of a k8s node.                         | `string`       | n/a                |    no    |
| image_pull_secrets     | The secrets to use to pull images from private docker registries       | `list(string)` | `[]`               |    no    |

## Outputs

| Name         | Description                                     |
|--------------|-------------------------------------------------|
| service_info | Info like IPs and ports of the deployed service |

<!-- END_TF_DOCS -->
