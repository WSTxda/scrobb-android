/**
 *
 */
package fm.last.android.activity;

import android.accounts.AccountAuthenticatorActivity;
import android.os.Bundle;
import android.widget.Toast;

import fm.last.android.R;

/**
 * @author sam
 *
 */
public class AccountFailActivity extends AccountAuthenticatorActivity {

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Toast.makeText(this, R.string.sync_only_one_account, Toast.LENGTH_LONG).show();
		finish();
	}
}
