package com.lambrk.messenger.Group;

public class ActiveGroupMembers {

    private String user_id;
    private String formatted_date;
    private long timestamp;
    private boolean isOnline;
    private String group_id;

    public ActiveGroupMembers() {

    }

    public ActiveGroupMembers(String user_id, String formatted_date, long timestamp, boolean isOnline, String group_id) {
        this.user_id = user_id;
        this.formatted_date = formatted_date;
        this.timestamp = timestamp;
        this.isOnline = isOnline;
        this.group_id = group_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getFormatted_date() {
        return formatted_date;
    }

    public void setFormatted_date(String formatted_date) {
        this.formatted_date = formatted_date;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getIsOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }
}
