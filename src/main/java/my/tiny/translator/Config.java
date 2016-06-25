package my.tiny.translator;

public final class Config {
    private Config() {}

    public static int SPLASH_DELAY = 750;
    public static int PROGRESS_DELAY = 250;
    public static int PROGRESS_FADE_DURATION = 200;
    public static String ICONFONT_FILENAME = "icons.ttf";
    public static String TRANSLATOR_APIKEY = ""; // https://tech.yandex.ru/keys/get/?service=trnsl
    public static String TRANSLATOR_APIURL = "https://translate.yandex.net/api/v1.5/tr.json/translate";
    public static String TRANSLATOR_DEFAULT_SOURCELANG = "en";
    public static String TRANSLATOR_DEFAULT_TARGETLANG = "ru";
}