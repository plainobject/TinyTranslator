package my.tiny.translator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import android.text.TextUtils;

public class YandexDictionaryProvider extends YandexDataProvider {
    public static final List<String> DIRS = Collections.unmodifiableList(
        Arrays.asList(
            "de-en", "de-ru",
            "en-de", "en-es", "en-fr", "en-it", "en-ru",
            "es-en", "es-ru",
            "fr-en", "fr-ru",
            "it-en", "it-ru",
            "ru-de", "ru-en", "ru-es", "ru-fr", "ru-it"
        )
    );

    private static final int TEXT_LIMIT = 100;

    private static final String FIELD_TR = "tr";
    private static final String FIELD_DEF = "def";
    private static final String FIELD_SYN = "syn";
    private static final String FIELD_TEXT = "text";

    private static final String COMMA_DELIMITER = ", ";
    private static final String NEWLINE_DELIMITER = "\n\n";

    public YandexDictionaryProvider(String url, String key) {
        super(url, key);
    }

    @Override
    public int getTextLimit() {
        return TEXT_LIMIT;
    }

    @Override
    protected String parseResponse(String response) {
        try {
            List<String> text = new ArrayList<>();
            JSONObject result = new JSONObject(response);
            JSONArray defs = result.getJSONArray(FIELD_DEF);
            for (int i = 0; i < defs.length(); i++) {
                List<String> block = new ArrayList<>();
                JSONObject def = defs.getJSONObject(i);
                JSONArray trs = def.getJSONArray(FIELD_TR);
                for (int j = 0; j < trs.length(); j++) {
                    JSONObject tr = trs.getJSONObject(j);
                    block.add(tr.getString(FIELD_TEXT));
                    if (tr.has(FIELD_SYN)) {
                        JSONArray syns = tr.getJSONArray(FIELD_SYN);
                        for (int k = 0; k < syns.length(); k++) {
                            JSONObject syn = syns.getJSONObject(k);
                            block.add(syn.getString(FIELD_TEXT));
                        }
                    }
                }
                text.add(TextUtils.join(COMMA_DELIMITER, block));
            }
            return TextUtils.join(NEWLINE_DELIMITER, text);
        } catch (Exception exception) {
            return "";
        }
    }

    @Override
    public boolean isLanguageSupported(String lang) {
        return DIRS.contains(lang);
    }
}
