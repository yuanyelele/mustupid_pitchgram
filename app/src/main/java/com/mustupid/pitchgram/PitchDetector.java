package com.mustupid.pitchgram;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;

class PitchDetector extends Thread {

	class Result {
		float frequency;
		float confidence;
	}

	private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
	private static int TAU;

	private final Handler mHandler;
	private final Runnable mCallback;

	private AudioRecord mAudioRecorder;
	private float mCents;
	private float mConfidence;

	PitchDetector(Handler handler, Runnable callback) {
		mHandler = handler;
		mCallback = callback;
	}

	@Override
	public void run() {
		int min_buffer_size = AudioRecord.getMinBufferSize(Settings.SAMPLE_RATE, CHANNEL_CONFIG, ENCODING);
		mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, Settings.SAMPLE_RATE, CHANNEL_CONFIG,
				ENCODING, min_buffer_size);
		if (mAudioRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
			return;
		}
		mAudioRecorder.startRecording();

		TAU = (int) (Settings.SAMPLE_RATE / midi_to_frequency(Settings.lowest));
		int buffer_size = Settings.window + TAU;
		short[] samples = new short[buffer_size];
		// TODO: Shift: precision: smaller, performance: recursion
		while (mAudioRecorder.read(samples, samples.length - Settings.shift, Settings.shift) > 0) {
			Result result = detectPitch(samples);
			mCents = (float)(Math.log(result.frequency / Settings.REFERENCE_FREQUENCY) / Math.log(2) * Settings.CENTS_IN_OCTAVE);
			mConfidence = result.confidence;
			mHandler.post(mCallback);
			System.arraycopy(samples, Settings.shift, samples, 0, samples.length - Settings.shift);
		}
	}

	void close() {
		if (mAudioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
			mAudioRecorder.stop();
			mAudioRecorder.release();
		}
	}

	float getCents() {
		return mCents;
	}

	float getConfidence() {
		return mConfidence;
	}

	private static float midi_to_frequency(float midi) {
		return (float) (Math.pow(2, (midi - Settings.REFERENCE_MIDI) / 12) * Settings.REFERENCE_FREQUENCY);
	}

	private float parabolic_interpolation_x(float a, float b, float c) {
		float d = a + c - b * 2;
		if (d == 0) {
			return 0;
		}
		return (a - c) / d / 2;
	}

	private float parabolic_interpolation_y(float a, float b, float c) {
		float d = a + c - b * 2;
		if (d == 0) {
			return b;
		}
		return b - (a - c) * (a - c) / d / 8;
	}

	private Result detectPitch(short[] sample) {
		Result result = new Result();

		float[] diff = new float[TAU];
		for (int t = 0; t < TAU; t++)
			for (int i = 1; i <= Settings.window; i++)
				diff[t] += Math.pow(sample[i] - sample[i + t], 2);

		float[] cmn = new float[TAU];
		cmn[0] = 1;
		float denominator = 0;
		for (int t = 1; t < TAU; t++) {
			denominator += diff[t];
			if (denominator != 0)
				cmn[t] = diff[t] * t / denominator;
			else
				cmn[t] = 1;
		}

		int period = 1;
		result.confidence = 0;
		// Find the first local minimum below threshold
		for (int i = 1; i < TAU - 1; i++){
			if (cmn[i] <= cmn[i - 1] && cmn[i] <= cmn[i + 1]) {
				float y = parabolic_interpolation_y(cmn[i - 1], cmn[i], cmn[i + 1]);
				if (y <= 1 - Settings.threshold1) {
					period = i;
					result.confidence = 1 - y;
					break;
				}
			}
		}

		// If not found, find global minimum under greater threshold.
		if (result.confidence == 0) {
			float minimum = 1;
			for (int i = 1; i < TAU - 1; i++) {
				if (cmn[i] <= cmn[i - 1] && cmn[i] <= cmn[i + 1]) {
					float y = parabolic_interpolation_y(cmn[i - 1], cmn[i], cmn[i + 1]);
					if (y < minimum) {
						period = i;
						result.confidence = 1 - y;
						minimum = y;
					}
				}
			}
		}

		result.frequency = Settings.SAMPLE_RATE /
				(period + parabolic_interpolation_x(diff[period - 1], diff[period], diff[period + 1]));
		if (result.frequency <= 0)
			result.frequency = Settings.SAMPLE_RATE;
		result.confidence = Math.min(1, result.confidence);

		return result;
	}

}
