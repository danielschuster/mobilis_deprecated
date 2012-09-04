package de.tudresden.inf.rn.mobilis.android.buddylist;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

/**
 * Original sources from:
 * http://androidguys.com/?p=644
 * 
 * @author Dirk
 */
public class AdapterWrapper implements ListAdapter {
    
    ListAdapter delegate=null;

    public AdapterWrapper(ListAdapter delegate) {
        this.delegate=delegate;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return delegate.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        return delegate.isEnabled(position);
    }

    @Override
    public int getCount() {
        return delegate.getCount();
    }

    @Override
    public Object getItem(int position) {
        return delegate.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return delegate.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return delegate.getItemViewType(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return delegate.getView(position, convertView, parent);
    }

    @Override
    public int getViewTypeCount() {
        return delegate.getViewTypeCount();
    }

    @Override
    public boolean hasStableIds() {
        return delegate.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        delegate.registerDataSetObserver(observer);        
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        delegate.unregisterDataSetObserver(observer);
    }

}
