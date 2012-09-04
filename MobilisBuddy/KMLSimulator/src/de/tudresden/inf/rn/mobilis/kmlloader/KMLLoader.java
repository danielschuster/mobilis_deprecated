package de.tudresden.inf.rn.mobilis.kmlloader;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class KMLLoader {

	private List<Coordinate> coordinates = new LinkedList<Coordinate>();

	public void readXML(File kmlFile) {

		try {
			// ---- Read XML file ----
			// Create a SAXBuilder
			SAXBuilder builder = new SAXBuilder();
			
			// Generate JDOM Document doc from kmlFile
			Document doc = builder.build(kmlFile);
			
			// Read out root Element of doc
			Element root = doc.getRootElement();
			
			// Get a list of all childrens of the root element
			List<Element> MainElementsList = root.getChildren();
			for (Iterator<Element> iter = MainElementsList.iterator(); iter
					.hasNext();) {
				Element elem = (Element) iter.next();
				List<Element> dokumenChilds = elem.getChildren();
				for (Iterator<Element> dokumentIter = dokumenChilds.iterator(); dokumentIter
						.hasNext();) {
					Element dokuChild = (Element) dokumentIter.next();

					List<Element> placemarkChilds = dokuChild.getChildren();
					for (Iterator<Element> placemarkIter = placemarkChilds
							.iterator(); placemarkIter.hasNext();) {
						Element placemarkChild = (Element) placemarkIter.next();

						List<Element> coordinateElements = placemarkChild
								.getChildren();
						for (Iterator<Element> coordinateIter = coordinateElements
								.iterator(); coordinateIter.hasNext();) {
							Element coordinates = (Element) coordinateIter
									.next();
							//Read out the String with the coordinates
							String cos = coordinates.getValue().toString();
							//Split the String for separating longitude and latitude
							String[] parts = cos.split(",");
							double longitude = Double.parseDouble(parts[0]);
							double latitude = Double.parseDouble(parts[1]);
							this.coordinates.add(new Coordinate(latitude,longitude));
							/*
							 * System.out.println(coordinates.getValue().toString
							 * ()); System.out.println(longitude);
							 * System.out.println(latitude);
							 */

						}
					}
				}

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public List<Coordinate> getCoordinates() {
		return coordinates;
	}
}
