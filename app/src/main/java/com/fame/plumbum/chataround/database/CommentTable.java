package com.fame.plumbum.chataround.database;

/**
 * Created by pankaj on 23/10/16.
 */

public class CommentTable {
    int id;
    String post_id;
    String commentor_id;
    String comment;
    String time;

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCommentor_id(String commentor_id) {
        this.commentor_id = commentor_id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public String getComment() {
        return comment;
    }

    public String getCommentor_id() {
        return commentor_id;
    }

    public String getPost_id() {
        return post_id;
    }

    public String getTime() {
        return time;
    }
}
