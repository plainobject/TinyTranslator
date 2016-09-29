package my.tiny.translator;

import android.view.View;
import android.widget.TextView;

import my.tiny.translator.core.Event;
import my.tiny.translator.core.Debouncer;
import my.tiny.translator.core.Presenter;
import my.tiny.translator.core.EventListener;

public class TranslatorPresenter extends Presenter<TextView, TranslatorModel> implements EventListener {
    private Debouncer requestDebouncer;
    private static final int REQUEST_DELAY = 250;

    public TranslatorPresenter(TextView view, TranslatorModel model) {
        super(view, model);
        model.addListener(this);
        requestDebouncer = new Debouncer(REQUEST_DELAY, new Runnable() {
            public void run() {
                getModel().requestTranslation();
            }
        });
    }

    public void setText(String text) {
        getModel().setProperty("text", Utils.normalizeText(text));
    }

    public void setSourceLang(String lang) {
        getModel().setProperty("sourceLang", lang);
    }

    public void setTargetLang(String lang) {
        getModel().setProperty("targetLang", lang);
    }

    @Override
    public void handleEvent(Event event) {
        if (event.type.equals("query")) {
            dispatchEvent(new Event("query"));
            return;
        }

        if (event.type.equals("error")) {
            dispatchEvent(new Event("error"));
            return;
        }

        if (event.type.equals("update")) {
            dispatchEvent(new Event("update"));
            return;
        }

        if (event.type.equals("change")) {
            String name = event.getDataValue("name");
            String value = event.getDataValue("value");
            switch (name) {
                case "text":
                case "sourceLang":
                case "targetLang":
                    TranslatorModel model = getModel();
                    if (!model.isValid()) {
                        model.cancelTask();
                        model.setProperty("translation", "");
                        requestDebouncer.cancel();
                        dispatchEvent(new Event("invalid"));
                        break;
                    }
                    requestTranslation();
                    break;

                case "translation":
                    getView().setText(value);
                    getView().setVisibility(value.isEmpty() ? View.GONE : View.VISIBLE);
                    Event innerEvent = new Event("change");
                    innerEvent.setDataValue("translation", value);
                    dispatchEvent(innerEvent);
                    break;
            }
        }
    }

    public void requestTranslation() {
        requestDebouncer.start();
    }
}