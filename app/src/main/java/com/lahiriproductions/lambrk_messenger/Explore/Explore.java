package com.lahiriproductions.lambrk_messenger.Explore;

public class Explore {

    private String name;
    private String username;
    private String profile_image;
    private String profile_background_image = null;
    private long acc_create_timestamp;

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
}
