package com.mustupid.pitchgram;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.content.ContextCompat;


public class PitchgramActivity extends AppCompatActivity {

	private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
	private PitchDetector mPitchDetector;
	private final Handler mHandler = new Handler();
	private final Runnable mCallback = new Runnable() {
		public void run() {
			float cents = mPitchDetector.getCents();
			if (cents == 0) {
				mCanvas.addPoint(-1);
				return;
			}
			mCanvas.addPoint(cents);
		}
	};
	private PitchgramView mCanvas;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pitchgram_activity);

		mCanvas = findViewById(R.id.view);

		// Make volume button always control just the media volume
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    	if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			mPitchDetector = new PitchDetector(mHandler, mCallback);
			mPitchDetector.start();
        }
    }

	@Override
	public void onResume() {
		super.onResume();
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
				== PackageManager.PERMISSION_GRANTED) {
			mPitchDetector = new PitchDetector(mHandler, mCallback);
			mPitchDetector.start();
		} else {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.RECORD_AUDIO},
					PERMISSIONS_REQUEST_RECORD_AUDIO);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mPitchDetector != null)
			mPitchDetector.close();
	}

}
