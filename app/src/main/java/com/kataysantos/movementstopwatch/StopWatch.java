package com.kataysantos.movementstopwatch;

import android.os.CountDownTimer;
import android.os.SystemClock;

/**
 * @author Katay
 * @author gubatron
 * @since 01/31/2017
 */
public class StopWatch {
    private long startTime; // start timestamp from currentTimeMillis().
    private boolean isPaused = true;
    private long totalTime = 0; // time that has passed

    void start() {
        this.startTime = SystemClock.elapsedRealtime();
        isPaused = false;
    }

    boolean isPaused() {
        return isPaused;
    }

    void pause() {
        totalTime += SystemClock.elapsedRealtime() - this.startTime;
        isPaused = true;
    }

    void reset() {
        this.totalTime = 0;
        this.startTime = SystemClock.elapsedRealtime();
        isPaused = true;
    }

    boolean isReset() {
        return isPaused && totalTime == 0;
    }

    public Time getTime() {
        if (!isPaused) {
            return Time.fromMillis(totalTime + (SystemClock.elapsedRealtime() - startTime));
        }
        return Time.fromMillis(totalTime);
    }

    public String toString() {
        return getTime().toString();
    }

    void warmUp(final long millisecs, final WarmUpListener listener) {
        new CountDownTimer(millisecs, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                long millisNormalized = (long) (Math.ceil((double)(millisUntilFinished/1000))*1000);
                try {
                    if (listener != null) {
                        listener.onTick(Time.fromMillis(millisNormalized));
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            @Override
            public void onFinish() {
                listener.onFinish();
            }
        }.start();
    }

    static class Time {
        final int hours;
        final int minutes;
        final int secs;
        final int millisecs;

        Time(int hours, int minutes, int secs, int millisecs) {
            this.hours = hours;
            this.minutes = minutes;
            this.secs = secs;
            this.millisecs = millisecs;
        }

        static Time fromMillis(long millis) {
            int hours = (int) (millis / 3600000);
            millis = millis % 3600000;

            int minutes = (int) (millis / 60000);
            millis = millis % 60000;

            int secs = (int) (millis / 1000);

            int ms = (int) (millis % 100);
            return new StopWatch.Time(hours, minutes, secs, ms);
        }

        @SuppressWarnings("unused")
        long toMillis() {
            return millisecs + (secs * 1000) + (minutes * 60 * 1000) + (hours * 60 * 60 * 1000);
        }

        private String twoDigits(int n) {
            return (n < 10 ? "0" : "") + String.valueOf(n);
        }

        public String toString() {
            return (hours > 0 ? twoDigits(hours) + ":" : "") + twoDigits(minutes) + ":" + twoDigits(secs) + "." + (millisecs % 10);
        }
    }

    @SuppressWarnings("unused")
    private static void testForNSeconds(int seconds) {
        long now = SystemClock.elapsedRealtime();
        long end = now + seconds * 1000;
        StopWatch watch = new StopWatch();
        watch.start();
        while (now < end) {
            now = System.currentTimeMillis();
            watch.getTime();
        }
        System.out.println(watch.getTime());
        watch.pause();
    }

    public static void main(String[] args) throws InterruptedException {
        StopWatch clock = new StopWatch();
        System.out.println(clock);
        clock.start();
        System.out.println(clock);
        System.out.println("sleep 5 seconds...");
        Thread.sleep(5000);
        clock.pause();
        System.out.println(clock.getTime());
        System.out.println("pause!");

        System.out.println("sleep 3 seconds (paused, time should be the same if i ask again)");
        Thread.sleep(3000);
        System.out.println(clock.getTime() + " << ???? was it the same as last time?");
        System.out.println("resume!");
        clock.start();
        System.out.println("wait another 7 seconds...(running)");
        Thread.sleep(7000);
        System.out.println(clock.getTime());
        System.out.println("reset!");
        clock.reset();
        System.out.println(clock.getTime());
    }
}