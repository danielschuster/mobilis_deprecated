package de.tudresden.inf.rn.mobilis.kmlloader;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class ConnectView extends JDialog implements ActionListener {
	
	private class Setting implements KeyListener, CaretListener {
		
		private String name;
		private String title;
		private String value;
		
		Setting(String name, String title, String defaultValue) {
			this.name = name;
			this.title = title;
			this.value = defaultValue;
		}
		
		public String getName() { return this.name; }
		public String getTitle() { return this.title; }
		public String getValue() { return this.value; }
		void setValue(String value) { this.value = value; }
		
		@Override public void keyPressed(KeyEvent e) {}
		@Override public void keyReleased(KeyEvent e) {};
		@Override public void keyTyped(KeyEvent e) {
			this.setValue(((JTextField) e.getSource()).getText());
		}
		@Override public void caretUpdate(CaretEvent e) {
			this.setValue(((JTextField) e.getSource()).getText());
		}
			
	}
	
	private static final long serialVersionUID = 1L;
	private List<Setting> settings = new LinkedList<Setting>();
	
	private JButton buttonOk = new JButton("OK");
	private JButton buttonCancel = new JButton("Cancel");
	private boolean okPressed = false;

	public ConnectView(Frame owner) {
		super(owner, "Connection Dialog");
		this.initializeSettings();
		this.initializeLayout();
	}
	
	public boolean askForSettings() {
		this.okPressed = false;
		this.setVisible(true);
		return this.okPressed;
	}
	
	public String getSetting(String name) {
		for (Setting s: this.settings)
			if (s.getName().equals(name))
				return s.getValue();
		return null;
	}

	private void initializeSettings() {
		this.settings.add(new Setting("host", "Host", "mobilis.inf.tu-dresden.de"));
		this.settings.add(new Setting("port", "Port", "5222"));
		this.settings.add(new Setting("service", "Service", "mobilis.inf.tu-dresden.de"));
		this.settings.add(new Setting("user", "XMPP-User", "client2"));
		this.settings.add(new Setting("password", "XMPP-Password",  "client2"));
		this.settings.add(new Setting("ressource", "XMPP-Ressource", "KMLLoader"));
		this.settings.add(new Setting("androidbuddy", "Androidbuddy-User", "mobilis@mobilis.inf.tu-dresden.de/Buddy"));
		this.settings.add(new Setting("interval", "Update Interval", "1000"));
	}
	
	private void initializeLayout() {
		this.setVisible(false);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setLayout(new GridLayout(this.settings.size()+1, 1));
		this.setSize(300, 24*(this.settings.size()+1));
		for (Setting s: this.settings)
			this.initializeSettingLayout(s);
		this.initializeButtonLayout();
	}
	
	private void initializeSettingLayout(Setting s) {
		JLabel l = new JLabel(s.getTitle());
		l.setSize(70,24);
		JTextField tf = new JTextField(s.getValue());
		tf.setSize(230,24);
		tf.addKeyListener(s);
		tf.addCaretListener(s);
		JPanel p = new JPanel();
		p.setSize(300,24);
		p.setLayout(new BorderLayout());
		p.add(l,  BorderLayout.WEST);
		p.add(tf, BorderLayout.CENTER);
		this.add(p);
	}
	
	private void initializeButtonLayout() {
		this.buttonOk.addActionListener(this);
		this.buttonOk.setSize(150, 24);
		this.buttonCancel.addActionListener(this);
		this.buttonCancel.setSize(150, 24);
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.setSize(300,24);
		p.add(this.buttonOk);
		p.add(this.buttonCancel);
		this.add(p);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.buttonCancel) {
			this.okPressed = false;
			this.setVisible(false);
		} else if (e.getSource() == this.buttonOk) {
			this.okPressed = true;
			this.setVisible(false);
		}
	}

}
