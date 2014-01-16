package com.claptrapsoundboard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper
{
	// Table name
	public static final String FILES_TABLE_NAME = "AudioFiles";
	public static final String PLAYS_TABLE_NAME = "PlayCount";

	// COLUMNS
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_CHARACTER = "character";
	public static final String COLUMN_PLAYS = "numPlays";

	// Some random things fed to a super's method
	private static final String DATABASE_NAME = "borderlands.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statements
	static final String FILE_CREATE = "CREATE TABLE " + FILES_TABLE_NAME + "(" + COLUMN_NAME
									  + " TEXT, " + COLUMN_CHARACTER + " TEXT);";
	static final String PLAYS_CREATE = "CREATE TABLE " + PLAYS_TABLE_NAME + "(" + COLUMN_NAME
									   + " TEXT, " + COLUMN_CHARACTER + " TEXT, " + COLUMN_PLAYS
									   + " INT);";
	
	static final String[] indexes = {"CREATE INDEX name_index ON " + FILES_TABLE_NAME + " (" + COLUMN_NAME + ");",
									 "CREATE INDEX character_index ON " + FILES_TABLE_NAME + " (" + COLUMN_CHARACTER + ");"};
	
	static final String DATABASE_DROP = "DROP TABLE " + FILES_TABLE_NAME;
	
	public MySQLiteHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database)
	{
		database.execSQL(FILE_CREATE);
		for (int i = 0; i < indexes.length; i++)
		{
			database.execSQL(indexes[i]);
		}
		database.execSQL(PLAYS_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		Log.w(MySQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + FILES_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + PLAYS_TABLE_NAME);
		onCreate(db);
	}
}
