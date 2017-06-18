package my.tiny.translator;

import my.tiny.translator.core.Event;
import my.tiny.translator.core.Model;
import my.tiny.translator.core.DataProvider;
import my.tiny.translator.core.EventListener;

public class TranslatorModel extends Model implements EventListener {
    public static final String PROP_TEXT = "text";
    public static final String PROP_SOURCE_LANG = "sourceLang";
    public static final String PROP_TARGET_LANG = "targetLang";
    public static final String PROP_TRANSLATION = "translation";
    public static final String EVENT_ERROR = "error";
    public static final String EVENT_QUERY = "query";
    public static final String EVENT_UPDATE = "update";

    protected DataProvider dataProvider;

    public TranslatorModel(DataProvider dataProvider) {
        dataProvider.addListener(this);
        this.dataProvider = dataProvider;
    }

    @Override
    public void handleEvent(Event event) {
        switch (event.type) {
            case DataProvider.EVENT_ERROR:
                dispatchEvent(new Event(EVENT_ERROR));
                break;

            case DataProvider.EVENT_RESULT:
                setProperty(PROP_TRANSLATION, event.getDataValue("data"));
                dispatchEvent(new Event(EVENT_UPDATE));
                break;
        }
    }

    @Override
    public boolean isValid() {
        String text = getProperty(PROP_TEXT);
        String sourceLang = getProperty(PROP_SOURCE_LANG);
        String targetLang = getProperty(PROP_TARGET_LANG);
        return !text.isEmpty() && text.length() < dataProvider.getTextLimit() &&
               !sourceLang.isEmpty() && dataProvider.isLanguageSupported(sourceLang) &&
               !targetLang.isEmpty() && dataProvider.isLanguageSupported(targetLang) &&
               !sourceLang.equals(targetLang);
    }

    public void abortRequest() {
        dataProvider.abortRequest();
    }

    public void requestTranslation() {
        abortRequest();
        if (!isValid()) {
            return;
        }
        dispatchEvent(new Event(EVENT_QUERY));
        dataProvider.requestData(
            getProperty(PROP_TEXT),
            getProperty(PROP_SOURCE_LANG),
            getProperty(PROP_TARGET_LANG)
        );
    }
}
