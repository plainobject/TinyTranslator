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
        String text = getProperty(TranslatorModel.PROP_TEXT);
        String sourceLang = getProperty(TranslatorModel.PROP_SOURCE_LANG);
        String targetLang = getProperty(TranslatorModel.PROP_TARGET_LANG);
        return !text.isEmpty() && text.length() < dataProvider.getTextLimit() &&
               !text.contains(NEW_LINE) && Utils.getTextTokens(text).length <= MAX_TOKENS &&
               !sourceLang.isEmpty() && !targetLang.isEmpty() &&
               dataProvider.isLanguageSupported(sourceLang + "-" + targetLang);
    }
}
