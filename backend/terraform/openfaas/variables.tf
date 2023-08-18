variable "deployment_id" {
  description = "The id of the deployment."
  type        = number
}

variable "name" {
  description = "The name of the function."
  type        = string
}

variable "image" {
  description = "The docker image to use."
  type        = string
}

variable "vm_props" {
  description = "A map containing the gateway url and auth password of the deployed resource"
  type       = object({
    gateway_url = string
    auth_password = string
  })
}

variable "basic_auth_user" {
  description = "The basic auth user name."
  type        = string
  default     = "admin"
}

variable "openfaas_depends_on" {
  description = "A dependency to wait for. This can be used to wait for a vm startup."
  type        = any
  default     = 0
}

variable "timeout" {
    description = "The timeout of the function in seconds"
    type =  number
    default = 5
}
