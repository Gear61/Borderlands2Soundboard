package com.claptrapsoundboard;

import java.io.IOException;
import java.io.InputStream;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;

public class Util
{
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
