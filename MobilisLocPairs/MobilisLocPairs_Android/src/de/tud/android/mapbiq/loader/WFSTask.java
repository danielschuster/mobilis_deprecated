package de.tud.android.mapbiq.loader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import de.javagis.jgis.geometry.Extent;
import de.javagis.jgis.geometry.Point;
import de.javagis.jgis.geometry.PointFactory;
import de.javagis.jgis.geometry.Polygon;
import de.tud.iiogis.Helper;
import de.tud.iiogis.wfs.WFSAPI;
import de.tud.iiogis.wfs.WFSLayer;
import de.tud.iiogis.wfs.WFSLayerAttribute;
import de.tud.iiogis.wfs.WFSServer;
import de.tud.server.model.BuildingPart;
import de.tud.server.model.ItemAttribute;
import de.tud.server.model.LocationModelAPI;
import de.tud.server.model.WLANAccessPoint;

/**
 * This class implements all 3 specific tasks for creating connection to
 * WFS-Servers. - getCapabilities - getFeature - describeFeatureType Singleton
 * pattern is used.
 * 
 * @author nsh
 * 
 */
public class WFSTask {

	private static WFSTask instance = null;
	private Map<String, String> capNS_URIs;
	private Map<String, String> descrNS_URIs;
	private Map<String, String> featNS_URIs;

	// lokale Datenhaltung
	private boolean localExists = false;

	private SAXReader reader;
	private DocumentFactory fact;

	private WFSTask() {

		capNS_URIs = new HashMap<String, String>(3);
		capNS_URIs.put("wfs", "http://www.opengis.net/wfs");
		capNS_URIs.put("ogc", "http://www.opengis.net/ogc");
		capNS_URIs.put("xml", XMLConstants.XML_NS_URI);

		descrNS_URIs = new HashMap<String, String>(5);
		descrNS_URIs.put("ms", "http://mapserver.gis.umn.edu/mapserver");
		descrNS_URIs.put("ogc", "http://www.opengis.net/ogc");
		descrNS_URIs.put("xsd", "http://www.w3.org/2001/XMLSchema");
		descrNS_URIs.put("gml", "http://www.opengis.net/gml");
		descrNS_URIs.put("xml", XMLConstants.XML_NS_URI);

		featNS_URIs = new HashMap<String, String>(6);
		featNS_URIs.put("ms", "http://mapserver.gis.umn.edu/mapserver");
		featNS_URIs.put("wfs", "http://www.opengis.net/wfs");
		featNS_URIs.put("gml", "http://www.opengis.net/gml");
		featNS_URIs.put("kml", "http://earth.google.com/kml/2.0");
		featNS_URIs.put("ogc", "http://www.opengis.net/ogc");
		featNS_URIs.put("xml", XMLConstants.XML_NS_URI);


	}

	public static WFSTask getInstance() {

		if (instance == null)
			instance = new WFSTask();

		return instance;
	}

