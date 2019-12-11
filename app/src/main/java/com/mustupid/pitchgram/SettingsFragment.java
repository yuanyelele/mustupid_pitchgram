package com.mustupid.pitchgram;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        SeekBarPreference preference = findPreference("threshold1");
        if (preference != null) {
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Settings.threshold1 = (int)newValue / 100f;
                    return true;
                }
            });
        }

        preference = findPreference("threshold2");
        if (preference != null) {
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Settings.threshold2 = (int)newValue / 100f;
                    return true;
                }
            });
        }

        preference = findPreference("lowest");
        if (preference != null) {
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Settings.lowest = (int)newValue;
                    return true;
                }
            });
        }

        preference = findPreference("window");
        if (preference != null) {
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Settings.window = (int)newValue;
                    return true;
                }
            });
        }

        preference = findPreference("shift");
        if (preference != null) {
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Settings.shift = (int)newValue;
                    return true;
                }
            });
        }

        preference = findPreference("smoothness");
        if (preference != null) {
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Settings.smoothness = (int)newValue / 100f;
                    return true;
                }
            });
        }

    }

}
