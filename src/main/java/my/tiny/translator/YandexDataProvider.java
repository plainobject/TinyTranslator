package my.tiny.translator;

import org.json.JSONObject;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import my.tiny.translator.core.HTTPRequest;
import my.tiny.translator.core.DataProvider;

public class YandexDataProvider implements DataProvider {
    protected String url;
    protected String key;

    public static final int REQUEST_TIMEOUT = 10000;
    public static final Map<String, String> langs;
    static {
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("de", "German");
        hashMap.put("en", "English");
        hashMap.put("es", "Spanish");
        hashMap.put("fr", "French");
        hashMap.put("it", "Italian");
        hashMap.put("ru", "Russian");
        langs = Collections.unmodifiableMap(hashMap);
    }

    public YandexDataProvider(String url, String key) {
        this.url = url;
        this.key = key;
    }

    public int getTextLimit() {
        return 5000;
    }

    public String parseResponse(String response) {
        try {
            JSONObject result = new JSONObject(response);
            return result.getJSONArray("text").getString(0);
        } catch (Exception exception) {
            return "";
        }
    }

    public HTTPRequest createRequest(String text, String sourceLang, String targetLang) {
        HTTPRequest request = new HTTPRequest(url);
        request.setTimeout(REQUEST_TIMEOUT);
        request.addUrlParam("key", key);
        request.addBodyParam("text", text);
        request.addBodyParam("lang", sourceLang + "-" + targetLang);
        return request;
    }

    public boolean isLanguageSupported(String lang) {
        return langs.containsKey(lang);
    }
}