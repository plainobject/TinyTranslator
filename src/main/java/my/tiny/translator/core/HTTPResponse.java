package my.tiny.translator.core;

public class HTTPResponse {
    public final int code;
    public final String text;

    public HTTPResponse(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }
}
