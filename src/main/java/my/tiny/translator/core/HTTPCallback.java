package my.tiny.translator.core;

import android.os.Handler;

public abstract class HTTPCallback {
    private final Handler handler = new Handler();

    public void postResponse(final HTTPResponse response) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onResponse(response);
            }
        });
    }

    public abstract void onResponse(HTTPResponse response);
}
