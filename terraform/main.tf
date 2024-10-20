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

resource "google_cloudfunctions_function" "function" {
  name        = var.function_name
  description = "Java Cloud Function deployed with Terraform"
  runtime     = var.runtime
  entry_point = var.entry_point
  trigger_http = true
  available_memory_mb = var.memory
  timeout             = 60

  source_archive_bucket = google_storage_bucket.function_source_bucket.name
  source_archive_object = google_storage_bucket_object.function_source_archive.name
}