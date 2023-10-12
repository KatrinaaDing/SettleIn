package com.example.property_management.sensors;
import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.property_management.callbacks.SensorCallback;

public class AudioSensor {
    private final int audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
    private final int sampleRateInHz = 44100;
    private final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private final int bufferSizeInBytes;
    private final int bufferSizeInShorts;
    private final AudioRecord audioRecord;
    private SensorCallback callback;
    private boolean isRecording = false;

    @SuppressLint("MissingPermission")
    public AudioSensor(SensorCallback callback) {
        this.callback = callback;
        this.bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        this.bufferSizeInShorts = bufferSizeInBytes / 2;

        this.audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
    }

    public void startTest() {
        Log.d("AudioSensor", "startTest() called");
        isRecording = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                audioRecord.startRecording();
                byte[] audioBuffer = new byte[bufferSizeInShorts];
                double sumDb = 0;
                int count = 0;
                while (isRecording && count < 30) {
                    int result = audioRecord.read(audioBuffer, 0, bufferSizeInShorts);
                    if (result > 0) {
                        double sum = 0;
                        for (int i = 0; i < result; i++) {
                            sum += audioBuffer[i] * audioBuffer[i];
                        }
                        if (sum > 0) {
                            double amplitude = sum / result;
                            double db = 10 * Math.log10(amplitude);
                            sumDb += db;
                            count++;
                            callback.onCurrentDbCalculated(db);
                        }
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                double averageDb = sumDb / count;
                callback.onAverageDbCalculated(averageDb);
            }
        }).start();
    }

    public void stopTest() {
        isRecording = false;
        audioRecord.stop();
    }

    public void setCallback(SensorCallback callback) {
        this.callback = callback;
    }

}