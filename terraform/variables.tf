variable "project_id" {
  description = "The GCP project ID"
  type        = string
}

variable "region" {
  description = "The GCP region"
  type        = string
  default     = "us-east1"
}

variable "function_name" {
  description = "Name of the Cloud Function"
  type        = string
}

variable "entry_point" {
  description = "Entry point of the Cloud Function"
  type        = string
}

variable "runtime" {
  description = "Runtime environment for the function"
  type        = string
  default     = "java21"
}

variable "memory" {
  description = "Memory allocated to the function (in MB)"
  type        = number
  default     = 1024 * 16
}

variable "ftp_username" {
  description = "FTP username for Bunny CDN"
  type        = string
}

variable "ftp_password" {
  description = "FTP password for Bunny CDN"
  type        = string
  sensitive   = true
}

variable "bucket_name" {
  description = "The name of the target GCS bucket"
  type        = string
  default     = "gard-test"
}

variable "bucket_location" {
  description = "The location of the target GCS bucket"
  type        = string
  default     = "us-east1"
}
