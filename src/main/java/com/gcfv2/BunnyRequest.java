package com.gcfv2;

public class BunnyRequest {
    private String contentName;
    private String storageZone;
    private String path;
    private String link;
    private FtpDetails ftp;
    

    public static class FtpDetails {
        private String https;
        private String useName;
        private String password;
        private String Hostname;
        private int port;
        public String getHttps() {
            return https;
        }
        public void setHttps(String https) {
            this.https = https;
        }
        public String getUseName() {
            return useName;
        }
        public void setUseName(String userName) {
            this.useName = userName;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
        public String getHostname() {
            return Hostname;
        }
        public void setHostname(String hostname) {
            this.Hostname = hostname;
        }
        public int getPort() {
            return port;
        }
        public void setPort(int port) {
            this.port = port;
        }

        // Getters and setters
        
    }

    public String getContentName() {
        return contentName;
    }

    public void setContentName(String contentName) {
        this.contentName = contentName;
    }

    public String getStorageZone() {
        return storageZone;
    }

    public void setStorageZone(String storageZone) {
        this.storageZone = storageZone;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public FtpDetails getFtp() {
        return ftp;
    }

    public void setFtp(FtpDetails ftp) {
        this.ftp = ftp;
    }
}