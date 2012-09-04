package de.tudresden.inf.rn.mobilis.kmlloader;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;


public class MainView extends JFrame implements ActionListener {

	
	private JButton sendButton = new JButton("simulate route");
	private JButton loadButton = new JButton("open kml-file");
	private JTextArea kmlTextArea = new JTextArea();
	private ConnectView connectView = new ConnectView(this);

	KMLLoader kmlLoader = new KMLLoader();
	StringBuffer TextBuffer = new StringBuffer();
	

	public MainView(String Title) {
		super(Title);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				System.exit(0);
			}
		});
		setSize(300, 400);
		setResizable(false);	
		setLocation(centerPoint());
		JPanel p = new JPanel();
		// p.setSize(300, 100);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		getContentPane().add(p);
		loadButton.setSize(200, 100);
		sendButton.setSize(200, 100);
		kmlTextArea.setEditable(false);
		kmlTextArea.setSize(300, 200);
		JScrollPane scr = new JScrollPane(kmlTextArea);
		p.add(scr);
		p.add(loadButton);
		p.add(sendButton);
		sendButton.addActionListener(this);
		loadButton.addActionListener(this);
		setVisible(true);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new MainView("KML-Routing-Simulator");
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == sendButton) {
			if (this.connectView.askForSettings()) {
				LocationJabberer lj = new LocationJabberer(
					this.connectView.getSetting("host"),
					this.connectView.getSetting("port"),
					this.connectView.getSetting("service"),
					this.connectView.getSetting("user"),
					this.connectView.getSetting("password"),
					this.connectView.getSetting("ressource"),
					this.connectView.getSetting("androidbuddy"),
					this.connectView.getSetting("interval")
				);
				lj.setCoordinates(this.kmlLoader.getCoordinates());
				lj.start();
			}
		} else if (event.getSource() == loadButton) {
			chooseFile();
		}
	}

	public void chooseFile() {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Choose KML-File");
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(".kml")
						|| f.isDirectory();
			}

			public String getDescription() {
				return "KML Files";
			}
		});

		int returnVal = fc.showOpenDialog(MainView.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			kmlLoader.readXML(file);
			updateTextArea();
		} else {

		}

	}

	public void updateTextArea() {

		List<Coordinate> coordinates = kmlLoader.getCoordinates();
		for (Coordinate c: coordinates) {
			TextBuffer.append("longitude: " + c.lon + " latitude: " + c.lat + "\n");
		}
		kmlTextArea.setText(TextBuffer.toString());

	}
	
	public void showConfigDialog(){
		JDialog configDialog = new JDialog(this);
		configDialog.setTitle("Configuration");
		configDialog.setSize(300,200);
		configDialog.setModal(true);
		configDialog.setLocation(centerPoint());
		configDialog.setResizable(false);
		configDialog.setLayout(new BorderLayout());
		//configDialog.setUndecorated(true);
		
		
		
		configDialog.setVisible(true);
		
		
	}
	
	public Point centerPoint(){
		// For centering
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension windowSize = getSize();
		int windowX = Math.max(0, (screenSize.width  - windowSize.width ) / 2);
		int windowY = Math.max(0, (screenSize.height - windowSize.height) / 2);
		Point point = new Point(windowX,windowY);
		
		return point;
	}

}
