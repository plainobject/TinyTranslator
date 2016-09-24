package my.tiny.translator;

import java.util.Locale;
import java.util.Collection;
import java.util.regex.Pattern;
import java.lang.StringBuilder;
import android.view.View;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

public final class Utils {
    private Utils() {}

    public static Pattern TOKENS_PATTERN = Pattern.compile("\\s+");

    public static Pattern NORMALIZE_PATTERN = Pattern.compile("[^\\S\\n]+");

    public static void fadeInView(View view, long duration) {
        if (view.getVisibility() == View.VISIBLE) {
            return;
        }
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setListener(null);
    }

    public static void fadeOutView(final View view, long duration) {
        if (view.getVisibility() == View.GONE) {
            return;
        }
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
    }

    public static String normalizeText(String text) {
        return NORMALIZE_PATTERN.matcher(text).replaceAll(" ").trim();
    }

    public static String capitalizeText(String text, String lang) {
        return text.substring(0, 1).toUpperCase(new Locale(lang)) +
               text.substring(1);
    }

    public static String[] getStringTokens(String str) {
        return TOKENS_PATTERN.split(str);
    }

    public static boolean hasLetterOrDigit(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetterOrDigit(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static String joinWithSeparator(Collection<?> values, String separator) {
        if (values == null) {
            return "";
        }
        if (separator == null) {
            separator = "";
        }
        StringBuilder result = new StringBuilder();
        for (Object value : values) {
            result.append(separator);
            result.append(value.toString());
        }
        return result.substring(separator.length());
    }
}