package com.claptrapsoundboard;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.text.Html;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

public class Util
{
	public static String addS(int size)
	{
		if (size == 1) {return "";}
		return "s";
	}
	
	public static String verb(int size)
	{
		if (size == 1) {return "was";}
		return "were";
	}
	
	public static void createCharRow(TableLayout table, String first, String second,
									 String third, String fourth, boolean bold, Context context)
	{
		TableLayout.LayoutParams params1 = new TableLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.1f);
		params1.setMargins(10, 0, 10, 0); // Left, top, right, bottom
		
		TableLayout.LayoutParams params2 = new TableLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.15f);
		params2.setMargins(10, 0, 10, 0); // Left, top, right, bottom
		
		TableLayout.LayoutParams params3 = new TableLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.13f);
		params2.setMargins(10, 0, 10, 0); // Left, top, right, bottom
		
		TableLayout.LayoutParams params4 = new TableLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.14f);
		params2.setMargins(10, 0, 10, 0); // Left, top, right, bottom
		
		TextView text = new TextView(context);
        text.setText(first);
        text.setLayoutParams(params4);
        
        TextView text2 = new TextView(context);
        text2.setText(second);
        text2.setLayoutParams(params1);
        
        TextView text3 = new TextView(context);
        text3.setText(third);
        text3.setLayoutParams(params3);
        
        TextView text4 = new TextView(context);
        text4.setText(fourth);
        text4.setLayoutParams(params2);
        
        if (bold)
        {
	        text2.setTypeface(null, Typeface.BOLD);
	        text3.setTypeface(null, Typeface.BOLD);
        }
        
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.addView(text);
        layout.addView(text2);
        layout.addView(text3);
        layout.addView(text4);
		
		// add the TableRow to the TableLayout
		table.addView(layout);
	}
	
	public static void createRow(TableLayout table, String first, String second, String third, boolean bold, Context context)
	{
		TableLayout.LayoutParams params1 = new TableLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.1f);
		params1.setMargins(10, 0, 10, 0); // Left, top, right, bottom
		
		TableLayout.LayoutParams params2 = new TableLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.08f);
		params2.setMargins(10, 0, 10, 0); // Left, top, right, bottom
		
		TextView text = new TextView(context);
        text.setText(first);
        text.setLayoutParams(params2);
        
        TextView text2 = new TextView(context);
        text2.setText(second);
        text2.setLayoutParams(params1);
        
        TextView text3 = new TextView(context);
        text3.setText(third);
        text3.setLayoutParams(params1);
        
        if (bold)
        {
	        text.setTypeface(null, Typeface.BOLD);
	        text2.setTypeface(null, Typeface.BOLD);
	        text3.setTypeface(null, Typeface.BOLD);
        }
        
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.addView(text);
        layout.addView(text2);
        layout.addView(text3);
		
		// add the TableRow to the TableLayout
		table.addView(layout);
	}
	
	public static String createCharList (ArrayList<Ranking> rankings)
	{
		String charList = "Character";
		for (int i = 0; i < rankings.size(); i++)
		{
			charList += "<br>" + (i+1) + ". " + rankings.get(i).getRank();
		}
		return charList;
	}
	
	public static String createCharPlays (ArrayList<Ranking> rankings)
	{
		String charPlays = "# of Plays";
		for (int i = 0; i < rankings.size(); i++)
		{
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
