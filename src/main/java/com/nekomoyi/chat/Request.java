package com.nekomoyi.chat;

public class Request {
    public static class LoginRequest {
        public String username;
        public String password;
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class SendTextMessageRequest {
        public String token;
        public String message;
        public String session;
        public long timestamp;
        public SendTextMessageRequest(String token, String session,String message) {
            this.token = token;
            this.session = session;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public static class SendImageMessageRequest {
        public String token;
        public String imageB64;
        public String session;
        public long timestamp;
        public SendImageMessageRequest(String token, String session,String imageB64) {
            this.token = token;
            this.session = session;
            this.imageB64 = imageB64;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public static class SendFileMessageRequest {
        public String token;
        public String filename;
        public String fileB64;
        public String session;
        public long timestamp;
        public SendFileMessageRequest(String token, String session,String filename, String fileB64) {
            this.token = token;
            this.session = session;
            this.filename = filename;
            this.fileB64 = fileB64;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public static class getFriendListRequest {
        public String token;
        public getFriendListRequest(String token) {
            this.token = token;
        }
    }

    public static class getGroupListRequest {
        public String token;
        public getGroupListRequest(String token) {
            this.token = token;
        }
    }

    public static class getSessionHistoryRequest {
        public String token;
        public String session;
        public getSessionHistoryRequest(String token, String session) {
            this.token = token;
            this.session = session;
        }
    }

    public static class serverMessageRequest {
        public String sessionId;
        public Entities.Message message;
        public serverMessageRequest(String sessionId, Entities.Message message) {
            this.sessionId = sessionId;
            this.message = message;
        }
    }

    public static class addFriendRequest {
        public String token;
        public String username;
        public addFriendRequest(String token, String username) {
            this.token = token;
            this.username = username;
        }
    }
}
