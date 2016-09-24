package my.tiny.translator.core;

public interface DataProvider {
    int getTextLimit();
    String parseResponse(String response);
    boolean isLanguageSupported(String lang);
    HTTPRequest createRequest(String text, String sourceLang, String targetLang);
}