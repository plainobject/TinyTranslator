package my.tiny.translator;

import org.json.JSONObject;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import my.tiny.translator.core.Event;
import my.tiny.translator.core.HTTPManager;
import my.tiny.translator.core.HTTPRequest;
import my.tiny.translator.core.HTTPCallback;
import my.tiny.translator.core.HTTPResponse;
import my.tiny.translator.core.DataProvider;

public class YandexDataProvider extends DataProvider {
    protected final String url;
    protected final String key;
    protected HTTPRequest request;
    protected HTTPManager requestManager = new HTTPManager();

    public static final List<String> LANGS = Collections.unmodifiableList(
        Arrays.asList("de", "en", "es", "fr", "it", "ru")
    );

    private static final int TEXT_LIMIT = 5000;
    private static final int REQUEST_TIMEOUT = 10000;

    private static final String PARAM_KEY = "key";
    private static final String PARAM_TEXT = "text";
    private static final String PARAM_LANG = "lang";

    private static final String FIELD_TEXT = "text";

    protected class RequestCallback extends HTTPCallback {
        @Override
        public void onResponse(HTTPResponse response) {
            if (response.isSuccessful()) {
                Event event = new Event(EVENT_RESULT);
                event.setDataValue("data", parseResponse(response.text));
                dispatchEvent(event);
            } else {
                dispatchEvent(new Event(EVENT_ERROR));
            }
        }
    }

    public YandexDataProvider(String url, String key) {
        this.url = url;
        this.key = key;
    }

    @Override
    public int getTextLimit() {
        return TEXT_LIMIT;
    }

    @Override
    public void abortRequest() {
        if (request != null) {
            request.cancel();
        }
        request = null;
    }

    @Override
    public void requestData(String text, String sourceLang, String targetLang) {
        abortRequest();
        request = createRequest(
            text,
            sourceLang,
            targetLang
        );
        requestManager.addRequest(request);
    }

    protected String parseResponse(String response) {
        try {
            JSONObject result = new JSONObject(response);
            return result.getJSONArray(FIELD_TEXT).getString(0);
        } catch (Exception exception) {
            return "";
        }
    }

    protected HTTPRequest createRequest(String text, String sourceLang, String targetLang) {
        HTTPRequest request = new HTTPRequest(url, new RequestCallback());
        request.setTimeout(REQUEST_TIMEOUT);
        request.addUrlParam(PARAM_KEY, key);
        request.addBodyParam(PARAM_TEXT, text);
        request.addBodyParam(PARAM_LANG, sourceLang + "-" + targetLang);
        return request;
    }

    @Override
    public boolean isLanguageSupported(String lang) {
        return LANGS.contains(lang);
    }
}
