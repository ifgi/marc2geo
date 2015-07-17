package de.ubl.marc2geo.converter;

import org.apache.log4j.Logger;

import de.ulb.marc2geo.core.GlobalSettings;

public class MapsHealthCheck {
	
	private static Logger logger = Logger.getLogger("HealthCheck");
	
	public static void main(String[] args) {
		
		logger.info("Service started:\n\n"  +
				"Universitäts- und Landesbibliothek \n" +
				"Westfälische Wilhelms-Universität Münster\n" + 
				"http://ulb.uni-muenster.de/\n\n" +
				
				"Database Host: "+ GlobalSettings.getDatabaseHost() + "\n" +
				"Database Name: "+ GlobalSettings.getDatabaseName() + "\n" +
				"Java Runtime: "+ System.getProperty("java.version") + "\n" +
				"Operating System: " + System.getProperty("os.name").toString() + " " + 
									   System.getProperty("os.version").toString() + " (" + 
									   System.getProperty("os.arch").toString()+")\n\nStarting Map Health Check...\n");
		
		
		DataLoader loader = new DataLoader();		
		loader.getMaps();
		
		
	}
}
