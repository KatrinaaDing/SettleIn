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
    private final AudioRecord audioRecord;
    private SensorCallback callback;
    private boolean isRecording = false;
    private final double offset = 100.0;

    @SuppressLint("MissingPermission")
    public AudioSensor(SensorCallback callback) {
        this.callback = callback;
        this.bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        this.audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
    }

    public void startTest() {
        Log.d("AudioSensor", "startTest() called");
        isRecording = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                audioRecord.startRecording();
                short[] audioBuffer = new short[bufferSizeInBytes / 2];
                double sumDb = 0;
                int count = 0;
                while (isRecording && count < 30) {
                    int result = audioRecord.read(audioBuffer, 0, audioBuffer.length);
                    if (result > 0) {
                        double sum = 0;
                        for (int i = 0; i < result; i++) {
                            sum += audioBuffer[i] * audioBuffer[i];
                        }
                        if (sum > 0) {
                            double rms = Math.sqrt(sum / result);
                            double db = 20 * Math.log10(rms / 32768.0); // 32768.0 is the maximum value for a signed 16-bit number
                            db += offset; // 添加偏移量
                            sumDb += db;
                            count++;
                            callback.onCurrentDbCalculated(db);
                        }
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        Log.e("AudioSensor", "Recording thread interrupted", e);
                    }
                }
                double averageDb = sumDb / count;
                if (isRecording) { // 只有当仍在记录时才更新平均值
                    callback.onAverageDbCalculated(averageDb);
                }
                callback.onAudioTestCompleted();
            }
        }).start();
    }

    public void stopTest() {
        isRecording = false;
        audioRecord.stop();
        audioRecord.release(); // Make sure to release the audioRecord's resources
    }

    public void setCallback(SensorCallback callback) {
        this.callback = callback;
    }

}