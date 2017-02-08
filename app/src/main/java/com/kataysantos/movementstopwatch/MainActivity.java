package com.kataysantos.movementstopwatch;




import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
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
                clockTextView.setText(time.toString());
            }
        });
    }

    private void initComponents() {

        clockTextView = (TextView) findViewById(R.id.content_main_text_clock);
        clockTextView.setText(stopWatch.toString());
        ImageView metronomeButton = (ImageView) findViewById(R.id.content_main_metronomeButton);
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(100,100);
        metronomeButton.setLayoutParams(parms);
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

    private static class ResetButtonListener implements  View.OnClickListener{

        private final StopWatch stopWatch;
        private final TextView clockTextView;
        private final StopWatchLoopController loopController;

        ResetButtonListener(StopWatch stopWatch, TextView clockTextView, StopWatchLoopController loopController){
            this.clockTextView = clockTextView;
            this.stopWatch = stopWatch;
            this.loopController = loopController;
        }

        @Override
        public void onClick(View v) {
            stopWatch.reset();
            clockTextView.setText("00:00.0");
            loopController.stopStopWatchLoop();
        }
    }

    private static class StartStopButtonListener implements View.OnClickListener {

        private final StopWatch stopWatch;
        private final Button button;
        private final StopWatchLoopController loopController;
        private final TextView clockTextView;
        private final WarmUpListener warmupListener = new WarmUpListener() {
            @Override
            public void onTick(StopWatch.Time timeUntilFinished) {
                clockTextView.setText(timeUntilFinished.toString());
            }

            @Override
            public void onFinish() {
                startLoopAndStopWatch();
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
            if (stopWatch.isReset()){
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

        private  void stopLoopAndStopWatch() {
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

    private class MetronomeButtonListener implements  View.OnClickListener{

        private final MainActivity mainActivity;

        public MetronomeButtonListener(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onClick(View v) {
            MediaPlayer mPlayer = MediaPlayer.create(mainActivity, R.raw.beep);
            mPlayer.start();
        }
    }
}
