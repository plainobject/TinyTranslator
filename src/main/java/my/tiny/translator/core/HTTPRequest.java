package my.tiny.translator.core;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.HttpURLConnection;
import android.os.Handler;
import android.net.Uri;
import android.text.TextUtils;

public class HTTPRequest {
    private static final String CHARSET = "UTF-8";
    private static final String CONTENT_TYPE_FIELD = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";

    private int timeout;
    private volatile boolean cancelled;
    private Uri.Builder urlBuilder;
    private Uri.Builder bodyBuilder;
    private HTTPCallback callback;

    public HTTPRequest(String baseUrl) {
        this(baseUrl, null);
    }

    public HTTPRequest(String baseUrl, HTTPCallback callback) {
        if (TextUtils.isEmpty(baseUrl)) {
            throw new IllegalArgumentException("Invalid baseUrl!");
        }
        urlBuilder = Uri.parse(baseUrl).buildUpon();
        bodyBuilder = new Uri.Builder();
        this.callback = callback;
    }

    public void cancel() {
        cancelled = true;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void addUrlParam(String param, String value) {
        urlBuilder.appendQueryParameter(param, value);
    }

    public void addBodyParam(String param, String value) {
        bodyBuilder.appendQueryParameter(param, value);
    }

    public void send() {
        if (cancelled) {
            return;
        }
        HTTPResponse response = connect();
        if (callback != null && !cancelled) {
            callback.postResponse(response);
        }
    }

    protected HTTPResponse connect() {
        int responseCode = 0;
        String responseText = "";
        InputStream responseStream = null;
        HttpURLConnection connection = null;
        try {
            URL urlObj = new URL(urlBuilder.build().toString());
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setReadTimeout(timeout);
            connection.setConnectTimeout(timeout);

            String bodyParams = bodyBuilder.build().getQuery();
            if (!TextUtils.isEmpty(bodyParams)) {
                connection.setDoOutput(true);
                connection.setRequestProperty(
                    CONTENT_TYPE_FIELD,
                    CONTENT_TYPE_VALUE
                );
                connection.setFixedLengthStreamingMode(
                    bodyParams.getBytes().length
                );
                BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(connection.getOutputStream(), CHARSET)
                );
                writer.write(bodyParams);
                writer.close();
            }

            responseCode = connection.getResponseCode();
            responseStream = connection.getErrorStream();
            if (responseStream == null) {
                responseStream = connection.getInputStream();
            }
            responseText = convertStreamToString(responseStream);
        } catch (Exception exception) {
            responseText = exception.getMessage();
        } finally {
            if (responseStream != null) {
                try {
                    responseStream.close();
                } catch (Exception exception) {}
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new HTTPResponse(responseCode, responseText);
    }

    private static String convertStreamToString(final InputStream stream) throws Exception {
        String line;
        StringBuilder result = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, CHARSET));
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }
}
