package fm.last.android.db;
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

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import fm.last.android.ScrobbApplication;

public class LastFmDbHelper extends SQLiteOpenHelper {

	/**
	 * The name of the Last.fm database.
	 */
	public static final String DB_NAME = "lastfm";

	/**
	 * The DB's version number.
	 * This needs to be increased on schema changes.
	 */
	public static final int DB_VERSION = 6;

	/**
	 * Singleton instance of {@link ScrobblerQueueDao}.
	 */
	private static LastFmDbHelper instance = null;

	private LastFmDbHelper() {
		super(ScrobbApplication.getInstance().getApplicationContext(), DB_NAME, null, DB_VERSION);
	}

	/**
	 * @return the {@link ScrobblerQueueDao} singleton.
	 */
	public static LastFmDbHelper getInstance() {
		if(instance != null) {
			return instance;
		} else {
			return new LastFmDbHelper();
		}
	}

	public void clearDatabase() {
		ScrobblerQueueDao.getInstance().clearTable();
		TrackDurationCacheDao.getInstance().clearTable();
	}

	/*
	 * (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// create table for scrobbling queue
		// the start time is used as PK because there can be only one track at a time
		db.execSQL("CREATE TABLE IF NOT EXISTS " + ScrobblerQueueDao.DB_TABLE_SCROBBLERQUEUE +
				" (Artist VARCHAR NOT NULL," +
				" Title VARCHAR NOT NULL," +
				" Album VARCHAR NOT NULL," +
				" TrackAuth VARCHAR NOT NULL," +
				" Rating VARCHAR NOT NULL," +
				" StartTime INTEGER NOT NULL PRIMARY KEY," +
				" Duration INTEGER NOT NULL," +
				" PostedNowPlaying INTEGER NOT NULL," +
				" Loved INTEGER NOT NULL," +
				" CurrentTrack INTEGER NOT NULL)");

		// create the table for caching track durations
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TrackDurationCacheDao.DB_TABLE_TRACKDURATIONS +
				" (Artist VARCHAR NOT NULL, " +
				"Title VARCHAR NOT NULL, " +
				"Duration VARCHAR NOT NULL, PRIMARY KEY(Artist, Title))");

	}

	/*
	 * (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// for now we just drop everything and create it again
		db.execSQL("DROP TABLE IF EXISTS " + ScrobblerQueueDao.DB_TABLE_SCROBBLERQUEUE);
		db.execSQL("DROP TABLE IF EXISTS " + TrackDurationCacheDao.DB_TABLE_TRACKDURATIONS);

		onCreate(db);
	}

}
