package my.tiny.translator;

import android.view.View;
import android.widget.Button;

import my.tiny.translator.core.Event;
import my.tiny.translator.core.Presenter;
import my.tiny.translator.core.EventListener;

public class SpeakerPresenter extends Presenter<Button, SpeakerModel> implements EventListener {
    public SpeakerPresenter(Button view, SpeakerModel model) {
        super(view, model);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });
        model.addListener(this);
    }

    public boolean stop() {
        SpeakerModel model = getModel();
        boolean speaking = model.isSpeaking();
        if (speaking) {
            model.stop();
        }
        return speaking;
    }

    public void toggle() {
        if (!stop()) {
            getModel().speak();
        }
    }

    public void destroy() {
        getModel().destroy();
    }

    public void setText(String text) {
        getModel().setProperty("text", Utils.normalizeText(text));
    }

    public void setLang(String lang) {
        getModel().setProperty("lang", lang);
    }

    @Override
    public void handleEvent(Event event) {
        switch (event.type) {
            case "stop":
            case "start":
                dispatchEvent(new Event(event.type));
                break;

            case "change":
                getView().setVisibility(getModel().isValid() ? View.VISIBLE : View.GONE);
                getModel().stop();
                break;
        }
    }
}