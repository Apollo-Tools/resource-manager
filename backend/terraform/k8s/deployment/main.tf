provider "kubernetes" {
  config_path    = var.config_path
  config_context = var.config_context
}

locals {
  name = "${replace(var.name, "-", "_")}-${var.reservation_id}-${formatdate("YYYY-MM-DD-hh-mm-ss", timestamp())}"
}

resource "kubernetes_service_v1" "service" {
  count = length(var.ports)
  metadata {
    name = local.name
    namespace = var.namespace
  }
  spec {
    selector = {
      app = kubernetes_deployment_v1.deployment.metadata[0].labels.app
    }
    dynamic "port" {
      for_each = var.ports
      content {
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

          dynamic "port" {
            for_each = var.ports
            content {
              container_port = port.value.container_port
            }
          }
        }
      }
    }
  }
}
