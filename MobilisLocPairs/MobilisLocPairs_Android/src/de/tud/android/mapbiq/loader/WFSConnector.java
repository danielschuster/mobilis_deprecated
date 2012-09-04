package de.tud.android.mapbiq.loader;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.tud.android.mapbiq.LocPairsApp;
import de.tud.android.mapbiq.R;
import de.tud.iiogis.wfs.WFSAPI;
import de.tud.iiogis.wfs.WFSServer;
import de.tud.server.model.LocationModelAPI;

public class WFSConnector {

	private WFSServer picked;
	private Handler handler;

	private void getServers() {
		// create some data
		if (LocationModelAPI.getAvailableWFSServers().size() == 0) {

			WFSServer wfs1 = WFSAPI
					.createWFSServer(
							"Fakult�t Informatik",
							"http://carlos.inf.tu-dresden.de/cgi-bin/mapserv.exe?MAP=tud_inf.map&SERVICE=wfs&VERSION=1.0.0",
							"title1");
			LocationModelAPI.addWFSServer(wfs1);
		}

	}


	public boolean connectToServer() {
		getServers();
		picked = LocationModelAPI.getAvailableWFSServers().get(0);
		final WFSServer wfs = picked;
		// Process getCapabilities request..
		boolean success = WFSTask.getInstance()
				.getCapabilites(wfs, handler, LocPairsApp.getContext());
		if (!success) {
			return false;
		}

		// Process describeFeatureType request..
		success = WFSTask.getInstance().describeFeatureType(wfs, handler, LocPairsApp.getContext());
		if (!success) {
			return false;
		}

		// Process getFeature request..
		success = WFSTask.getInstance().getFeature(wfs, handler, LocPairsApp.getContext());
		wfs.setIntegrated(true);
		if (!success) {
			return false;
		}
		return true;
	}

}
