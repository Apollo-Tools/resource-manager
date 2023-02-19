variable "name" {
  description = "The name of the faasd instance."
  type        = string
}

variable "basic_auth_user" {
  description = "The basic auth user name."
  type        = string
  default     = "admin"
}

variable "aws_role" {
  description = "The user role to use."
  type        = string
}

variable "vpc_id" {
  description = "VPC ID."
  type        = string
}

variable "subnet_id" {
  description = "VPC Subnet ID to launch in."
  type        = string
}

variable "instance_type" {
  description = "The instance type to use for the instance."
  type        = string
  default     = "t2.micro"
}
