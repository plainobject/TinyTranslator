package my.tiny.translator;

import java.util.Map;
import java.util.HashMap;
import android.util.AttributeSet;
import android.widget.Button;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;

public class FontButton extends Button {
    private static Map<String, Typeface> fontCache = new HashMap<>();

    public FontButton(Context context) {
        super(context);
    }

    public FontButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(context, attrs, 0);
    }

    public FontButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeface(context, attrs, defStyleAttr);
    }

    private void setTypeface(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray attrsArray = context.obtainStyledAttributes(
            attrs, R.styleable.FontButton, defStyleAttr, 0
        );
        try {
            String fontPath = attrsArray.getString(R.styleable.FontButton_fontPath);
            Typeface typeface = fontCache.get(fontPath);
            if (typeface == null) {
                typeface = Typeface.createFromAsset(context.getAssets(), fontPath);
                fontCache.put(fontPath, typeface);
            }
            setTypeface(typeface);
        } finally {
            attrsArray.recycle();
        }
    }
}
