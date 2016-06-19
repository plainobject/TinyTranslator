package my.tiny.translator.core;

import java.util.HashMap;

public class Model extends EventDispatcher {
    private HashMap<String, String> props = new HashMap<String, String>();

    public boolean isValid() {
        return true;
    }

    public void setProperty(String name, String value) {
        String oldValue = getProperty(name);
        if (value.equals(oldValue)) {
            return;
        }
        props.put(name, value);
        HashMap<String, String> eventData = new HashMap<String, String>();
        eventData.put("name", name);
        eventData.put("value", value);
        eventData.put("oldValue", oldValue);
        dispatchEvent(new Event("change", eventData));
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