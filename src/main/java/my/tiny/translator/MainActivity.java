package my.tiny.translator;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.MetricAffectingSpan;
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
import android.graphics.Typeface;

import my.tiny.translator.core.Event;
import my.tiny.translator.core.Model;
import my.tiny.translator.core.Debouncer;
import my.tiny.translator.core.EventListener;

public class MainActivity extends Activity {
    private static final int SPEECH_RECOGNITION_CODE = 1;
    private static final String MIMETYPE_TEXT_PLAIN = "text/plain";
    private Model mainModel = new Model();
    private ArrayList<String> recognizerLangs = new ArrayList<String>();
    private Typeface iconFont;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        iconFont = Typeface.createFromAsset(getAssets(), Config.ICONFONT_FILENAME);

        initCopy();
        initSwap();
        initClear();
        initPaste();
        initShare();
        initSpeakers();
        initSpinners();
        initTextarea();
        initTranslator();
        initDictionary();
        requestRecognizerLangs();

        onNewIntent(getIntent());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainModel.dispatchEvent(new Event("destroy"));
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String intentType = intent.getType();
        String intentAction = intent.getAction();

        if (Intent.ACTION_SEND.equals(intentAction) && MIMETYPE_TEXT_PLAIN.equals(intentType)) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                mainModel.setProperty("text", sharedText);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_RECOGNITION_CODE && resultCode == Activity.RESULT_OK) {
            ArrayList<String> list = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String text = list.get(0);
            text = Utils.capitalizeText(text, mainModel.getProperty("sourceLang"));
            mainModel.setProperty("text", text);
        }
    }

    public void copyText(String text) {
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

    public boolean pasteText() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            String text = item.coerceToText(getApplicationContext()).toString();
            if (!text.isEmpty()) {
                mainModel.setProperty("text", text);
                return true;
            }
        }
        return false;
    }

    public void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void setSetting(String name, String value) {
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name, value);
        editor.apply();
    }

    public void toggleKeyboard(EditText editText, boolean shown) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (shown) {
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        } else {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void recognizeSpeech(String lang) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, SPEECH_RECOGNITION_CODE);
    }

    public void requestRecognizerLangs() {
        Intent intent = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
        BroadcastReceiver resultReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle results = getResultExtras(true);
                if (results.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
                    ArrayList<String> langs = results.getStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES);
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
                if (event.type.equals("change")) {
                    switch (event.data.get("name")) {
                        case "translation":
                            copyButton.setVisibility(event.data.get("value").isEmpty() ? View.GONE : View.VISIBLE);
                            break;
                    }
                }
            }
        });
        copyButton.setTypeface(iconFont);
        copyButton.setVisibility(mainModel.getProperty("translation").isEmpty() ? View.GONE : View.VISIBLE);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyText(mainModel.getProperty("translation"));
                showToast(getString(R.string.messageCopy));
            }
        });
    }

    public void initSwap() {
        final Button swapButton = (Button) findViewById(R.id.swapButton);
        swapButton.setTypeface(iconFont);
        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sourceLang = mainModel.getProperty("sourceLang");
                String targetLang = mainModel.getProperty("targetLang");
                String translation = mainModel.getProperty("translation");
                if (!translation.isEmpty()) {
                    mainModel.setProperty("text", translation);
                }
                mainModel.setProperty("sourceLang", targetLang);
                mainModel.setProperty("targetLang", sourceLang);
            }
        });
    }

    public void initClear() {
        final Button clearButton = (Button) findViewById(R.id.clearButton);
        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (event.type.equals("change")) {
                    switch (event.data.get("name")) {
                        case "text":
                            clearButton.setVisibility(event.data.get("value").isEmpty() ? View.GONE : View.VISIBLE);
                            break;
                    }
                }
            }
        });
        clearButton.setTypeface(iconFont);
        clearButton.setVisibility(mainModel.getProperty("text").isEmpty() ? View.GONE : View.VISIBLE);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainModel.setProperty("text", "");
            }
        });
    }

    public void initShare() {
        final Button shareButton = (Button) findViewById(R.id.shareButton);
        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (event.type.equals("change")) {
                    switch (event.data.get("name")) {
                        case "translation":
                            shareButton.setVisibility(event.data.get("value").isEmpty() ? View.GONE : View.VISIBLE);
                            break;
                    }
                }
            }
        });
        shareButton.setTypeface(iconFont);
        shareButton.setVisibility(mainModel.getProperty("translation").isEmpty() ? View.GONE : View.VISIBLE);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareText(mainModel.getProperty("translation"));
            }
        });
    }

    public void initPaste() {
        final Button pasteButton = (Button) findViewById(R.id.pasteButton);
        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (event.type.equals("change")) {
                    switch (event.data.get("name")) {
                        case "text":
                            pasteButton.setVisibility(
                                event.data.get("value").isEmpty() ? View.VISIBLE : View.GONE
                            );
                            break;
                    }
                }
            }
        });
        pasteButton.setTypeface(iconFont);
        pasteButton.setVisibility(
            mainModel.getProperty("text").isEmpty() ? View.VISIBLE : View.GONE
        );
        pasteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(getString(pasteText() ?
                    R.string.messagePaste :
                    R.string.messagePasteEmpty
                ));
            }
        });
    }

    public void initSpeakers() {
        final Button sourceSpeakButton = (Button) findViewById(R.id.sourceSpeakButton);
        final Button targetSpeakButton = (Button) findViewById(R.id.targetSpeakButton);
        SpeakerModel sourceSpeakerModel = new SpeakerModel(getApplicationContext());
        SpeakerModel targetSpeakerModel = new SpeakerModel(getApplicationContext());
        final SpeakerPresenter sourceSpeakerPresenter = new SpeakerPresenter(sourceSpeakButton, sourceSpeakerModel);
        final SpeakerPresenter targetSpeakerPresenter = new SpeakerPresenter(targetSpeakButton, targetSpeakerModel);

        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (event.type.equals("change")) {
                    String value = event.data.get("value");
                    switch (event.data.get("name")) {
                        case "text":
                            sourceSpeakerPresenter.setText(value);
                            break;

                        case "sourceLang":
                            sourceSpeakerPresenter.setLang(value);
                            break;

                        case "targetLang":
                            targetSpeakerPresenter.setLang(value);
                            break;

                        case "translation":
                            targetSpeakerPresenter.setText(value);
                            break;
                    }
                    return;
                }
                if (event.type.equals("destroy")) {
                    sourceSpeakerPresenter.destroy();
                    targetSpeakerPresenter.destroy();
                }
            }
        });

        sourceSpeakButton.setTypeface(iconFont);
        targetSpeakButton.setTypeface(iconFont);

        sourceSpeakerPresenter.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                switch (event.type) {
                    case "stop":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sourceSpeakButton.setText(
                                    getString(R.string.iconSpeak)
                                );
                            }
                        });
                        break;

                    case "start":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sourceSpeakButton.setText(
                                    getString(R.string.iconPause)
                                );
                            }
                        });
                        break;
                }
            }
        });

        targetSpeakerPresenter.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                switch (event.type) {
                    case "stop":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                targetSpeakButton.setText(
                                    getString(R.string.iconSpeak)
                                );
                            }
                        });
                        break;

                    case "start":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                targetSpeakButton.setText(
                                    getString(R.string.iconPause)
                                );
                            }
                        });
                        break;
                }
            }
        });

        sourceSpeakerPresenter.setText(mainModel.getProperty("text"));
        sourceSpeakerPresenter.setLang(mainModel.getProperty("sourceLang"));
        targetSpeakerPresenter.setLang(mainModel.getProperty("targetLang"));
        targetSpeakerPresenter.setText(mainModel.getProperty("translation"));
    }

    public void initSpinners() {
        final Spinner sourceSpinner = (Spinner) findViewById(R.id.sourceLang);
        final Spinner targetSpinner = (Spinner) findViewById(R.id.targetLang);

        final HashMap<String, TranslatorLang> langMap = new HashMap<String, TranslatorLang>();

        for (Map.Entry<String, String> entry : YandexDataProvider.langs.entrySet()) {
            langMap.put(entry.getKey(), new TranslatorLang(entry.getKey(), entry.getValue()));
        }

        ArrayList<TranslatorLang> langList = new ArrayList<TranslatorLang>(langMap.values());

        Collections.sort(langList, new Comparator<TranslatorLang>() {
            @Override
            public int compare(TranslatorLang lang1, TranslatorLang lang2) {
                return lang1.name.compareTo(lang2.name);
            }
        });

        final ArrayAdapter<TranslatorLang> sourceAdapter = new ArrayAdapter<TranslatorLang>(
            this, R.layout.spinner, langList
        );
        sourceAdapter.setDropDownViewResource(R.layout.option);
        sourceSpinner.setAdapter(sourceAdapter);

        final ArrayAdapter<TranslatorLang> targetAdapter = new ArrayAdapter<TranslatorLang>(
            this, R.layout.spinner, langList
        );
        targetAdapter.setDropDownViewResource(R.layout.option);
        targetSpinner.setAdapter(targetAdapter);

        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (event.type.equals("change")) {
                    String value = event.data.get("value");
                    switch (event.data.get("name")) {
                        case "sourceLang":
                            setSetting("sourceLang", value);
                            sourceSpinner.setSelection(
                                sourceAdapter.getPosition(langMap.get(value))
                            );
                            TranslatorLang targetSelectedItem = (TranslatorLang) targetSpinner.getSelectedItem();
                            if (value.equals(targetSelectedItem.code)) {
                                targetSpinner.setSelection(
                                    targetAdapter.getPosition(langMap.get(event.data.get("oldValue")))
                                );
                            }
                            break;

                        case "targetLang":
                            setSetting("targetLang", value);
                            targetSpinner.setSelection(
                                targetAdapter.getPosition(langMap.get(value))
                            );
                            TranslatorLang sourceSelectedItem = (TranslatorLang) sourceSpinner.getSelectedItem();
                            if (value.equals(sourceSelectedItem.code)) {
                                sourceSpinner.setSelection(
                                    sourceAdapter.getPosition(langMap.get(event.data.get("oldValue")))
                                );
                            }
                            break;
                    }
                }
            }
        });

        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        String startSourceLang = settings.getString("sourceLang", Config.TRANSLATOR_DEFAULT_SOURCELANG);
        String startTargetLang = settings.getString("targetLang", Config.TRANSLATOR_DEFAULT_TARGETLANG);

        sourceSpinner.setSelection(
            sourceAdapter.getPosition(langMap.get(startSourceLang))
        );
        targetSpinner.setSelection(
            targetAdapter.getPosition(langMap.get(startTargetLang))
        );

        sourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TranslatorLang sourceSelectedItem = (TranslatorLang) parent.getItemAtPosition(position);
                mainModel.setProperty("sourceLang", sourceSelectedItem.code);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        targetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TranslatorLang targetSelectedItem = (TranslatorLang) parent.getItemAtPosition(position);
                mainModel.setProperty("targetLang", targetSelectedItem.code);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void initTextarea() {
        final EditText textarea = (EditText) findViewById(R.id.textarea);
        textarea.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mainModel.setProperty("text", s.toString());
                if (count > 1) {
                    Editable text = textarea.getText();
                    MetricAffectingSpan[] spans = text.getSpans(
                        0, text.length(),
                        MetricAffectingSpan.class
                    );
                    for (MetricAffectingSpan span : spans) {
                        text.removeSpan(span);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        });
        textarea.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                toggleKeyboard(textarea, hasFocus);
            }
        });
        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (event.type.equals("change")) {
                    switch (event.data.get("name")) {
                        case "text":
                            String value = event.data.get("value");
                            if (textarea.getText().toString().equals(value)) {
                                break;
                            }
                            textarea.setText(value);
                            if (value.isEmpty()) {
                                textarea.requestFocus();
                                toggleKeyboard(textarea, true);
                            }
                            textarea.setSelection(value.length());
                            break;
                    }
                }
            }
        });
    }

    public void initRecognizer() {
        final Button recognizeButton = (Button) findViewById(R.id.recognizeButton);
        boolean visible = mainModel.getProperty("text").isEmpty() &&
                          recognizerLangs.contains(mainModel.getProperty("sourceLang"));
        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (event.type.equals("change")) {
                    switch (event.data.get("name")) {
                        case "text":
                        case "sourceLang":
                            boolean visible = mainModel.getProperty("text").isEmpty() &&
                                              recognizerLangs.contains(mainModel.getProperty("sourceLang"));
                            recognizeButton.setVisibility(visible ? View.VISIBLE : View.GONE);
                            break;
                    }
                }
            }
        });
        recognizeButton.setTypeface(iconFont);
        recognizeButton.setVisibility(visible ? View.VISIBLE : View.GONE);
        recognizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recognizeSpeech(mainModel.getProperty("sourceLang"));
            }
        });
    }

    public void initTranslator() {
        final Button retryButton = (Button) findViewById(R.id.retryButton);
        final View progressLayout = (View) findViewById(R.id.progressLayout);
        final Debouncer progressDebouncer = new Debouncer(Config.PROGRESS_DELAY, new Runnable() {
            public void run() {
                Utils.fadeInView(progressLayout, Config.PROGRESS_FADE_DURATION);
            }
        });
        TextView translatorView = (TextView) findViewById(R.id.translation);
        TranslatorModel translatorModel = new TranslatorModel(new YandexDataProvider(
            Config.TRANSLATOR_APIURL,
            Config.TRANSLATOR_APIKEY
        ));
        final TranslatorPresenter translatorPresenter = new TranslatorPresenter(
            translatorView,
            translatorModel
        );
        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (event.type.equals("retry")) {
                    translatorPresenter.requestTranslation();
                    return;
                }

                if (event.type.equals("change")) {
                    String value = event.data.get("value");
                    switch (event.data.get("name")) {
                        case "text":
                            translatorPresenter.setText(value);
                            break;

                        case "sourceLang":
                            translatorPresenter.setSourceLang(value);
                            break;

                        case "targetLang":
                            translatorPresenter.setTargetLang(value);
                            break;
                    }
                }
            }
        });
        retryButton.setTypeface(iconFont);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainModel.dispatchEvent(new Event("retry"));
            }
        });
        translatorPresenter.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                switch (event.type) {
                    case "query":
                        progressDebouncer.start();
                        break;

                    case "error":
                        retryButton.setVisibility(View.VISIBLE);
                        progressDebouncer.cancel();
                        Utils.fadeOutView(progressLayout, Config.PROGRESS_FADE_DURATION);
                        showToast(getString(R.string.messageError));
                        break;

                    case "change":
                        mainModel.setProperty("translation", event.data.get("translation"));
                        break;

                    case "update":
                    case "invalid":
                        retryButton.setVisibility(View.GONE);
                        progressDebouncer.cancel();
                        Utils.fadeOutView(progressLayout, Config.PROGRESS_FADE_DURATION);
                        break;
                }
            }
        });
        translatorPresenter.setText(mainModel.getProperty("text"));
        translatorPresenter.setSourceLang(mainModel.getProperty("sourceLang"));
        translatorPresenter.setTargetLang(mainModel.getProperty("targetLang"));
    }

    public void initDictionary() {
        TextView dictionaryView = (TextView) findViewById(R.id.dictionary);
        DictionaryModel dictionaryModel = new DictionaryModel(new YandexDictionaryProvider(
            Config.DICTIONARY_APIURL,
            Config.DICTIONARY_APIKEY
        ));
        final TranslatorPresenter dictionaryPresenter = new TranslatorPresenter(
            dictionaryView,
            dictionaryModel
        );
        mainModel.addListener(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (event.type.equals("retry")) {
                    dictionaryPresenter.requestTranslation();
                    return;
                }

                if (event.type.equals("change")) {
                    String value = event.data.get("value");
                    switch (event.data.get("name")) {
                        case "text":
                            dictionaryPresenter.setText(value);
                            break;

                        case "sourceLang":
                            dictionaryPresenter.setSourceLang(value);
                            break;

                        case "targetLang":
                            dictionaryPresenter.setTargetLang(value);
                            break;
                    }
                }
            }
        });
        dictionaryPresenter.setText(mainModel.getProperty("text"));
        dictionaryPresenter.setSourceLang(mainModel.getProperty("sourceLang"));
        dictionaryPresenter.setTargetLang(mainModel.getProperty("targetLang"));
    }
}