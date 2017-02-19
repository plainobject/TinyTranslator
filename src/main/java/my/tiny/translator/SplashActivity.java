package my.tiny.translator;

import java.lang.Runnable;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

import my.tiny.translator.core.Debouncer;

public class SplashActivity extends Activity {
    private Debouncer startDebouncer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startDebouncer = new Debouncer(Config.SPLASH_DELAY, new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                );
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (startDebouncer != null) {
            startDebouncer.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (startDebouncer != null) {
            startDebouncer.start();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (startDebouncer != null) {
            startDebouncer.cancel();
        }
    }
}