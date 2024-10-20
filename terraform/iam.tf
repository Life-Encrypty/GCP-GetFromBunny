resource "google_cloud_run_service_iam_member" "allow_all_users_invoker" {
  location       = var.region
  project        = var.project_id
  service        = google_cloudfunctions2_function.function.service_config[0].service
  role           = "roles/run.invoker"
  member         = "allUsers"

  depends_on = [google_cloudfunctions2_function.function]
}

resource "google_storage_bucket_iam_member" "function_bucket_access" {
  bucket = data.google_storage_bucket.target_bucket.name
  role   = "roles/storage.objectAdmin"
  member = "serviceAccount:${google_cloudfunctions2_function.function.service_config[0].service_account_email}"
}

