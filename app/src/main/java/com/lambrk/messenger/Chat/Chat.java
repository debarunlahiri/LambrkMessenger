package com.lambrk.messenger.Chat;

public class Chat {

    private String sender_user_id = null;
    private String receiver_user_id = null;
    private long timestamp;
    private String formatted_date = null;
    private String chat_id = null;
    private String message = null;
    private boolean has_seen;
    private String reply_chat_id = null;
    private String media;
    private String media_type;

    public Chat() {

    }

    public Chat(String sender_user_id, String receiver_user_id, long timestamp, String formatted_date, String chat_id, String message, boolean has_seen, String reply_chat_id, String media, String media_type) {
        this.sender_user_id = sender_user_id;
        this.receiver_user_id = receiver_user_id;
        this.timestamp = timestamp;
        this.formatted_date = formatted_date;
        this.chat_id = chat_id;
        this.message = message;
        this.has_seen = has_seen;
        this.reply_chat_id = reply_chat_id;
        this.media = media;
        this.media_type = media_type;
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

    public String getFormatted_date() {
        return formatted_date;
    }

    public void setFormatted_date(String formatted_date) {
        this.formatted_date = formatted_date;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isHas_seen() {
        return has_seen;
    }

    public void setHas_seen(boolean has_seen) {
        this.has_seen = has_seen;
    }

    public String getReply_chat_id() {
        return reply_chat_id;
    }

    public void setReply_chat_id(String reply_chat_id) {
        this.reply_chat_id = reply_chat_id;
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
