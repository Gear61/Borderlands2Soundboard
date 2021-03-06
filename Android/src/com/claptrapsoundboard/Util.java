package com.claptrapsoundboard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

public class Util
{
	// Parses Character: Quote into its respective components
	public static AudioFile parseData(String quote, String currentCharacter)
	{
		AudioFile returnFile = new AudioFile();
		if (quote.contains(": "))
		{
			returnFile.setCharacter(quote.split(": ")[0].replace(" ", "_"));
			returnFile.setName(quote.split(": ")[1]);
		}
		else
		{
			returnFile.setCharacter(currentCharacter);
			returnFile.setName(quote);
		}
		return returnFile;
	}
	
	public static void setTone(String fileName, Context context, String currentCharacter,
							   String type, AssetManager manager, ContentResolver resolver)
	{
		String oggName = "";
		String toneType = "";
		String errorMessage = "";
		if (type.equals("Borderlands 2 Ringtone"))
		{
			errorMessage = "Apologies! We were unable to set your ringtone.";
			toneType = "ringtone";
			oggName = "borderlands2ringtone.ogg";
		}
		if (type.equals("Borderlands 2 Notification"))
		{
			errorMessage = "Apologies! We were unable to set your notification tone.";
			toneType = "notification tone";
			oggName = "borderlands2notificationtone.ogg";
		}

		File newSoundFile = new File(Environment.getExternalStorageDirectory().getPath() + 
				"/Borderlands2/Ringtones", oggName);
		if (newSoundFile.exists())
		{
			newSoundFile.delete();
		}
		// Try to get contents of .mp3 file to copy over
		InputStream fis;
		try
		{
			fis = manager.open(currentCharacter + "/" + fileName);
		}
		catch (IOException e)
		{
			Util.showDialog(errorMessage, context);
			return;
		}
		
		try
		{
			byte[] readData = new byte[1024];
			FileOutputStream fos = new FileOutputStream(newSoundFile);
			int i = fis.read(readData);

			while (i != -1)
			{
				fos.write(readData, 0, i);
				i = fis.read(readData);
			}
			fos.close();
		}
		catch (IOException io)
		{
			Util.showDialog(errorMessage, context);
			return;
		}
		
		// Set up MediaPlayer to get file duration in ms
		MediaPlayer mp = MediaPlayer.create(context, Uri.parse(Environment.getExternalStorageDirectory().getPath()
				+ "/Borderlands2/Ringtones/" + oggName));

		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, newSoundFile.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, type);
		values.put(MediaStore.MediaColumns.SIZE, newSoundFile.length());
		values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/ogg");
		values.put(MediaStore.Audio.Media.ARTIST, currentCharacter.replace("_", " "));
		values.put(MediaStore.Audio.Media.DURATION, mp.getDuration());
		if (type.equals("Borderlands 2 Ringtone"))
		{
			values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
			values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
		}
		if (type.equals("Borderlands 2 Notification"))
		{
			values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
			values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
		}
		values.put(MediaStore.Audio.Media.IS_ALARM, false);
		values.put(MediaStore.Audio.Media.IS_MUSIC, false);
		
		// Remove all previous incarnations of the Borderlands 2 ringtone
		String where = MediaStore.MediaColumns.TITLE + " = ?";
		String[] args = new String[] {type};
		resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where, args);
				
		// Insert new tone into the database
		Uri uri = MediaStore.Audio.Media.getContentUriForPath(newSoundFile.getAbsolutePath());
		Uri newUri = resolver.insert(uri, values);

		if (type.equals("Borderlands 2 Ringtone"))
		{
			RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri);
		}
		if (type.equals("Borderlands 2 Notification"))
		{
			RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, newUri);
		}
		Util.showDialog("Your " + toneType + " was successfully changed.", context);
	}
	
	public static String addS(int size)
	{
		if (size == 1) {return "";}
		return "s";
	}
	
	public static String verb(int size, boolean pastTense)
	{
		if (pastTense)
		{
			if (size == 1) {return "was";}
			return "were";
		}
		if (size == 1) {return "is";}
		return "are";
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
