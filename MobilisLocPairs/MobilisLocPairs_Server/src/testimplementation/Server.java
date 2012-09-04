package testimplementation;

import java.io.File;
import java.io.IOException;

import org.jibble.simplewebserver.SimpleWebServer;


public class Server {

	/**
	 * @param args
	 */
	
	public Server() {
		String dirName = generiereDateipfad("/mobilisLocpairs/fingerprints/");
		File f = new File(dirName);
		if (f.isDirectory()) {
			System.out.println("\nDas Verzeichnis \"" + dirName
					+ "\" existiert bereits.\n");
		} else {
			f.mkdir();
			System.out.println("\nIch habe das Verzeichnis \"" + dirName
					+ "\" erzeugt.\n");
		}
		SimpleWebServer server = null;
		try {
			server = new SimpleWebServer(f, 1231);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("start miniserver");
		server.run();
	}
	private String generiereDateipfad(String dateipfadrelativ) {
		String klassenname = this.getClass().getName();
		String klassenpfadrelativ = "/" + klassenname.replace(".", "/")
				+ ".class";
		String klassenpfadabsolut = getClass().getResource(klassenpfadrelativ)
				.getFile();
		String classespfad = klassenpfadabsolut.replace(klassenpfadrelativ, "");
		String buildpfad = classespfad.substring(0, classespfad
				.lastIndexOf("/"));
		String projektpfad = buildpfad.substring(0, buildpfad.lastIndexOf("/"));
		dateipfadrelativ = dateipfadrelativ.replace("\\", "/");
		if (dateipfadrelativ.charAt(0) != '/')
			dateipfadrelativ = "/" + dateipfadrelativ;
		String dateipfadabsolut = projektpfad + dateipfadrelativ;
		dateipfadabsolut = dateipfadabsolut.replace("%20", " ");
		dateipfadabsolut = dateipfadabsolut.substring(1);
		return dateipfadabsolut;
	}
}
