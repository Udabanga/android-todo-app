package com.example.todomanager;

import java.util.HashMap;

public class Item {
    String id, name_TODO, status;

    public Item(String id, String name_TODO, String status) {
        this.id = id;
        this.name_TODO = name_TODO;
        this.status = status;
    }

    public Item() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName_TODO() {
        return name_TODO;
    }

    public void setName_TODO(String name_TODO) {
        this.name_TODO = name_TODO;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public HashMap<String,String> toFirebaseObject() {
        HashMap<String,String> item =  new HashMap<String,String>();
        item.put("id", id);
        item.put("name_TODO", name_TODO);
        item.put("status", status);

        return item;
    }

}
