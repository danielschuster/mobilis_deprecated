/*******************************************************************************
 * Copyright (C) 2010 Technische Universität Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
package de.tudresden.inf.rn.mobilis.media.views;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import de.tudresden.inf.rn.mobilis.media.R;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;

public class RosterSpinnerAdapter extends ContentObserver implements SpinnerAdapter {

	private Context context;
	private int listItemResource;
	private int dropdownItemResource;
	private boolean indeterminateEnabled;
	
	public RosterSpinnerAdapter(Handler updateHandler, Context context,
			int listItemResource, int dropdownItemResource, boolean indeterminateEnabled) {
		super(updateHandler);
		this.context = context;
		this.context.getContentResolver()
				.registerContentObserver(ConstMXA.RosterItems.CONTENT_URI, true, this);
		this.dropdownItemResource = dropdownItemResource;
		this.listItemResource = listItemResource;
		this.indeterminateEnabled = indeterminateEnabled;
		this.setupRoster();
	}
	
	private String[] roster = new String[0];
	
	private void setupRoster() {
		ContentResolver cr = this.context.getContentResolver();
		Cursor c = cr.query(ConstMXA.RosterItems.CONTENT_URI,
				new String[] { ConstMXA.RosterItems.XMPP_ID },
				null, null, null);
		String[] r = new String[c.getCount() + (this.indeterminateEnabled?1:0)];
		if (c.moveToFirst()) do
			r[c.getPosition() + (this.indeterminateEnabled?1:0)]
					= c.getString(c.getColumnIndex(
							ConstMXA.RosterItems.XMPP_ID
						));
		while (c.moveToNext());
		if (this.indeterminateEnabled)
			r[0] = this.context.getResources().getString(R.string.none_entry);
		this.roster = r;
	}
		
	
	public View getView(int position, View target, int viewResource) {
		if (target == null)
			target = View.inflate(this.context, viewResource, null);
		((TextView)target).setText(this.roster[position]);
		return target;
	}
	
	public View getDropDownView(int position, View recycleView, ViewGroup parent) {
		return this.getView(position, recycleView, this.dropdownItemResource);
	}

	public View getView(int position, View recycleView, ViewGroup parentView) {
		return this.getView(position, recycleView, this.listItemResource);
	}
	
	public int getCount() {
		return this.roster.length;
	}

	public String getItem(int position) {
		if (this.indeterminateEnabled && position == 0)
			return null;
		else
			return this.roster[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		return 0;
	}

	public int getViewTypeCount() {
		return 1;
	}

	public boolean hasStableIds() {
		return false;
	}

	public boolean isEmpty() {
		return (this.roster.length==0);
	}

	private Set<DataSetObserver> dataSetObservers = Collections.synchronizedSet(
			new HashSet<DataSetObserver>() );
	
	public void registerDataSetObserver(DataSetObserver observer) {
		synchronized (this.dataSetObservers) {
			this.dataSetObservers.add(observer);
		}
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		synchronized (this.dataSetObservers) {
			this.dataSetObservers.remove(observer);
		}
	}
	
	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		this.setupRoster();
		synchronized (this.dataSetObservers) {
			for (DataSetObserver dso: this.dataSetObservers)
				dso.onChanged();
		}
	}

}
