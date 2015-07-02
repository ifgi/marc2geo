package de.ulb.marc2geo.infrastructure;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class MySQLConnector {

	private java.sql.Connection connect = null;

	public Connection getConnection(){

		try {

			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection("jdbc:mysql://giv-lodum.uni-muenster.de/transfer?user=jones&password=");

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return connect;

	}
}
