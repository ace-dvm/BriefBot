package bbtrial.nl.logicgate.ace;

import java.io.PrintStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FakeNQuery extends Query {
	
	private List<Map<String, Object>> bbn = new ArrayList<Map<String, Object>>();
	private SQLObjects sql;
	
	public FakeNQuery(Connection conn, PrintStream err) {
		super(conn, err);
		sql = new SQLObjects();
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
					RCalendar a = new RCalendar(sql.sqlTimestampToRCalendar(m.get("aanmaakDT")));
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
		RCalendar date = new RCalendar("d", -1);
		Map<String, Object> bbn1 = new LinkedHashMap<String, Object>();
		bbn1.put("PID", "amy");
		bbn1.put("aanmaakDT", new RCalendar(date, "d", -7).toTimestamp());
		bbn1.put("status",0);
		bbn1.put("complete",true);
		bbn1.put("specialismeID",0);
		bbn1.put("ondertekenaar", "who");
		bbn1.put("aanpassingDT",new RCalendar(date, "d", -7).toTimestamp());
		bbn1.put("nBriefID", 1);
		bbn.add(bbn1);
		Map<String, Object> bbn2 = new LinkedHashMap<String, Object>();
		bbn2.put("PID", "rory");
		bbn2.put("aanmaakDT", new RCalendar(date, "d", -30).toTimestamp());
		bbn2.put("status",0);
		bbn2.put("complete",true);
		bbn2.put("specialismeID",0);
		bbn2.put("ondertekenaar", "who");
		bbn2.put("aanpassingDT",new RCalendar(date, "d", -30).toTimestamp());
		bbn2.put("nBriefID", 2);
		bbn.add(bbn2);
		Map<String, Object> bbn3 = new LinkedHashMap<String, Object>();
		bbn3.put("PID", "amy");
		bbn3.put("aanmaakDT", new RCalendar(date, "d", -7).toTimestamp());
		bbn3.put("status",0);
		bbn3.put("complete",true);
		bbn3.put("specialismeID",0);
		bbn3.put("ondertekenaar", "who");
		bbn3.put("aanpassingDT",new RCalendar(date, "d", -7).toTimestamp());
		bbn3.put("nBriefID", 3);
		bbn.add(bbn3);
		Map<String, Object> bbn4 = new LinkedHashMap<String, Object>();
		bbn4.put("PID", "dodo");
		bbn4.put("aanmaakDT", new RCalendar("y",-3).toTimestamp());
		bbn4.put("status",0);
		bbn4.put("complete",true);
		bbn4.put("specialismeID",0);
		bbn4.put("ondertekenaar", "who");
		bbn4.put("aanpassingDT", new RCalendar("y",-3).toTimestamp());
		bbn4.put("nBriefID", 4);
		bbn.add(bbn4);
		Map<String, Object> bbn5 = new LinkedHashMap<String, Object>();
		bbn5.put("PID", "jamie");
		bbn5.put("aanmaakDT", new RCalendar("d",-100).toTimestamp());
		bbn5.put("status",0);
		bbn5.put("complete",true);
		bbn5.put("specialismeID",0);
		bbn5.put("ondertekenaar", "who");
		bbn5.put("aanpassingDT", new RCalendar("d",-100).toTimestamp());
		bbn5.put("nBriefID", 5);
		bbn.add(bbn5);
		Map<String, Object> bbn100 = new LinkedHashMap<String, Object>();
		bbn100.put("PID", "river");
		bbn100.put("aanmaakDT", new RCalendar("d",-100).toTimestamp());
		bbn100.put("status",0);
		bbn100.put("complete",true);
		bbn100.put("specialismeID",0);
		bbn100.put("ondertekenaar", "who");
		bbn100.put("aanpassingDT", new RCalendar("d",-100).toTimestamp());
		bbn100.put("nBriefID", 2012);
		bbn.add(bbn100);
		Map<String, Object> bbn101 = new LinkedHashMap<String, Object>();
		bbn101.put("PID", "peri");
		bbn101.put("aanmaakDT", new RCalendar("m",-3).toTimestamp());
		bbn101.put("status",0);
		bbn101.put("complete",true);
		bbn101.put("specialismeID",0);
		bbn101.put("ondertekenaar", "who");
		bbn101.put("aanpassingDT", new RCalendar("m",-3).toTimestamp());
		bbn101.put("nBriefID", 1984);
		bbn.add(bbn101);
	}
	
}

