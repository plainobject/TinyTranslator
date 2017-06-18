package my.tiny.translator;

import android.view.View;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.content.Context;

public class TextArea extends EditText {
    private ValueChangeListener listener;

    public interface ValueChangeListener {
        public void onActionCut();
        public void onActionCopy();
        public void onActionPaste();
        public void onValueChange(String value);
        public void onFocusChange(boolean hasFocus);
    }

    public TextArea(Context context) {
        super(context);
        setup();
    }

    public TextArea(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public TextArea(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    public void setValue(String value) {
        if (TextUtils.equals(value, getValue())) {
            return;
        }
        setText(value);
        setSelection(length());
    }

    public void setValueChangeListener(ValueChangeListener listener) {
        this.listener = listener;
    }

    public String getValue() {
        return getText().toString();
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        boolean result = super.onTextContextMenuItem(id);
        if (listener == null) {
            return result;
        }
        switch (id) {
            case android.R.id.cut:
                listener.onActionCut();
                break;

            case android.R.id.copy:
                listener.onActionCopy();
                break;

            case android.R.id.paste:
                listener.onActionPaste();
                break;
        }
        return result;
    }

    private void setup() {
        addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (listener != null) {
                    listener.onValueChange(getValue());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        });
        setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (listener != null) {
                    listener.onFocusChange(hasFocus);
                }
            }
        });
    }
}
