package de.ubl.marc2geo.converter;

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
									   System.getProperty("os.arch").toString()+")\n\nStarting Loader...\n");
		
		
		DataLoader loader = new DataLoader();		
		ArrayList<MapRecord> maps = new ArrayList<MapRecord>(); 
		maps = loader.getMaps();
		
		for (int i = 0; i < maps.size(); i++) {
			
//			System.out.println("\nMap URI: "+ maps.get(i).getUri());
//			System.out.println("Map ID: "+ maps.get(i).getId());
//			System.out.println("Map Title: "+ maps.get(i).getTitle());
//			System.out.println("Description: "+ maps.get(i).getDescription());
//			System.out.println("Year: "+ maps.get(i).getYear());
//			System.out.println("Size: "+ maps.get(i).getMapSize());
//			System.out.println("Scale: "+ maps.get(i).getScale());
//			System.out.println("Geometry: "+ maps.get(i).getGeometry());
//			System.out.println("Map Image: "+ maps.get(i).getImage());
//			System.out.println("Map Presentation: "+ maps.get(i).getPresentation());
			
//			System.out.println(loader.getSPARQLInsert(maps.get(i)));
			loader.storeTriples(loader.getSPARQLInsert(maps.get(i)));
			
//			System.out.println("\n");
			
		}
	}
}
