variable "deployment_id" {
  description = "The id of the deployment."
  type        = number
}

variable "names" {
  description = "The names of the functions to deploy."
  type        = list(string)
}

variable "paths" {
  description = "The absolute paths to the source code."
  type        = list(string)
}

variable "handlers" {
  description = "The function handlers."
  type        = list(string)
}

variable "timeouts" {
  description = "The function timeouts."
  type        = list(number)
}

variable "memory_sizes" {
  description = "The memory size for each function."
  type        = list(number)
}

variable "layers" {
  description = "The function layers."
  type        = list(string)
}

variable "runtimes" {
  description = "The function runtimes."
  type        = list(string)
}

variable "deployment_roles" {
  description = "The user roles to use."
  type        = list(string)
}
