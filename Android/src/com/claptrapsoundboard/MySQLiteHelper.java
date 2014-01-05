package com.claptrapsoundboard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper
{
	// Table name
	public static final String TABLE_NAME = "AudioFiles";

	// COLUMNS
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_CHARACTER = "character";

	// Some random things fed to a super's method
	private static final String DATABASE_NAME = "borderlands.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_NAME
			+ " TEXT, " + COLUMN_CHARACTER + " TEXT);";
	
	static final String[] indexes = {"CREATE INDEX name_index ON " + TABLE_NAME + " (" + COLUMN_NAME + ");",
									 "CREATE INDEX character_index ON " + TABLE_NAME + " (" + COLUMN_CHARACTER + ");"};
	
	static final String DATABASE_DROP = "DROP TABLE " + TABLE_NAME;
	
	public MySQLiteHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database)
	{
		database.execSQL(DATABASE_CREATE);
		for (int i = 0; i < indexes.length; i++)
		{
			database.execSQL(indexes[i]);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		Log.w(MySQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
}
