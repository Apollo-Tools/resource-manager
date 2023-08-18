variable "deployment_id" {
  description = "The id of the deployment."
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
  description = "The selected k8s context."
  type        = string
}

variable "images" {
  description = "The images to pre pull."
  type = list(string)
}

variable "timeout" {
  description = "The timeout for the creation, update and deletion of the pre pull pod."
  type = string
  default = "5m"
}

variable "hostname" {
  description = "The value of the hostname label of a k8s node."
  type = string
  default = null
}

variable "image_pull_secrets" {
  description = "The secrets to use to pull images from private docker registries"
  type = list(string)
  default = []
}
