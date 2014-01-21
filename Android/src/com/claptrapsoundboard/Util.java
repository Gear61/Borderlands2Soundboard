package com.claptrapsoundboard;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;

public class Util
{
	public static String createCharList (ArrayList<Ranking> rankings)
	{
		String charList = "<b>Character</b>";
		for (int i = 0; i < rankings.size(); i++)
		{
			charList += "<br>" + (i+1) + ". " + rankings.get(i).getRank();
		}
		return charList;
	}
	
	public static String createCharPlays (ArrayList<Ranking> rankings)
	{
		String charPlays = "<b># of Plays</b><br>";
		for (int i = 0; i < rankings.size(); i++)
		{
			if (i == 0)
			{
				charPlays += rankings.get(i).getTotalPlays();
				continue;
			}
			charPlays += "<br>" + rankings.get(i).getTotalPlays();
		}
		return charPlays;
	}
	
	private static String[] allVaultHunters = {"Krieg", "Gaige", "Maya", "Zer0", "Axton", "Salvador"};
	public static String[] getAllVaultHunters ()
	{
		return allVaultHunters;
	}
	
	private static String vaultHunterSearch =
			MySQLiteHelper.COLUMN_CHARACTER + " = ? OR " + MySQLiteHelper.COLUMN_CHARACTER + " = ? OR " +
			MySQLiteHelper.COLUMN_CHARACTER + " = ? OR " + MySQLiteHelper.COLUMN_CHARACTER + " = ? OR " +
			MySQLiteHelper.COLUMN_CHARACTER + " = ? OR " + MySQLiteHelper.COLUMN_CHARACTER + " = ?";
	
	public static String vaultHunterSearch()
	{
		return vaultHunterSearch;
	}
	
	public static String[] allCharacters = {"Claptrap", "Moxxi", "Sir Hammerlock", "Mister Torgue",
											"Handsome Jack", "Tiny Tina", "Scooter", "Gaige", "Krieg",
											"Maya", "Salvador", "Zer0", "Axton"};
	
	public static int count(InputStream filename) throws IOException
	{
		try
		{
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = filename.read(c)) != -1)
			{
				empty = false;
				for (int i = 0; i < readChars; ++i)
				{
					if (c[i] == '\n')
					{
						++count;
					}
				}
			}
			return (count == 0 && !empty) ? 1 : count;
		}
		finally
		{
			filename.close();
		}
	}
	
	public static void showDialog(String message, Context context)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

		// set dialog message
		alertDialogBuilder.setMessage(Html.fromHtml(message)).setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						// if this button is clicked, close
						// current activity
						dialog.cancel();
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}
}
