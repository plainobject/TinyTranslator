package my.tiny.translator.core;

import java.util.HashMap;

public class Model extends EventDispatcher {
    private HashMap<String, String> props = new HashMap<>();

    public boolean isValid() {
        return true;
    }

    public void setProperty(String name, String value) {
        String oldValue = getProperty(name);
        if (value.equals(oldValue)) {
            return;
        }
        props.put(name, value);
        Event event = new Event("change");
        event.setDataValue("name", name);
        event.setDataValue("value", value);
        event.setDataValue("oldValue", oldValue);
        dispatchEvent(event);
    }

    public String getProperty(String name) {
        String value = "";
        if (hasProperty(name)) {
            value = props.get(name);
        }
        return value;
    }

    public boolean hasProperty(String name) {
        return props.containsKey(name);
    }
}