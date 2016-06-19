package my.tiny.translator.core;

import java.util.ArrayList;

public class EventDispatcher {
    private ArrayList<EventListener> listeners = new ArrayList<EventListener>();

    public void addListener(EventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(EventListener listener) {
        listeners.remove(listener);
    }

    public void dispatchEvent(Event event) {
        for (EventListener listener : listeners) {
            listener.handleEvent(event);
        }
    }
}