package de.ubl.converter.goettingen;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import de.ulb.marc2geo.core.GlobalSettings;
import de.ulb.marc2geo.core.MapRecord;

public class Converter {
	
	private static Logger logger = Logger.getLogger("Converter");
	
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
		ArrayList<MapRecord> maps = new ArrayList<MapRecord>(); 
		maps = loader.getMaps();
		
		
		logger.info("Storing valid entries to the triple store [" + GlobalSettings.getEndpoint() + "]\n");
		
		for (int i = 0; i < maps.size(); i++) {
			
//			logger.info("Dropping named graph [" + GlobalSettings.getGraphBaseURI() + maps.get(i).getId() + "] ...");
//			loader.dropNamedGraph(maps.get(i));
			
			logger.info("Storing map " + maps.get(i).getHT() + " \"" + maps.get(i).getTitle() + "\" at [" + GlobalSettings.getGraphBaseURI() + maps.get(i).getHT() + "] ...");
			loader.storeTriples(loader.getSPARQLInsert(maps.get(i)));					
			
		}

		//loader.createSpatiotemporalIndexes(maps);

		
	}
}
