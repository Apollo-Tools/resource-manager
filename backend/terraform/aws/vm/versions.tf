terraform {
  required_version = ">= 1.0.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 4.16.0"
    }
    random = {
      source  = "hashicorp/random"
      version = ">= 3.1.0"
    }
    http = {
      source = "MehdiAtBud/http"
      version = "2.3.1"
    }
  }
}
