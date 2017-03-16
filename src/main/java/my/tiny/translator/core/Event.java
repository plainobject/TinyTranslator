package my.tiny.translator.core;

import java.util.HashMap;

public class Event {
    public final String type;
    protected HashMap<String, String> data = new HashMap<>();

    public Event(String type) {
        this(type, null);
    }

    public Event(String type, HashMap<String, String> data) {
        if (type == null) {
            throw new IllegalArgumentException("Invalid type");
        }
        this.type = type;
        if (data != null) {
            this.data.putAll(data);
        }
    }

    public Event clone() {
        return new Event(type, data);
    }

    public String getDataValue(String key) {
        String value = data.get(key);
        return (value == null) ? "" : value;
    }

    public void setDataValue(String key, String value) {
        data.put(key, value);
    }
}