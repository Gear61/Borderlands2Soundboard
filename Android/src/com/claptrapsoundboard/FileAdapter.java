package com.claptrapsoundboard;

import java.util.List;

import android.content.Context;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FileAdapter extends BaseAdapter /* implements OnClickListener */
{
	private Context context;
	private List<Spanned> fileList;

	public FileAdapter(Context context, List<Spanned> fileList)
	{
		this.context = context;
		this.fileList = fileList;
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
        public TextView item1;
    }
    
    public View getView(int position, View convertView, ViewGroup parent)
	{
    	View v = convertView;
        ViewHolder holder;
        if (v == null)
        {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.file_item, null);
            holder = new ViewHolder();
            holder.item1 = (TextView) v.findViewById(R.id.file);
            v.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)v.getTag();
        }
 
        final Spanned file = fileList.get(position);
        if (file != null)
        {
            holder.item1.setText(file);
        }
        return v;
    }
}