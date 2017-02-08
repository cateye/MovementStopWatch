package com.kataysantos.movementstopwatch;

/**
 * @author gubatron
 * @since 02/05/2017
 */
class StopWatchLoop implements Runnable {
    private final static long SLEEP_TIME = 75;
    private final StopWatch stopWatch;
    private final MainActivity mainActivity;
    private boolean running;

    StopWatchLoop(MainActivity mainActivity, StopWatch stopWatch) {
        this.mainActivity = mainActivity;
        this.stopWatch = stopWatch;
        this.running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(SLEEP_TIME);
                if (!stopWatch.isPaused()) {
                    mainActivity.updateOnTime(stopWatch.getTime());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void stop() {
        running = false;
    }
}