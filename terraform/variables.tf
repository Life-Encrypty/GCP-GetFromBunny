variable "project_id" {
  description = "guardxpert"
}

variable "region" {
  description = "The GCP region"
  default     = "us-east1"
}

variable "function_name" {
  description = "GCP-GetFromBunny"
}

variable "entry_point" {
  description = "GetFromBunny"
}

variable "runtime" {
  description = "Runtime environment for the function"
  default     = "java21"
}

variable "memory" {
  description = "Memory allocated to the function (in MB)"
  default     = 1024
}