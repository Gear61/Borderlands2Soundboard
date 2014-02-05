package com.claptrapsoundboard;

import java.util.List;

import android.content.Context;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileAdapter extends BaseAdapter /* implements OnClickListener */
{
	private Context context;
	private List<Spanned> fileList;
	private String currentCharacter;
	private AudioFileDataSource datasource;

	public FileAdapter(Context context, List<Spanned> fileList,
					   String currentCharacter, AudioFileDataSource datasource)
	{
		this.context = context;
		this.fileList = fileList;
		this.currentCharacter = currentCharacter;
		this.datasource = datasource;
	}

	public int getCount()
	{
		return fileList.size();
	}

	public Object getItem(int position)
	{
		return fileList.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}

	public static class ViewHolder
    {
        public ImageView item1;
        public TextView item2;
    }
    
    public View getView(int position, View convertView, ViewGroup parent)
	{
    	View v = convertView;
        final ViewHolder holder;
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.file_item, null);
            holder = new ViewHolder();
            holder.item1 = (ImageView) v.findViewById(R.id.star);
            holder.item2 = (TextView) v.findViewById(R.id.file);
            v.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)v.getTag();
        }
 
        final Spanned file = fileList.get(position);
        if (file != null)
        {
        	AudioFile tempFile = Util.parseData(file.toString(), currentCharacter);
        	if (datasource.isFavorited(tempFile))
        	{
        		holder.item1.setImageResource(R.drawable.star);
        	}
        	else
        	{
        		holder.item1.setImageResource(R.drawable.unstar);
        	}
        	holder.item1.setOnClickListener(new OnClickListener()
        	{
				@Override
				public void onClick(View v)
				{
					AudioFile tempFile = Util.parseData(file.toString(), currentCharacter);
					if (datasource.toggleFavorite(tempFile))
					{
						holder.item1.setImageResource(R.drawable.star);
					}
					else
					{
						holder.item1.setImageResource(R.drawable.unstar);
					}
				}
			});
        	holder.item2.setText(file);
        }
        return v;
    }
}