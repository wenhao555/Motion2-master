package com.james.motion.commmon.bean;

/**
 * Created by
 */

public class EventBusBean {
    private String tag;


    public EventBusBean(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
