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
  description = <<EOT
    layers = {
      layers: "The layers for each function. The .zip of the layer has to have the same name as the entries of this list."
      path: "The path to the root folder of the layers."
    }
  EOT
  type        = object({
    layers: list(string)
    path: string
  })
}

variable "runtimes" {
  description = "The function runtimes."
  type        = list(string)
}

variable "deployment_roles" {
  description = "The user roles to use."
  type        = list(string)
}
