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
    private GestureDetector gestureDetector;

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent event) {
            getModel().setSpeed(HALF_SPEED);
            toggle();
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            getModel().setSpeed(NORMAL_SPEED);
            toggle();
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