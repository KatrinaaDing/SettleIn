package com.example.property_management.sensors;
import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.property_management.callbacks.SensorCallback;

/**
 * The AudioSensor class captures audio through the device's microphone and calculates the sound level in decibels.
 * It utilizes the AudioRecord class to capture audio data in PCM format.
 */
public class AudioSensor {
    private final int audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
    private final int sampleRateInHz = 44100;
    private final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private final int bufferSizeInBytes;
    private final AudioRecord audioRecord;
    private SensorCallback callback;
    private boolean isRecording = false;
    private final double offset = 98.0;

    /**
     * The AudioSensor class captures audio through the device's microphone and calculates the sound level in decibels.
     * It utilizes the AudioRecord class to capture audio data in PCM format.
     */
    @SuppressLint("MissingPermission")
    public AudioSensor(SensorCallback callback) {
        this.callback = callback;
        this.bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        this.audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
    }

    /**
     * Starts the audio recording and dB calculation on a separate thread.
     */
    public void startTest() {
        Log.d("AudioSensor", "startTest() called");
        isRecording = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Begin audio recording
                audioRecord.startRecording();
                // Buffer to hold the audio data
                short[] audioBuffer = new short[bufferSizeInBytes / 2];
                double sumDb = 0;
                int count = 0;
                // Loop to capture and process audio data
                while (isRecording && count < 30) {
                    int result = audioRecord.read(audioBuffer, 0, audioBuffer.length);
                    if (result > 0) {
                        double sum = 0;
                        // Calculate the root mean square (RMS) of the audio buffer
                        for (int i = 0; i < result; i++) {
                            sum += audioBuffer[i] * audioBuffer[i];
                        }
                        if (sum > 0) {
                            double rms = Math.sqrt(sum / result);
                            // Convert RMS to decibels
                            double db = 20 * Math.log10(rms / 32768.0); // 32768.0 is the maximum value for a signed 16-bit number
                            db += offset; // Add calibration offset
                            db *= (db >= 40) ? 1.2 : 0.8; // Scale the dB value
                            sumDb += db;
                            count++;
                            // Callback with the current dB value
                            callback.onCurrentDbCalculated(db);
                        }
                    }

                    // Handle thread interruption
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        Log.e("AudioSensor", "Recording thread interrupted", e);
                    }
                }
                // Calculate the average dB if recording is still active
                double averageDb = sumDb / count;
                if (isRecording) {
                    callback.onAverageDbCalculated(averageDb);
                }
                // Callback to indicate the completion of the audio test
                callback.onAudioTestCompleted();
            }
        }).start();
    }

    /**
     * Stops the audio recording and releases the resources associated with the AudioRecord object.
     */
    public void stopTest() {
        isRecording = false;
        audioRecord.stop();
    }

    /**
     * Sets the callback for sending dB updates.
     * @param callback The SensorCallback interface to set.
     */
    public void setCallback(SensorCallback callback) {
        this.callback = callback;
    }
}