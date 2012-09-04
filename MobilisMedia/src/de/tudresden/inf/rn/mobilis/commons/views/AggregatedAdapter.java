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

import java.util.LinkedList;
import java.util.List;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public class AggregatedAdapter implements ListAdapter {

	private List<ListAdapter> adapters;
	
	public AggregatedAdapter() {
		this.adapters = new LinkedList<ListAdapter>();
	}
	
	public void addAdapter(ListAdapter adapter) {
		this.adapters.add(adapter);
	}
	
	public boolean areAllItemsEnabled() {
		for (ListAdapter a: this.adapters)
			if (!a.areAllItemsEnabled())
				return false;
		return true;
	}
	
	public boolean hasStableIds() {
		for (ListAdapter a: this.adapters)
			if (!a.hasStableIds())
				return false;
		return true;
	}
	
	public boolean isEmpty() {
		for (ListAdapter a: this.adapters)
			if (!a.isEmpty())
				return false;
		return true;
	}
	
	public void registerDataSetObserver(DataSetObserver observer) {
		for (ListAdapter a: this.adapters)
			a.registerDataSetObserver(observer);
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		for (ListAdapter a: this.adapters)
			a.unregisterDataSetObserver(observer);
	}
	
	public boolean isEnabled(int aggregatedPosition) {
		PositionLocator pl = new PositionLocator(aggregatedPosition);
		return this.adapters.get(pl.adapter).isEnabled(pl.position);
	}

	public Object getItem(int aggregatedPosition) {
		PositionLocator pl = new PositionLocator(aggregatedPosition);
		return this.adapters.get(pl.adapter).getItem(pl.position);
	}

	public View getView(int aggregatedPosition, View convertView, ViewGroup parent) {
		PositionLocator pl = new PositionLocator(aggregatedPosition);
		return this.adapters.get(pl.adapter).getView(pl.position, convertView, parent);
	}
	
	public long getItemId(int aggregatedPosition) {
		PositionLocator pl = new PositionLocator(aggregatedPosition);
		return this.adapters.get(pl.adapter).getItemId(pl.position);
	}

	public int getItemViewType(int aggregatedPosition) {
		PositionLocator pl = new PositionLocator(aggregatedPosition);
		return pl.viewTypeBase + this.adapters.get(pl.adapter).getItemViewType(pl.position);
	}
	
	public int getViewTypeCount() {//
		int sum = 0;
		for (ListAdapter a: this.adapters) {
			int viewTypeCount = a.getViewTypeCount();
			if (viewTypeCount == ListAdapter.IGNORE_ITEM_VIEW_TYPE)
				return ListAdapter.IGNORE_ITEM_VIEW_TYPE;
			else
				sum += a.getViewTypeCount(); 
		}
		return sum;
	}
	
	public int getCount() {
		int sum = 0;
		for (ListAdapter a: this.adapters)
			sum += a.getCount();
		return sum;
	}
	
	private class PositionLocator {
		
		public int adapter;
		public int position;
		public int viewTypeBase;
		
		public PositionLocator(int aggregatedPosition) {
			int currentPosition = 0;
			int currentAdapter = 0;
			int viewTypeBase = 0;
			List<ListAdapter> adapters = AggregatedAdapter.this.adapters;
			while ( currentPosition + adapters.get(currentAdapter).getCount() <= aggregatedPosition
					&& currentAdapter < adapters.size() ) {
				currentPosition += adapters.get(currentAdapter).getCount();
				viewTypeBase += adapters.get(currentAdapter).getViewTypeCount();
				currentAdapter++;
			}
			this.position = aggregatedPosition-currentPosition;
			this.adapter  = currentAdapter;
		}
 
	}
	
}
