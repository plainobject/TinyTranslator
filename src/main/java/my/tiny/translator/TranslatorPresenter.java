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
            @Override
            public void run() {
                getModel().requestTranslation();
            }
        });
    }

    public void setText(String text) {
        getModel().setProperty(TranslatorModel.PROP_TEXT, Utils.normalizeText(text));
    }

    public void setSourceLang(String lang) {
        getModel().setProperty(TranslatorModel.PROP_SOURCE_LANG, lang);
    }

    public void setTargetLang(String lang) {
        getModel().setProperty(TranslatorModel.PROP_TARGET_LANG, lang);
    }

    @Override
    public void handleEvent(Event event) {
        switch (event.type) {
            case TranslatorModel.EVENT_ERROR:
            case TranslatorModel.EVENT_QUERY:
            case TranslatorModel.EVENT_UPDATE:
                dispatchEvent(new Event(event.type));
                return;
        }

        if (event.type.equals(TranslatorModel.EVENT_CHANGE)) {
            String name = event.getDataValue("name");
            String value = event.getDataValue("value");
            switch (name) {
                case TranslatorModel.PROP_TEXT:
                case TranslatorModel.PROP_SOURCE_LANG:
                case TranslatorModel.PROP_TARGET_LANG:
                    TranslatorModel model = getModel();
                    if (!model.isValid()) {
                        abortRequest();
                        model.setProperty(TranslatorModel.PROP_TRANSLATION, "");
                        dispatchEvent(new Event("invalid"));
                        break;
                    }
                    requestTranslation();
                    break;

                case TranslatorModel.PROP_TRANSLATION:
                    TextView view = getView();
                    view.setText(value);
                    view.setVisibility(value.isEmpty() ? View.GONE : View.VISIBLE);
                    Event innerEvent = new Event("change");
                    innerEvent.setDataValue("translation", value);
                    dispatchEvent(innerEvent);
                    break;
            }
        }
    }

    public void abortRequest() {
        getModel().abortRequest();
        requestDebouncer.cancel();
    }

    public void requestTranslation() {
        requestDebouncer.start();
    }
}
