package com.mustupid.pitchgram;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.content.SharedPreferences;
import android.widget.ImageButton;

import androidx.preference.PreferenceManager;

public class PitchgramActivity extends AppCompatActivity {

	private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

	private final Handler mHandler = new Handler();
	private final Runnable mCallback = new Runnable() {
		@Override
		public void run() {
			mPitchgramView.addPoint(mPitchDetector.getCents(), mPitchDetector.getConfidence());
			mCentsView.addCents(mPitchDetector.getCents(), mPitchDetector.getConfidence());
		}
	};

	private PitchgramView mPitchgramView;
	private CentsView mCentsView;
	private PitchDetector mPitchDetector;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pitchgram_activity);

		mPitchgramView = findViewById(R.id.pitchgram_view);
		mCentsView = findViewById(R.id.cents_view);
		ImageButton settingsButton = findViewById(R.id.settings_button);
		settingsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PitchgramActivity.this, SettingsActivity.class);
				startActivity(intent);
			}
		});

		// Make volume button always control just the media volume
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Resources res = getResources();
		Settings.threshold1 = preferences.getInt("threshold1", res.getInteger(R.integer.threshold1_default)) / 100f;
		Settings.threshold2 = preferences.getInt("threshold2", res.getInteger(R.integer.threshold2_default)) / 100f;
		Settings.lowest = preferences.getInt("lowest", res.getInteger(R.integer.lowest_default));
		Settings.window = preferences.getInt("window", res.getInteger(R.integer.window_default));
		Settings.shift = preferences.getInt("shift", res.getInteger(R.integer.shift_default));
		Settings.smoothness = preferences.getInt("smoothness", res.getInteger(R.integer.smoothness_default)) / 100f;
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
