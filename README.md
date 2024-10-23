# **Deploying a Java-based Google Cloud Function Using Terraform**

This guide provides step-by-step instructions to deploy a Java-based Google Cloud Function using Terraform. It covers setting up the environment, configuring Terraform, packaging the function, and allowing unauthenticated invocations.

---

## **Table of Contents**

* Prerequisites  
* Project Structure  
* Step 1: Set Up the Java Project  
* Step 2: Configure Terraform  
  * 2.1. Initialize Terraform Configuration  
  * 2.2. Define Variables  
  * 2.3. Configure the Provider  
  * 2.4. Define Resources  
  * 2.5. Configure IAM Permissions  
  * 2.6. Output Variables  
* Step 3: Package the Java Function  
* Step 4: Authenticate with Google Cloud  
* Step 5: Deploy the Function Using Terraform  
* Step 6: Test the Deployed Function  
* Additional Considerations  
  * Monitoring and Logging  
  * Security Considerations  
  * Cleaning Up Resources  
* API Request Format  
* Commit Messages

---

## **Prerequisites**

* **Java Development Kit (JDK) 21** installed.  
* **Maven** installed for building the Java project.  
* **Terraform** installed for infrastructure deployment.  
* **Google Cloud SDK** installed for authentication.  
* A **Google Cloud Platform (GCP) project** with billing enabled.  
* A **service account** with the necessary permissions.

---

## **Project Structure**

`project-root/`  
`├── .gitignore`  
`├── pom.xml`  
`├── README.md`  
`├── src/`  
`│   └── main/`  
`│       └── java/`  
`│           └── com/`  
`│               └── gcfv2/`  
`│                   └── GetFromBunny.java`  
`├── terraform/`  
`│   ├── main.tf`  
`│   ├── variables.tf`  
`│   ├── provider.tf`  
`│   ├── outputs.tf`  
`│   ├── iam.tf`  
`│   └── terraform.tfvars`  
`└── package_function.bat`

---

## **Step 1: Set Up the Java Project**

**Update `pom.xml` to Use Java 21:**

`<properties>`  
    `<java.version>17</java.version>`  
    `<maven.compiler.source>21</maven.compiler.source>`  
    `<maven.compiler.target>21</maven.compiler.target>`  
    `<maven.compiler.release>21</maven.compiler.release>`  
`</properties>`

**Include the Maven Shade Plugin:**
```
`<build>`  
    `<plugins>`  
        `<!-- Maven Compiler Plugin -->`  
        `<plugin>`  
            `<groupId>org.apache.maven.plugins</groupId>`  
            `<artifactId>maven-compiler-plugin</artifactId>`  
            `<version>3.10.1</version>`  
            `<configuration>`  
                `<source>21</source>`  
                `<target>21</target>`  
            `</configuration>`  
        `</plugin>`

        `<!-- Maven Shade Plugin to create an uber JAR -->`  
        `<plugin>`  
            `<groupId>org.apache.maven.plugins</groupId>`  
            `<artifactId>maven-shade-plugin</artifactId>`  
            `<version>3.2.4</version>`  
            `<executions>`  
                `<execution>`  
                    `<phase>package</phase>`  
                    `<goals>`  
                        `<goal>shade</goal>`  
                    `</goals>`  
                    `<configuration>`  
                        `<createDependencyReducedPom>false</createDependencyReducedPom>`  
                    `</configuration>`  
                `</execution>`  
            `</executions>`  
        `</plugin>`  
    `</plugins>`  
`</build>`
```
**Implement the Cloud Function:**
```
`package com.gcfv2;`

`import com.google.cloud.functions.HttpFunction;`  
`import com.google.cloud.functions.HttpRequest;`  
`import com.google.cloud.functions.HttpResponse;`

`import java.io.BufferedWriter;`

`public class GetFromBunny implements HttpFunction {`  
    `@Override`  
    `public void service(HttpRequest request, HttpResponse response) throws Exception {`  
        `BufferedWriter writer = response.getWriter();`  
        `writer.write("Hello from Java Cloud Function!");`  
    `}`  
`}`
```
---

## **Step 2: Configure Terraform**

### **2.1. Initialize Terraform Configuration**

Create a `terraform` directory and navigate into it:

```
`mkdir terraform`  
`cd terraform`
```

### **2.2. Define Variables**

Create `variables.tf`:

```
`variable "project_id" {`  
  `description = "The GCP project ID"`  
`}`

`variable "region" {`  
  `description = "The GCP region"`  
  `default     = "us-east1"`  
`}`

`variable "function_name" {`  
  `description = "Name of the Cloud Function"`  
`}`

`variable "entry_point" {`  
  `description = "The fully qualified name of your Java class"`  
`}`

`variable "runtime" {`  
  `description = "Runtime environment for the function"`  
  `default     = "java17"`  
`}`

`variable "memory" {`  
  `description = "Memory allocated to the function (in MB)"`  
  `default     = 1024`  
`}`
```

### **2.3. Configure the Provider**

Create `provider.tf`:

```
`terraform {`  
  `required_version = ">= 0.13"`  
  `required_providers {`  
    `google = {`  
      `source  = "hashicorp/google"`  
      `version = ">= 4.0.0"`  
    `}`  
  `}`  
`}`

`provider "google" {`  
  `project = var.project_id`  
  `region  = var.region`  
`}`
```

