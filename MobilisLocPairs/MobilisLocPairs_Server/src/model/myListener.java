package model;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

public class myListener implements PacketListener, ConnectionListener{
	
	private Player player;
	
	public myListener(Player player){
		this.player = player;
	}

/*	@Override
	public void processMessage(Chat chat, Message message) {
		System.out.println("myListener.processMessage()");
		if(player.processMessage(message.getBody())){
			try {
				chat.sendMessage("OK");
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			try {
				chat.sendMessage("ERROR");
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
*/

	@Override
	public void connectionClosed() {
		System.out.println("myListener.connectionClosed()");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectionClosedOnError(Exception arg0) {
		System.out.println("myListener.connectionClosedOnError()");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reconnectingIn(int arg0) {
		System.out.println("myListener.reconnectingIn()");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reconnectionFailed(Exception arg0) {
		System.out.println("myListener.reconnectionFailed()");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reconnectionSuccessful() {
		System.out.println("myListener.reconnectionSucessFul()");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processPacket(Packet packet) {
		System.out.println("myListener.processPackage()");
		// TODO Auto-generated method stub
//		player.processPackage(packet.toXML());
//		System.out.print(packet.toString());
		
	}
	
}
