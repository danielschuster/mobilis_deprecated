package main;

import java.util.Timer;
import java.util.TimerTask;
import de.tudresden.inf.rn.mobilis.server.locpairs.model.Game;
public class Server {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Game game = new Game();
		Timer t = new Timer();
		TimerTask tt = new TimerTask() {
			
			@Override
			public void run() {
				System.out.println("Hallo!");
			}
		};
		t.schedule(tt, 2);
	}

}
