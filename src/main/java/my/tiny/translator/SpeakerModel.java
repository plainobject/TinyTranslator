package my.tiny.translator;

import java.util.Locale;
import java.util.HashMap;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.content.Context;
import android.annotation.TargetApi;

import my.tiny.translator.core.Event;
import my.tiny.translator.core.Model;

public class SpeakerModel extends Model {
    private TextToSpeech tts = null;
    private boolean initialized = false;
    private static final String UTTERANCE_ID = "12345";

    private static final HashMap<String, String> countryMap;
    static {
        countryMap = new HashMap<String, String>();
        countryMap.put("en", "GB");
        countryMap.put("es", "ES");
        countryMap.put("fr", "FR");
    }
    
    public SpeakerModel(Context context) {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    initialized = true;
                }
            }
        });
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onDone(final String utteranceId) {
                dispatchEvent(new Event("stop"));
            }

            @Override
            @Deprecated
            public void onError(final String utteranceId) {
                dispatchEvent(new Event("stop"));
            }

            @Override
            public void onError(final String utteranceId, final int errorCode) {
                dispatchEvent(new Event("stop"));
            }

            @Override
            public void onStart(final String utteranceId) {
                dispatchEvent(new Event("start"));
            }
        });
    }

    public void stop() {
        tts.stop();
        dispatchEvent(new Event("stop"));
    }

    public void speak() {
        if (!isValid()) {
            return;
        }
        tts.setLanguage(getAvailableLocale());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            speakOld();
        } else {
            speakNew();
        }
    }

    @SuppressWarnings("deprecation")
    private void speakOld() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
        tts.speak(getProperty("text"), TextToSpeech.QUEUE_FLUSH, params);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void speakNew() {
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
        tts.speak(getProperty("text"), TextToSpeech.QUEUE_FLUSH, params, UTTERANCE_ID);
    }

    public void destroy() {
        tts.shutdown();
    }

    @Override
    public boolean isValid() {
        String text = getProperty("text");
        return initialized == true && !text.isEmpty() &&
               text.length() < TextToSpeech.getMaxSpeechInputLength() &&
               Utils.hasLetterOrDigit(text) && getAvailableLocale() != null;
    }

    public boolean isSpeaking() {
        return tts.isSpeaking();
    }

    public Locale getAvailableLocale() {
        String lang = getProperty("lang");
        if (lang.isEmpty()) {
            return null;
        }
        if (countryMap.containsKey(lang)) {
            Locale countryLocale = new Locale(lang, countryMap.get(lang));
            if (tts.isLanguageAvailable(countryLocale) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                return countryLocale;
            }
        }
        Locale locale = new Locale(lang);
        if (tts.isLanguageAvailable(locale) == TextToSpeech.LANG_AVAILABLE) {
            return locale;
        }
        return null;
    }
}