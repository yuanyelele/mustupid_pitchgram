package com.mustupid.pitchgram;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;

class PitchDetector extends Thread {
	static {
		System.loadLibrary("pitch_detector");
	}

	/**
	 * Detect pitch in samples.
	 *
	 * @param sample samples
	 * @param samplerate sample rate
	 * @return pitch in Hz or 0 if not detected
	 */
	public native float detectPitch(short[] sample, int samplerate);

	private static final int SAMPLE_RATE = 44100;
	final private static float BASE_FREQUENCY = 65.41f;

	private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
	private static final int BUFFER_SIZE = (int) (SAMPLE_RATE / BASE_FREQUENCY * 2);
	private final double CENTS_IN_OCTAVE = 1200;
	private final double A = CENTS_IN_OCTAVE / Math.log(2);
	private final double B = -CENTS_IN_OCTAVE * Math.log(BASE_FREQUENCY) / Math.log(2);

	private AudioRecord mAudioRecorder;
	private final Handler mHandler;
	private Runnable mCallback;
	private float mCents;

	PitchDetector(Handler handler, Runnable callback) {
		mHandler = handler;
		mCallback = callback;
	}

	@Override
	public void run() {
		mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG,
				ENCODING, SAMPLE_RATE * 6);
		if (mAudioRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
			return; // Do nothing if not initialized
		}
		mAudioRecorder.startRecording();
		short[] readBuffer = new short[BUFFER_SIZE];
		while (mAudioRecorder.read(readBuffer, 0, readBuffer.length) > 0) {
			float frequency = detectPitch(readBuffer, SAMPLE_RATE);
			if (frequency == 0) {
				mCents = 0;
			} else {
				mCents = (float) (A * Math.log(frequency) + B);
			}

			mHandler.post(mCallback);
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
}
