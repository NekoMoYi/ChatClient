package com.nekomoyi.chat;

import java.util.ArrayList;

public class Entities {
    public static class Friend {
        public String username;
        public int id;
        public Friend(String username, int id) {
            this.username = username;
            this.id = id;
        }
    }

    public static class Group {
        public int id;
        public String name;
        public ArrayList<Friend> members;
        public Group(int id, String name, ArrayList<Friend> members) {
            this.id = id;
            this.name = name;
            this.members = members;
        }
    }

    public static class Message {
        public Friend sender;
        public String type;
        public String content;
        public long timestamp;
        public Message(Friend sender, String type, String content, long timestamp) {
            this.sender = sender;
            this.type = type;
            this.content = content;
            this.timestamp = timestamp;
        }
    }
}
