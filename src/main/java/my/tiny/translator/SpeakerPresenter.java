package my.tiny.translator;

import android.view.View;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.widget.Button;

import my.tiny.translator.core.Event;
import my.tiny.translator.core.Presenter;
import my.tiny.translator.core.EventListener;

public class SpeakerPresenter extends Presenter<Button, SpeakerModel> implements EventListener {
    private static final float HALF_SPEED = 0.5f;
    private static final float NORMAL_SPEED = 1f;
    private final GestureDetector gestureDetector;

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent event) {
            toggle(HALF_SPEED);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            toggle(NORMAL_SPEED);
            return true;
        }
    }

    public SpeakerPresenter(Button view, SpeakerModel model) {
        super(view, model);
        gestureDetector = new GestureDetector(view.getContext(), new GestureListener());
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean detected = gestureDetector.onTouchEvent(event);
                if (detected) {
                    v.performClick();
                }
                return detected;
            }
        });
        model.addListener(this);
    }

    public boolean stop() {
        boolean speaking = getModel().isSpeaking();
        if (speaking) {
            SpeakerModel.stop();
        }
        return speaking;
    }

    private void toggle(float rate) {
        if (!stop()) {
            setRate(rate);
            getModel().speak();
        }
    }

    public void setLang(String lang) {
        getModel().setProperty(SpeakerModel.PROP_LANG, lang);
    }

    public void setRate(float rate) {
        getModel().setProperty(SpeakerModel.PROP_RATE, String.valueOf(rate));
    }

    public void setText(String text) {
        getModel().setProperty(SpeakerModel.PROP_TEXT, Utils.normalizeText(text));
    }

    @Override
    public void handleEvent(Event event) {
        switch (event.type) {
            case SpeakerModel.EVENT_LOAD:
            case SpeakerModel.EVENT_STOP:
            case SpeakerModel.EVENT_START:
                dispatchEvent(new Event(event.type));
                break;

            case SpeakerModel.EVENT_CHANGE:
                stop();
                getView().setVisibility(getModel().isValid() ? View.VISIBLE : View.GONE);
                break;
        }
    }
}
