package my.tiny.translator.core;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.HashMap;
import java.lang.StringBuilder;

public class HTTPRequest {
    public static final String CHARSET = "UTF-8";
    public static final String QUERY_DELIMITER = "?";
    public static final String PARAM_DELIMITER = "&";
    public static final String VALUE_DELIMITER = "=";
    public static final String CONTENT_TYPE_FIELD = "Content-Type";
    public static final String CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";

    protected int timeout = 0;
    protected String url;
    protected HashMap<String, String> urlParams = new HashMap<String, String>();
    protected HashMap<String, String> bodyParams = new HashMap<String, String>();

    public HTTPRequest(String url) {
        if (url == null) {
            throw new IllegalArgumentException("Invalid URL");
        }
        this.url = url;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void addUrlParam(String param, String value) {
        urlParams.put(param, value);
    }

    public void addBodyParam(String param, String value) {
        bodyParams.put(param, value);
    }

    public void setUrlParams(Map<String, String> params) {
        if (params != null) {
            urlParams.putAll(params);
        }
    }

    public void setBodyParams(Map<String, String> params) {
        if (params != null) {
            bodyParams.putAll(params);
        }
    }

    public HTTPResponse send() {
        StringBuilder finalUrl = new StringBuilder(url);
        if (!urlParams.isEmpty()) {
            finalUrl.append(
                finalUrl.indexOf(QUERY_DELIMITER) < 0 ?
                    QUERY_DELIMITER :
                    PARAM_DELIMITER
            );
            finalUrl.append(toQueryString(urlParams));
        }

        int responseCode = -1;
        String responseText = "";
        HttpURLConnection connection = null;
        try {
            URL urlObj = new URL(finalUrl.toString());
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setReadTimeout(timeout);
            connection.setConnectTimeout(timeout);

            if (!bodyParams.isEmpty()) {
                connection.setDoOutput(true);
                connection.setRequestProperty(
                    CONTENT_TYPE_FIELD,
                    CONTENT_TYPE_VALUE
                );
                String bodyParamsString = toQueryString(bodyParams);
                connection.setFixedLengthStreamingMode(
                    bodyParamsString.getBytes().length
                );
                BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(connection.getOutputStream())
                );
                writer.write(bodyParamsString);
                writer.close();
            }

            responseCode = connection.getResponseCode();
            InputStream responseStream = connection.getErrorStream();
            if (responseStream == null) {
                responseStream = connection.getInputStream();
            }
            responseText = convertStreamToString(responseStream);
            responseStream.close();
        } catch (Exception exception) {
            responseCode = 0;
            responseText = exception.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new HTTPResponse(responseCode, responseText);
    }

    public static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, CHARSET);
        } catch (Exception exception) {
            return value;
        }
    }

    public static String toQueryString(Map<String, String> map) {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            result.append(PARAM_DELIMITER);
            result.append(encodeValue(entry.getKey()));
            result.append(VALUE_DELIMITER);
            result.append(encodeValue(entry.getValue()));
        }

        return result.substring(PARAM_DELIMITER.length());
    }

    public static String convertStreamToString(InputStream stream) {
        try {
            String line;
            StringBuilder result = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (Exception exception) {
            return "";
        }
    }
}