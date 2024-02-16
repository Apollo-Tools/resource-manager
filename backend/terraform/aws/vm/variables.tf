variable "deployment_id" {
  description = "The id of the deployment."
  type        = number
}

variable "names" {
  description = "The names of the faasd instances."
  type        = list(string)
}

variable "basic_auth_user" {
  description = "The basic auth user name."
  type        = string
  default     = "admin"
}

variable "vpc_id" {
  description = "VPC ID."
  type        = string
}

variable "subnet_id" {
  description = "VPC Subnet ID to launch in."
  type        = string
}

variable "instance_types" {
  description = "The instance types to use for the instances."
  type        = list(string)
}

variable "metrics_port" {
  description = "The metrics port of the node exporter."
  type = number
  default = 8082
}
