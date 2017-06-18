package my.tiny.translator;

import java.util.Map;
import java.util.Locale;
import java.util.HashMap;
import java.util.Collections;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.annotation.TargetApi;

public class TextToSpeechWrapper extends BroadcastReceiver implements Handler.Callback, TextToSpeech.OnInitListener {
    private static final int STATE_INIT = 0;
    private static final int STATE_LOAD = 1;
    private static final int STATE_STOP = 2;
    private static final int STATE_START = 3;

    private static final int MAX_TEXT_LENGTH = 1000;

    private static final String UTTERANCE_ID = "12345";
    private static final Map<String, String> COUNTRY_MAP;
    static {
        Map<String, String> countryMap = new HashMap<>();
        countryMap.put("de", "DE");
        countryMap.put("en", "GB");
        countryMap.put("es", "ES");
        countryMap.put("fr", "FR");
        countryMap.put("it", "IT");
        countryMap.put("ru", "RU");
        COUNTRY_MAP = Collections.unmodifiableMap(countryMap);
    }

    private Context context;
    private Handler stateHandler;
    private TextToSpeech textToSpeech;
    private TextToSpeechListener listener;
    private final Map<String, Locale> localeCache = new HashMap<>();

    public interface TextToSpeechListener {
        public void onInit();
        public void onLoad();
        public void onStop(boolean error);
        public void onStart();
    }

    public TextToSpeechWrapper(Context context) {
        this.context = context;
        stateHandler = new Handler(this);
        textToSpeech = new TextToSpeech(context, this);
    }

    public void stop() {
        if (isSpeaking()) {
            textToSpeech.stop();
            sendMessage(STATE_STOP, false);
        }
    }

    public void start(String lang, String text) {
        start(0f, lang, text);
    }

    public void start(float rate, String lang, String text) {
        if (isSpeaking()) {
            return;
        }
        sendMessage(STATE_LOAD);
        if (rate > 0) {
            textToSpeech.setSpeechRate(rate);
        }
        try {
            textToSpeech.setLanguage(getLocaleFromCache(lang));
        } catch (Exception exception) {
            // https://code.google.com/p/android/issues/detail?id=80696
            sendMessage(STATE_STOP, true);
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            speakOld(text);
        } else {
            speakNew(text);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            sendMessage(STATE_INIT);
        }
    }

    public void destroy() {
        stop();
        context.unregisterReceiver(this);
        context = null;
        listener = null;
        localeCache.clear();
        textToSpeech.setOnUtteranceProgressListener(null);
        textToSpeech.shutdown();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextToSpeech.Engine.ACTION_TTS_DATA_INSTALLED.equals(intent.getAction())) {
            localeCache.clear();
        }
    }

    public void setListener(TextToSpeechListener listener) {
        this.listener = listener;
    }

    public boolean isSpeaking() {
        return textToSpeech.isSpeaking();
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case STATE_INIT:
                if (listener != null) {
                    listener.onInit();
                }
                registerReceiver();
                setProgressListener();
                break;

            case STATE_LOAD:
                if (listener != null) {
                    listener.onLoad();
                }
                break;

            case STATE_STOP:
                if (listener != null) {
                    listener.onStop((boolean) message.obj);
                }
                break;

            case STATE_START:
                if (listener != null) {
                    listener.onStart();
                }
                break;
        }
        return true;
    }

    public boolean isDataSupported(String lang, String text) {
        if (lang == null || text == null || lang.isEmpty() || text.isEmpty()) {
            return false;
        }
        return text.length() <= MAX_TEXT_LENGTH && getLocaleFromCache(lang) != null;
    }

    @SuppressWarnings("deprecation")
    private void speakOld(String text) {
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void speakNew(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
    }

    private void sendMessage(int state) {
        sendMessage(state, null);
    }

    private void sendMessage(int state, Object result) {
        stateHandler.obtainMessage(state, result).sendToTarget();
    }

    private void registerReceiver() {
        context.registerReceiver(
            this, new IntentFilter(TextToSpeech.Engine.ACTION_TTS_DATA_INSTALLED)
        );
    }

    private void setProgressListener() {
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onDone(final String utteranceId) {
                sendMessage(STATE_STOP, false);
            }

            @Override
            @Deprecated
            public void onError(final String utteranceId) {
                sendMessage(STATE_STOP, true);
            }

            @Override
            public void onError(final String utteranceId, final int errorCode) {
                sendMessage(STATE_STOP, true);
            }

            @Override
            public void onStart(final String utteranceId) {
                sendMessage(STATE_START);
            }
        });
    }

    private Locale getLocaleFromCache(String lang) {
        if (localeCache.containsKey(lang)) {
            return localeCache.get(lang);
        }
        Locale locale = getAvailableLocale(lang);
        localeCache.put(lang, locale);
        return locale;
    }

    private Locale getAvailableLocale(String lang) {
        if (COUNTRY_MAP.containsKey(lang)) {
            Locale countryLocale = new Locale(lang, COUNTRY_MAP.get(lang));
            if (textToSpeech.isLanguageAvailable(countryLocale) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                return countryLocale;
            }
        }
        Locale locale = new Locale(lang);
        if (textToSpeech.isLanguageAvailable(locale) == TextToSpeech.LANG_AVAILABLE) {
            return locale;
        }
        return null;
    }
}
