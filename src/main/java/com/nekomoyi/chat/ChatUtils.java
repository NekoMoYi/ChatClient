package com.nekomoyi.chat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChatUtils {
    private static MessageDigest md5;

    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String str2md5(String str) {
        byte[] md5Bytes = md5.digest(str.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : md5Bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String str2b64(String str){
        return new String(java.util.Base64.getEncoder().encode(str.getBytes()));
    }

    public static String b642str(String b64str){
        return new String(java.util.Base64.getDecoder().decode(b64str.getBytes()));
    }

    public static String getFriendSessionId(Integer userId1, Integer userId2) {
        String sessionId = "";
        sessionId = userId1 < userId2 ? userId1.toString() + "-" + userId2.toString() : userId2.toString() + "-" + userId1.toString();
        return str2b64(sessionId);
    }

    public static String getGroupSessionId(Integer groupId){
        return str2b64(groupId.toString());
    }

    public static Boolean isGroupSession(String b64sessionId) {
        String sessionId = b642str(b64sessionId);
        return !sessionId.contains("-");
    }

    public static Integer getGroupId(String b64sessionId) {
        String sessionId = b642str(b64sessionId);
        return Integer.parseInt(sessionId);
    }

    public static String getDatetimeString(long timestamp) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));
    }

    public static String file2b64(String path) {
        try {
            java.io.FileInputStream fis = new java.io.FileInputStream(path);
            byte[] bytes = new byte[fis.available()];
            fis.read(bytes);
            fis.close();
            return new String(java.util.Base64.getEncoder().encode(bytes));
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}