### **2.4. Define Resources**

Create `main.tf`:

```
`resource "google_storage_bucket" "function_source_bucket" {`  
  `name          = "${var.project_id}-function-source"`  
  `location      = var.region`  
  `force_destroy = true`  
`}`

`resource "google_storage_bucket_object" "function_source_archive" {`  
  `name   = "${var.function_name}.zip"`  
  `bucket = google_storage_bucket.function_source_bucket.name`  
  `source = "${path.module}/../function_source.zip"`  
`}`

`resource "google_cloudfunctions_function" "function" {`  
  `name        = var.function_name`  
  `description = "Java Cloud Function deployed with Terraform"`  
  `runtime     = var.runtime`  
  `entry_point = var.entry_point`  
  `trigger_http = true`  
  `available_memory_mb = var.memory`  
  `timeout             = 60`

  `source_archive_bucket = google_storage_bucket.function_source_bucket.name`  
  `source_archive_object = google_storage_bucket_object.function_source_archive.name`  
`}`
```

### **2.5. Configure IAM Permissions**

Create `iam.tf`:

```
`resource "google_cloudfunctions_function_iam_member" "allow_all_users_invoker" {`  
  `project        = var.project_id`  
  `region         = var.region`  
  `cloud_function = google_cloudfunctions_function.function.name`  
  `role           = "roles/cloudfunctions.invoker"`  
  `member         = "allUsers"`  
`}`
```

### **2.6. Output Variables**

Create `outputs.tf`:

```
`output "function_url" {`  
  `value       = google_cloudfunctions_function.function.https_trigger_url`  
  `description = "The URL of the deployed Cloud Function"`  
`}`
```

---

## **Step 3: Package the Java Function**

Create a batch script `package_function.bat` in the project root:

```
`@echo off`  
`set JAR_FILE=target\http-0.0.1.jar`  
`set ZIP_FILE=function_source.zip`

`IF NOT EXIST %JAR_FILE% (`  
    `ECHO JAR file not found. Please build the project first.`  
    `EXIT /B 1`  
`)`

`IF EXIST %ZIP_FILE% DEL %ZIP_FILE%`

`powershell -Command "Compress-Archive -Path '%JAR_FILE%' -DestinationPath '%ZIP_FILE%' -Force"`

`ECHO Packaged function into %ZIP_FILE%`  
```

**Build and Package the Function:**

```
`mvn clean package`  
`package_function.bat`
```

---

## **Step 4: Authenticate with Google Cloud**

Set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable:

`$env:GOOGLE_APPLICATION_CREDENTIALS = "E:\path\to\your\service-account-key.json"`

Alternatively, authenticate using `gcloud`:

`gcloud auth application-default login`

---

## **Step 5: Deploy the Function Using Terraform**

**Initialize Terraform:**

`terraform init`

**Create `terraform.tfvars`:**

```

`project_id    = "your-gcp-project-id"`  
`function_name = "GCP-GetFromBunny"`  
`entry_point   = "com.gcfv2.GetFromBunny"`  
`region        = "us-east1"`  
`runtime       = "java17"`  
`memory        = 1024`
```

**Plan the Deployment:**

`terraform plan -var-file="terraform.tfvars"`

**Apply the Configuration:**

`terraform apply -var-file="terraform.tfvars"`  
---

## **Step 6: Test the Deployed Function**

**Retrieve the Function URL:**  
The function URL will be output after deployment.

`Outputs:`

`function_url = "https://us-east1-your-gcp-project-id.cloudfunctions.net/GCP-GetFromBunny"`

**Invoke the Function:**  
**Using cURL:**

`curl https://us-east1-your-gcp-project-id.cloudfunctions.net/GCP-GetFromBunny`

1. **Using a Web Browser:**  
   * Navigate to the function URL.

---

## **Additional Considerations**

### **Monitoring and Logging**

* **Cloud Logging:** Monitor logs via the Logs Explorer.  
* **Cloud Monitoring:** Set up dashboards and alerts in Cloud Monitoring.

### **Security Considerations**

* **Public Access:** The function is publicly accessible. Ensure it doesn't expose sensitive data.  
* **Input Validation:** Implement proper input validation within your function.

### **Cleaning Up Resources**

To avoid incurring charges, destroy the resources when no longer needed:

`terraform destroy -var-file="terraform.tfvars"`

---

## **API Request Format**

This API accepts `POST` requests, and the request body **must be in JSON format**. The structure of the JSON should follow the template below. Note that **all keys are case-sensitive**, meaning they must be provided exactly as shown:

```
{
    "contentName": "name.mp4",
    "storageZoon": "g",
    "path": "Content/name.mp4",
    "link": "https://GuardXpert.b-cdn.net/Content2/name.mp4",
    "ftp": {
        "useName": "username",
        "password": "password",
        "Hostname": "storage.bunnycdn.com",
        "port": "21"
    }
}
```

Make sure to use the correct key names and casing when constructing your JSON request.

---

## **Commit Messages**

Commit Messages must follow `"https://www.conventionalcommits.org/en/v1.0.0/"`

---

**Note:** Replace placeholders like `"your-gcp-project-id"` and file paths with your actual project ID and paths.


