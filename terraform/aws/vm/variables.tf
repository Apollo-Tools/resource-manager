variable "reservation" {
  description = "The reservation identifier."
  type        = string
}

variable "names" {
  description = "The names of the faasd instance."
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