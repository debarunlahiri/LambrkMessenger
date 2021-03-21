package com.lahiriproductions.lambrk_messenger.Inbox;

public class Inbox {

    private String message = null;
    private String sender_user_id = null;
    private String receiver_user_id = null;
    private long timestamp;
    private boolean has_seen;
    private String formatted_Date = null;
    private String user_key = null;
    private String reply_chat_id = null;
    private String chat_id = null;
    private String media;
    private String media_type;

    public Inbox() {

    }

    public Inbox(String message, String sender_user_id, String receiver_user_id, long timestamp, boolean has_seen, String formatted_Date, String user_key, String reply_chat_id, String chat_id, String media, String media_type) {
        this.message = message;
        this.sender_user_id = sender_user_id;
        this.receiver_user_id = receiver_user_id;
        this.timestamp = timestamp;
        this.has_seen = has_seen;
        this.formatted_Date = formatted_Date;
        this.user_key = user_key;
        this.reply_chat_id = reply_chat_id;
        this.chat_id = chat_id;
        this.media = media;
        this.media_type = media_type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender_user_id() {
        return sender_user_id;
    }

    public void setSender_user_id(String sender_user_id) {
        this.sender_user_id = sender_user_id;
    }

    public String getReceiver_user_id() {
        return receiver_user_id;
    }

    public void setReceiver_user_id(String receiver_user_id) {
        this.receiver_user_id = receiver_user_id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isHas_seen() {
        return has_seen;
    }

    public void setHas_seen(boolean has_seen) {
        this.has_seen = has_seen;
    }

    public String getFormatted_Date() {
        return formatted_Date;
    }

    public void setFormatted_Date(String formatted_Date) {
        this.formatted_Date = formatted_Date;
    }

    public String getUser_key() {
        return user_key;
    }

    public void setUser_key(String user_key) {
        this.user_key = user_key;
    }

    public String getReply_chat_id() {
        return reply_chat_id;
    }

    public void setReply_chat_id(String reply_chat_id) {
        this.reply_chat_id = reply_chat_id;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getMedia_type() {
        return media_type;
    }

    public void setMedia_type(String media_type) {
        this.media_type = media_type;
    }
}
