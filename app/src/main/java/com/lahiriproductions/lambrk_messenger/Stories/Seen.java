package com.lahiriproductions.lambrk_messenger.Stories;

public class Seen {

    private String story_id;
    private String user_id;
    private String story_user_id;
    private long timestamp;
    private boolean has_seen_story;

    public Seen() {

    }

    public Seen(String story_id, String user_id, String story_user_id, long timestamp, boolean has_seen_story) {
        this.story_id = story_id;
        this.user_id = user_id;
        this.story_user_id = story_user_id;
        this.timestamp = timestamp;
    }

    public String getStory_id() {
        return story_id;
    }

    public void setStory_id(String story_id) {
        this.story_id = story_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getStory_user_id() {
        return story_user_id;
    }

    public void setStory_user_id(String story_user_id) {
        this.story_user_id = story_user_id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getIsHas_seen_story() {
        return has_seen_story;
    }

    public void setHas_seen_story(boolean has_seen_story) {
        this.has_seen_story = has_seen_story;
    }
}
