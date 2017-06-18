package my.tiny.translator.core;

public abstract class DataProvider extends EventDispatcher {
    public static final String EVENT_ERROR = "error";
    public static final String EVENT_RESULT = "result";

    public abstract int getTextLimit();
    public abstract void requestData(String text, String sourceLang, String targetLang);
    public abstract void abortRequest();
    public abstract boolean isLanguageSupported(String lang);
}
