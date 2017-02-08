package com.kataysantos.movementstopwatch;
/**
 * @author gubatron
 * @since 02/07/2017
 */
interface WarmUpListener {
    void onTick(StopWatch.Time timeUntilFinished);
    void onFinish();
}
