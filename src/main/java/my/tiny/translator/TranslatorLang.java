package my.tiny.translator;

public class TranslatorLang {
    public String code;
    public String name;

    public TranslatorLang(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}