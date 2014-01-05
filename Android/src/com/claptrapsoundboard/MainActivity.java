package com.claptrapsoundboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity
{
	final Context context = this;
	private AudioFileDataSource datasource = new AudioFileDataSource(context);
	private int prg = 0;
	private TextView tv;
	private ProgressBar pb;
	private String currentCharacter;
	
	MediaPlayer player = new MediaPlayer();
	Map<String, ArrayList<Spanned>> cache = new HashMap<String, ArrayList<Spanned>>();
	
	private int fileSize = 0;
	final public int getFileSize()
	{
		return fileSize;
	}

	private ArrayList<String> listAssetFiles(String path)
	{
		String[] list;
		try
		{
			list = getAssets().list(path);
			if (list.length > 0)
			{
				return new ArrayList<String>(Arrays.asList(list));
			}
			else
			{
				Util.showDialog("Setting up your soundbites database failed.", context);
				return null;
			}
		}
		catch (IOException e)
		{
			Util.showDialog("Setting up your soundbites database failed.", context);
		}
		return null;
	}
	
	public boolean killKeyboard()
	{
		View view = this.getWindow().getDecorView().findViewById(android.R.id.content);
		Rect r = new Rect();
		view.getWindowVisibleDisplayFrame(r);

		int heightDiff = view.getRootView().getHeight() - (r.bottom - r.top);
		if (heightDiff > 100)
		{
			InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Leave in to trigger a reboot
		// datasource.deleteAll();

		final AssetManager assetManager = getAssets();
		final ArrayList<String> listFiles = listAssetFiles("lists");

		// Run a SQL query on the number of rows in the user's card database
		String[] columns = {"COUNT (*)"};
		Cursor cursor = datasource.execQuery(columns, null, null, null, null, null);
		cursor.moveToFirst();
		int dbSize = cursor.getInt(0);

		// Get number of cards represented in audio files
		try
		{
			for (int i = 0; i < listFiles.size(); i++)
			{
				fileSize += Util.count(assetManager.open("lists/" + listFiles.get(i)));
			}
		}
		catch (IOException e1)
		{
			Util.showDialog("Setting up your soundbites database failed.", context);
			return;
		}

		int layoutId = 0;
		// Sum up number of audio files represented in list files and compare to database record amount
		// If these quantities are different, switch to the initialization page and start importing files
		if (getFileSize() != dbSize)
		{
			getActionBar().hide();
			layoutId = R.layout.progress;
		}
		else
		{
			layoutId = R.layout.activity_main;
		}
		setContentView(layoutId);

		if (getFileSize() != dbSize)
		{
			datasource.deleteAll();
			pb = (ProgressBar) findViewById(R.id.pbId);
			pb.setMax(getFileSize());
			tv = (TextView) findViewById(R.id.tvId);
			
			Runnable myThread = new Runnable()
			{
				@SuppressLint("InlinedApi")
				@Override
				public void run()
				{
					// Reset progress from older runs
					if (prg != 0) {prg = 0;}
					// Import soundbites into SQLite database
					String line;
					String character;
					for (int i = 0; i < listFiles.size(); i++)
					{
						datasource.open();
						datasource.beginTransaction();
						character = listFiles.get(i).split(".txt")[0];
						try
						{
							BufferedReader in = new BufferedReader(new InputStreamReader(assetManager.open("lists/"
									+ listFiles.get(i))));
							while ((line = in.readLine()) != null)
							{
								AudioFile tempFile = new AudioFile(line, character);
								datasource.insertFile(tempFile);
								hnd.sendMessage(hnd.obtainMessage());
							}
							// Close the BufferedReader after using it
							try
							{
								in.close();
							}
							catch (IOException e)
							{
								Util.showDialog("Setting up your soundbites database failed.", context);
								return;
							}
						}
						catch (IOException e)
						{
							Util.showDialog("Setting up your soundbites database failed.", context);
						}
						finally
						{
							datasource.performTransaction();
							datasource.close();
						}
					}

					runOnUiThread(new Runnable()
					{
						public void run()
						{
							tv.setText("All audio files successfully imported.");
							Util.showDialog("All audio files successfully imported.", context);
							setContentView(R.layout.activity_main);
							getActionBar().show();
						};
					});
				}

				@SuppressLint("HandlerLeak")
				Handler hnd = new Handler()
				{
					@Override
					public void handleMessage(Message msg)
					{
						prg++;
						pb.setProgress(prg);

						String perc = String.valueOf(prg).toString();
						tv.setText(perc + "/" + String.valueOf(getFileSize()) + " audio files imported.");
					}
				};
			};
			new Thread(myThread).start();
		}
	}
	
	public void setUpAudioList ()
	{
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.audioview);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		TextView title = (TextView) findViewById(R.id.title);
		title.setText((currentCharacter.replace("_", " ")) + " Quotes");
		
		final ListView listview = (ListView) findViewById(R.id.characterSearchResults);

		// clear previous results in the LV
		listview.setAdapter(null);
		
		ArrayList<Spanned> fileList= cache.get(currentCharacter);
		// If we haven't loaded this character's list before, create it and add it to the hashmap
		if (fileList == null)
		{
			fileList = new ArrayList<Spanned> ();
				
			// Get all sound bites associated with character
			String[] columns = {MySQLiteHelper.COLUMN_NAME};
			String whereClause = "character = ?";
			String[] whereArgs = new String[] {currentCharacter};
			Cursor cursor = datasource.execQuery(columns, whereClause, whereArgs, null, null, null);
			while(cursor.moveToNext())
			{
				fileList.add(Html.fromHtml(cursor.getString(0)));
			}
			cache.put(currentCharacter, fileList);
		}
		
		FileAdapter lvAdapter = new FileAdapter(context, fileList);
		listview.setAdapter(lvAdapter);
		
		listview.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) throws IllegalArgumentException, IllegalStateException
			{
				// Account for the empty placeholder element at the top
				if (((Spanned) parent.getItemAtPosition(position)).toString().trim().equals(""))
				{
					return;
				}
				player.reset();
				AssetFileDescriptor afd = null;
				try
				{
					String file = currentCharacter + "/" + ((Spanned) parent.getItemAtPosition(position)).toString().trim() + ".mp3";
					afd = getAssets().openFd(file);
				}
				catch (IOException e)
				{
					Util.showDialog("We are unable to play the requested audio file.", context);
					return;
				}
				try
				{
					player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
				}
				catch (IOException e1)
				{
					Util.showDialog("We are unable to play the requested audio file.", context);
					e1.printStackTrace();
				}
				try
				{
					player.prepare();
				}
				catch (IOException e)
				{
					Util.showDialog("We are unable to play the requested audio file.", context);
					e.printStackTrace();
				}
			    player.start();
			}
		});
		TextView list_message = (TextView) findViewById(R.id.message);
		String stats = "This app currently has " + fileList.size() + " sound bites associated with <b>" + currentCharacter.replace("_", " ")
				+ "</b>. Simply click on a file to hear it. Search for nothing to bring back this full, original list of files.";
		list_message.setText(Html.fromHtml(stats));
		
		// Set up auto-complete
		ArrayList<String> damnit = new ArrayList<String>();
		for (int i = 0; i < fileList.size(); i++)
		{
			damnit.add(fileList.get(i).toString());
		}
		
		AutoCompleteTextView cardSearch = (AutoCompleteTextView)findViewById(R.id.search_box);
		TextAdapter adapter = new TextAdapter(context, android.R.layout.simple_dropdown_item_1line, damnit);
		cardSearch.setAdapter(adapter);
	}
	
	public void prepareClaptrap (View view)
	{
		currentCharacter = "Claptrap";
		setUpAudioList();
	}
	
	public void prepareTorgue (View view)
	{
		currentCharacter = "Mister_Torgue";
		setUpAudioList();
	}
	
	public void prepareMoxxi (View view)
	{
		currentCharacter = "Moxxi";
		setUpAudioList();
	}
	
	public void prepareTina (View view)
	{
		currentCharacter = "Tiny_Tina";
		setUpAudioList();
	}
	
	public void narrowResults(View view)
	{
		killKeyboard();
		
		// Grab listview
		final ListView listview = (ListView) findViewById(R.id.characterSearchResults);
		// clear previous results in the LV
		listview.setAdapter(null);
		
		// Get contents of textbox. Check it
		EditText criteria = (EditText) findViewById(R.id.search_box);
		String quoteName = criteria.getText().toString();
		
		ArrayList<Spanned> fileList = new ArrayList<Spanned>();
		// Get all sound bites associated with character
		String[] columns =
		{ MySQLiteHelper.COLUMN_NAME };
		String whereClause = "name LIKE ? COLLATE NOCASE AND character = ?";
		String[] whereArgs = new String[] {"%" + quoteName + "%", currentCharacter};
		Cursor cursor = datasource.execQuery(columns, whereClause, whereArgs, null, null, null);
		while (cursor.moveToNext())
		{
			fileList.add(Html.fromHtml(cursor.getString(0)));
		}
		
		FileAdapter lvAdapter = new FileAdapter(context, fileList);
		listview.setAdapter(lvAdapter);
		
		listview.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) throws IllegalArgumentException, IllegalStateException
			{
				// Account for the empty placeholder element at the top
				if (((Spanned) parent.getItemAtPosition(position)).toString().trim().equals(""))
				{
					return;
				}
				player.reset();
				AssetFileDescriptor afd = null;
				try
				{
					String file = currentCharacter + "/" + ((Spanned) parent.getItemAtPosition(position)).toString().trim() + ".mp3";
					afd = getAssets().openFd(file);
				}
				catch (IOException e)
				{
					Util.showDialog("We are unable to play the requested audio file.", context);
					return;
				}
				try
				{
					player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
				}
				catch (IOException e1)
				{
					Util.showDialog("We are unable to play the requested audio file.", context);
					e1.printStackTrace();
				}
				try
				{
					player.prepare();
				}
				catch (IOException e)
				{
					Util.showDialog("We are unable to play the requested audio file.", context);
					e.printStackTrace();
				}
			    player.start();
			}
		});
		TextView list_message = (TextView) findViewById(R.id.message);
		String stats;
		if (fileList.size() == 0)
		{
			stats = "No audio files were found for your given search criteria.";
		}
		else if (quoteName.trim().equals(""))
		{
			stats = "This app currently has " + fileList.size() + " sound bites associated with <b>" + currentCharacter.replace("_", " ")
					+ "</b>.";
		}
		else
		{
			stats = Integer.toString(fileList.size()) + " audio files were found for your given parameters.";
		}
		list_message.setText(Html.fromHtml(stats));
		criteria.setText("");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			// I use the home button as a derpy back button
			case android.R.id.home:
				killKeyboard();
				setContentView(R.layout.activity_main);
				getActionBar().setDisplayHomeAsUpEnabled(false);
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
