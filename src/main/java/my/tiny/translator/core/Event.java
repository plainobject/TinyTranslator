package my.tiny.translator.core;

import java.util.HashMap;

public class Event {
    public String type;
    public HashMap<String, String> data;

    public Event(String type, HashMap<String, String> data) {
        this.type = type;
        this.data = data;
    }
}