package my.tiny.translator;

import java.util.Locale;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.media.AudioManager;
import android.widget.Toast;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.speech.RecognizerIntent;
import android.content.Intent;
import android.content.Context;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.content.BroadcastReceiver;

import my.tiny.translator.core.Event;
import my.tiny.translator.core.Model;
import my.tiny.translator.core.Debouncer;
import my.tiny.translator.core.EventListener;

public class MainActivity extends Activity {
    private static final int PROGRESS_DELAY = 250;
    private static final int SPEECH_RECOGNIZER_CODE = 1;
    private static final int PROGRESS_FADE_DURATION = 200;
    private static final String SETTING_SOURCE_LANG = "sourceLang";
    private static final String SETTING_TARGET_LANG = "targetLang";
    private static final String MIMETYPE_TEXT_PLAIN = "text/plain";
    private static final String DICTIONARY_APIKEY = ""; // https://tech.yandex.ru/keys/get/?service=dict
    private static final String DICTIONARY_APIURL = "https://dictionary.yandex.net/api/v1/dicservice.json/lookup";
    private static final String TRANSLATOR_APIKEY = ""; // https://tech.yandex.ru/keys/get/?service=trnsl
    private static final String TRANSLATOR_APIURL = "https://translate.yandex.net/api/v1.5/tr.json/translate";
    private static final String TRANSLATOR_DEFAULT_SOURCELANG = "en";
    private static final String TRANSLATOR_DEFAULT_TARGETLANG = "ru";
    private Model mainModel = new MainModel();
    private ArrayList<String> recognizerLangs = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initCopy();
        initSwap();
        initClear();
        initPaste();
        initRetry();
        initShare();
        initProgress();
        initSpeakers();
        initSpinners();
        initTextarea();
        initTranslator();
        initDictionary();
        requestRecognizerLangs();

        onNewIntent(getIntent());

        if (isEmptyText()) {
            mainModel.dispatchEvent(new Event(MainModel.EVENT_FOCUS));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mainModel.dispatchEvent(new Event(MainModel.EVENT_PAUSE));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainModel.dispatchEvent(new Event(MainModel.EVENT_DESTROY));
    }

    @Override
    public void onBackPressed() {
        if (isEmptyText()) {
            super.onBackPressed();
        } else {
            clearText();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String intentType = intent.getType();
        String intentAction = intent.getAction();

        if (Intent.ACTION_SEND.equals(intentAction) && MIMETYPE_TEXT_PLAIN.equals(intentType)) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                mainModel.setProperty(MainModel.PROP_TEXT, sharedText);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_RECOGNIZER_CODE && resultCode == Activity.RESULT_OK) {
            ArrayList<String> list = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String text = list.get(0);
            text = Utils.capitalizeText(text, mainModel.getProperty(MainModel.PROP_SOURCE_LANG));
            mainModel.setProperty(MainModel.PROP_TEXT, text);
        }
    }

