resource "google_storage_bucket" "function_source_bucket" {
  name          = "${var.project_id}-function-source"
  location      = var.region
  force_destroy = true
}

resource "google_storage_bucket_object" "function_source_archive" {
  name   = "${var.function_name}.zip"
  bucket = google_storage_bucket.function_source_bucket.name
  source = "${path.module}/../function_source.zip"
}

resource "google_cloudfunctions2_function" "function" {
  name        = var.function_name
  description = "Java Cloud Function deployed with Terraform (Gen 2)"
  location    = var.region

  build_config {
    runtime     = var.runtime
    entry_point = var.entry_point
    source {
      storage_source {
        bucket = google_storage_bucket.function_source_bucket.name
        object = google_storage_bucket_object.function_source_archive.name
      }
    }
    environment_variables = {
      FTP_USERNAME = var.ftp_username
      FTP_PASSWORD = var.ftp_password
    }
  }

  service_config {
    available_memory = "${var.memory}M"   # Specify memory in MB, e.g., "1024M"
    ingress_settings = "ALLOW_ALL"
    available_cpu    = "4"
    timeout_seconds = 300
  }

  labels = {
    "deployment-tool" = "terraform"
  }
}

data "google_storage_bucket" "target_bucket" {
  name = var.bucket_name
}