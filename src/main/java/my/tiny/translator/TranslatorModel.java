package my.tiny.translator;

import java.util.HashMap;
import android.os.AsyncTask;

import my.tiny.translator.core.Event;
import my.tiny.translator.core.Model;
import my.tiny.translator.core.HTTPRequest;
import my.tiny.translator.core.DataProvider;
import my.tiny.translator.core.HTTPResponse;

public class TranslatorModel extends Model {
    protected DataProvider dataProvider;
    protected TranslateTask translateTask;

    private class TranslateTask extends AsyncTask<HTTPRequest, Void, String> {
        @Override
        protected String doInBackground(HTTPRequest... requests) {
            HTTPResponse response = requests[0].send();
            return (response.isOK()) ? response.text : null;
        }

        @Override
        protected void onPostExecute(String response) {
            if (isCancelled()) {
                return;
            }
            if (response == null) {
                dispatchEvent(new Event("error"));
            } else {
                setProperty("translation", dataProvider.parseResponse(response));
                dispatchEvent(new Event("update"));
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

    public void cancelTask() {
        if (translateTask != null &&
            translateTask.getStatus() != AsyncTask.Status.FINISHED) {
            translateTask.cancel(true);
        }
    }

    public void requestTranslation() {
        cancelTask();
        if (!isValid()) {
            return;
        }
        dispatchEvent(new Event("query"));
        translateTask = new TranslateTask();
        translateTask.execute(dataProvider.createRequest(
            getProperty("text"),
            getProperty("sourceLang"),
            getProperty("targetLang")
        ));
    }
}