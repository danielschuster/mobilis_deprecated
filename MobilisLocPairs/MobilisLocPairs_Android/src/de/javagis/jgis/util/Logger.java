/**
 * Logger.java
 * ----------------------------------------------
 * Dieser Code ist Teil des Buches "Java & GIS". 
 * Autoren:
 * @author Bjoern Koos, Michael Herter
 * ----------------------------------------------
 *
 * Hinweis:
 * Die Software wird bereitgestellt, ohne dass
 * damit irgendeine direkte oder indirekte Gewaehr
 * fuer die Korrektheit und Funktionsfaehigkeit
 * uebernommen wird. Ebenso wird keinerlei Haftung
 * fuer Schaeden, fuer den Verlust von Daten etc.
 * uebernommen, die durch den Einsatz dieser Software
 * entstehen.
 */
package de.javagis.jgis.util;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;


/**
 * 
 * Logger
 * 
 * Funktion: Funktionen zur Ausgabe von Meldungen unterschiedlicher Art, welche
 * auch in eine Log-Datei geschrieben werden k�nnen sollen.
 * 
 * @author Bjoern Koos, Michael Herter
 */
public class Logger {
  /**
   * PrintWriter zur erleichterten formatierten Ausgabe
   */
  private static PrintWriter out = null;

  /**
   * Variable, die angibt, ob wirklich ausgaben erfolgen sollen.
   */
  private static boolean loggerOn = false;

  public static final int DEBUG = 0;
  /**
   * Konstante, die angibt, dass Meldung eine Information ist.
   */
  public static final int INFO = 1;

  /**
   * Konstante, die angibt, dass Meldung eine Warnmeldung ist.
   */
  public static final int WARNING = 2;

  /**
   * Konstante, die angibt, dass Meldung einen Fehler anzeigt.
   */
  public static final int ERROR = 3;

  // Initialisierung des Loggers beim ersten Referenzieren der Klasse
    static int logLevel = INFO;

    static {
    	
    	String env = System.getProperty("Logger");
      
    	loggerOn = (env != null);

    	if (loggerOn) {
    	  if ("debug".equalsIgnoreCase(env)) {
    	    logLevel = DEBUG;
    	  } else if ("info".equalsIgnoreCase(env)) {
    	    logLevel = INFO;
    	  } else if ("warning".equalsIgnoreCase(env)) {
    	    logLevel = WARNING;
    	  } else if ("error".equalsIgnoreCase(env)) {
    	    logLevel = ERROR;
    	  }
    	}
      
    	if (loggerOn) {
    	try {
            out = new PrintWriter(new FileWriter("./jgis.log"));
            System.out.println("LOGGER STARTED - Level "+logLevel);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
      } else {
        System.out.println("LOGGER DISABLED! Use Environment variable 'Logger=(debug|info|warning|error)'");
      }
    }

    
    synchronized public static void log(String message, int typ) {
        if (!loggerOn || typ < logLevel) {
            return;
        }
        
        String msg = new Date().toString();
        switch (typ) {
        case DEBUG:
          msg += " DEBUG: ";
          break;
        case INFO:
            msg += " Info: ";
            break;
        case WARNING:
            msg += " ! Warning: ";
            break;
        case ERROR:
            msg += " ### ERROR: ";
            break;
        }

        msg += message;
        
        out.println(msg);
        out.flush();
        
        System.out.println(message);
        System.out.flush();
    }
}
