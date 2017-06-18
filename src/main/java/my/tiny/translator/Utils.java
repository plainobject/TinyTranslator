package my.tiny.translator;

import java.util.Locale;
import java.util.regex.Pattern;
import android.view.View;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

public final class Utils {
    private Utils() {}

    public static final String SPACE = " ";

    public static final Pattern TOKENS_PATTERN = Pattern.compile("\\s+");

    public static final Pattern NORMALIZE_PATTERN = Pattern.compile("[^\\S\\n]+");

    public static void fadeInView(View view, long duration) {
        view.setVisibility(View.VISIBLE);
        view.animate()
            .alpha(1f)
            .withLayer()
            .setDuration(duration)
            .setListener(null);
    }

    public static void fadeOutView(final View view, long duration) {
        view.animate()
            .alpha(0f)
            .withLayer()
            .setDuration(duration)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
    }

    public static String normalizeText(String text) {
        return NORMALIZE_PATTERN.matcher(text.trim()).replaceAll(SPACE);
    }

    public static String capitalizeText(String text, String lang) {
        return text.substring(0, 1).toUpperCase(new Locale(lang)) +
               text.substring(1);
    }

    public static String getLanguageName(String lang) {
        Locale locale = new Locale(lang);
        Locale defaultLocale = Locale.getDefault();
        return capitalizeText(
            locale.getDisplayLanguage(defaultLocale),
            defaultLocale.getLanguage()
        );
    }

    public static String[] getTextTokens(String text) {
        return TOKENS_PATTERN.split(text);
    }

    public static boolean hasLetterOrDigit(String text) {
        for (int i = 0, n = text.length(); i < n; i++) {
            if (Character.isLetterOrDigit(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
