/***************************************************************************
 *   Copyright 2005-2009 Last.fm Ltd.                                      *
 *   Portions contributed by Casey Link, Lukasz Wisniewski,                *
 *   Mike Jennings, and Michael Novak Jr.                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.         *
 ***************************************************************************/
package fm.last.android;

import android.app.AlertDialog;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import java.util.Locale;

import fm.last.android.db.LastFmDbHelper;
import fm.last.android.sync.AccountAuthenticatorService;
import fm.last.api.Session;
import fm.last.api.WSError;
import fm.last.util.UrlUtil;

public class LastFMApplication extends Application {

	private static LastFMApplication instance = null;
	public Session session;
	public fm.last.android.player.IRadioPlayer player = null;
	public Context mCtx;
	public GoogleAnalyticsTracker tracker;
	private String mRequestedURL;
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			player = fm.last.android.player.IRadioPlayer.Stub.asInterface(service);
		}

		public void onServiceDisconnected(ComponentName className) {
			player = null;
		}
	};

	public static LastFMApplication getInstance() {
		if(instance != null) {
			return instance;
		} else {
			return new LastFMApplication();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;

		String version;
		try {
			version = "/" + LastFMApplication.getInstance().getPackageManager().getPackageInfo("fm.last.android", 0).versionName;
		} catch(Exception e) {
			version = "";
		}

		UrlUtil.useragent = "MobileLastFM" + version + " (" + android.os.Build.MODEL + "; " + Locale.getDefault().getCountry().toLowerCase() + "; "
				+ "Android " + android.os.Build.VERSION.RELEASE + ")";

		// Populate our Session object
		SharedPreferences settings = getSharedPreferences(LastFm.PREFS, 0);
		String username = settings.getString("lastfm_user", "");
		String session_key = settings.getString("lastfm_session_key", "");
		String subscriber = settings.getString("lastfm_subscriber", "0");

		session = new Session(username, session_key, subscriber);
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(PrivateAPIKey.ANALYTICS_ID, this);
	}

	@Override
	public void onTerminate() {
		session = null;
		instance = null;
		tracker.stop();
		super.onTerminate();
	}

	public void presentError(Context ctx, WSError error) {
		int title = 0;
		int description = 0;

		if(error != null) {
			Log.e("Last.fm", "Received a webservice error during method: " + error.getMethod() + ", message: " + error.getMessage());
			try {
				LastFMApplication.getInstance().tracker.trackEvent("Errors", // Category
						error.getMethod(), // Action
						error.getMessage(), // Label
						0); // Value
			} catch(Exception e) {
				//Google Analytics doesn't appear to be thread safe
			}

			if(error.getMethod().equals("user.signUp")) {
				title = R.string.ERROR_SIGNUP_TITLE;
				switch(error.getCode()) {
					case WSError.ERROR_InvalidParameters:
						presentError(ctx, getResources().getString(title), error.getMessage());
						return;

				}
			}
		}

		if(title == 0) {
			title = R.string.ERROR_SERVER_UNAVAILABLE_TITLE;
		}

		if(description == 0) {
			if(error != null) {
				switch(error.getCode()) {
					case WSError.ERROR_AuthenticationFailed:
					case WSError.ERROR_InvalidSession:
						title = R.string.ERROR_SESSION_TITLE;
						description = R.string.ERROR_SESSION;
						break;
					case WSError.ERROR_InvalidAPIKey:
						title = R.string.ERROR_UPGRADE_TITLE;
						description = R.string.ERROR_UPGRADE;
						break;
					case WSError.ERROR_SubscribersOnly:
						title = R.string.ERROR_SUBSCRIPTION_TITLE;
						description = R.string.ERROR_SUBSCRIPTION;
						break;
					default:
						presentError(ctx, getResources().getString(title), getResources().getString(R.string.ERROR_SERVER_UNAVAILABLE) + "\n\n" + error.getMethod() + ": " + error.getMessage());
						return;
				}
			} else {
				description = R.string.ERROR_SERVER_UNAVAILABLE;
			}
		}

		presentError(ctx, getResources().getString(title), getResources().getString(description));
	}

	public void presentError(Context ctx, String title, String description) {
		AlertDialog.Builder d = new AlertDialog.Builder(ctx);

		d.setTitle(title);
		d.setMessage(description);
		d.setNeutralButton(R.string.common_ok, null);

		try {
			d.show();
		} catch(Exception ignored) {
		}
	}

	public void logout() {
		SharedPreferences settings = getSharedPreferences(LastFm.PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();

		editor.remove("lastfm_user");
		editor.remove("lastfm_pass");
		editor.remove("lastfm_session_key");
		editor.remove("lastfm_subscriber");
		editor.remove("lastfm_freetrial");
		editor.remove("lastfm_radio");
		editor.remove("lastfm_playselapsed");
		editor.remove("lastfm_playsremaining");
		editor.remove("lastfm_freetrialexpirationwarning");
		editor.remove("scrobbler_session");
		editor.remove("scrobbler_subsurl");
		editor.remove("scrobbler_npurl");
		editor.remove("sync_nag");
		editor.remove("sync_nag_cal");
		editor.remove("sync_schema");
		editor.remove("do_full_sync");
		editor.remove("cal_sync_schema");
		editor.remove("cal_do_full_sync");

		editor.apply();

		session = null;

		try {
			LastFmDbHelper.getInstance().clearDatabase();

			if(Build.VERSION.SDK_INT >= 6) {
				AccountAuthenticatorService.removeLastfmAccount(this);
			}
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
