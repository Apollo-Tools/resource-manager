variable "name" {
  description = "The name of the service."
  type        = string
  default = "service"
}

variable "reservation_id" {
  description = "The id of the reservation."
  type        = number
}

variable "namespace" {
  description = "The k8s namespace that should be used."
  type        = string
  default = "default"
}

variable "config_path" {
  description = "The path to the k8s config"
  type        = string
  default = "~/.kube/config"
}

variable "config_context" {
  description = "The selected k8s context"
  type        = string
}

variable "image" {
  description = "The image to use"
  type = string
}

variable "replicas" {
  description = "The amount of replicas for this service"
  type = number
  default = 1
}

variable "cpu" {
  description = "The amount of requested and limited compute processing units."
  type = string
  default = "50m"
}

variable "memory" {
  description = "The amount of requested and limited memory."
  type = string
  default = "50Mi"
}

variable "ports" {
  description = "The ports to expose"
  type = list(object({
    container_port: number
    service_port: number
  }))
  default = []
}

variable "service_type" {
  description = "The type of the service."
  type = string
  default = "NodePort"

  validation {
    condition     = var.service_type == "NodePort" || var.service_type == "LoadBalancer"
    error_message = "The service type must be of type NodePort or LoadBalancer"
  }
}

variable "external_ip" {
  description = "The external ip used for the load balancer if present."
  type = string
  default = ""
}
