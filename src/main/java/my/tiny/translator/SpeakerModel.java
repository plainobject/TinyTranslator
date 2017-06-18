package my.tiny.translator;

import android.content.Context;

import my.tiny.translator.core.Event;
import my.tiny.translator.core.Model;

public class SpeakerModel extends Model implements TextToSpeechWrapper.TextToSpeechListener {
    public static final String PROP_LANG = "lang";
    public static final String PROP_RATE = "rate";
    public static final String PROP_TEXT = "text";
    public static final String EVENT_LOAD = "load";
    public static final String EVENT_STOP = "stop";
    public static final String EVENT_ERROR = "error";
    public static final String EVENT_START = "start";

    private static TextToSpeechWrapper ttsWrapper;

    private boolean ready;
    private boolean speaking;

    public static void init(Context context) {
        if (ttsWrapper == null) {
            ttsWrapper = new TextToSpeechWrapper(context);
        }
    }

    public static void stop() {
        if (ttsWrapper != null) {
            ttsWrapper.stop();
        }
    }

    public static void destroy() {
        if (ttsWrapper != null) {
            ttsWrapper.destroy();
        }
    }

    public void speak() {
        if (!isValid()) {
            return;
        }
        stop();
        ttsWrapper.setListener(this);
        String rate = getProperty(PROP_RATE);
        String lang = getProperty(PROP_LANG);
        String text = getProperty(PROP_TEXT);
        if (rate.isEmpty()) {
            ttsWrapper.start(lang, text);
        } else {
            ttsWrapper.start(Float.parseFloat(rate), lang, text);
        }
    }

    @Override
    public void onInit() {
        ready = true;
    }

    @Override
    public void onLoad() {
        speaking = true;
        dispatchEvent(new Event(EVENT_LOAD));
    }

    @Override
    public void onStop(boolean error) {
        speaking = false;
        dispatchEvent(new Event(error ? EVENT_ERROR : EVENT_STOP));
    }

    @Override
    public void onStart() {
        dispatchEvent(new Event(EVENT_START));
    }

    @Override
    public boolean isValid() {
        // Utils.hasLetterOrDigit(text)
        return ready &&
               ttsWrapper != null &&
               ttsWrapper.isDataSupported(
                   getProperty(PROP_LANG),
                   getProperty(PROP_TEXT)
               );
    }

    public boolean isSpeaking() {
        return speaking;
    }
}