    public void putTextToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("", text));
    }

    public void shareText(String text) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.setType(MIMETYPE_TEXT_PLAIN);
        startActivity(intent);
    }

    public boolean isSoundMuted() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        return audioManager != null &&
               audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0;
    }

    public String getTextFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            return item.coerceToText(getApplicationContext()).toString();
        }
        return "";
    }

    public boolean isEmptyText() {
        return mainModel.getProperty(MainModel.PROP_TEXT).isEmpty();
    }

    public void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void clearText() {
        mainModel.setProperty(MainModel.PROP_TEXT, "");
        mainModel.dispatchEvent(new Event(MainModel.EVENT_FOCUS));
    }

    public void setSetting(String name, String value) {
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name, value);
        editor.apply();
    }

    public String getSetting(String name, String defaultValue) {
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        return settings.getString(name, defaultValue);
    }

    public void toggleKeyboard(EditText editText, boolean shown) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (shown) {
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        } else {
            imm.hideSoftInputFromWindow(
                editText.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS
            );
        }
    }

    public void recognizeSpeech(String lang) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        startActivityForResult(intent, SPEECH_RECOGNIZER_CODE);
    }

    public void requestRecognizerLangs() {
        Intent intent = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
        BroadcastReceiver resultReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle results = getResultExtras(true);
                if (results.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
                    ArrayList<String> langs = results.getStringArrayList(
                        RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES
                    );
                    for (String lang : langs) {
                        lang = lang.split("-")[0];
                        if (!recognizerLangs.contains(lang)) {
                            recognizerLangs.add(lang);
                        }
                    }
                    initRecognizer();
                }
            }
        };
        sendOrderedBroadcast(intent, null, resultReceiver, null, Activity.RESULT_OK, null, null);
    }

    public void initCopy() {
        final Button copyButton = (Button) findViewById(R.id.copyButton);
        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (MainModel.EVENT_CHANGE.equals(event.type)) {
                    switch (event.getDataValue("name")) {
                        case MainModel.PROP_TRANSLATION:
                            copyButton.setVisibility(event.getDataValue("value").isEmpty() ? View.GONE : View.VISIBLE);
                            break;
                    }
                }
            }
        });
        copyButton.setVisibility(mainModel.getProperty(MainModel.PROP_TRANSLATION).isEmpty() ? View.GONE : View.VISIBLE);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                putTextToClipboard(mainModel.getProperty(MainModel.PROP_TRANSLATION));
                showToast(getString(R.string.messageCopy));
            }
        });
    }

    public void initSwap() {
        final Button swapButton = (Button) findViewById(R.id.swapButton);
        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sourceLang = mainModel.getProperty(MainModel.PROP_SOURCE_LANG);
                String targetLang = mainModel.getProperty(MainModel.PROP_TARGET_LANG);
                String translation = mainModel.getProperty(MainModel.PROP_TRANSLATION);
                if (!translation.isEmpty()) {
                    mainModel.setProperty(MainModel.PROP_TEXT, translation);
                }
                mainModel.setProperty(MainModel.PROP_SOURCE_LANG, targetLang);
                mainModel.setProperty(MainModel.PROP_TARGET_LANG, sourceLang);
            }
        });
    }

    public void initClear() {
        final Button clearButton = (Button) findViewById(R.id.clearButton);
        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (MainModel.EVENT_CHANGE.equals(event.type)) {
                    switch (event.getDataValue("name")) {
                        case MainModel.PROP_TEXT:
                            clearButton.setVisibility(isEmptyText() ? View.GONE : View.VISIBLE);
                            break;
                    }
                }
            }
        });
        clearButton.setVisibility(isEmptyText() ? View.GONE : View.VISIBLE);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearText();
            }
        });
    }

    public void initRetry() {
        final Button retryButton = (Button) findViewById(R.id.retryButton);
        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                switch (event.type) {
                    case MainModel.EVENT_ERROR:
                        retryButton.setVisibility(View.VISIBLE);
                        break;

                    case MainModel.EVENT_RESULT:
                        retryButton.setVisibility(View.GONE);
                        break;
                }
            }
        });
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainModel.dispatchEvent(new Event(MainModel.EVENT_RETRY));
            }
        });
    }

    public void initShare() {
        final Button shareButton = (Button) findViewById(R.id.shareButton);
        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (MainModel.EVENT_CHANGE.equals(event.type)) {
                    switch (event.getDataValue("name")) {
                        case MainModel.PROP_TRANSLATION:
                            shareButton.setVisibility(event.getDataValue("value").isEmpty() ? View.GONE : View.VISIBLE);
                            break;
                    }
                }
            }
        });
        shareButton.setVisibility(mainModel.getProperty(MainModel.PROP_TRANSLATION).isEmpty() ? View.GONE : View.VISIBLE);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareText(mainModel.getProperty(MainModel.PROP_TRANSLATION));
            }
        });
    }

    public void initPaste() {
        final Button pasteButton = (Button) findViewById(R.id.pasteButton);
        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (MainModel.EVENT_CHANGE.equals(event.type)) {
                    switch (event.getDataValue("name")) {
                        case MainModel.PROP_TEXT:
                            pasteButton.setVisibility(
                                isEmptyText() ? View.VISIBLE : View.GONE
                            );
                            break;
                    }
                }
            }
        });
        pasteButton.setVisibility(
            isEmptyText() ? View.VISIBLE : View.GONE
        );
        pasteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String clipboardText = getTextFromClipboard();
                if (clipboardText.isEmpty()) {
                    showToast(getString(R.string.messagePasteEmpty));
                } else {
                    mainModel.setProperty(MainModel.PROP_TEXT, clipboardText);
                }
            }
        });
    }

    public void initProgress() {
        final View progressLayout = (View) findViewById(R.id.progressLayout);
        final Debouncer progressDebouncer = new Debouncer(PROGRESS_DELAY, new Runnable() {
            @Override
            public void run() {
                Utils.fadeInView(progressLayout, PROGRESS_FADE_DURATION);
            }
        });
        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                switch (event.type) {
                    case MainModel.EVENT_QUERY:
                        progressDebouncer.start();
                        break;

                    case MainModel.EVENT_ERROR:
                    case MainModel.EVENT_RESULT:
                        progressDebouncer.cancel();
                        Utils.fadeOutView(progressLayout, PROGRESS_FADE_DURATION);
                        break;

                    case MainModel.EVENT_DESTROY:
                        progressDebouncer.cancel();
                        break;
                }
            }
        });
    }

    public void initSpeakers() {
        SpeakerModel.init(getApplicationContext());
        final Button sourceSpeakButton = (Button) findViewById(R.id.sourceSpeakButton);
        final Button targetSpeakButton = (Button) findViewById(R.id.targetSpeakButton);
        final SpeakerModel sourceSpeakerModel = new SpeakerModel();
        final SpeakerModel targetSpeakerModel = new SpeakerModel();
        final SpeakerPresenter sourceSpeakerPresenter = new SpeakerPresenter(
            sourceSpeakButton, sourceSpeakerModel
        );
        final SpeakerPresenter targetSpeakerPresenter = new SpeakerPresenter(
            targetSpeakButton, targetSpeakerModel
        );

        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (event.type.equals(MainModel.EVENT_PAUSE)) {
                    SpeakerModel.stop();
                    return;
                }
                if (event.type.equals(MainModel.EVENT_DESTROY)) {
                    SpeakerModel.destroy();
                    return;
                }
                if (MainModel.EVENT_CHANGE.equals(event.type)) {
                    String value = event.getDataValue("value");
                    switch (event.getDataValue("name")) {
                        case MainModel.PROP_TEXT:
                            sourceSpeakerPresenter.setText(value);
                            break;

                        case MainModel.PROP_SOURCE_LANG:
                            sourceSpeakerPresenter.setLang(value);
                            break;

                        case MainModel.PROP_TARGET_LANG:
                            targetSpeakerPresenter.setLang(value);
                            break;

                        case MainModel.PROP_TRANSLATION:
                            targetSpeakerPresenter.setText(value);
                            break;
                    }
                    return;
                }
            }
        });

        EventListener speakerEventListener = new EventListener() {
            @Override
            public void handleEvent(Event event) {
                switch (event.type) {
                    case SpeakerModel.EVENT_STOP:
                    case SpeakerModel.EVENT_ERROR:
                        final boolean error = SpeakerModel.EVENT_ERROR.equals(event.type);
                        if (error) {
                            showToast(getString(R.string.messageSpeakerError));
                        }
                        String buttonSpeakText = getString(R.string.iconSpeak);
                        sourceSpeakButton.setText(buttonSpeakText);
                        targetSpeakButton.setText(buttonSpeakText);
                        break;

                    case SpeakerModel.EVENT_START:
                        if (isSoundMuted()) {
                            showToast(getString(R.string.messageMuted));
                        }
                        String buttonPauseText = getString(R.string.iconPause);
                        if (sourceSpeakerModel.isSpeaking()) {
                            sourceSpeakButton.setText(buttonPauseText);
                        } else {
                            targetSpeakButton.setText(buttonPauseText);
                        }
                        break;
                }
            }
        };

        sourceSpeakerPresenter.addListener(speakerEventListener);
        targetSpeakerPresenter.addListener(speakerEventListener);

        sourceSpeakerPresenter.setText(mainModel.getProperty(MainModel.PROP_TEXT));
        sourceSpeakerPresenter.setLang(mainModel.getProperty(MainModel.PROP_SOURCE_LANG));
        targetSpeakerPresenter.setLang(mainModel.getProperty(MainModel.PROP_TARGET_LANG));
        targetSpeakerPresenter.setText(mainModel.getProperty(MainModel.PROP_TRANSLATION));
    }

    public void initSpinners() {
        final Spinner sourceSpinner = (Spinner) findViewById(R.id.sourceLang);
        final Spinner targetSpinner = (Spinner) findViewById(R.id.targetLang);

        final HashMap<String, TranslatorLang> langMap = new HashMap<>();

        for (String lang : YandexDataProvider.LANGS) {
            langMap.put(lang, new TranslatorLang(lang, Utils.getLanguageName(lang)));
        }

        ArrayList<TranslatorLang> langList = new ArrayList<>(langMap.values());

        Collections.sort(langList, new Comparator<TranslatorLang>() {
            @Override
            public int compare(TranslatorLang lang1, TranslatorLang lang2) {
                return lang1.name.compareTo(lang2.name);
            }
        });

        final ArrayAdapter<TranslatorLang> sourceAdapter = new ArrayAdapter<>(
            this, R.layout.spinner, langList
        );
        sourceAdapter.setDropDownViewResource(R.layout.option);
        sourceSpinner.setAdapter(sourceAdapter);

        final ArrayAdapter<TranslatorLang> targetAdapter = new ArrayAdapter<>(
            this, R.layout.spinner, langList
        );
        targetAdapter.setDropDownViewResource(R.layout.option);
        targetSpinner.setAdapter(targetAdapter);

        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (MainModel.EVENT_CHANGE.equals(event.type)) {
                    String value = event.getDataValue("value");
                    switch (event.getDataValue("name")) {
                        case MainModel.PROP_SOURCE_LANG:
                            setSetting(SETTING_SOURCE_LANG, value);
                            sourceSpinner.setSelection(
                                sourceAdapter.getPosition(langMap.get(value))
                            );
                            TranslatorLang targetSelectedItem = (TranslatorLang) targetSpinner.getSelectedItem();
                            if (targetSelectedItem != null && value.equals(targetSelectedItem.code)) {
                                targetSpinner.setSelection(
                                    targetAdapter.getPosition(
                                        langMap.get(event.getDataValue("oldValue"))
                                    )
                                );
                            }
                            break;

                        case MainModel.PROP_TARGET_LANG:
                            setSetting(SETTING_TARGET_LANG, value);
                            targetSpinner.setSelection(
                                targetAdapter.getPosition(langMap.get(value))
                            );
                            TranslatorLang sourceSelectedItem = (TranslatorLang) sourceSpinner.getSelectedItem();
                            if (sourceSelectedItem != null && value.equals(sourceSelectedItem.code)) {
                                sourceSpinner.setSelection(
                                    sourceAdapter.getPosition(
                                        langMap.get(event.getDataValue("oldValue"))
                                    )
                                );
                            }
                            break;
                    }
                }
            }
        });

        TranslatorLang sourceStartItem = langMap.get(
            getSetting(SETTING_SOURCE_LANG, TRANSLATOR_DEFAULT_SOURCELANG)
        );
        TranslatorLang targetStartItem = langMap.get(
            getSetting(SETTING_TARGET_LANG, TRANSLATOR_DEFAULT_TARGETLANG)
        );
        if (sourceStartItem != null) {
            sourceSpinner.setSelection(
                sourceAdapter.getPosition(sourceStartItem)
            );
        }
        if (targetStartItem != null) {
            targetSpinner.setSelection(
                targetAdapter.getPosition(targetStartItem)
            );
        }

        sourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TranslatorLang sourceSelectedItem = (TranslatorLang) parent.getItemAtPosition(position);
                mainModel.setProperty(MainModel.PROP_SOURCE_LANG, sourceSelectedItem.code);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        targetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TranslatorLang targetSelectedItem = (TranslatorLang) parent.getItemAtPosition(position);
                mainModel.setProperty(MainModel.PROP_TARGET_LANG, targetSelectedItem.code);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void initTextarea() {
        final TextArea textarea = (TextArea) findViewById(R.id.textarea);
        textarea.setValueChangeListener(new TextArea.ValueChangeListener() {
            @Override
            public void onActionCut() {}

            @Override
            public void onActionCopy() {}

            @Override
            public void onActionPaste() {
                // TODO
            }

            @Override
            public void onValueChange(String value) {
                mainModel.setProperty(MainModel.PROP_TEXT, value);
            }

            @Override
            public void onFocusChange(boolean hasFocus) {
                toggleKeyboard(textarea, hasFocus);
            }
        });
        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                switch (event.type) {
                    case MainModel.EVENT_FOCUS:
                        textarea.requestFocus();
                        toggleKeyboard(textarea, true);
                        break;

                    case MainModel.EVENT_CHANGE:
                        if (MainModel.PROP_TEXT.equals(event.getDataValue("name"))) {
                            textarea.setValue(event.getDataValue("value"));
                        }
                        break;
                }
            }
        });
    }

    public void initRecognizer() {
        final Button recognizeButton = (Button) findViewById(R.id.recognizeButton);
        final Runnable recognizerRoutine = new Runnable() {
            @Override
            public void run() {
                boolean visible =
                    isEmptyText() &&
                    recognizerLangs.contains(mainModel.getProperty(MainModel.PROP_SOURCE_LANG));
                recognizeButton.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        };
        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (MainModel.EVENT_CHANGE.equals(event.type)) {
                    switch (event.getDataValue("name")) {
                        case MainModel.PROP_TEXT:
                        case MainModel.PROP_SOURCE_LANG:
                            recognizerRoutine.run();
                            break;
                    }
                }
            }
        });
        recognizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recognizeSpeech(mainModel.getProperty(MainModel.PROP_SOURCE_LANG));
            }
        });
        recognizerRoutine.run();
    }

    public void initTranslator() {
        TextView translatorView = (TextView) findViewById(R.id.translation);
        TranslatorModel translatorModel = new TranslatorModel(new YandexDataProvider(
            TRANSLATOR_APIURL,
            TRANSLATOR_APIKEY
        ));
        final TranslatorPresenter translatorPresenter = new TranslatorPresenter(
            translatorView,
            translatorModel
        );
        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (event.type.equals(MainModel.EVENT_RETRY)) {
                    translatorPresenter.requestTranslation();
                    return;
                }

                if (event.type.equals(MainModel.EVENT_DESTROY)) {
                    translatorPresenter.abortRequest();
                    return;
                }

                if (MainModel.EVENT_CHANGE.equals(event.type)) {
                    String value = event.getDataValue("value");
                    switch (event.getDataValue("name")) {
                        case MainModel.PROP_TEXT:
                            translatorPresenter.setText(value);
                            break;

                        case MainModel.PROP_SOURCE_LANG:
                            translatorPresenter.setSourceLang(value);
                            break;

                        case MainModel.PROP_TARGET_LANG:
                            translatorPresenter.setTargetLang(value);
                            break;
                    }
                }
            }
        });
        translatorPresenter.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                switch (event.type) {
                    case TranslatorModel.EVENT_QUERY:
                        mainModel.dispatchEvent(new Event(MainModel.EVENT_QUERY));
                        break;

                    case TranslatorModel.EVENT_ERROR:
                        mainModel.dispatchEvent(new Event(MainModel.EVENT_ERROR));
                        showToast(getString(R.string.messageError));
                        break;

                    case TranslatorModel.EVENT_CHANGE:
                        mainModel.setProperty(MainModel.PROP_TRANSLATION, event.getDataValue("translation"));
                        break;

                    case TranslatorModel.EVENT_UPDATE:
                    case "invalid":
                        mainModel.dispatchEvent(new Event(MainModel.EVENT_RESULT));
                        break;
                }
            }
        });
        translatorPresenter.setText(mainModel.getProperty(MainModel.PROP_TEXT));
        translatorPresenter.setSourceLang(mainModel.getProperty(MainModel.PROP_SOURCE_LANG));
        translatorPresenter.setTargetLang(mainModel.getProperty(MainModel.PROP_TARGET_LANG));
    }

    public void initDictionary() {
        TextView dictionaryView = (TextView) findViewById(R.id.dictionary);
        DictionaryModel dictionaryModel = new DictionaryModel(new YandexDictionaryProvider(
            DICTIONARY_APIURL,
            DICTIONARY_APIKEY
        ));
        final TranslatorPresenter dictionaryPresenter = new TranslatorPresenter(
            dictionaryView,
            dictionaryModel
        );
        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (event.type.equals(MainModel.EVENT_RETRY)) {
                    dictionaryPresenter.requestTranslation();
                    return;
                }

                if (event.type.equals(MainModel.EVENT_DESTROY)) {
                    dictionaryPresenter.abortRequest();
                    return;
                }

                if (MainModel.EVENT_CHANGE.equals(event.type)) {
                    String value = event.getDataValue("value");
                    switch (event.getDataValue("name")) {
                        case MainModel.PROP_TEXT:
                            dictionaryPresenter.setText(value);
                            break;

                        case MainModel.PROP_SOURCE_LANG:
                            dictionaryPresenter.setSourceLang(value);
                            break;

                        case MainModel.PROP_TARGET_LANG:
                            dictionaryPresenter.setTargetLang(value);
                            break;
                    }
                }
            }
        });
        dictionaryPresenter.setText(mainModel.getProperty(MainModel.PROP_TEXT));
        dictionaryPresenter.setSourceLang(mainModel.getProperty(MainModel.PROP_SOURCE_LANG));
        dictionaryPresenter.setTargetLang(mainModel.getProperty(MainModel.PROP_TARGET_LANG));
    }
}
