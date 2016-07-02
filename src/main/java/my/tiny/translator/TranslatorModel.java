package my.tiny.translator;

import java.util.HashMap;
import android.os.AsyncTask;

import my.tiny.translator.core.Event;
import my.tiny.translator.core.Model;
import my.tiny.translator.core.DataProvider;

public class TranslatorModel extends Model {
    protected DataProvider dataProvider;
    protected TranslateTask translateTask = null;
    private static final int REQUEST_TIMEOUT = 10000;

    private class TranslateTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return Utils.downloadUrl(urls[0], REQUEST_TIMEOUT);
            } catch (Exception exception) {
                return null;
            }
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
        translateTask.execute(dataProvider.generateUrl(
            getProperty("text"),
            getProperty("sourceLang"),
            getProperty("targetLang")
        ));
    }
}