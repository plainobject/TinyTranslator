package my.tiny.translator.core;

public class Presenter<T1, T2> extends EventDispatcher {
    private T1 view;
    private T2 model;

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