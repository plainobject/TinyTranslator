package my.tiny.translator;

import java.lang.Runnable;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;

public class SplashActivity extends Activity {
    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        runnable = new Runnable() {
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
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (handler != null && runnable != null) {
            handler.postDelayed(runnable, Config.SPLASH_DELAY);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}