output "function_url" {
  value       = google_cloudfunctions2_function.function.service_config[0].uri
  description = "The URL of the deployed Cloud Function (GetFromBunny)"
}