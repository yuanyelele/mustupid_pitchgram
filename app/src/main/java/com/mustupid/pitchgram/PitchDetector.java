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

	private static final int SAMPLE_RATE = 22050;
	private static final float REFERENCE_FREQUENCY = 440;
	private static final float REFERENCE_NOTE = 69;
	static final int LOWEST_NOTE = 36; // C2
	private static final float BASE_FREQUENCY = midi_to_frequency(LOWEST_NOTE);

	private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
	private static final int TAU = (int) (SAMPLE_RATE / BASE_FREQUENCY);
	private static final int WINDOW = TAU * 5;
	private static final int BUFFER_SIZE = WINDOW + TAU;
	private final double CENTS_IN_OCTAVE = 1200;
	private final double A = CENTS_IN_OCTAVE / Math.log(2);
	private final double B = -CENTS_IN_OCTAVE * Math.log(BASE_FREQUENCY) / Math.log(2);
	private final int STEP = 500;

	private final float THRESHOLD = 0.1f;

	private AudioRecord mAudioRecorder;
	private final Handler mHandler;
	private final Runnable mCallback;
	private float mCents;
	private float mConfidence;

	PitchDetector(Handler handler, Runnable callback) {
		mHandler = handler;
		mCallback = callback;
	}

	@Override
	public void run() {
		int min_buffer_size = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, ENCODING);
		mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG,
				ENCODING, min_buffer_size);
		if (mAudioRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
			return; // Do nothing if not initialized
		}
		mAudioRecorder.startRecording();
		short[] samples = new short[BUFFER_SIZE];

		while (mAudioRecorder.read(samples, samples.length - STEP, STEP) > 0) {
			Result result = detectPitch(samples);
			mCents = (float) (A * Math.log(result.frequency) + B);
			mConfidence = result.confidence;
			mHandler.post(mCallback);
			System.arraycopy(samples, STEP, samples, 0, samples.length - STEP);
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
	float getConfidence() { return mConfidence; }

	private static float midi_to_frequency(float midi) {
		return (float) (Math.pow(2, (midi - REFERENCE_NOTE) / 12) * REFERENCE_FREQUENCY);
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
			for (int i = 1; i <= WINDOW; i++)
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
				if (y <= THRESHOLD) {
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

		result.frequency = SAMPLE_RATE / (period + parabolic_interpolation_x(diff[period - 1], diff[period], diff[period + 1]));
		if (result.frequency <= 0)
			result.frequency = SAMPLE_RATE;
		result.confidence = Math.min(1, result.confidence);

		return result;
	}

}
