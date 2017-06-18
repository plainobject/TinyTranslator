package my.tiny.translator.core;

import java.util.LinkedList;
import android.os.Process;

public class HTTPManager {
    private static final int DEFAULT_THREAD_POOL_SIZE = 4;
    private final HTTPThread[] threads;
    private final LinkedList<HTTPRequest> queue;

    public HTTPManager() {
        this(DEFAULT_THREAD_POOL_SIZE);
    }

    public HTTPManager(int threadPoolSize) {
        queue = new LinkedList<>();
        threads = new HTTPThread[threadPoolSize];

        for (int i = 0; i < threadPoolSize; i++) {
            threads[i] = new HTTPThread();
            threads[i].start();
        }
    }

    public void stop() {
        for (int i = 0; i < threads.length; i++) {
            if (threads[i] != null) {
                threads[i].interrupt();
            }
        }
    }

    public void addRequest(HTTPRequest request) {
        synchronized (queue) {
            queue.addLast(request);
            queue.notifyAll();
        }
    }

    private class HTTPThread extends Thread {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            HTTPRequest request;
            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException exception) {
                            return;
                        }
                    }
                    request = queue.removeFirst();
                }

                try {
                    request.send();
                } catch (RuntimeException exception) {
                    // ignore
                }
            }
        }
    }
}
