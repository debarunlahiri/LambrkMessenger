package com.lambrk.messenger.Group;

public class InviteGroup {

    private String user_id = null;

    public InviteGroup() {

    }

    public InviteGroup(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
