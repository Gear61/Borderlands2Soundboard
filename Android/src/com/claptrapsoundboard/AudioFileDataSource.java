package com.claptrapsoundboard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class AudioFileDataSource
{
	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	public String[] allColumns =
	{ MySQLiteHelper.COLUMN_NAME, MySQLiteHelper.COLUMN_CHARACTER};

	// Constructor
	public AudioFileDataSource(Context context)
	{
		dbHelper = new MySQLiteHelper(context);
	}

	// Open connection to database
	public void open() throws SQLException
	{
		database = dbHelper.getWritableDatabase();
	}

	// Terminate connection to database
	public void close()
	{
		dbHelper.close();
	}
	
	public void beginTransaction()
	{
		database.beginTransaction();
	}
	
	public void performTransaction()
	{
		database.setTransactionSuccessful();
		database.endTransaction();
	}

	// This does an insert
	public void insertFile(AudioFile file)
	{
		ContentValues values = new ContentValues();

		values.put(MySQLiteHelper.COLUMN_NAME, file.getName());
		values.put(MySQLiteHelper.COLUMN_CHARACTER, file.getCharacter());

		database.insert(MySQLiteHelper.TABLE_NAME, null, values);
	}

	public void deleteAll()
	{
		open();
		database.delete(MySQLiteHelper.TABLE_NAME, null, null);
		close();
	}
	
	public Cursor execQuery(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)
	{
		open();
		return database.query(MySQLiteHelper.TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy);
	}
}
