package my.tiny.translator.core;

public interface DataProvider {
    int getTextLimit();
    String generateUrl(String text, String sourceLang, String targetLang);
    String parseResponse(String response);
    boolean isLanguageSupported(String lang);
}