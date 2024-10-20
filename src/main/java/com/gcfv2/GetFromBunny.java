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
      // Parse JSON input
      Gson gson = new Gson();
      BunnyRequest bunnyRequest = gson.fromJson(request.getReader(), BunnyRequest.class);

      // Validate input
      if (bunnyRequest == null || bunnyRequest.getContentName() == null || bunnyRequest.getFtp() == null) {
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

      // Connect to Bunny CDN via FTP
      FTPClient ftpClient = new FTPClient();
      try {
        System.out.println("Attempting to connect to FTP: " + ftpHost + ":" + ftpPort);
        ftpClient.connect(ftpHost, ftpPort);
        System.out.println("Connected to FTP server");
          ftpClient.enterLocalPassiveMode(); // Passive mode might be necessary

          boolean login = ftpClient.login(ftpUser, ftpPassword);
          if (!login) {
              response.setStatusCode(401);
              response.getWriter().write("FTP login failed");
              return;
          }
          ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

          // Download the video file
          InputStream inputStream = ftpClient.retrieveFileStream(remoteFilePath);
          if (inputStream == null) {
              response.setStatusCode(404);
              response.getWriter().write("File not found on FTP server");
              return;
          }

          // Upload the video file to GCS
          String bucketName = "gard-test";
          String blobName = bunnyRequest.getContentName();
          Storage storage = StorageOptions.getDefaultInstance().getService();
          BlobId blobId = BlobId.of(bucketName, blobName);
          BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

          storage.create(blobInfo, inputStream);
          inputStream.close();
          ftpClient.completePendingCommand();

          // Create a JSON file with the content name without the extension
          String jsonFileName = bunnyRequest.getContentName().replace(".mp4", "") + ".json";
          BlobId jsonBlobId = BlobId.of(bucketName, jsonFileName);
          BlobInfo jsonBlobInfo = BlobInfo.newBuilder(jsonBlobId).build();

          // Convert the request back to JSON string
          String jsonData = gson.toJson(bunnyRequest);
          storage.create(jsonBlobInfo, jsonData.getBytes());

          // Respond with success
          response.setStatusCode(200);
          response.getWriter().write("File transferred successfully");
      } catch (Exception e) {
          e.printStackTrace();
          response.setStatusCode(500);
          response.getWriter().write("Internal server error: " + e.getMessage());
      } finally {
          if (ftpClient.isConnected()) {
              ftpClient.logout();
              ftpClient.disconnect();
          }
      }
  }
}
