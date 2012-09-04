package de.tudresden.inf.rn.mobilis.server.locpairs.model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;

import de.tudresden.inf.rn.mobilis.server.locpairs.model.LocPairsServerTime;

import testimplementation.NtpMessage;

public class LocPairsServerTime {

	public static final String ntpHost = "0.de.pool.ntp.org";
	private static long offset;
	@SuppressWarnings("unused")
	private static LocPairsServerTime instance = new LocPairsServerTime();

	private LocPairsServerTime(){
		try {
			getOffset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Date getTime(){
		long time = Calendar.getInstance().getTimeInMillis();
		long newTime = time + offset;
		return new Date(newTime);
	}
	public static boolean synchronize() {
		try {
			getOffset();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static void getOffset() throws IOException {
//			System.out.println("LocPairsServerTime synchronized");
			DatagramSocket socket = new DatagramSocket();
			InetAddress address = InetAddress.getByName(ntpHost);
			byte[] buf = new NtpMessage().toByteArray();
			DatagramPacket packet = new DatagramPacket(buf, buf.length,
					address, 123);

			// Set the transmit timestamp *just* before sending the packet
			// ToDo: Does this actually improve performance or not?
			NtpMessage.encodeTimestamp(packet.getData(), 40, (Calendar.getInstance().getTimeInMillis() / 1000.0) + 2208988800.0);

			socket.send(packet);

			// Get response
			// System.out.println("NTP request sent, waiting for response...\n");
			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

			NtpMessage msg = new NtpMessage(packet.getData());
			// Immediately record the incoming timestamp
			double destinationTimestamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;
			System.out.println(destinationTimestamp);

			Double localClockOffset = ((msg.receiveTimestamp - msg.originateTimestamp) + (msg.transmitTimestamp - destinationTimestamp)) / 2;
			offset = localClockOffset.longValue()*1000;
			System.out.println("Zeitdiff: " + offset);
	}
}
