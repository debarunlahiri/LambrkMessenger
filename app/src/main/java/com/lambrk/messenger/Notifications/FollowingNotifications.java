package com.lambrk.messenger.Notifications;

public class FollowingNotifications {

    private String user_id;
    private String searched_user_id;
    private long timestamp;
    private boolean hasUserRead;

    public  FollowingNotifications() {

    }

    public FollowingNotifications(String user_id, String searched_user_id, long timestamp, boolean hasUserRead) {
        this.user_id = user_id;
        this.searched_user_id = searched_user_id;
        this.timestamp = timestamp;
        this.hasUserRead = hasUserRead;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getSearched_user_id() {
        return searched_user_id;
    }

    public void setSearched_user_id(String searched_user_id) {
        this.searched_user_id = searched_user_id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isHasUserRead() {
        return hasUserRead;
    }

    public void setHasUserRead(boolean hasUserRead) {
        this.hasUserRead = hasUserRead;
    }
}
