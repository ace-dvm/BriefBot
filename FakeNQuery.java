package bbtrial.nl.logicgate.ace;

import java.io.PrintStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FakeNQuery extends Query {
	
	private List<Map<String, Object>> bbn = new ArrayList<Map<String, Object>>();

	public FakeNQuery(Connection conn, PrintStream err) {
		super(conn, err);
		populate();
	}
	

	@Override
	public List<Map<String, Object>> executeQuery(String query) {		
		List<Map<String,Object>> rs = new ArrayList<Map<String,Object>>();	
		if(query.contains("SELECT TOP 1 * FROM n ")){
			Pattern p1 = Pattern.compile("patientID='[^']+");
			Matcher m1 = p1.matcher(query);
			String pid;
			if (m1.find()) {
				pid = m1.group(0).substring(11);
			} else {pid = null;}
			boolean newLet = false;
			Pattern p2;		
			if(query.contains("aanmaakDT >= CAST('")){
				p2 = Pattern.compile("aanmaakDT >= CAST\\('[^']+");
				newLet = true;
			} else {
				p2 = Pattern.compile("aanmaakDT < CAST\\('[^']*");
			}
			Matcher m2 = p2.matcher(query);
			RCalendar visit;
			if(m2.find()){
				visit = new RCalendar(m2.group(0).substring(m2.group(0).indexOf("'")+1));
			} else {visit = null;}
			for(Map<String,Object> m : bbn){
				if(m.get("PID").equals(pid)){
					RCalendar a = new RCalendar(m.get("aanmaakDT").toString());
					if(newLet){
						if(a.after(visit) || a.equals(visit)){
							rs.add(m);
							return rs; //we ensure there is only one match in this demo
						}
					} else {
						if(a.before(visit)){
							rs.add(m);
							return rs;
						}
					}
				}
			}
		}
		return rs; //if we haven't returned anything else, return an empty rs
	}
		
		
	private void populate() {
		 //TODO figure out who needs letters and create them
/*		Map<String, Object> bbn1 = new LinkedHashMap<String, Object>();
		bbn1.put("PID", "leela");
		bbn1.put("aanmaakDT",0);
		bbn1.put("status",0);
		bbn1.put("complete",true);
		bbn1.put("specialismeID",0);
		bbn1.put("ondertekenaar",0);
		bbn1.put("aanpassingDT",0);
		bbn1.put("nBriefID",0);
		bbn.add(bbn1);
*/
		
	}
	
}

