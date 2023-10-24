package com.lambrk.messenger.Notifications;

public class Notifications {

    private long timestamp;
    private boolean hasUserRead;
    private String story_id;
    private String notification_type;
    private String to_user_id;
    private String from_user_id;
    private String story_comment_id;
    private String notification_id;

    public Notifications() {

    }

    public Notifications(long timestamp, boolean hasUserRead, String story_id, String notification_type, String to_user_id, String from_user_id, String story_comment_id, String notification_id) {
        this.timestamp = timestamp;
        this.hasUserRead = hasUserRead;
        this.story_id = story_id;
        this.notification_type = notification_type;
        this.to_user_id = to_user_id;
        this.from_user_id = from_user_id;
        this.story_comment_id = story_comment_id;
        this.notification_id = notification_id;
    }

    public String getTo_user_id() {
        return to_user_id;
    }

    public void setTo_user_id(String to_user_id) {
        this.to_user_id = to_user_id;
    }

    public String getFrom_user_id() {
        return from_user_id;
    }

    public void setFrom_user_id(String from_user_id) {
        this.from_user_id = from_user_id;
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


    public String getStory_id() {
        return story_id;
    }

    public void setStory_id(String story_id) {
        this.story_id = story_id;
    }

    public String getNotification_type() {
        return notification_type;
    }

    public void setNotification_type(String notification_type) {
        this.notification_type = notification_type;
    }

    public String getStory_comment_id() {
        return story_comment_id;
    }

    public void setStory_comment_id(String story_comment_id) {
        this.story_comment_id = story_comment_id;
    }

    public String getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(String notification_id) {
        this.notification_id = notification_id;
    }
}
