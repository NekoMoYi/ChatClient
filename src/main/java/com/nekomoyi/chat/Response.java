package com.nekomoyi.chat;

import java.util.ArrayList;

public class Response {
    public static class LoginResponse {
        public int code;
        public String token;
        public LoginResponse(int code, String token) {
            this.code = code;
            this.token = token;
        }
    }

    public static class getFriendListResponse {
        public int code;
        public ArrayList<Entities.Friend> friends;
        public getFriendListResponse(int code, ArrayList<Entities.Friend> friends) {
            this.code = code;
            this.friends = friends;
        }
    }

    public static class getGroupListResponse {
        public int code;
        public ArrayList<Entities.Group> groups;
        public getGroupListResponse(int code, ArrayList<Entities.Group> groups) {
            this.code = code;
            this.groups = groups;
        }
    }

    public static class getSessionHistoryResponse {
        public int code;
        public ArrayList<Entities.Message> messages;
        public getSessionHistoryResponse(int code, ArrayList<Entities.Message> messages) {
            this.code = code;
            this.messages = messages;
        }
    }

    public static class serverMessageResponse {
        public String sessionId;
        public Entities.Message message;
        public serverMessageResponse(String sessionId, Entities.Message message) {
            this.sessionId = sessionId;
            this.message = message;
        }
    }

    public static class addFriendResponse {
        public int code;
        public String msg;
        public addFriendResponse(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }
}
