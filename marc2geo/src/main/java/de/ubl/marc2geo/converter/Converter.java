package de.ubl.marc2geo.converter;

import java.util.ArrayList;

import de.ulb.marc2geo.core.MapRecord;

public class Converter {

	public static void main(String[] args) {
		
		DataLoader loader = new DataLoader();		
		ArrayList<MapRecord> maps = new ArrayList<MapRecord>(); 
		maps = loader.getMaps();
		
		for (int i = 0; i < maps.size(); i++) {
			
			System.out.println("Map Title: "+maps.get(i).getTitle());
			
		}
	}
}