	@SuppressWarnings("unchecked")
	public boolean getCapabilites(WFSServer wfs, Handler handler, Context cntxt) {

		ArrayList<WFSLayer> layers = new ArrayList<WFSLayer>();

		/*
		 * String filename = "Loadtest4"; FileInputStream fis; try { fis =
		 * cntxt.openFileInput(filename); localExists = true; } catch
		 * (FileNotFoundException e) { e.printStackTrace(); }
		 */

		try {
			
			reader = new SAXReader();
			reader.setEncoding("ISO-8859-1");
			fact = DocumentFactory.getInstance();
			fact.setXPathNamespaceURIs(capNS_URIs);
			
			
			InputStream is = cntxt.getAssets().open("layers.xml");

			Document doc = reader.read(is);

			String serviceTitle = doc.selectSingleNode("//wfs:Service/wfs:Title").getText();
			Log.d("getCap", serviceTitle);

			ArrayList<Node> nodeList = (ArrayList<Node>) doc.selectNodes("//wfs:FeatureType");

			for (int i = 0; i < nodeList.size(); i++) {
				Node currNode = nodeList.get(i);

				String name = currNode.selectSingleNode("wfs:Name").getText();
				String title = currNode.selectSingleNode("wfs:Title").getText();
				String projection = currNode.selectSingleNode("wfs:SRS").getText();

				double minx = Double.valueOf(currNode.selectSingleNode("wfs:LatLongBoundingBox/@minx").getText());
				double miny = Double.valueOf(currNode.selectSingleNode("wfs:LatLongBoundingBox/@miny").getText());
				double maxx = Double.valueOf(currNode.selectSingleNode("wfs:LatLongBoundingBox/@maxx").getText());
				double maxy = Double.valueOf(currNode.selectSingleNode("wfs:LatLongBoundingBox/@maxy").getText());

				// add new layer
				layers.add(new WFSLayer(name, title, "", projection, new Extent(minx, miny, maxx, maxy)));

			}
		} catch (MalformedURLException e) {
			Log.d("getCap", "Error with URL while trying to connect to WFS-Server.\n");
			e.printStackTrace();
			return false;
		} catch (DocumentException e) {
			Log.d("getCap", "Error with parsing document while trying to connect to WFS-Server.\n");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// add layers to server-object
		wfs.setWfsLayers(layers);
		Log.d("getCap", wfs.getWfsName() + " contents " + layers.size() + " layers.");

		return LocationModelAPI.connectWFSServer(wfs);
	}

	// GetFeature from WFS-Server for all layers and integrate
	@SuppressWarnings("unchecked")
	public boolean getFeature(WFSServer wfs, Handler handler, Context cntxt) {
		int counter = 0;
		// GetFeature from WFS-Server for all layers and integrate
		try {
			// init SAX

			for (int i = 0; i < wfs.getWfsLayers().size(); i++) {
				String layerName = wfs.getWfsLayers().get(i).getLayerName();

				/*
				 * InputStream is = cntxt.getAssets().open("feature"+ i +
				 * ".xml"); Document doc = reader.read(is);
				 */

				URL url = new URL(wfs.getWfsUrl() + "&REQUEST=GetFeature&TYPENAME=" + layerName);
				// "&REQUEST=GetFeature&TYPENAME=" + layerName);
				Log.d("FEATURECOUNT", "feature" + i + ".xml");
				// Log.d("getFeature", url.toString());

				Document doc = reader.read(url);

				// get layer data and integrate

				// boundedBy -> extent of the whole layer
				Node extent = doc.selectSingleNode("//gml:boundedBy");
				String coord = extent.selectSingleNode(".//gml:coordinates/text()").getText();

				ArrayList<Point> pos = Helper.parseCoordinates(coord, " ", false);
				wfs.getWfsLayers().get(i).setLayerExtent(new Extent(pos.get(0).getX(), pos.get(0).getY(), pos.get(1).getX(), pos.get(1).getY()));

				// featureMember -> attributes of the single features
				ArrayList<Node> list = (ArrayList<Node>) doc.selectNodes("//gml:featureMember");
				wfs.getWfsLayers().get(i).setLayerType(list.get(0).selectSingleNode("//ms:msGeometry/*[1]").getName());

				for (int j = 0; j < list.size(); j++) {
					Node geometry = list.get(j).selectSingleNode(".//ms:msGeometry");

					ArrayList<ItemAttribute> attributes = new ArrayList<ItemAttribute>();
					String macAddress = "";
					Iterator it = wfs.getWfsLayers().get(i).getLayerAttributes().iterator();

					while (it.hasNext()) {
						String attributeName = ((WFSLayerAttribute) it.next()).getAttributeName();
						String attributeValue = list.get(j).selectSingleNode(".//ms:" + attributeName).getText();
						// System.out.print("(" + attributeName + ": " +
						// attributeValue + ") ");
						attributes.add(new ItemAttribute(attributeName, attributeValue));
						if (attributeName.startsWith("MAC")) {
							macAddress = attributeValue;
						}
					}

					// point layer
					if (wfs.getWfsLayers().get(i).getLayerType().equalsIgnoreCase("Point")) {
						String coordinates = geometry.selectSingleNode(".//gml:coordinates").getText();
						Point point = Helper.parseCoordinate(coordinates, ",");

						// System.out.print("(" + point.getY() + ", " +
						// point.getX() + ")");

						if (wfs.getWfsLayers().get(i).getLayerName().endsWith("_WFS")) {
							// wfs server layer
							String serverName = "";
							String serverURL = "";
							for (Iterator attrIt = attributes.iterator(); attrIt.hasNext();) {
								ItemAttribute attr = (ItemAttribute) attrIt.next();
								if (attr.getAttributeName().equals("NAME")) {
									serverName = attr.getAttributeValue();
								}
								if (attr.getAttributeName().equals("SERVER-URL")) {
									serverURL = attr.getAttributeValue();
								}
								// Log.d("getFeature", ia.getAttributeName() +
								// ": " + ia.getAttributeValue());
							}

							// create and add wfs server
							WFSServer server = WFSAPI.createWFSServer(serverName, serverURL, "");
							if (server == null) {
								Log.d("getFeature", "Could not create new WFS-Server!");
							} else {
								// check if given server url is valid
								if (!WFSAPI.isWFSServerURLValid(serverURL)) {
									Log.d("getFeature", "The URL \"" + serverURL + "\" of the WFS-Server \"" + serverName + "\" is not valid!");
								}
								server.setWfsCoordinate(point);
								wfs.addNearbyWfs(server);
							}

							// check if server is already existing or if server
							// url is not valid
							if (!WFSAPI.isWFSServerAlreadyExisting(serverURL) && WFSAPI.isWFSServerURLValid(serverURL)) {
								// a server with this url is not already
								// existing and the server url seems to be valid
								if (!LocationModelAPI.addWFSServer(server)) {
									Log.d("getFeature", "Could not add new WFS-Server!");
								}
								// test output
								Log.d("getFeature", "Server-Name: " + server.getWfsName());
								Log.d("getFeature", "Server-URL: " + server.getWfsUrl());
								Log.d("getFeature", "Coordinates: " + "(" + server.getWfsCoordinate().getY() + ", " + server.getWfsCoordinate().getX() + ")\n");

							}
						} else {
							// access point layer
							WLANAccessPoint ap = new WLANAccessPoint(point, attributes);
							ap.setItemId(macAddress);
							wfs.getWfsLayers().get(i).addItem(ap);
							LocationModelAPI.addAccessPoint(ap);
						}
					}

					// polygon layer
					if (wfs.getWfsLayers().get(i).getLayerType().equalsIgnoreCase("Polygon")) {
						String coordinates = geometry.selectSingleNode(".//gml:coordinates").getText();
						ArrayList<Point> positions = parseCoordinates(coordinates, " ", true);
						Point[] points = new Point[positions.size()];

						for (int k = 0; k < positions.size(); k++) {
							// System.out.print("(" + positions.get(k).getY() +
							// ", " + positions.get(k).getX() + ")");
							points[k] = positions.get(k);
							// if(k != (positions.size() - 1)){
							// System.out.print("; ");
							// }
						}
						Log.d("getFeature", points.toString());

						// add item to layer
						wfs.getWfsLayers().get(i).addItem(new BuildingPart(new Polygon(points), attributes));
					}
				}

				// set geometry-status to "integrated"
				wfs.setIntegrated(true);

				// //inform progress bar about progress
				// Message msg = handler.obtainMessage();
				// Bundle b = new Bundle();
				// b.putInt("total", -1);
				// b.putInt("progress", 3);
				// msg.setData(b);
				// handler.sendMessage(msg);

			}
		} catch (MalformedURLException e) {
			Log.d("getFeature", "URL exception while trying to connect to WFS-Server.\n");
			e.printStackTrace();
			return false;
		} catch (DocumentException e) {
			Log.d("getFeature", "Document exception while trying to connect to WFS-Server.\n");
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			Log.d("getFeature", "Error while creating building part.\n");
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean describeFeatureType(WFSServer wfsServer, Handler handler, Context cntxt) {
		int counter = 0;
		try {

			if (wfsServer.getWfsLayers() == null)
				return false;

			fact.setXPathNamespaceURIs(descrNS_URIs);

			// parse layer attributes
			for (WFSLayer layer : wfsServer.getWfsLayers()) {

				ArrayList<WFSLayerAttribute> attributes = new ArrayList<WFSLayerAttribute>();
				String layerName = layer.getLayerName();
				URL url = new URL(wfsServer.getWfsUrl() + "&REQUEST=DescribeFeatureType&TYPENAME=" + layerName);

				// Log.d("descFeatType", url.toString());

				Document doc = reader.read(url);

				// get layer attributes
				Log.d("descFeatType", "Layer-Attributes of Layer: " + layerName);
				ArrayList<Node> elemList = (ArrayList<Node>) doc.selectNodes("//xsd:sequence/xsd:element");

				for (Node node : elemList) {
					String name = node.valueOf("@name");
					String type = node.valueOf("@type");

					// Log.d("descFeatType", "Attribute-Name: " + name +
					// ", Attribute-Type: " + type);

					attributes.add(new WFSLayerAttribute(name, type));
				}

				// set attributes of the layer
				layer.setLayerAttributes(attributes);

				// //inform progress bar about progress
				// Message msg = handler.obtainMessage();
				// Bundle b = new Bundle();
				// b.putInt("total", -1);
				// b.putInt("progress", 1);
				// msg.setData(b);
				// handler.sendMessage(msg);

			}

		} catch (MalformedURLException e) {
			Log.d("descFeatType", "URL exception while trying to connect to WFS-Server.\n");
			e.printStackTrace();
			return false;
		} catch (DocumentException e) {
			Log.d("descFeatType", "Document exception while trying to connect to WFS-Server.\n");
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean testQuery() {

		try {
			URL url = new URL("http://141.76.40.250/cgi-bin/mapserv.exe" + "?MAP=tud_inf.map&SERVICE=wfs&VERSION=1.0.0&REQUEST=GetFeature&TypeName=TUD_INF_E0");

			Document doc = (Document) reader.read(url);

			Node node = doc.selectSingleNode("//ms:NUTZUNG");
			System.out.println("Node: " + node.getText() + "#");

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		return true;
	}

	// // HELPER methods taken from CARLOS

	/**
	 * parse coordinates and return as arraylist of points
	 * (de.javagis.jgis.geometry.Point)
	 * 
	 * @param String
	 *            coord - string with coordinates
	 * @param String
	 *            regex - seperator between two coordinate strings
	 * @param boolean polygon - if true, the last coordinate will be ignored (in
	 *        polygones first = last coordinate)
	 * @return ArrayList<Point> - arraylist of point-objects
	 */
	public static ArrayList<Point> parseCoordinates(String coord, String regex, boolean polygon) {
		int end = 0;
		if (polygon)
			end = 1;
		ArrayList<Point> points = new ArrayList<Point>();
		String[] coordinates = coord.split(regex);
		for (int i = 0; i < (coordinates.length - end); i++) {
			// longitude -> x, latitude -> y
			points.add(parseCoordinate(coordinates[i], ","));
		}
		return points;
	}

	/**
	 * parse coordinates and return as single point
	 * (de.javagis.jgis.geometry.Point)
	 * 
	 * @param String
	 *            coord - String with coordinates
	 * @param String
	 *            regex - seperator between the coordinate values
	 * @return Point - point-object of the coordinates
	 */
	public static Point parseCoordinate(String coord, String regex) {
		return PointFactory.createPoint(Double.parseDouble(coord.split(regex)[0]), Double.parseDouble(coord.split(regex)[1]));
	}

}
