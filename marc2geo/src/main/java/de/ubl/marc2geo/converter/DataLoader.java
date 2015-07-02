package de.ubl.marc2geo.converter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import de.ulb.marc2geo.core.MapRecord;
import de.ulb.marc2geo.infrastructure.MySQLConnector;

public class DataLoader {

	private ResultSet loadData(){

		Statement statement = null;
		ResultSet result = null;
		MySQLConnector cnn = new MySQLConnector();

		try {

			statement =  cnn.getConnection().createStatement();
			result = statement.executeQuery("select * from transfer.KATALOG");

		} catch (SQLException e) {

			e.printStackTrace();
		}

		return result;

	}


	public ArrayList<MapRecord> getMaps(){

		ArrayList<MapRecord> result = new ArrayList<MapRecord>();
		ResultSet data = this.loadData();

		
		try {

			while (data.next()) {
				
				MapRecord map = new MapRecord();
				
				map.setTitle(data.getString("title"));
				map.setGeometry(data.getString("rawxml"));			
				
				result.add(map);

			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}


		return result;
	}
}
