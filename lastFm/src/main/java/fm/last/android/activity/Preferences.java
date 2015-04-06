/**
 *
 */
package fm.last.android.activity;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import fm.last.android.LastFMApplication;
import fm.last.android.LastFm;
import fm.last.android.R;


/**
 * @author sam
 *
 */
public class Preferences extends PreferenceActivity {

	Preference.OnPreferenceChangeListener scrobbletoggle = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if(preference.getKey().equals("scrobble")) {
				if((Boolean) newValue) {
					getPackageManager().setComponentEnabledSetting(
							new ComponentName("fr.outadev.lastfm.scrobb", "fr.outadev.lastfm.scrobb.scrobbler.MusicIntentReceiver"),
							PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);

					//Re-enable the rest of the recievers to match the current preference state
					if(preference.getSharedPreferences().getBoolean("scrobble_music_player", true)) {
						getPackageManager().setComponentEnabledSetting(
								new ComponentName("fr.outadev.lastfm.scrobb", "fr.outadev.lastfm.scrobb.scrobbler.AndroidMusicIntentReceiver"),
								PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
					} else {
						getPackageManager().setComponentEnabledSetting(
								new ComponentName("fr.outadev.lastfm.scrobb", "fr.outadev.lastfm.scrobb.scrobbler.AndroidMusicIntentReceiver"),
								PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
					}

					if(preference.getSharedPreferences().getBoolean("scrobble_sdroid", true)) {
						getPackageManager().setComponentEnabledSetting(
								new ComponentName("fr.outadev.lastfm.scrobb", "fr.outadev.lastfm.scrobb.scrobbler.ScrobbleDroidIntentReceiver"),
								PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
					} else {
						getPackageManager().setComponentEnabledSetting(
								new ComponentName("fr.outadev.lastfm.scrobb", "fr.outadev.lastfm.scrobb.scrobbler.ScrobbleDroidIntentReceiver"),
								PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
					}

					if(preference.getSharedPreferences().getBoolean("scrobble_sls", true)) {
						getPackageManager().setComponentEnabledSetting(
								new ComponentName("fr.outadev.lastfm.scrobb", "fr.outadev.lastfm.scrobb.scrobbler.SLSIntentReceiver"),
								PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
					} else {
						getPackageManager().setComponentEnabledSetting(
								new ComponentName("fr.outadev.lastfm.scrobb", "fr.outadev.lastfm.scrobb.scrobbler.SLSIntentReceiver"),
								PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
					}

				} else {
					getPackageManager().setComponentEnabledSetting(
							new ComponentName("fr.outadev.lastfm.scrobb", "fr.outadev.lastfm.scrobb.scrobbler.MusicIntentReceiver"),
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
					getPackageManager().setComponentEnabledSetting(
							new ComponentName("fr.outadev.lastfm.scrobb", "fr.outadev.lastfm.scrobb.scrobbler.AndroidMusicIntentReceiver"),
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
					getPackageManager().setComponentEnabledSetting(
							new ComponentName("fr.outadev.lastfm.scrobb", "fr.outadev.lastfm.scrobb.scrobbler.ScrobbleDroidIntentReceiver"),
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
					getPackageManager().setComponentEnabledSetting(
							new ComponentName("fr.outadev.lastfm.scrobb", "fr.outadev.lastfm.scrobb.scrobbler.SLSIntentReceiver"),
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				}

			} else if(preference.getKey().equals("scrobble_music_player")) {
				if((Boolean) newValue) {
					getPackageManager().setComponentEnabledSetting(
							new ComponentName("fr.outadev.lastfm.scrobb", "fr.outadev.lastfm.scrobb.scrobbler.AndroidMusicIntentReceiver"),
							PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
				} else {
					getPackageManager().setComponentEnabledSetting(
							new ComponentName("fr.outadev.lastfm.scrobb", "fr.outadev.lastfm.scrobb.scrobbler.AndroidMusicIntentReceiver"),
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				}

			} else if(preference.getKey().equals("scrobble_sdroid")) {
				if((Boolean) newValue) {
					getPackageManager().setComponentEnabledSetting(
							new ComponentName("fr.outadev.lastfm.scrobb", "fr.outadev.lastfm.scrobb.scrobbler.ScrobbleDroidIntentReceiver"),
							PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
				} else {
					getPackageManager().setComponentEnabledSetting(
							new ComponentName("fr.outadev.lastfm.scrobb", "fr.outadev.lastfm.scrobb.scrobbler.ScrobbleDroidIntentReceiver"),
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				}

			} else if(preference.getKey().equals("scrobble_sls")) {
				if((Boolean) newValue) {
					getPackageManager().setComponentEnabledSetting(
							new ComponentName("fr.outadev.lastfm.scrobb", "fr.outadev.lastfm.scrobb.scrobbler.SLSIntentReceiver"),
							PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
				} else {
					getPackageManager().setComponentEnabledSetting(
							new ComponentName("fr.outadev.lastfm.scrobb", "fr.outadev.lastfm.scrobb.scrobbler.SLSIntentReceiver"),
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				}
			}

			NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			nm.cancel(1338);
			return true;
		}
	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		addPreferencesFromResource(R.xml.preferences_scrobbler);
		addPreferencesFromResource(R.xml.preferences_account);
		addPreferencesFromResource(R.xml.preferences_about);

		SharedPreferences settings = getSharedPreferences(LastFm.PREFS, 0);

		findPreference("current_user").setSummary(
				getString(R.string.prefs_current_user_sum, settings.getString("lastfm_user", "-")));

		findPreference("current_user").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				LastFMApplication.getInstance().logout();

				Intent i = new Intent(Preferences.this, LastFm.class);

				startActivity(i);
				finish();
				return true;
			}

		});

		findPreference("scrobble").setOnPreferenceChangeListener(scrobbletoggle);
		findPreference("scrobble_music_player").setOnPreferenceChangeListener(scrobbletoggle);
		findPreference("scrobble_sdroid").setOnPreferenceChangeListener(scrobbletoggle);
		findPreference("scrobble_sls").setOnPreferenceChangeListener(scrobbletoggle);

		try {
			findPreference("version").setSummary(getPackageManager().getPackageInfo("fr.outadev.lastfm.scrobb", 0).versionName);
		} catch(NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			LastFMApplication.getInstance().tracker.trackPageView("/Preferences");
		} catch(Exception e) {
			//Google Analytics doesn't appear to be thread safe
		}
	}

}
