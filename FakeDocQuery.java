package bbtrial.nl.logicgate.ace;

import java.io.PrintStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FakeDocQuery extends Query {

	private List<Map<String, Object>> bbd = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> bbc = new ArrayList<Map<String, Object>>(); //leave empty
	private List<Map<String, Object>> bbcp = new ArrayList<Map<String, Object>>(); //leave empty
	private List<Map<String, Object>> bbrp = new ArrayList<Map<String, Object>>(); //leave empty
	
	public FakeDocQuery(Connection conn, PrintStream err) {
		super(conn, err);
		populate();
	}
	
	@Override
	public List<Map<String, Object>> executeQuery(String query) {
		List<Map<String,Object>> rs = new ArrayList<Map<String,Object>>();
		if(query.equals("SELECT * FROM bb_doctors")){
			return bbd;
	    }
		if(query.contains("SELECT * FROM bb_categoryPrefs")){
			String doctorID = extractDoctorID(query);
			for(Map<String,Object> m : bbcp){
				if(m.get("doctorID").equals(doctorID)){
					rs.add(m);
				}
			}
			return rs;
		}
		if(query.contains("SELECT * FROM bb_reminderPrefs")){
			String doctorID = extractDoctorID(query);
			for(Map<String,Object> m : bbrp){
				if(m.get("doctorID").equals(doctorID)){
					rs.add(m);
				}
			}
			return rs;
		}
		if(query.equals("SELECT * FROM bb_changes")){
			return bbc;
		}
		return rs; //if query does not parse, return empty result set
	}
	
	@Override
	public int executeUpdate(String query) {
		if(query.contains("UPDATE bb_reminderPrefs SET lastReminder='")){
			String doctorID = extractDoctorID(query);
			String date = extractDate(query);
			for(Map<String,Object> m : bbrp){
				if(m.get("doctorID").equals(doctorID)){
					m.put("lastReminder", date);
					return 1;
				}
			}
			return 0;
		}
		return 0;
	}

	private String extractDate(String query) {
		Pattern p2 = Pattern.compile("[0-9]+-[0-9]+-[0-9]+");
		Matcher m2 = p2.matcher(query);
		String date;
		if(m2.find()){
			date = m2.group(0);
		} else {date = null;}
		return date;
	}

	private String extractDoctorID(String query) {
//System.out.println(query);		
		Pattern p1 = Pattern.compile("doctorID='[^']+");
		Matcher m1 = p1.matcher(query);
		if (m1.find()) {
//System.out.println(m1.group(0));
			String doctorID = m1.group(0).substring(10);
//System.out.println(doctorID);
			return doctorID;
		} return null;
	}
	
	@Override
	public int executeCount(String query) {
		// I believe this is only used to return the number of changes in "bb_changes" which, for this demo, will be zero
		return 0;
	}


	private void populate() {
		Map<String, Object> bbcp1 = new LinkedHashMap<String, Object>();
		bbcp1.put("doctorID", "defaultdoc");
		bbcp1.put("category", "cp");
		bbcp1.put("dueBase", "lastLetter"); 
		bbcp1.put("dueOffset", 1);
		bbcp1.put("dueUnit", "y"); //letters are due 1 year after the previous letter
		bbcp1.put("due2Base", "visitDate");
		bbcp1.put("due2Offset", 0);
		bbcp1.put("due2Unit", "d"); //or if there is no previous letter, they are due as soon as the patient visits
		bbcp1.put("remindBase", "visitDate");
		bbcp1.put("remindOffset", -1);
		bbcp1.put("remindUnit", "w"); //a reminder may be sent no earlier than 1 week before the visit date
		bbcp.add(bbcp1);
		Map<String, Object> bbcp2 = new LinkedHashMap<String, Object>();
		bbcp2.put("doctorID", "defaultdoc");
		bbcp2.put("category", "np");
		bbcp2.put("dueBase", "visitDate"); 
		bbcp2.put("dueOffset", 60);
		bbcp2.put("dueUnit", "d"); //letters are due 60 days after the visit
		bbcp2.put("due2Base", "visitDate");
		bbcp2.put("due2Offset", 60);
		bbcp2.put("due2Unit", "d"); //there is no back-up plan; all patients have a visit date
		bbcp2.put("remindBase", "visitDate");
		bbcp2.put("remindOffset", 6);
		bbcp2.put("remindUnit", "w"); //a reminder may be sent no earlier than 6 weeks after the visit
		bbcp.add(bbcp2);	
//doctorID may also use the doctor's type (e.g. department or other role)
		
		Map<String, Object> bbrp1 = new LinkedHashMap<String, Object>();
		bbrp1.put("doctorID", "defaultdoc");
		bbrp1.put("never", false);
		bbrp1.put("nth", 1);
		bbrp1.put("unit", "monday");
		bbrp1.put("mth", 1); // NRC is clever (i.e. difficult to explain). This sends a reminder the first monday of every month
		bbrp1.put("lastReminder", 0);
		bbrp.add(bbrp1);
				
		Map<String, Object> bbd1 = new LinkedHashMap<String, Object>();
		bbd1.put("doctorID", "who");
		bbd1.put("email", "who@");
		bbd1.put("salutation", "Dear Doctor");
		bbd1.put("type", "INT");
		bbd.add(bbd1);
		Map<String, Object> bbd2 = new LinkedHashMap<String, Object>();
		bbd2.put("doctorID", "strangelove");
		bbd2.put("email", "strangelove@");
		bbd2.put("salutation", "Dear Doctor Strangelove,");
		bbd2.put("type", "GER");
		bbd.add(bbd2);
	}


}
