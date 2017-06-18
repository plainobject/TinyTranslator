package my.tiny.translator;

import my.tiny.translator.core.Model;

public class MainModel extends Model {
    public static final String PROP_TEXT = "text";
    public static final String PROP_SOURCE_LANG = "sourceLang";
    public static final String PROP_TARGET_LANG = "targetLang";
    public static final String PROP_TRANSLATION = "translation";
    public static final String EVENT_ERROR = "error";
    public static final String EVENT_FOCUS = "focus";
    public static final String EVENT_PAUSE = "pause";
    public static final String EVENT_QUERY = "query";
    public static final String EVENT_RETRY = "retry";
    public static final String EVENT_RESULT = "result";
    public static final String EVENT_DESTROY = "destroy";
}
