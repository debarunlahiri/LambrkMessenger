package com.lahiriproductions.lambrk_messenger.Group;

public class GroupPostComment {

    private String group_id;
    private String group_post_id;
    private String user_id;
    private long timestamp;
    private String body;
    private String comment_id;
    private boolean userHasLikedComment;

    GroupPostComment() {

    }

    public GroupPostComment(String group_id, String group_post_id, String user_id, long timestamp, String body, String comment_id) {
        this.group_id = group_id;
        this.group_post_id = group_post_id;
        this.user_id = user_id;
        this.timestamp = timestamp;
        this.body = body;
        this.comment_id = comment_id;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_post_id() {
        return group_post_id;
    }

    public void setGroup_post_id(String group_post_id) {
        this.group_post_id = group_post_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    public boolean isUserHasLikedComment() {
        return userHasLikedComment;
    }

    public void setUserHasLikedComment(boolean userHasLikedComment) {
        this.userHasLikedComment = userHasLikedComment;
    }
}
