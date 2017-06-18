package my.tiny.translator.core;

import java.util.Map;
import java.util.HashMap;

public class Model extends EventDispatcher {
    public static final String EMPTY_VALUE = "";
    public static final String EVENT_CHANGE = "change";

    private Map<String, String> props = new HashMap<>();

    public boolean isValid() {
        return true;
    }

    public void setProperty(String name, String value) {
        setProperty(name, value, false);
    }

    public void setProperty(String name, String value, boolean silent) {
        String oldValue = getProperty(name);
        if (oldValue.equals(value)) {
            return;
        }
        props.put(name, value);
        if (silent) {
            return;
        }
        Event event = new Event(EVENT_CHANGE);
        event.setDataValue("name", name);
        event.setDataValue("value", value);
        event.setDataValue("oldValue", oldValue);
        dispatchEvent(event);
    }

    public String getProperty(String name) {
        String value = props.get(name);
        return (value == null) ? EMPTY_VALUE : value;
    }
}
