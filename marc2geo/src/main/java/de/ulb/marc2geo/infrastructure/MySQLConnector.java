package de.ulb.marc2geo.infrastructure;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import de.ulb.marc2geo.core.GlobalSettings;


public class MySQLConnector {

	private java.sql.Connection connect = null;
	private static Logger logger = Logger.getLogger("DataLoader");
	
	public Connection getConnection(){

		try {

			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection("jdbc:mysql://"+GlobalSettings.getDatabaseHost()+"/"+GlobalSettings.getDatabaseName()+
												  "?user="+GlobalSettings.getDatabaseUser()+"&password="+GlobalSettings.getDatabasePassword());
			//connect = DriverManager.getConnection("jdbc:mysql://giv-lodum.uni-muenster.de/transfer?user=jones&password=");

		} catch (ClassNotFoundException e) {		
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
			
			logger.fatal("Error connecting to the database server >> " +  e.getMessage());
		}

		return connect;

	}
}
