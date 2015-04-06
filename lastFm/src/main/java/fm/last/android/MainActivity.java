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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import fm.last.android.activity.Preferences;
import fm.last.android.activity.SignUp;
import fm.last.android.utils.AsyncTaskEx;
import fm.last.api.LastFmServer;
import fm.last.api.MD5;
import fm.last.api.Session;
import fm.last.api.SessionInfo;
import fm.last.api.WSError;

public class MainActivity extends Activity {

	public static final String PREFS = "LoginPrefs";

	private boolean mLoginShown;
	private EditText mPassField;
	private EditText mUserField;
	private Button mLoginButton;
	private Button mSignupButton;

	/** Specifies if the user has just signed up */
	private boolean mNewUser = false;
	private LoginTask mLoginTask;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		SharedPreferences settings = getSharedPreferences(PREFS, 0);
		String user = settings.getString("lastfm_user", "");
		String session_key = settings.getString("lastfm_session_key", "");
		String pass;

		if(!user.equals("") && !session_key.equals("")) {
			if(getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_SEARCH)) {
				String query;

				if(getIntent().getStringExtra(SearchManager.QUERY) != null) {
					query = getIntent().getStringExtra(SearchManager.QUERY);
				} else {
					query = getIntent().getData().toString();
				}

				Log.i("LastFm", "Query: " + query);
			} else {
				Intent intent = new Intent(MainActivity.this, Preferences.class);
				startActivity(intent);
				Intent i = new Intent("fr.outadev.lastfm.scrobb.scrobbler.FLUSH");
				sendBroadcast(i);
			}

			finish();
			return;
		}

		setContentView(R.layout.login);
		mPassField = (EditText) findViewById(R.id.password);
		mUserField = (EditText) findViewById(R.id.username);

		if(!user.equals("")) {
			mUserField.setText(user);
		}

		mLoginButton = (Button) findViewById(R.id.sign_in_button);
		mSignupButton = (Button) findViewById(R.id.sign_up_button);
		mUserField.setNextFocusDownId(R.id.password);

		mPassField.setOnKeyListener(new View.OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch(event.getKeyCode()) {
					case KeyEvent.KEYCODE_ENTER:
						mLoginButton.setPressed(true);
						mLoginButton.performClick();
						return true;
				}
				return false;
			}
		});

		if(icicle != null) {
			user = icicle.getString("username");
			pass = icicle.getString("pass");
			if(user != null) {
				mUserField.setText(user);
			}

			if(pass != null) {
				mPassField.setText(pass);
			}
		}

		mLoginButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mLoginTask != null) {
					return;
				}

				String user = mUserField.getText().toString();
				String password = mPassField.getText().toString();

				if(user.length() == 0 || password.length() == 0) {
					ScrobbApplication.getInstance().presentError(v.getContext(), getResources().getString(R.string.ERROR_MISSINGINFO_TITLE),
							getResources().getString(R.string.ERROR_MISSINGINFO));
					return;
				}

				mLoginTask = new LoginTask(v.getContext());
				mLoginTask.execute(user, password);
			}
		});

		mSignupButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SignUp.class);
				startActivityForResult(intent, 0);
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("loginshown", mLoginShown);
		if(mLoginShown) {
			String user = mUserField.getText().toString();
			String password = mPassField.getText().toString();
			outState.putString("username", user);
			outState.putString("password", password);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode != 0 || resultCode != RESULT_OK) {
			return;
		}

		mUserField.setText(data.getExtras().getString("username"));
		mPassField.setText(data.getExtras().getString("password"));
		mNewUser = true;
		mLoginButton.requestFocus();
		mLoginButton.performClick();
	}

	/**
	 * In a task because it can take a while, and Android has a tendency to
	 * panic and show the force quit/wait dialog quickly. And this blocks.
	 */
	private class LoginTask extends AsyncTaskEx<String, Void, Session> {

		Context context;
		ProgressDialog mDialog;
		SessionInfo userSession;

		Exception e;
		WSError wse;

		LoginTask(Context c) {
			this.context = c;
			mLoginButton.setEnabled(false);

			mDialog = ProgressDialog.show(c, "", getString(R.string.main_authenticating), true, false);
			mDialog.setCancelable(true);
		}

		@Override
		public Session doInBackground(String... params) {
			String user = params[0];
			String pass = params[1];

			try {
				return login(user, pass);
			} catch(WSError e) {
				e.printStackTrace();
				wse = e;
			} catch(Exception e) {
				e.printStackTrace();
				this.e = e;
			}

			return null;
		}

		@Override
		public void onPostExecute(Session session) {
			mLoginButton.setEnabled(true);
			mLoginTask = null;

			if(session != null) {
				SharedPreferences.Editor editor = getSharedPreferences(PREFS, 0).edit();
				editor.putString("lastfm_user", session.getName());
				editor.putString("lastfm_session_key", session.getKey());
				editor.putString("lastfm_subscriber", session.getSubscriber());
				editor.putBoolean("remove_playlists", true);
				editor.putBoolean("remove_tags", true);
				editor.putBoolean("remove_loved", true);

				if(userSession != null) {
					editor.putBoolean("lastfm_radio", userSession.getRadio());
					editor.putBoolean("lastfm_freetrial", userSession.getFreeTrial());
					editor.putBoolean("lastfm_expired", userSession.getExpired());
					editor.putInt("lastfm_playsleft", userSession.getPlaysLeft());
					editor.putInt("lastfm_playselapsed", userSession.getPlaysElapsed());
				}

				editor.apply();

				ScrobbApplication.getInstance().session = session;

				Intent i = new Intent(MainActivity.this, Preferences.class);
				startActivity(i);
				finish();
			} else if(wse != null || (e != null && e.getMessage() != null)) {
				AlertDialog.Builder d = new AlertDialog.Builder(MainActivity.this);

				d.setNeutralButton(R.string.common_ok, null);

				if((wse != null && wse.getCode() == WSError.ERROR_AuthenticationFailed) ||
						(e != null && e.getMessage().contains("code 403"))) {
					d.setTitle(getResources().getString(R.string.ERROR_AUTH_TITLE));
					d.setMessage(getResources().getString(R.string.ERROR_AUTH));
					((EditText) findViewById(R.id.password)).setText("");

					d.setNegativeButton(getString(R.string.main_forgotpassword), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							final Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://www.last.fm/settings/lostpassword"));
							startActivity(myIntent);
						}
					});

				} else {
					d.setTitle(getResources().getString(R.string.ERROR_SERVER_UNAVAILABLE_TITLE));
					d.setMessage(getResources().getString(R.string.ERROR_SERVER_UNAVAILABLE));
				}
				d.show();
			}

			if(mDialog.isShowing()) {
				try {
					mDialog.dismiss();
				} catch(Exception e) { //This occasionally fails
					e.printStackTrace();
				}
			}
		}

		Session login(String user, String pass) throws Exception, WSError {
			user = user.toLowerCase().trim();
			LastFmServer server = AndroidLastFmServerFactory.getSecureServer();
			String md5Password = MD5.getInstance().hash(pass);
			String authToken = MD5.getInstance().hash(user + md5Password);
			Session session = server.getMobileSession(user, authToken);

			if(session == null) {
				throw (new WSError("auth.getMobileSession", "auth failure", WSError.ERROR_AuthenticationFailed));
			}

			server = AndroidLastFmServerFactory.getServer();
			userSession = server.getSessionInfo(session.getKey());
			return session;
		}
	}

}
