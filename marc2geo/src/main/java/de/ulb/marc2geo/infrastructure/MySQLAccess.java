package de.ulb.marc2geo.infrastructure;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLAccess {
  private Connection connect = null;
  private Statement statement = null;
  private ResultSet resultSet = null;

  public void readDataBase() throws Exception {
    
	  try {
      // This will load the MySQL driver, each DB has its own driver
      Class.forName("com.mysql.jdbc.Driver");
      // Setup the connection with the DB
      connect = DriverManager.getConnection("jdbc:mysql://giv-lodum.uni-muenster.de/transfer?user=jones&password=");

      // Statements allow to issue SQL queries to the database
      statement = connect.createStatement();
      // Result set get the result of the SQL query
      resultSet = statement.executeQuery("select * from transfer.KATALOG");
      writeResultSet(resultSet);
      
    } catch (Exception e) {
      throw e;
    } finally {
      close();
    }

  }


  private void writeResultSet(ResultSet resultSet) throws SQLException {

    while (resultSet.next()) {
    	
      String xml = resultSet.getString("rawxml");
      String title = resultSet.getString("title");
      System.out.println("MARC21 XML: " + xml);
      System.out.println("Title: " + title);
      
    }
  }


  private void close() {
    try {
      if (resultSet != null) {
        resultSet.close();
      }

      if (statement != null) {
        statement.close();
      }

      if (connect != null) {
        connect.close();
      }
    } catch (Exception e) {

    }
  }

  
  public static void main(String[] args) throws Exception {
	    
	  MySQLAccess dao = new MySQLAccess();
	  dao.readDataBase();
	  
}
} 