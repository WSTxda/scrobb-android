/***************************************************************************
 *   Copyright 2015 Baptiste Candellier - outa[dev]                        *
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

package fm.last.android.scrobbler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;

import fm.last.android.ScrobbApplication;
import fm.last.android.db.ScrobblerQueueDao;
import fm.last.api.Session;

/**
 * @author sam
 *
 */
public class MusicIntentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Session s = ScrobbApplication.getInstance().session;
		if(s != null && s.getKey().length() > 0 && PreferenceManager.getDefaultSharedPreferences(ScrobbApplication.getInstance()).getBoolean("scrobble", true)) {
			if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				if(ScrobblerQueueDao.getInstance().getQueueSize() < 1) {
					return;
				}
			}
			final Intent out = new Intent(context, ScrobblerService.class);
			out.setAction(intent.getAction());
			out.putExtras(intent);
			context.startService(out);
		}
	}
}
