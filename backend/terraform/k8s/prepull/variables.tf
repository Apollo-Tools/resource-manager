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

variable "images" {
  description = "The images to pre pull"
  type = list(string)
}

variable "timeout" {
  description = "The timeout for the creation, update and deletion of the pre pull pod"
  type = string
  default = "5m"
}
