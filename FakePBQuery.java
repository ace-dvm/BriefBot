package bbtrial.nl.logicgate.ace;

import java.io.PrintStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FakePBQuery extends Query {

	private List<Map<String, Object>> bbpb = new ArrayList<Map<String, Object>>();
	
	public FakePBQuery(Connection conn, PrintStream err) {
		super(conn, err);
	}

	//These queries were part of the internal workings due to some letters being stored in a legacy database.
	//To keep things simple, all letters in this demo will come from the other database
	//This one always returns an empty result set
	
	@Override
	public List<Map<String, Object>> executeQuery(String query) {
			return bbpb;
	}
	
}
