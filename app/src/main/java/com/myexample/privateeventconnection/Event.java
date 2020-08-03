package com.myexample.privateeventconnection;

import java.util.ArrayList;
import java.util.List;

public class Event {
    String EventName;
    String Admin;
    String Description;
    String Location;
    String EventTime;
    public Event(String eventName, String description, String eventTime,
          String admin, String location){
        this.EventName = eventName;
        this.Description = description;
        this.EventTime = eventTime;
        this.Admin = admin;
        this.Location = location;
    }

    public Event(){

    }

    public String getAdmin() {
        return Admin;
    }

    public void setAdmin(String admin) {
        Admin = admin;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getEventName() {
        return EventName;
    }

    public void setEventName(String eventName) {
        EventName = eventName;
    }

    public String getEventTime() {
        return EventTime;
    }

    public void setEventTime(String eventTime) {
        EventTime = eventTime;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }
}
