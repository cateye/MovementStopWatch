package com.kataysantos.movementstopwatch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private final StopWatch stopWatch = new StopWatch();
    private TextView clockTextView;

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

    private static String twoDigits(int n) {
        return (n < 10 ? "0" : "") + String.valueOf(n);
    }

    public void updateOnTime(final StopWatch.Time time) {
        if (clockTextView == null) {
            return;
        }
        final String timeString = twoDigits(time.hours) + ":" + twoDigits(time.minutes) + ":" + twoDigits(time.secs) + "." + (time.millisecs % 10);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                clockTextView.setText(timeString);
            }
        });

    }

    private void initComponents() {
        clockTextView = (TextView) findViewById(R.id.content_main_text_clock);
        clockTextView.setText(stopWatch.toString());
        ListView listView = (ListView) findViewById(R.id.content_main_listview);
        LapAdapter lapAdapter = new LapAdapter(this);
        Button buttonReset = (Button) findViewById(R.id.content_main_button_reset);
        Button buttonStart = (Button) findViewById(R.id.content_main_button_start_stop);
        Button buttonLap = (Button) findViewById(R.id.content_main_button_lap);
        StopWatchLoop loop = new StopWatchLoop(this, stopWatch);
        new Thread(loop).start();
        ResetButtonListener resetButtonListener = new ResetButtonListener(stopWatch, clockTextView);
        StartStopButtonListener buttonListener = new StartStopButtonListener(stopWatch, buttonStart);
        LapButtonListener lapButtonListener = new LapButtonListener(stopWatch, lapAdapter, listView);
        buttonStart.setOnClickListener(buttonListener);
        buttonReset.setOnClickListener(resetButtonListener);
        buttonLap.setOnClickListener(lapButtonListener);

        listView.setAdapter(lapAdapter);
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

    private static class StopWatchLoop implements Runnable {
        private final static long SLEEP_TIME = 75;
        private final StopWatch stopWatch;
        private final MainActivity mainActivity;


        StopWatchLoop(MainActivity mainActivity, StopWatch stopWatch) {
            this.mainActivity = mainActivity;
            this.stopWatch = stopWatch;

        }

        @Override
        public void run() {
            while (true) {
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
    }

    private static class ResetButtonListener implements  View.OnClickListener{

        private final StopWatch stopWatch;
        private final TextView clockTextView;

        ResetButtonListener(StopWatch stopWatch, TextView clockTextView){
            this.clockTextView = clockTextView;
            this.stopWatch = stopWatch;
        }

        @Override
        public void onClick(View v) {
            stopWatch.reset();
            clockTextView.setText("00:00:00.0");
        }
    }

    private static class StartStopButtonListener implements View.OnClickListener, View.OnLongClickListener {

        private final StopWatch stopWatch;
        private final Button button;

        StartStopButtonListener(StopWatch stopWatch, Button b) {
            this.stopWatch = stopWatch;
            this.button = b;
        }

        @Override
        public void onClick(View v) {
            if (stopWatch.isPaused()) {
                stopWatch.start();
                button.setText(R.string.stop);
            } else {
                stopWatch.pause();
                button.setText(R.string.start);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            stopWatch.reset();
            return true;
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
}
