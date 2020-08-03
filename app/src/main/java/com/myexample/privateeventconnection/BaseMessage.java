package com.myexample.privateeventconnection;

public class BaseMessage {
    private String name;
    private String content;
    private String time;
    private String uid;

    public BaseMessage(String name, String content, String time, String uID){
        this.name = name;
        this.content = content;
        this.time = time;
        this.uid = uID;
    }
    public BaseMessage(){

    }


    public void setuID(String uID) {
        this.uid = uID;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getuID() {
        return uid;
    }

    public String getContent() {
        return content;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }
}
