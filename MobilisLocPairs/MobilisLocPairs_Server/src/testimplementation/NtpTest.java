package testimplementation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.Calendar;

public class NtpTest {


	private static String serverName = "0.de.pool.ntp.org";
	public static void main(String[] args) throws IOException {
		
		DatagramSocket socket = new DatagramSocket();
		InetAddress address = InetAddress.getByName(serverName);
		byte[] buf = new NtpMessage().toByteArray();
		DatagramPacket packet =
			new DatagramPacket(buf, buf.length, address, 123);
		
		// Set the transmit timestamp *just* before sending the packet
		// ToDo: Does this actually improve performance or not?
		NtpMessage.encodeTimestamp(packet.getData(), 40,
			(System.currentTimeMillis()/1000.0) + 2208988800.0);
		
		socket.send(packet);
		
		
		// Get response
		System.out.println("NTP request sent, waiting for response...\n");
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		
		// Immediately record the incoming timestamp
		double destinationTimestamp =
			(System.currentTimeMillis()/1000.0) + 2208988800.0;
		
		Calendar cal = (Calendar)Calendar.getInstance().clone();
		cal.setTimeInMillis(new Long(String.valueOf(destinationTimestamp)));
		// Process response
		NtpMessage msg = new NtpMessage(packet.getData());
		
		// Corrected, according to RFC2030 errata
		double roundTripDelay = (destinationTimestamp-msg.originateTimestamp) -
			(msg.transmitTimestamp-msg.receiveTimestamp);
			
		double localClockOffset =
			((msg.receiveTimestamp - msg.originateTimestamp) +
			(msg.transmitTimestamp - destinationTimestamp)) / 2;
		
		
		// Display response
		System.out.println("NTP server: " + serverName);
		System.out.println(msg.toString());
		
		System.out.println("Dest. timestamp:     " +
			NtpMessage.timestampToString(destinationTimestamp));
		
		System.out.println("Round-trip delay: " +
			new DecimalFormat("0.00").format(roundTripDelay*1000) + " ms");
		
		System.out.println("Local clock offset: " +
			new DecimalFormat("0.00").format(localClockOffset*1000) + " ms");
		
		socket.close();
	}

}
