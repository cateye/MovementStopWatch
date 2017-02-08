package com.kataysantos.movementstopwatch;

/**
 * Created by katay on 2/7/17.
 */
public interface WarmUpListener {
    void onTick(StopWatch.Time timeUntilFinished);

    void onFinish();
}
