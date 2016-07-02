package my.tiny.translator;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class YandexDictionaryProvider extends YandexDataProvider {
    public static final ArrayList<String> dirs;
    static {
        dirs = new ArrayList<String>();
        dirs.add("de-en");
        dirs.add("de-ru");
        dirs.add("en-de");
        dirs.add("en-es");
        dirs.add("en-fr");
        dirs.add("en-it");
        dirs.add("en-ru");
        dirs.add("es-en");
        dirs.add("es-ru");
        dirs.add("fr-en");
        dirs.add("fr-ru");
        dirs.add("it-en");
        dirs.add("it-ru");
        dirs.add("ru-de");
        dirs.add("ru-en");
        dirs.add("ru-es");
        dirs.add("ru-fr");
        dirs.add("ru-it");
    }

    public YandexDictionaryProvider(String url, String key) {
        super(url, key);
    }

    @Override
    public int getTextLimit() {
        return 100;
    }

    @Override
    public String parseResponse(String response) {
        try {
            ArrayList<String> text = new ArrayList<String>();
            JSONObject result = new JSONObject(response);
            JSONArray defs = result.getJSONArray("def");
            for (int i = 0; i < defs.length(); i++) {
                JSONObject def = defs.getJSONObject(i);
                JSONArray trs = def.getJSONArray("tr");
                for (int j = 0; j < trs.length(); j++) {
                    JSONObject tr = trs.getJSONObject(j);
                    text.add(tr.getString("text"));
                    if (tr.has("syn")) {
                        JSONArray syns = tr.getJSONArray("syn");
                        for (int k = 0; k < syns.length(); k++) {
                            JSONObject syn = syns.getJSONObject(k);
                            text.add(syn.getString("text"));
                        }
                    }
                }
            }
            return Utils.joinWithSeparator(text, ", ");
        } catch (Exception exception) {
            return "";
        }
    }

    @Override
    public boolean isLanguageSupported(String lang) {
        return dirs.contains(lang);
    }
}