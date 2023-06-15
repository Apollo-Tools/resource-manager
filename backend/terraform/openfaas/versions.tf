terraform {
  required_version = ">= 1.2.0"

  required_providers {
    openfaas = {
      source = "Waterdrips/openfaas"
      version = "0.0.4"
    }
  }
}
