package my.tiny.translator;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class YandexDictionaryProvider extends YandexDataProvider {
    public static final List<String> dirs;
    static {
        List<String> arrayList = new ArrayList<>();
        arrayList.add("de-en");
        arrayList.add("de-ru");
        arrayList.add("en-de");
        arrayList.add("en-es");
        arrayList.add("en-fr");
        arrayList.add("en-it");
        arrayList.add("en-ru");
        arrayList.add("es-en");
        arrayList.add("es-ru");
        arrayList.add("fr-en");
        arrayList.add("fr-ru");
        arrayList.add("it-en");
        arrayList.add("it-ru");
        arrayList.add("ru-de");
        arrayList.add("ru-en");
        arrayList.add("ru-es");
        arrayList.add("ru-fr");
        arrayList.add("ru-it");
        dirs = Collections.unmodifiableList(arrayList);
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
            ArrayList<String> text = new ArrayList<>();
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