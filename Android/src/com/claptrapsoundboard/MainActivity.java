package com.claptrapsoundboard;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.text.Html;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity
{
	final Context context = this;
	private AudioFileDataSource datasource = new AudioFileDataSource(context);
	private int prg = 0;
	private TextView tv;
	private ProgressBar pb;
	private static String currentCharacter = "";
	private int lastKnownFavoriteCount = 0;
	MediaPlayer player = new MediaPlayer();
	Map<String, ArrayList<Spanned>> cache = new HashMap<String, ArrayList<Spanned>>();
	ArrayList<Spanned> allFavorites = new ArrayList<Spanned> ();
	
	private int fileSize = 0;
	final public int getFileSize()
	{
		return fileSize;
	}
	
	private static AssetManager manager = null;
	public void setAssetManager()
	{
		manager = getAssets();
	}
	
	private static ContentResolver resolver = null;
	public void setResolver()
	{
		resolver = getContentResolver();
	}
	
	// Confirm app close on back button press
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage("Would you like to exit the app?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							// Stop the activity
							MainActivity.this.finish();
						}
					}).setNegativeButton("No", null).show();
		}
		// In case another button was pressed, the super method shall be called
		else
			return super.onKeyDown(keyCode, event);
		return false;
	}
	
	public static void toneConfirmation(String message, final String fileName, final Context context,
										final String character)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

		// set dialog message
		alertDialogBuilder.setMessage(Html.fromHtml(message)).setCancelable(false)
				.setNegativeButton("Ringtone", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						dialog.cancel();
						Util.setTone(fileName, context, character,
								     "Borderlands 2 Ringtone", manager, resolver);
					}
				}).setNeutralButton("Notification Tone", new DialogInterface.OnClickListener()
				{
					public void onClick(final DialogInterface dialog, int id)
					{
						dialog.cancel();
						Util.setTone(fileName, context, character,
							     	 "Borderlands 2 Notification", manager, resolver);
					}
				}).setPositiveButton("Neither", new DialogInterface.OnClickListener()
				{
					public void onClick(final DialogInterface dialog, int id)
					{
						dialog.dismiss();
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
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
		
		// Create folder in external storage for us to store things in
		// Check if SD card is mounted
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
		{
			File Dir = new File(android.os.Environment.getExternalStorageDirectory(), "Borderlands2/Ringtones");
			if (!Dir.exists()) // if directory is not here
			{
				Dir.mkdirs(); // make directory
			}
		}
		
		// Leave in to trigger a reboot
		// datasource.deleteAll();

		// Initiate AssetManager and ContentResolver objects
		if (manager == null)
		{
			setAssetManager();
		}
		if (resolver == null)
		{
			setResolver();
		}
		
		final ArrayList<String> listFiles = listAssetFiles("lists");

		// Run a SQL query on the number of rows in the user's card database
		String[] columns = {"COUNT (*)"};
		Cursor cursor = datasource.execQuery(columns, null, null, null, null, null);
		cursor.moveToFirst();
		int dbSize = cursor.getInt(0);
		cursor.close();

		// Get number of cards represented in audio files
		try
		{
			for (int i = 0; i < listFiles.size(); i++)
			{
				fileSize += Util.count(manager.open("lists/" + listFiles.get(i)));
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
							BufferedReader in = new BufferedReader(new InputStreamReader(manager.open("lists/"
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
			cursor.close();
		}
		
		FileAdapter lvAdapter = new FileAdapter(context, fileList, currentCharacter, datasource);
		listview.setAdapter(lvAdapter);
		
		listview.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) throws IllegalArgumentException, IllegalStateException
			{
				player.reset();
				AssetFileDescriptor afd = null;
				String fileName = ((Spanned) parent.getItemAtPosition(position)).toString().trim();
				String file = currentCharacter + "/" + fileName + ".mp3";
				
				AudioFile tempFile = new AudioFile(fileName, currentCharacter);
				datasource.updateCount(tempFile, context);
				try
				{
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
		
		listview.setOnItemLongClickListener(new OnItemLongClickListener()
		{
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id)
            {
            	String fileName = ((Spanned) parent.getItemAtPosition(position)).toString().trim();
            	String message = "Set the quote <i>\"" + fileName + "\"</i> by <b>"
            					 + currentCharacter.replace("_", " ") + "</b> as my:";
            	toneConfirmation(message, fileName + ".mp3", context, currentCharacter);
                return true;
            }
        });
		
		TextView list_message = (TextView) findViewById(R.id.message);
		String stats = "This app currently has <b>" + fileList.size() + "</b> sound bites associated with <b>" + currentCharacter.replace("_", " ")
				+ "</b>. Simply click on a file to hear it.";
		list_message.setText(Html.fromHtml(stats));
		
		AutoCompleteTextView cardSearch = (AutoCompleteTextView)findViewById(R.id.search_box);
		@SuppressWarnings("unchecked")
		TextAdapter adapter = new TextAdapter(context, android.R.layout.simple_dropdown_item_1line,
											  (ArrayList<Spanned>) fileList.clone());
		cardSearch.setAdapter(adapter);
	}
	
	// MOVING AROUND FUNCTIONS
	public void allChars (View view)
	{
		setContentView(R.layout.characters);
		currentCharacter = "All Chars";
		getActionBar().setDisplayHomeAsUpEnabled(true);
		invalidateOptionsMenu();
	}
	
	public void prepareVaultHunters (View view)
	{
		setContentView(R.layout.vaulthunters);
		currentCharacter = "Vault Hunters";
	}
	
	public void about (View view)
	{
		setContentView(R.layout.about);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	public void goToOverview (View view)
	{
		setContentView(R.layout.overview);
		// Set up ranking
		Ranking myRanking = datasource.getRanking();
		TextView ranking = (TextView) findViewById(R.id.ranking);
		String report = "<b>Total Plays: </b>" + myRanking.getTotalPlays() + "<br>";
		report += "<b>Rank: </b>" + myRanking.getRank();
		ranking.setText(Html.fromHtml(report));
		
		// Set up character by plays
		HashMap<String, Integer> listByPlays = datasource.getCharPlays();
		ArrayList<Ranking> rankings = new ArrayList<Ranking>();
		
		Iterator<Entry<String, Integer>> it = listByPlays.entrySet().iterator();
	    while (it.hasNext())
	    {
	        Map.Entry <String, Integer> pairs = (Entry<String, Integer>)it.next();
	        rankings.add(new Ranking(pairs.getKey(), pairs.getValue()));
	    }
		
		Collections.sort(rankings, new Comparator<Ranking>()
		{
		    public int compare(Ranking o1, Ranking o2)
		    {
		        if (o1.getTotalPlays() > o2.getTotalPlays())
		        {
		        	return -1;
		        }
		        else if (o1.getTotalPlays() < o2.getTotalPlays())
		        {
		        	return 1;
		        }
		        return 0;
		    }
		});
		
		TableLayout charTable = (TableLayout) findViewById(R.id.character_layout);
		String charList[] = Util.createCharList(rankings).split("<br>");
		String charPlays[] = Util.createCharPlays(rankings).split("<br>");
		for (int i = 0; i < charList.length; i++)
		{
			if (i == 0)
			{
				Util.createCharRow(charTable, "", charList[i], charPlays[i], "", true, context);
			}
			else
			{
				Util.createCharRow(charTable, "", charList[i], charPlays[i], "", false, context);
			}
		}
		
		// Set up Top 10; get a reference for the TableLayout
		TableLayout table = (TableLayout) findViewById(R.id.my_table_layout);
		Util.createRow(table, "Quote", "Character", "# of Plays", true, context);
		
		Cursor cursor = datasource.getTopTen();
		int i = 0;
		for(i = 0; cursor.moveToNext(); i++)
		{
			Util.createRow(table, (i+1) + ". " + cursor.getString(0), cursor.getString(1).replace("_", " "),
					  Integer.toString(cursor.getInt(2)), false, context);
		}
		cursor.close();
		
		if (i < 10)
		{
			TextView complain = (TextView) findViewById(R.id.complain);
			complain.setText("Your Top 10 looks a little... empty there buddy.");
		}
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	public void showFavorites (View view)
	{
		currentCharacter = "Favorites";
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.favorites);
		invalidateOptionsMenu();
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		TextView title = (TextView) findViewById(R.id.title_favorites);
		title.setText("Favorites");
		int currentFavoriteCount = datasource.numFavorites();
		
		final ListView listview = (ListView) findViewById(R.id.favorites_list);

		// clear previous results in the LV
		listview.setAdapter(null);
		
		if (allFavorites.size() == 0 || lastKnownFavoriteCount != currentFavoriteCount)
		{
			allFavorites.clear();
			
			datasource.open();
			String[] columns = {MySQLiteHelper.COLUMN_NAME, MySQLiteHelper.COLUMN_CHARACTER};
			Cursor cursor = datasource.execFavoritesQuery(columns, null, null, null, null, null);
			while(cursor.moveToNext())
			{
				String addition = "<b>" + cursor.getString(1).replace("_", " ") + ": </b>" + cursor.getString(0);
				allFavorites.add(Html.fromHtml(addition));
			}
			lastKnownFavoriteCount = currentFavoriteCount;
			cursor.close();
		}
		
		FileAdapter lvAdapter = new FileAdapter(context, allFavorites, currentCharacter, datasource);
		listview.setAdapter(lvAdapter);
		
		listview.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) throws IllegalArgumentException, IllegalStateException
			{
				player.reset();
				AssetFileDescriptor afd = null;
				
				String fileName = ((Spanned) parent.getItemAtPosition(position)).toString().trim();
				AudioFile tempFile = Util.parseData(fileName, currentCharacter);
				String file = tempFile.getCharacter() + "/" + tempFile.getName() + ".mp3";
				
				datasource.updateCount(tempFile, context);
				try
				{
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
		
		listview.setOnItemLongClickListener(new OnItemLongClickListener()
		{
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id)
            {
            	String fileName = ((Spanned) parent.getItemAtPosition(position)).toString().trim();
            	AudioFile tempFile = Util.parseData(fileName, currentCharacter);
            	String message = "Set the quote <i>\"" + tempFile.getName() + "\"</i> by <b>"
            					 + tempFile.getCharacter().replace("_", " ") + "</b> as my:";
            	toneConfirmation(message, tempFile.getName() + ".mp3", context,
            					 tempFile.getCharacter().replace("_", " "));
                return true;
            }
        });
		
		TextView list_message = (TextView) findViewById(R.id.favorites_message);
		String stats = "Here are your <b>" + currentFavoriteCount + "</b> favorites."; 
		list_message.setText(Html.fromHtml(stats));
		
		AutoCompleteTextView cardSearch = (AutoCompleteTextView)findViewById(R.id.search_favorites);
		@SuppressWarnings("unchecked")
		TextAdapter adapter = new TextAdapter(context, android.R.layout.simple_dropdown_item_1line,
											  (ArrayList<Spanned>) allFavorites.clone());
		cardSearch.setAdapter(adapter);
	}
	
	// LIST PREPARATION
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
	
	public void prepareHammerlock (View view)
	{
		currentCharacter = "Sir_Hammerlock";
		setUpAudioList();
	}
	
	public void prepareHandsomeJack (View view)
	{
		currentCharacter = "Handsome_Jack";
		setUpAudioList();
	}
	
	public void prepareScooter (View view)
	{
		currentCharacter = "Scooter";
		setUpAudioList();
	}
	
	// VAULT HUNTERS
	public void prepareKrieg (View view)
	{
		currentCharacter = "Krieg";
		setUpAudioList();
	}
	
	public void prepareSalvador (View view)
	{
		currentCharacter = "Salvador";
		setUpAudioList();
	}
	
	public void prepareMaya (View view)
	{
		currentCharacter = "Maya";
		setUpAudioList();
	}
	
	public void prepareAxton (View view)
	{
		currentCharacter = "Axton";
		setUpAudioList();
	}
	
	public void prepareZer0 (View view)
	{
		currentCharacter = "Zer0";
		setUpAudioList();
	}
	
	public void prepareGaige (View view)
	{
		currentCharacter = "Gaige";
		setUpAudioList();
	}
	
	public void randomFile () throws IOException
	{
		String columns[] = {MySQLiteHelper.COLUMN_NAME, MySQLiteHelper.COLUMN_CHARACTER};
		String selection = null;
		String selectionArgs[] = null;
		if (currentCharacter.equals("Vault Hunters"))
		{
			selection = Util.vaultHunterSearch();
			selectionArgs = Util.getAllVaultHunters();
		}
		else if (!currentCharacter.equals("All Chars"))
		{
			selection = MySQLiteHelper.COLUMN_CHARACTER + " = ?";
			selectionArgs = new String[] {currentCharacter};
		}
		Cursor cursor;
		if (currentCharacter.equals("Favorites"))
		{
			cursor = datasource.execFavoritesQuery(columns, null, null, null, null, "RANDOM() LIMIT 1");
		}
		else
		{
			cursor = datasource.execQuery(columns, selection, selectionArgs, null, null, "RANDOM() LIMIT 1");
		}
		
		String file = "";
		if (cursor.moveToNext())
		{
			// Update count
			AudioFile tempFile = new AudioFile(cursor.getString(0), cursor.getString(1));
			datasource.updateCount(tempFile, context);
			file = cursor.getString(1) + "/" + cursor.getString(0).trim() + ".mp3";
		}
		else
		{
			Util.showDialog("Random soundbite functionality failed.", context);
			cursor.close();
			return;
		}
		cursor.close();
		player.reset();
		AssetFileDescriptor afd = null;
		afd = getAssets().openFd(file);
		player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
		player.prepare();
		player.start();
	}
	
	// Search functions
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
		String[] columns = { MySQLiteHelper.COLUMN_NAME };
		String whereClause = "name LIKE ? COLLATE NOCASE AND character = ?";
		String[] whereArgs = new String[] {"%" + quoteName + "%", currentCharacter};
		Cursor cursor = datasource.execQuery(columns, whereClause, whereArgs, null, null, null);
		while (cursor.moveToNext())
		{
			fileList.add(Html.fromHtml(cursor.getString(0)));
		}
		cursor.close();
		
		FileAdapter lvAdapter = new FileAdapter(context, fileList, currentCharacter, datasource);
		listview.setAdapter(lvAdapter);
		
		TextView list_message = (TextView) findViewById(R.id.message);
		String stats;
		if (fileList.size() == 0)
		{
			stats = "No audio files were found for your given search criteria.";
		}
		else if (quoteName.trim().equals(""))
		{
			stats = "This app currently has <b>" + fileList.size() + 
					"</b> sound bites associated with <b>" + currentCharacter.replace("_", " ") + "</b>.";
		}
		else
		{
			stats = "<b>" + Integer.toString(fileList.size()) + "</b> audio file" +
				    Util.addS(fileList.size()) + " " + Util.verb(fileList.size(), true) + 
				    " found for your given parameters.";
		}
		list_message.setText(Html.fromHtml(stats));
		criteria.setText("");
	}
	
	public void narrowFavorites(View view)
	{
		killKeyboard();
		
		// Get contents of textbox. Check it
		EditText criteria = (EditText) findViewById(R.id.search_favorites);
		String quoteName = criteria.getText().toString();
		
		// Grab listview
		final ListView listview = (ListView) findViewById(R.id.favorites_list);
		// clear previous results in the LV
		listview.setAdapter(null);
		
		ArrayList<Spanned> fileList = new ArrayList<Spanned>();
		for (int i = 0; i < allFavorites.size(); i++)
		{
			if (allFavorites.get(i).toString().toLowerCase().contains(quoteName.toLowerCase()))
			{
				fileList.add(allFavorites.get(i));
			}
		}
		
		FileAdapter lvAdapter = new FileAdapter(context, fileList, currentCharacter, datasource);
		listview.setAdapter(lvAdapter);
		
		TextView list_message = (TextView) findViewById(R.id.favorites_message);
		String stats;
		if (fileList.size() == 0)
		{
			stats = "No audio files were found for your given search criteria.";
		}
		else if (quoteName.trim().equals(""))
		{
			stats = "Here " + Util.verb(fileList.size(), false) + " your <b>" + fileList.size() +
					"</b> favorite" + Util.addS(fileList.size()) + ".";
		}
		else
		{
			stats = "<b>" + Integer.toString(fileList.size()) + "</b> audio file" + Util.addS(fileList.size()) +
					" " + Util.verb(fileList.size(), true) + " found for your given parameters.";
		}
		list_message.setText(Html.fromHtml(stats));
		criteria.setText("");
	}

	// Action Bar functions
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem random = menu.findItem(R.id.random);
		if (!currentCharacter.equals(""))
		{
			random.setVisible(true);
		}
		else
		{
			random.setVisible(false);
		}
		super.onPrepareOptionsMenu(menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			// I use the home button as a derpy back button
			case android.R.id.home:
				killKeyboard();
				if (currentCharacter.equals("") || currentCharacter.equals("All Chars")
					|| currentCharacter.equals("Favorites"))
				{
					currentCharacter = "";
					setContentView(R.layout.activity_main);
					getActionBar().setDisplayHomeAsUpEnabled(false);
				}
				else if (currentCharacter.equals("Krieg") || currentCharacter.equals("Maya")
						 || currentCharacter.equals("Zer0") || currentCharacter.equals("Salvador")
						 || currentCharacter.equals("Axton") ||currentCharacter.equals("Gaige"))
				{
					killKeyboard();
					currentCharacter = "Vault Hunters";
					setContentView(R.layout.vaulthunters);
				}
				// We're on a character page
				else
				{
					killKeyboard();
					currentCharacter = "All Chars";
					setContentView(R.layout.characters);
				}
				break;
			case R.id.silence:
				player.stop();
				break;
			case R.id.random:
				try
				{
					randomFile();
				}
				catch (IOException e)
				{
					Util.showDialog("The random button failed.", context);
				}
				break;
			default:
				break;
		}
		invalidateOptionsMenu();
		return super.onOptionsItemSelected(item);
	}
}
