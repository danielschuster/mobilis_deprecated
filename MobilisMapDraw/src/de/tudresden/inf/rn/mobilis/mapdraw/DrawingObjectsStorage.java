/*******************************************************************************
 * Copyright (C) 2011 Technische Universitšt Dresden
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
package de.tudresden.inf.rn.mobilis.mapdraw;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.graphics.Canvas;

import com.google.android.maps.MapView;

/**
 * Singleton class for storing all drawable objects. DrawingOverlay and CachingLayer fetching these from here for rendering.
 * @author Dirk Hering
 */
public class DrawingObjectsStorage {
	
	private static DrawingObjectsStorage instance;
	private Set<DrawingObject> drawnObjects = Collections.synchronizedSet(new HashSet<DrawingObject>());
	private MapView mapView;

	public static DrawingObjectsStorage getInstance() {
		if (instance == null) {
			instance = new DrawingObjectsStorage();
		}
		return instance;
	}

	public void clear() {
		drawnObjects.clear();
	}
	
	public void addDrawingObject(DrawingObject drawingObject) {
		drawnObjects.add(drawingObject);
	}

	public MapView getMapView() {
		return mapView;
	}

	public void setMapView(MapView mapView) {
		this.mapView = mapView;
	}
	
	/**
	 * Defers a drawing call to all stored DrawingObjects.
	 * @param canvas the Canvas to draw on
	 */
	public void draw(Canvas canvas) {
		synchronized(drawnObjects) {
			for (DrawingObject d : drawnObjects) {
				d.draw(canvas);
			}
		}
		
		// Measuring Feedthrough
//		Monitoring mon = Monitoring.get();
//		System.out.println("Redraw End - isFromRemote:" + mon.isFromRemote());
//		if (mon.isFromRemote()) mon.endTimer(true);
	}
}
