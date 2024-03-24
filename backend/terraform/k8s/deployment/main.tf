provider "kubernetes" {
  config_path    = var.config_path
  config_context = var.config_context
}

locals {
  name = "${replace(var.name, "-", "_")}-${var.deployment_id}-${var.resource_deployment_id}${var.hostname != null ? var.hostname : ""}"
}

resource "kubernetes_service_v1" "service" {
  count = var.service_type == "NoService" ? 0 : 1
  metadata {
    name = local.name
    namespace = var.namespace
    labels = {
      source = "apollo-rm-deployment"
      deployment = var.deployment_id
      resource_deployment = var.resource_deployment_id
      service = var.service_id
    }
  }
  spec {
    selector = {
      app = kubernetes_deployment_v1.deployment.metadata[0].labels.app
    }
    dynamic "port" {
      for_each = var.ports
      content {
        name = "port-${port.value.service_port}-${port.value.container_port}"
        port = port.value.service_port
        target_port = port.value.container_port
      }
    }
    type = var.service_type
    external_ips = var.service_type == "LoadBalancer" && length(var.external_ip) > 0 ? [var.external_ip] : null
  }
  wait_for_load_balancer = false
}

resource "kubernetes_deployment_v1" "deployment" {
  metadata {
    name = local.name
    labels = {
      app = local.name
      source = "apollo-rm-deployment"
      deployment = var.deployment_id
      resource_deployment = var.resource_deployment_id
      service = var.service_id
    }
    namespace = var.namespace
  }
  spec {
    replicas = var.replicas
    selector {
      match_labels = {
        app = local.name
      }
    }

    template {
      metadata {
        labels = {
          app = local.name
          source = "apollo-rm-deployment"
          deployment = var.deployment_id
          resource-deployment = var.resource_deployment_id
          service = var.service_id
          apollo-type = "pod"
        }
      }
      spec {
        container {
          image = var.image
          name  = local.name

          resources {
            limits = {
              cpu    = var.cpu
              memory = var.memory
            }
            requests = {
              cpu    = var.cpu
              memory = var.memory
            }
          }

          dynamic "env" {
            for_each = var.env_vars
            content {
              name = env.value.name
              value = env.value.value
            }
          }

          dynamic "port" {
            for_each = var.ports
            content {
              name = "port-${port.value.service_port}-${port.value.container_port}"
              container_port = port.value.container_port
            }
          }

          dynamic "volume_mount" {
            for_each = var.volume_mounts
            content {
              mount_path = volume_mount.value.mountPath
              name = volume_mount.value.name
            }
          }

        }
        dynamic "image_pull_secrets" {
          for_each = var.image_pull_secrets
          content {
            name = image_pull_secrets.value
          }
        }
        node_selector = var.hostname != null ? {"kubernetes.io/hostname" = var.hostname} : null

        dynamic "volume" {
          for_each = var.volume_mounts
          content {
            name = volume.value.name
            empty_dir {
              size_limit = "${volume.value.sizeMegaBytes}M"
            }
          }
        }
      }
    }
  }

  timeouts {
    create = "2m"
    update = "2m"
    delete = "2m"
  }
}

data "kubernetes_resources" "pods" {
  depends_on = [kubernetes_deployment_v1.deployment]
  api_version    = "v1"
  kind           = "Pod"
  label_selector = "app=${local.name}"
  namespace = var.namespace
}
