package com.kataysantos.movementstopwatch;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements StopWatchLoopController {
    private final StopWatch stopWatch = new StopWatch();
    private TextView clockTextView;
    private StopWatchLoop stopWatchLoop;
    private long lastMetronomeBeep = -1;
    private final Metronome metronome = new Metronome(0.1, 1103);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initComponents();
    }

    public void updateOnTime(final StopWatch.Time time) {
        if (clockTextView == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playMetronomeBeep();
                clockTextView.setText(time.toString());
            }
        });
    }

    private void playMetronomeBeep() {
        // TODO: skip playback if metronome off.
        // if (metronome is disabled) {
        //     return;
        // }

        if (lastMetronomeBeep == -1 || SystemClock.elapsedRealtime() - lastMetronomeBeep >= 1000) {
            if (lastMetronomeBeep != -1) {
                metronome.play();
            }
            lastMetronomeBeep = SystemClock.elapsedRealtime();
        }
    }

    private void initComponents() {
        clockTextView = (TextView) findViewById(R.id.content_main_text_clock);
        clockTextView.setText(stopWatch.toString());
        ImageView metronomeButton = (ImageView) findViewById(R.id.content_main_metronomeButton);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
        metronomeButton.setLayoutParams(params);
        ListView listView = (ListView) findViewById(R.id.content_main_listview);
        LapAdapter lapAdapter = new LapAdapter(this);
        Button buttonReset = (Button) findViewById(R.id.content_main_button_reset);
        Button buttonStart = (Button) findViewById(R.id.content_main_button_start_stop);
        Button buttonLap = (Button) findViewById(R.id.content_main_button_lap);
        ResetButtonListener resetButtonListener = new ResetButtonListener(stopWatch, clockTextView, this);
        StartStopButtonListener buttonListener = new StartStopButtonListener(stopWatch, buttonStart, clockTextView, this);
        MetronomeButtonListener metronomeButtonListener = new MetronomeButtonListener(this);
        LapButtonListener lapButtonListener = new LapButtonListener(stopWatch, lapAdapter, listView);
        buttonStart.setOnClickListener(buttonListener);
        buttonReset.setOnClickListener(resetButtonListener);
        buttonLap.setOnClickListener(lapButtonListener);
        metronomeButton.setOnClickListener(metronomeButtonListener);
        listView.setAdapter(lapAdapter);
    }

    public void startStopWatchLoop() {
        stopWatchLoop = new StopWatchLoop(this, stopWatch);
        new Thread(stopWatchLoop).start();
    }

    public void stopStopWatchLoop() {
        if (stopWatchLoop != null) {
            stopWatchLoop.stop();
        }
        lastMetronomeBeep = -1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class ResetButtonListener implements View.OnClickListener {

        private final StopWatch stopWatch;
        private final TextView clockTextView;
        private final StopWatchLoopController loopController;

        ResetButtonListener(StopWatch stopWatch, TextView clockTextView, StopWatchLoopController loopController) {
            this.clockTextView = clockTextView;
            this.stopWatch = stopWatch;
            this.loopController = loopController;
        }

        @Override
        public void onClick(View v) {
            stopWatch.reset();
            clockTextView.setText(R.string.stopwatch_reset_value);
            loopController.stopStopWatchLoop();
        }
    }

    private static class StartStopButtonListener implements View.OnClickListener {
        private final StopWatch stopWatch;
        private final Button button;
        private final StopWatchLoopController loopController;
        private final TextView clockTextView;
        private final Metronome lowBeep = new Metronome(0.15, 547);
        private final Metronome highBeep = new Metronome(0.15, 1103);
        private long lastPlayedBeep = -1;
        private final WarmUpListener warmupListener = new WarmUpListener() {
            @Override
            public void onTick(StopWatch.Time timeUntilFinished) {
                if (lastPlayedBeep == -1 || SystemClock.elapsedRealtime() - lastPlayedBeep >= 1000) {
                    // skip first beep, otherwise it does an extra one.
                    if (lastPlayedBeep != -1) {
                        lowBeep.play();
                    }
                    lastPlayedBeep = SystemClock.elapsedRealtime();
                }
                clockTextView.setText(timeUntilFinished.toString());
            }

            @Override
            public void onFinish() {
                String threadName = Thread.currentThread().getName();
                System.out.println("WarmupListener.onFinish() -> on what thread am I? " + threadName);
                startLoopAndStopWatch();
                highBeep.play();
                lastPlayedBeep = -1;
            }
        };

        StartStopButtonListener(StopWatch stopWatch, Button b, TextView clockTextView, StopWatchLoopController loopController) {
            this.stopWatch = stopWatch;
            this.button = b;
            this.loopController = loopController;
            this.clockTextView = clockTextView;
        }

        @Override
        public void onClick(View v) {
            if (stopWatch.isReset()) {
                stopWatch.warmUp(3900, warmupListener);
                // TODO: Change color

            } else if (stopWatch.isPaused()) {
                // TODO: Change color
                startLoopAndStopWatch();
            } else {
                stopLoopAndStopWatch();
            }
        }

        private void startLoopAndStopWatch() {
            loopController.startStopWatchLoop();
            stopWatch.start();
            button.setText(R.string.stop);
        }

        private void stopLoopAndStopWatch() {
            stopWatch.pause();
            button.setText(R.string.start);
            loopController.stopStopWatchLoop();
        }

    }

    private class LapButtonListener implements View.OnClickListener {
        private final LapAdapter lapAdapter;
        private final StopWatch stopWatch;
        private final ListView listView;

        LapButtonListener(StopWatch stopWatch, LapAdapter lapAdapter, ListView listView) {
            this.stopWatch = stopWatch;
            this.lapAdapter = lapAdapter;
            this.listView = listView;
        }

        @Override
        public void onClick(View v) {
            lapAdapter.add(stopWatch.getTime());
            listView.invalidateViews();
        }
    }

    private class MetronomeButtonListener implements View.OnClickListener {

        private final MainActivity mainActivity;

        MetronomeButtonListener(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onClick(View v) {
        }
    }
}
