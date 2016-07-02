package my.tiny.translator;

import my.tiny.translator.core.DataProvider;

public class DictionaryModel extends TranslatorModel {
    private static final int MAX_TOKENS = 3;
    private static final String NEW_LINE = System.getProperty("line.separator");

    public DictionaryModel(DataProvider dataProvider) {
        super(dataProvider);
    }

    @Override
    public boolean isValid() {
        String text = getProperty("text");
        String sourceLang = getProperty("sourceLang");
        String targetLang = getProperty("targetLang");
        return !text.isEmpty() && text.length() < dataProvider.getTextLimit() &&
               !text.contains(NEW_LINE) && text.split("\\s+").length <= MAX_TOKENS &&
               !sourceLang.isEmpty() && !targetLang.isEmpty() &&
               dataProvider.isLanguageSupported(sourceLang + "-" + targetLang);
    }
}