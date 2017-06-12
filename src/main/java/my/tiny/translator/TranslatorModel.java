package my.tiny.translator;

import java.util.HashMap;

import my.tiny.translator.core.Event;
import my.tiny.translator.core.Model;
import my.tiny.translator.core.HTTPRequest;
import my.tiny.translator.core.DataProvider;
import my.tiny.translator.core.HTTPCallback;
import my.tiny.translator.core.HTTPResponse;

public class TranslatorModel extends Model {
    protected HTTPRequest request;
    protected DataProvider dataProvider;

    protected class RequestCallback extends HTTPCallback {
        @Override
        public void onResponse(HTTPResponse response) {
            if (response.isSuccessful()) {
                setProperty("translation", dataProvider.parseResponse(response.text));
                dispatchEvent(new Event("update"));
            } else {
                dispatchEvent(new Event("error"));
            }
        }
    }

    public TranslatorModel(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public boolean isValid() {
        String text = getProperty("text");
        String sourceLang = getProperty("sourceLang");
        String targetLang = getProperty("targetLang");
        return !text.isEmpty() && text.length() < dataProvider.getTextLimit() &&
               !sourceLang.isEmpty() && dataProvider.isLanguageSupported(sourceLang) &&
               !targetLang.isEmpty() && dataProvider.isLanguageSupported(targetLang) &&
               !sourceLang.equals(targetLang);
    }

    public void abortRequest() {
        if (request != null) {
            request.abort();
        }
        request = null;
    }

    public void requestTranslation() {
        abortRequest();
        if (!isValid()) {
            return;
        }
        dispatchEvent(new Event("query"));
        request = dataProvider.createRequest(
            getProperty("text"),
            getProperty("sourceLang"),
            getProperty("targetLang")
        );
        request.send(new RequestCallback());
    }
}