package my.tiny.translator.core;

import android.view.View;

public class Presenter<T1 extends View, T2 extends Model> extends EventDispatcher {
    private final T1 view;
    private final T2 model;

    public Presenter(T1 view, T2 model) {
        this.view = view;
        this.model = model;
    }

    public T1 getView() {
        return view;
    }

    public T2 getModel() {
        return model;
    }
}
