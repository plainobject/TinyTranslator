package my.tiny.translator.core;

public class HTTPResponse {
    public final int code;
    public final String text;
    public static final int CODE_OK = 200;

    public HTTPResponse(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public boolean isOK() {
        return code == CODE_OK;
    }
}