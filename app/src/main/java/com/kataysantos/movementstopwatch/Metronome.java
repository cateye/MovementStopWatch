package com.kataysantos.movementstopwatch;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * @author gubatron
 * @since 02/08/2017
 */
class Metronome {
    private final int sampleRate = 8000;
    private double freqOfToneInHz;
    private byte[] generatedSnd;
    private double durationInSecs;
    private int playbacks = 0;

    Metronome(double durationInSecs, double freqInHz) {
        this.durationInSecs = durationInSecs;
        this.freqOfToneInHz = freqInHz;
        genTone();
    }

    private void genTone() {
        // fill out the array
        int numSamples = (int) (durationInSecs * sampleRate);
        double[] sample = new double[numSamples];
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfToneInHz));
        }
        generatedSnd = new byte[2 * numSamples];
        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
    }

    private AudioTrack genAudioTrack() {
        return new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STATIC);

    }

    void play() {
        AudioTrack audioTrack = genAudioTrack();
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        try {
            audioTrack.play();
            playbacks++;
            System.out.println("playbacks -> " + playbacks);
        } catch (Throwable t) {
            System.out.println("releasing after it failed on playback " + playbacks);
            playbacks = 0;
            audioTrack.release();
            audioTrack = genAudioTrack();
            audioTrack.write(generatedSnd, 0, generatedSnd.length);
            audioTrack.play();
        }
    }
}
