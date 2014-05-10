package net.lutzky.transportdroidil;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Preferences extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	private ListPreference providerPreference;
	private ListPreference extraInfoPreference;
	private SharedPreferences sharedPreferences;

	/*
	 * Preference activities are deprecated in favor of preference fragments,
	 * but those are only supported from API 11. (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Preferences.applyTheme(this);

		super.onCreate(savedInstanceState);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		addPreferencesFromResource(R.xml.preferences);
	}

	@SuppressWarnings("deprecation")
	ListPreference getProviderPreference() {
		if (providerPreference == null) {
			providerPreference = (ListPreference) findPreference("provider");
		}

		return providerPreference;
	}
	
	@SuppressWarnings("deprecation")
	ListPreference getExtraInfoPreference() {
		if (extraInfoPreference == null) {
			extraInfoPreference = (ListPreference) findPreference("extra_info");
		}
		
		return extraInfoPreference;
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateSummaries();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	private void updateSummaries() {
		getProviderPreference().setSummary(
				getProviderName(sharedPreferences.getString("provider", "")));
		
		getExtraInfoPreference().setSummary(
				sharedPreferences.getString("extra_info", ""));
	}

	@Override
	protected void onPause() {
		super.onPause();
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		updateSummaries();
	}

	private String getProviderName(String providerKey) {
		String provider_names[] = getResources().getStringArray(
				R.array.providers_names);
		String provider_values[] = getResources().getStringArray(
				R.array.provider_values);

		for (int i = 0; i < provider_values.length; ++i) {
			if (provider_values[i].equals(providerKey)) {
				return provider_names[i];
			}
		}

		return "";
	}

	public static void applyTheme(Activity activity) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);

		if (settings.getBoolean("light_theme", false)) {
			activity.setTheme(R.style.TDILLight);
		}
		else {
			activity.setTheme(R.style.TDILDark);
		}
	}
}
