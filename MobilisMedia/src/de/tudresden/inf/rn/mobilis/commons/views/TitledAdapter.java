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
package de.tudresden.inf.rn.mobilis.commons.views;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;
import de.tudresden.inf.rn.mobilis.media.R;

public class TitledAdapter implements ListAdapter {

	private ListAdapter contentAdapter;
	private View titleView;

	public TitledAdapter(Context c, String title) {
		TextView tv = (TextView) View.inflate(c, R.layout.list_title, null);
		tv.setText(title);
		this.titleView = tv;
	}
	
	public void setContentAdapter(ListAdapter contentAdapter) {
		this.contentAdapter = contentAdapter;
	}
		
	public boolean areAllItemsEnabled() {
		return false;
	}

	public boolean isEnabled(int position) {
		if (position == 0)
			return false;
		else
			return this.contentAdapter.isEnabled(position-1);
	}

	public int getCount() {
		return 1+this.contentAdapter.getCount();
	}

	public Object getItem(int position) {
		if (position == 0)
			return null;
		else
			return this.contentAdapter.getItem(position-1);
	}

	public long getItemId(int position) {
		if (position == 0)
			return -1;
		else
			return this.contentAdapter.getItemId(position-1);
	}

	public int getItemViewType(int position) {
		if (position == 0)
			return 0;
		else
			return 1+this.contentAdapter.getItemViewType(position-1);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (position == 0)
			return this.titleView;
		else
			return this.contentAdapter.getView(position-1, convertView, parent);
	}

	public int getViewTypeCount() {
		return 1+this.contentAdapter.getViewTypeCount();
	}

	public boolean hasStableIds() {
		return this.contentAdapter.hasStableIds();
	}

	public boolean isEmpty() {
		return this.contentAdapter.isEmpty();
	}

	public void registerDataSetObserver(DataSetObserver observer) {
		this.contentAdapter.registerDataSetObserver(observer);
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		this.contentAdapter.unregisterDataSetObserver(observer);
	}

}
