package bbtrial.nl.logicgate.ace;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class QueryConnection {

	private Query query;
	private Connection conn;
	
	public QueryConnection(){
		query = null;
		conn = null;
	}
	
	public Query getQuery(){
		return query;
	}
	
	public boolean establishConnection(String testConnName){
		conn = new FakeConnection();
		if(testConnName.equals("storage")){
			query = new FakeStorageQuery(conn, System.err);
		}
		if(testConnName.equals("docDB")){
			query = new FakeDocQuery(conn, System.err);
		}
		if(testConnName.equals("nDB")){
			query = new FakeNQuery(conn, System.err);
		}
		if(testConnName.equals("pbDB")){
			query = new FakePBQuery(conn, System.err);
		}
		return true;
	}
	
	public boolean establishConnection(String dbDriver, String dbURL, String loginName, String password){
		conn = connect(dbDriver, dbURL, loginName, password);
		if(conn!=null){
			query = new Query(conn, System.err);
			return true;
		} else {return false;}
	}
	
	private Connection connect(String dbDriver, String dbURL, String loginName, String password){
		Connection conn = null;
		// start database driver and connect to db
		try{
			Class.forName(dbDriver);    	
			conn = DriverManager.getConnection(dbURL, loginName, password);
		}
		catch(ClassNotFoundException e){
			// driver not found
			new WriteFile().appendFile(BriefBot.ERROR_FILE, "Driver not found, cannot connect to " + dbURL + "\n");
		}
		catch(SQLException e){
			new WriteFile().appendFile(BriefBot.ERROR_FILE, "SQL error: cannot connect to " + dbURL + "\n");
		}
		return conn;
	}
	
	public boolean isConnected(){
		if(conn==null){
			return false;
		} else {
			return true;
		}
	}
	
	public boolean close() {
		try{
			if(conn != null){
				conn.close();
				conn = null;
			}
		}
		catch(SQLException e){ // something happened, perhaps check if connection was already closed?
			new WriteFile().appendFile(BriefBot.ERROR_FILE, "storage close: Error closing database connection \n");
			return false;
		}
		return true;
	}
}
