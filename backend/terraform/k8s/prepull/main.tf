provider "kubernetes" {
  config_path    = var.config_path
  config_context = var.config_context
}

locals {
  init_containers = [
    for image_name in var.images:
    {
      name = "prepuller-${index(var.images, image_name) + 1}"
      # Set the image you want to pull
      image = image_name
      # Use a known command that will exit successfully immediately
      # Any no-op command will do but YMMV with scratch based containers
      command = [
        "sh",
        "-c",
        "'true'",
      ]
    }
  ]
  name = "prepuller-${var.deployment_id}${var.hostname != null ? var.hostname : ""}-${formatdate("YYYY-MM-DD-hh-mm-ss", timestamp())}"
  image_pull_secrets = [
    for secret in var.image_pull_secrets:
    {
      name = secret
    }
  ]
}

resource "kubernetes_manifest" "pre_puller" {
  manifest = {
    # Source: https://jacobtomlinson.dev/posts/2023/quick-and-dirty-way-to-pre-pull-container-images-on-kubernetes/
    "apiVersion" = "apps/v1"
    "kind" = "DaemonSet"
    "metadata" = {
      "name" = local.name
      "namespace" = var.namespace
    }
    "spec" = {
      "selector" = {
        "matchLabels" = {
          "name" = local.name
        }
      }
      "template" = {
        "metadata" = {
          "labels" = {
            "name" = local.name
            "source" = "apollo-rm-deployment"
            "deployment" = var.deployment_id
            "apollo-type" = "prepuller"
          }
        }
        "spec" = {
          # Configure an init container for each image you want to pull
          "initContainers" = local.init_containers,
          # Use the pause container to ensure the Pod goes into a `Running` phase
          # but doesn't take up resource on the cluster
          "containers" = [
            {
              "image" = "gcr.io/google_containers/pause"
              "name" = "pause"
            },
          ],
          "imagePullSecrets" = length(local.image_pull_secrets) > 0 ? local.image_pull_secrets : null
          "nodeSelector" = var.hostname != null ? {
            "kubernetes.io/hostname" = var.hostname
          } : null
        }
      }
    }
  }

  wait {
    rollout = true
  }

  timeouts {
    create = var.timeout
    update = var.timeout
    delete = var.timeout
  }
}
