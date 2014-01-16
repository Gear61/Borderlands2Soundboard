package com.claptrapsoundboard;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.widget.Filter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TextAdapter extends ArrayAdapter<Spanned> {
    private ArrayList<Spanned> items;
    private ArrayList<Spanned> itemsAll;
    private ArrayList<Spanned> suggestions;
    
    private Context context;

    @SuppressWarnings("unchecked")
    public TextAdapter(Context context, int viewResourceId, ArrayList<Spanned> items)
    {
    	super(context, viewResourceId, items);
    	this.context = context;
        this.items = items;
        this.itemsAll = (ArrayList<Spanned>) items.clone();
        this.suggestions = new ArrayList<Spanned>();
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
            v = vi.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.item1 = (TextView) v.findViewById(R.id.item);
            v.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)v.getTag();
        }
 
        final String item = (items.get(position)).toString();
        if (item != null)
        {
            holder.item1.setText(item.trim());
        }
        return v;
    }

    @Override
    public android.widget.Filter getFilter()
    {
        return nameFilter;
    }

    @SuppressLint("DefaultLocale")
	Filter nameFilter = new Filter()
    {
        public String convertResultToString(Object resultValue)
        {
            String str = ((Spanned) (resultValue)).toString();
            return str;
        }

        @SuppressLint("DefaultLocale")
		@Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            if (constraint != null)
            {
                suggestions.clear();
                for (int i = 0, j = 0; i < itemsAll.size() && j <= 10; i++)
                {
                    if (itemsAll.get(i).toString().toLowerCase().contains(constraint.toString().toLowerCase()))
                    {
                        j++;
                    	suggestions.add(itemsAll.get(i));
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            }
            else
            {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            @SuppressWarnings("unchecked")
            ArrayList<Spanned> filteredList = (ArrayList<Spanned>) results.values;
            if (results != null && results.count > 0)
            {
                clear();
                for (Spanned c : filteredList)
                {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }
    };

}
