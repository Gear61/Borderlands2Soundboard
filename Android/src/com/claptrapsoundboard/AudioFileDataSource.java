package com.claptrapsoundboard;

import java.util.HashMap;
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
	public String[] allColumns = {MySQLiteHelper.COLUMN_NAME, MySQLiteHelper.COLUMN_CHARACTER};

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

		database.insert(MySQLiteHelper.FILES_TABLE_NAME, null, values);
	}
	
	// This does an insert
	public void insertPlayRecord(AudioFile file)
	{
		ContentValues values = new ContentValues();

		values.put(MySQLiteHelper.COLUMN_NAME, file.getName());
		values.put(MySQLiteHelper.COLUMN_CHARACTER, file.getCharacter());
		values.put(MySQLiteHelper.COLUMN_PLAYS, 1);

		database.insert(MySQLiteHelper.PLAYS_TABLE_NAME, null, values);
	}
	
	// If file is favorited, return true, else false
	public boolean isFavorited(AudioFile file)
	{
		if (!tableExists("Favorites"))
		{
			database.execSQL(MySQLiteHelper.FAVORITES_CREATE);
		}
		
		String selection = MySQLiteHelper.COLUMN_NAME + " = ? AND " + 
							 MySQLiteHelper.COLUMN_CHARACTER + " = ?";
		String[] selectionArgs = {file.getName(), file.getCharacter()};
		Cursor cursor = database.query(MySQLiteHelper.FAVORITES_TABLE_NAME, null, selection,
									   selectionArgs, null, null, null);
		
		if (cursor.moveToFirst())
		{
			cursor.close();
			return true;
		}
		cursor.close();
		return false;
	}
	
	// Get number of files user has favorited
	public int numFavorites()
	{
		if (!tableExists("Favorites"))
		{
			database.execSQL(MySQLiteHelper.FAVORITES_CREATE);
		}
		
		String columns[] = {"COUNT (*)"};
		Cursor cursor = database.query(MySQLiteHelper.FAVORITES_TABLE_NAME, columns,
					    null, null, null, null, null);
		
		cursor.moveToFirst();
		return cursor.getInt(0);
	}
	
	// This toggles a file's "favorite" state. TRUE = favorited, FALSE = unfavorited
	public boolean toggleFavorite(AudioFile file)
	{	
		if (isFavorited(file))
		{
			String whereArgs[] = {file.getName(), file.getCharacter()};
			database.delete(MySQLiteHelper.FAVORITES_TABLE_NAME,
					MySQLiteHelper.COLUMN_NAME + " = ? AND " + 
					MySQLiteHelper.COLUMN_CHARACTER + " = ?", whereArgs);
			return false;
		}
		else
		{
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.COLUMN_NAME, file.getName());
			values.put(MySQLiteHelper.COLUMN_CHARACTER, file.getCharacter());
			database.insert(MySQLiteHelper.FAVORITES_TABLE_NAME, null, values);
			return true;
		}
	}
	
	public boolean tableExists(String target)
	{
		open();
		String[] columns = {"name"};
		String selection = "type = 'table' AND name = ?";
		String selectionArgs[] = new String[1];
		if (target.equals("Play Count"))
		{
			selectionArgs[0] = MySQLiteHelper.PLAYS_TABLE_NAME;
		}
		if (target.equals("Favorites"))
		{
			selectionArgs[0] = MySQLiteHelper.FAVORITES_TABLE_NAME;
		}
		Cursor cursor = database.query("sqlite_master", columns, selection, selectionArgs, null, null, null);
		if (cursor.moveToFirst())
		{
			cursor.close();
			return true;
		}
		else
		{
			cursor.close();
			return false;
		}
	}
	
	public void updateCount(AudioFile file, Context context)
	{
		open();
		
		if (!tableExists("Play Count"))
		{
			database.execSQL(MySQLiteHelper.PLAYS_CREATE);
		}
		
		String[] columns = {MySQLiteHelper.COLUMN_PLAYS};
		String selection = MySQLiteHelper.COLUMN_NAME + " = ? AND " + 
							 MySQLiteHelper.COLUMN_CHARACTER + " = ?";
		String[] selectionArgs = {file.getName(), file.getCharacter()};
		Cursor cursor = database.query(MySQLiteHelper.PLAYS_TABLE_NAME, columns, selection,
									   selectionArgs, null, null, null);
		if (cursor.moveToFirst())
		{
			int currentCount = cursor.getInt(0);
			// Update current play count
			String strFilter = MySQLiteHelper.COLUMN_NAME + " = \"" + file.getName()
							   + "\" AND " + MySQLiteHelper.COLUMN_CHARACTER + " = \"" +
							   file.getCharacter() + "\"";
			ContentValues args = new ContentValues();
			args.put(MySQLiteHelper.COLUMN_PLAYS, (currentCount + 1));
			database.update(MySQLiteHelper.PLAYS_TABLE_NAME, args, strFilter, null);
		}
		else
		{
			insertPlayRecord(file);
		}
		Ranking ranking = getRanking();
		String congrats = "Congratulations! You have achieved the rank of <b>" + ranking.getRank() + "</b>!"; 
		if (ranking.getTotalPlays() == 10 || ranking.getTotalPlays() == 100 || ranking.getTotalPlays() == 250
			|| ranking.getTotalPlays() == 500 || ranking.getTotalPlays() == 1000 || ranking.getTotalPlays() == 5000
			|| ranking.getTotalPlays() == 9001)
		{
			Util.showDialog(congrats, context);
		}
		cursor.close();
		close();
	}
	
	public int getCount(AudioFile file)
	{
		open();
		
		if (!tableExists("Play Count"))
		{
			database.execSQL(MySQLiteHelper.PLAYS_CREATE);
		}
		
		String[] columns = {MySQLiteHelper.COLUMN_PLAYS};
		String selection = MySQLiteHelper.COLUMN_NAME + " = ? AND " + 
							 MySQLiteHelper.COLUMN_CHARACTER + " = ?";
		String[] selectionArgs = {file.getName(), file.getCharacter()};
		Cursor cursor = database.query(MySQLiteHelper.PLAYS_TABLE_NAME, columns, selection,
									   selectionArgs, null, null, null);
		
		if (cursor.moveToFirst())
		{
			int count = cursor.getInt(0);
			cursor.close();
			return count;
		}
		else
		{
			cursor.close();
			return 0;
		}
	}
	
	public int getPlaysByCharacter(String character)
	{
		int sum = 0;
		open();
		
		if (!tableExists("Play Count"))
		{
			database.execSQL(MySQLiteHelper.PLAYS_CREATE);
		}
		
		String[] columns = {MySQLiteHelper.COLUMN_PLAYS};
		String selection = MySQLiteHelper.COLUMN_CHARACTER + " = ?";
		String[] selectionArgs = {character};
		Cursor cursor = database.query(MySQLiteHelper.PLAYS_TABLE_NAME, columns, selection, selectionArgs, null, null, null);
		while (cursor.moveToNext())
		{
			sum += cursor.getInt(0);
		}
		cursor.close();
		return sum;
	}
	
	public Cursor getTopTen ()
	{
		open();
		
		if (!tableExists("Play Count"))
		{
			database.execSQL(MySQLiteHelper.PLAYS_CREATE);
		}
		
		String[] columns = {MySQLiteHelper.COLUMN_NAME, MySQLiteHelper.COLUMN_CHARACTER, MySQLiteHelper.COLUMN_PLAYS};
		String orderBy = MySQLiteHelper.COLUMN_PLAYS + " DESC LIMIT 10";
		Cursor cursor = database.query(MySQLiteHelper.PLAYS_TABLE_NAME, columns, null, null, null, null, orderBy);
		return cursor;
	}
	
	public Ranking getRanking()
	{
		int sum = 0;
		open();
		
		if (!tableExists("Play Count"))
		{
			database.execSQL(MySQLiteHelper.PLAYS_CREATE);
		}
		
		String[] columns = {MySQLiteHelper.COLUMN_PLAYS};
		Cursor cursor = database.query(MySQLiteHelper.PLAYS_TABLE_NAME, columns, null, null, null, null, null);
		while (cursor.moveToNext())
		{
			sum += cursor.getInt(0);
		}
		close();
		String ranking = "Claptrap's Minion";
		if (sum >= 10)
		{
			ranking = "Vault Hunter";
		}
		if (sum >= 100)
		{
			ranking = "True Vault Hunter";
		}
		if (sum >= 250)
		{
			ranking = "Ultimate Vault Hunter";
		}
		if (sum >= 500)
		{
			ranking = "Super Awesome Ultra Badass";
		}
		if (sum >= 1000)
		{
			ranking = "Ok, this is getting out of hand";
		}
		if (sum >= 5000)
		{
			ranking = "Seriously?";
		}
		if (sum >= 9001)
		{
			ranking = "MUTHAFUCKIN' SUPA SAIYAN YO";
		}
		cursor.close();
		return new Ranking (ranking, sum);
	}
	
	public HashMap<String, Integer> getCharPlays()
	{
		HashMap<String, Integer> playCounts = new HashMap<String, Integer>();
		for (int i = 0; i < Util.allCharacters.length; i++)
		{
			playCounts.put(Util.allCharacters[i],
						   getPlaysByCharacter(Util.allCharacters[i].replace(" ", "_")));
		}
		return playCounts;
	}

	public void deleteAll()
	{
		open();
		database.delete(MySQLiteHelper.FILES_TABLE_NAME, null, null);
		close();
	}
	
	public Cursor execQuery(String[] columns, String selection, 
							String[] selectionArgs, String groupBy, String having, String orderBy)
	{
		open();
		return database.query(MySQLiteHelper.FILES_TABLE_NAME, columns, selection, selectionArgs,
							  groupBy, having, orderBy);
	}
	
	public Cursor execPlaysQuery(String[] columns, String selection,
								 String[] selectionArgs, String groupBy, String having, String orderBy)
	{
		open();
		return database.query(MySQLiteHelper.PLAYS_TABLE_NAME, columns, selection, selectionArgs,
							  groupBy, having, orderBy);
	}
	
	public Cursor execFavoritesQuery(String[] columns, String selection,
			 String[] selectionArgs, String groupBy, String having, String orderBy)
	{
		open();
		return database.query(MySQLiteHelper.FAVORITES_TABLE_NAME, columns, selection, selectionArgs,
							  groupBy, having, orderBy);
	}
}
