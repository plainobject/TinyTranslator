package my.tiny.translator.core;

import java.lang.Runnable;
import android.os.Handler;

public class Debouncer {
    private long delay;
    private Handler handler = new Handler();
    private Runnable runnable;

    public Debouncer(long delay, Runnable runnable) {
        this.delay = delay;
        this.runnable = runnable;
    }

    public void start() {
        cancel();
        handler.postDelayed(runnable, delay);
    }

    public void cancel() {
        handler.removeCallbacks(runnable);
    }
}