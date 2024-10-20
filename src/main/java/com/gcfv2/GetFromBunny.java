package com.gcfv2;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import com.google.gson.Gson;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;

public class GetFromBunny implements HttpFunction {
  @Override
  public void service(HttpRequest request, HttpResponse response) throws Exception {
      // Log: Starting the service
      System.out.println("Starting the Google Cloud Function - GetFromBunny");

      // Parse JSON input
      Gson gson = new Gson();
      BunnyRequest bunnyRequest = gson.fromJson(request.getReader(), BunnyRequest.class);
      System.out.println("Parsed JSON input: " + bunnyRequest);

      // Validate input
      if (bunnyRequest == null || bunnyRequest.getContentName() == null || bunnyRequest.getFtp() == null) {
          System.out.println("Invalid input received.");
          response.setStatusCode(400);
          response.getWriter().write("Invalid input");
          return;
      }

      // FTP details
      String ftpHost = bunnyRequest.getFtp().getHostname();
      int ftpPort = bunnyRequest.getFtp().getPort();
      String ftpUser = bunnyRequest.getFtp().getUseName();
      String ftpPassword = bunnyRequest.getFtp().getPassword();
      String remoteFilePath = bunnyRequest.getPath();

      // Log: FTP details
      System.out.println("FTP Host: " + ftpHost);
      System.out.println("FTP Port: " + ftpPort);
      System.out.println("FTP User: " + ftpUser);
      System.out.println("Remote File Path: " + remoteFilePath);

      // Connect to Bunny CDN via FTP
      FTPClient ftpClient = new FTPClient();
      try {
          System.out.println("Attempting to connect to FTP: " + ftpHost + ":" + ftpPort);
          ftpClient.connect(ftpHost, ftpPort);
          System.out.println("Connected to FTP server");

          ftpClient.enterLocalPassiveMode(); // Passive mode might be necessary
          System.out.println("Entered passive mode for FTP connection");

          boolean login = ftpClient.login(ftpUser, ftpPassword);
          if (!login) {
              System.out.println("FTP login failed");
              response.setStatusCode(401);
              response.getWriter().write("FTP login failed");
              return;
          }
          System.out.println("Logged in to FTP server");

          ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
          System.out.println("Set FTP file type to binary");

          // Download the video file
          System.out.println("Attempting to retrieve file from FTP: " + remoteFilePath);
          InputStream inputStream = ftpClient.retrieveFileStream(remoteFilePath);
          if (inputStream == null) {
              System.out.println("File not found on FTP server");
              response.setStatusCode(404);
              response.getWriter().write("File not found on FTP server");
              return;
          }
          System.out.println("File retrieved successfully from FTP");

          // Upload the video file to GCS
          String bucketName = "gard-test";
          String blobName = bunnyRequest.getContentName();
          Storage storage = StorageOptions.getDefaultInstance().getService();
          BlobId blobId = BlobId.of(bucketName, blobName);
          BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

          System.out.println("Uploading file to Google Cloud Storage: " + blobName);
          storage.create(blobInfo, inputStream);
          inputStream.close();
          ftpClient.completePendingCommand();
          System.out.println("File uploaded successfully to GCS");

          // Create a JSON file with the content name without the extension
          String jsonFileName = bunnyRequest.getContentName().replace(".mp4", "") + ".json";
          BlobId jsonBlobId = BlobId.of(bucketName, jsonFileName);
          BlobInfo jsonBlobInfo = BlobInfo.newBuilder(jsonBlobId).build();

          // Convert the request back to JSON string
          String jsonData = gson.toJson(bunnyRequest);
          System.out.println("Uploading JSON metadata to Google Cloud Storage: " + jsonFileName);
          storage.create(jsonBlobInfo, jsonData.getBytes());

          // Respond with success
          System.out.println("File and metadata transferred successfully");
          response.setStatusCode(200);
          response.getWriter().write("File transferred successfully");
      } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Error occurred: " + e.getMessage());
          response.setStatusCode(500);
          response.getWriter().write("Internal server error: " + e.getMessage());
      } finally {
          if (ftpClient.isConnected()) {
              ftpClient.logout();
              ftpClient.disconnect();
              System.out.println("Disconnected from FTP server");
          }
      }
  }
}
