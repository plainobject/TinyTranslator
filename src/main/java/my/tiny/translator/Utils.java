package my.tiny.translator;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Locale;
import java.lang.StringBuilder;
import android.view.View;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

public final class Utils {
    private Utils() {}

    public static int STATUS_OK = 200;

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

    public static String downloadUrl(String urlString, int timeout) throws IOException {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setReadTimeout(timeout);
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(timeout);
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == STATUS_OK) {
                inputStream = urlConnection.getInputStream();
                String result = convertInputStreamToString(inputStream);
                return result;
            } else {
                throw new IOException(urlConnection.getResponseMessage());
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public static String normalizeText(String text) {
        return text.replaceAll("[^\\S\\n]+", " ").trim();
    }

    public static String capitalizeText(String text, String lang) {
        return text.substring(0, 1).toUpperCase(new Locale(lang)) +
               text.substring(1);
    }

    public static boolean hasLetterOrDigit(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetterOrDigit(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static String encodeURIComponent(String component) {
        try {
            return URLEncoder.encode(component, "UTF-8");
        } catch (Exception exception) {
            return component;
        }
    }

    public static String convertMapToQueryString(Map<String, String> map) {
        StringBuilder result = new StringBuilder();

        for (Entry<String, String> entry : map.entrySet()) {
            if (result.length() > 0) {
                result.append("&");
            }
            result.append(encodeURIComponent(entry.getKey()));
            result.append("=");
            result.append(encodeURIComponent(entry.getValue()));
        }

        return result.toString();
    }

    public static String convertInputStreamToString(InputStream inputStream) {
        try {
            String line;
            StringBuilder result = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
            return result.toString();
        } catch (Exception exception) {
            return "";
        }
    }
}