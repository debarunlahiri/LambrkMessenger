package com.lahiriproductions.lambrk_messenger.Explore;

public class Explore {

    private String name;
    private String username;
    private String profile_image;
    private String profile_background_image = null;
    private long acc_create_timestamp;

    private String body;
    private String user_id;
    private String post_id;
    private String group_id;
    private long timestamp;
    private String formatted_date;
    private String post_image = null;
    private String thumb_post_image = null;
    private boolean userHasliked = false;

    Explore() {

    }

    public Explore(String name, String username, String profile_image, long acc_create_timestamp, String profile_background_image) {
        this.name = name;
        this.username = username;
        this.profile_image = profile_image;
        this.acc_create_timestamp = acc_create_timestamp;
        this.profile_background_image = profile_background_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public long getAcc_create_timestamp() {
        return acc_create_timestamp;
    }

    public void setAcc_create_timestamp(long acc_create_timestamp) {
        this.acc_create_timestamp = acc_create_timestamp;
    }

    public String getProfile_background_image() {
        return profile_background_image;
    }

    public void setProfile_background_image(String profile_background_image) {
        this.profile_background_image = profile_background_image;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
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

    public String getPost_image() {
        return post_image;
    }

    public void setPost_image(String post_image) {
        this.post_image = post_image;
    }

    public String getThumb_post_image() {
        return thumb_post_image;
    }

    public void setThumb_post_image(String thumb_post_image) {
        this.thumb_post_image = thumb_post_image;
    }

    public boolean isUserHasliked() {
        return userHasliked;
    }

    public void setUserHasliked(boolean userHasliked) {
        this.userHasliked = userHasliked;
    }
}
