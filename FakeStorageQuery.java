package bbtrial.nl.logicgate.ace;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FakeStorageQuery extends Query {

	private List<Map<String, Object>> bbp = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> bbd = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> bbs = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> bbl = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> bbr = new ArrayList<Map<String, Object>>();
		
	public FakeStorageQuery(Connection conn, PrintStream err) {
		super(conn, err);
		populate();
	}
	
	/**
	 * Emulates the database for SELECT queries for the Storage class
	 */
	@Override
	public List<Map<String, Object>> executeQuery(String query) {		
		if(query.equals("SELECT * FROM bb_patients WHERE status='active'")){
			List<Map<String,Object>> active = new ArrayList<Map<String,Object>>();
			for(Map<String,Object> map:bbp){
				if(map.get("status").toString().equals("active")){
					active.add(map);
				}
			}
			return active;
	    }
		if(query.equals("SELECT * FROM bb_patients WHERE status!='no_show'")){
			List<Map<String,Object>> allPt = new ArrayList<Map<String,Object>>();
			for(Map<String,Object> map:bbp){
				if(map.get("status").toString().equals("no_show")){
					//do nothing
				} else {
					allPt.add(map);
				}
			}
			return allPt;
		}
		if(query.equals("SELECT * FROM bb_patients WHERE status='inc'")){
			List<Map<String,Object>> inc = new ArrayList<Map<String,Object>>();
			for(Map<String,Object> map:bbp){
				if(map.get("status").toString().equals("inc")){
					inc.add(map);
				}
			}
			return inc;
		}
		if(query.contains("SELECT * FROM bb_letters WHERE pk='")){	
			List<Map<String,Object>> let = new ArrayList<Map<String,Object>>();
			String pk = extractDBID(query);
			for(Map<String,Object> map:bbl){
				if(map.get("pk").toString().equals(pk)){
					let.add(map);
				}
			}
			return let;
		}
		if(query.equals("SELECT lastCheckDate FROM bb_system")){
			List<Map<String,Object>> lastcheck = new ArrayList<Map<String,Object>>();
			for(Map<String,Object> map : bbs){
				if(map.containsKey("lastCheckDate")){					
					lastcheck.add(map);					
				}
			}
			return lastcheck;
		}
		if(query.equals("SELECT maxDaysIncomplete FROM bb_system")){
			List<Map<String,Object>> maxd = new ArrayList<Map<String,Object>>();
			for(Map<String,Object> map:bbs){
				if(map.containsKey("maxDaysIncomplete")){
					maxd.add(map);
				}
			}
			return maxd;
		}
		return null;
	    }
	
	/**
	 * Emulates the database for UPDATE queries for the Storage class
	 */
	@Override
	public int executeUpdate(String query) {
		if(query.contains("UPDATE bb_patients SET status='no_show'")){
			String dbid = extractDBID(query);
			for(Map<String,Object> map : bbp){
				if(map.get("pk").toString().equals(dbid)){
					map.put("status", "no_show");
					return 1;
				}
			}
			return 0;
		}
		if(query.contains("UPDATE bb_patients SET visitDate=")){
			Pattern p1 = Pattern.compile("[0-9]+-[0-9]+-[0-9]+");
			Matcher m1 = p1.matcher(query);
			String visitDate;
			if(m1.find()){
				visitDate= m1.group(0);
			} else {visitDate = null;}
			String dbid = extractDBID(query);
			for(Map<String,Object> map : bbp){
				if(map.get("pk").toString().equals(dbid)){
					map.put("visitDate", visitDate);
					return 1;
				}
			}
			return 0;

		}
		if(query.contains("UPDATE bb_patients SET status='finished'")){
			Pattern p1 = Pattern.compile("newLetter='[0-9]+");
			Matcher m1;
			String newLetID;
			try {
				m1 = p1.matcher(query);
				if(m1.find()){
					newLetID = m1.group(0).substring(11);
				} else {newLetID = "0";}
			} catch (Exception e) {
				newLetID = "0";
			}
			String dbid = extractDBID(query); 
			for(Map<String,Object> map : bbp){
				if(map.get("pk").equals(dbid)){
					map.put("status","finished");
					if(!newLetID.equals("0")){map.put("newLetter",newLetID);}
					return 1;
				}
			}
			return 0;
		}
		if(query.contains("UPDATE bb_patients SET status='inc'")){
			Pattern p1 = Pattern.compile("newLetter='[0-9]+");
			Matcher m1;
			String newLetID;
			try {
				m1 = p1.matcher(query);
				if(m1.find()){
					newLetID = m1.group(0).substring(11);	
				} else {newLetID="0";}
			} catch (Exception e) {
				newLetID = "0";
			}
			String dbid = extractDBID(query); 
			for(Map<String,Object> map : bbp){
				if(map.get("pk").equals(dbid)){
					map.put("status","inc");
					if(!newLetID.equals("0")){map.put("newLetter",newLetID);}
					return 1;
				}
			}
			return 0;
		}
		if(query.contains("UPDATE bb_patients SET status='noRemind'")){
			Pattern p1 = Pattern.compile("newLetter='[0-9]+");
			Matcher m1;
			String newLetID;
			try {
				m1 = p1.matcher(query);
				if(m1.find()){
					newLetID =  m1.group(0).substring(11);
				} else {newLetID ="0";}
			} catch (Exception e) {
				newLetID = "0";
			}
			String dbid = extractDBID(query); 
			for(Map<String,Object> map : bbp){
				if(map.get("pk").equals(dbid)){
					map.put("status","noRemind");
					if(!newLetID.equals("0")){map.put("newLetter",newLetID);}
					return 1;
				}
			}
			return 0;
		}
		if(query.contains("UPDATE bb_patients SET unfinishedLetter = newLetter")){
			String dbid = extractDBID(query);
			for(Map<String,Object> map : bbp){
				if(map.get("pk").equals(dbid)){
					Object newLet = map.get("unfinishedLetter");
					map.put("newLetter",newLet);
					return 1;
				}
			}
			return 0;
		}
		if(query.contains("UPDATE bb_patients SET newLetter = null")){
			String dbid = extractDBID(query);
			for(Map<String,Object> map : bbp){
				if(map.get("pk").equals(dbid)){
					map.put("newLetter",null);
					return 1;
				}
			}
			return 0;
		}
		if(query.contains("UPDATE bb_patients SET status='active'")){
			String dbid = extractDBID(query);
			for(Map<String,Object> map : bbp){
				if(map.get("pk").equals(dbid)){
					map.put("status","active");
					return 1;
				}
			}
			return 0;
		}
		if(query.contains("UPDATE bb_system SET lastCheckDate='" )){
			Timestamp date = new RCalendar(query.substring(36, query.length()-1)).toTimestamp();
			for(Map<String,Object> map:bbs){
				if(map.containsKey("lastCheckDate")){
					map.put("lastCheckDate",date);
					return 1;
				}
			}
			return 0;
		}
		return 0;
    }

	private String extractDBID(String query) {
		Pattern p1 = Pattern.compile("pk='[0-9]+");
		Matcher m1 = p1.matcher(query);
		String dbid;
		if(m1.find()){
			dbid = m1.group(0).replaceAll("[^0-9]", "");
		} else {dbid = null;}
		return dbid;
	}
	
    /**
     * Returns the primary keys of the affected rows as a list of strings
     * @param query
     */
	@Override
    public List<String> executeInsert(String query) {
    	List<String> ins = new ArrayList<String>();
    	if(query.contains("INSERT INTO bb_duplicates VALUES")){
    		String vals = query.substring(34);
    		String[] valA = vals.split(", ");
    		int ar = 0;
    		while(ar < valA.length){
    			valA[ar] = valA[ar].replaceAll("'", "");
    			if(valA[ar].equals("null")){
    				valA[ar] = null;
    				ar++;
    			}
    		}
       		String pk = Integer.toString(bbd.size() + 1);
    		Map<String,Object> m = new LinkedHashMap<String,Object>();
    		m.put("pk", pk);
    		m.put("entryDate",new RCalendar().toTimestamp());
    		m.put("PID",valA[0]);
    		m.put("dept",valA[1]);
    		m.put("doctorID",valA[2]);
    		m.put("category",valA[3]);
    		m.put("visitDate",valA[4]);
    		m.put("matchDBID", valA[5]);
    		bbp.add(m);
    		ins.add(pk);
    		return ins;
		}
    	if(query.contains("INSERT INTO bb_patients VALUES")){
    		String vals = query.substring(32);
    		String[] valA = vals.split(", ");
    		int ar = 0;
    		while(ar < valA.length){
    			valA[ar] = valA[ar].replaceAll("'", "");
    			if(valA[ar].equals("null")){
    				valA[ar] = null;
    				ar++;
    			}
    		}
       		String pk = Integer.toString(bbp.size() + 1);
    		Map<String,Object> m = new LinkedHashMap<String,Object>();
    		m.put("pk", pk);
    		m.put("PID",valA[0]);
    		m.put("dept",valA[1]);
    		m.put("category",valA[2]);
    		m.put("visitDate",valA[3]);
    		m.put("doctorID",valA[4]);
    		m.put("letterDueDate",valA[5]);
    		m.put("firstPossReminder",valA[6]);
    		m.put("lastLetter",valA[7]);
    		m.put("newLetter",valA[8]);
    		m.put("incLetter",null);
    		m.put("status","active");
    		bbp.add(m);
    		ins.add(pk);
    		return ins;
    	}
    	if(query.contains("INSERT INTO bb_letters VALUES")){
    		String vals = query.substring(31);
    		String[] valA = vals.split(", ");
    		int ar = 0;
    		while(ar < valA.length){
    			valA[ar] = valA[ar].replaceAll("'", "");
    			if(valA[ar].equals("null")){
    				valA[ar] = null;
    				ar++;
    			}
    		}
       		String pk = Integer.toString(bbl.size() + 1);
    		Map<String,Object> m = new LinkedHashMap<String,Object>();
    		m.put("pk", pk);
    		m.put("PID", valA[0]);
    		m.put("complete", valA[1]);
    		m.put("visitDate", new RCalendar(valA[2]).toTimestamp());
    		m.put("doctorID", valA[3]);
    		m.put("startDate",new RCalendar( valA[4]).toTimestamp());
    		m.put("finishDate", new RCalendar(valA[5]).toTimestamp());
    		m.put("dept", valA[6]);
    		m.put("author", valA[7]);
    		m.put("lastChangeDate", new RCalendar(valA[8]).toTimestamp());
    		m.put("brief_id", valA[9]);
    		bbl.add(m);
    		ins.add(pk);
    		return ins;    		
    	}
    	if(query.contains("INSERT INTO bb_reminderDates VALUES")){
    		Pattern p1 = Pattern.compile("[0-9]+-[0-9]+-[0-9]+");
			Matcher m1 = p1.matcher(query);
			String date;
			if(m1.find()){
				date = m1.group(0);
			} else {date = null;}
    		Map<String,Object> m = new LinkedHashMap<String,Object>();
    		m.put("pk", "1"); //since for this demo the system can only ever have one reminder date, we just give it the PK of 1
    		m.put("rDate", new RCalendar(date).toTimestamp());
    		bbr.add(m);
    		ins.add("1");
    		return ins;
    	}
    	return ins;
    }

	private void populate() {
		RCalendar today = new RCalendar();
		/* simulate bb_system table */
		Map<String, Object> system = new LinkedHashMap<String, Object>();
		system.put("lastCheckDate", new RCalendar("w",-1).toTimestamp()); //briefbot generally ran once per week
		system.put("maxDaysIncomplete", "90");
		bbs.add(system);
		
		/* simulate stored patients */
		Map<String, Object> bbp1 = new LinkedHashMap<String, Object>();
		bbp1.put("PID", "clara"); //clara is an np, so her letter is due 60 days after the visit
		bbp1.put("pk", "1");
		bbp1.put("status", "active");
		bbp1.put("dept", "INT");
		bbp1.put("category", "np");
		bbp1.put("visitDate", new RCalendar("d",-67).toTimestamp());
		bbp1.put("doctorID", "who");
		bbp1.put("letterDueDate", new RCalendar("d",-7).toTimestamp());
		bbp1.put("firstPossReminder", today.toTimestamp());
		bbp1.put("lastLetter", null);
		bbp1.put("newLetter", null);
		bbp.add(bbp1);
		Map<String, Object> bbp2 = new LinkedHashMap<String, Object>();
		bbp2.put("PID", "susan"); //susan is listed as a CP but has no recorded letter, so her letterDueDate = her visit date
		bbp2.put("pk", "2");
		bbp2.put("status", "active");
		bbp2.put("dept", "INT");
		bbp2.put("category", "cp");
		bbp2.put("visitDate", new RCalendar("d",-7).toTimestamp());
		bbp2.put("doctorID", "who");
		bbp2.put("letterDueDate", new RCalendar("d",-7).toTimestamp());
		bbp2.put("firstPossReminder", today.toTimestamp());
		bbp2.put("lastLetter", null);
		bbp2.put("newLetter", null);
		bbp.add(bbp2);
		Map<String, Object> bbp3 = new LinkedHashMap<String, Object>();
		bbp3.put("PID", "kama"); //kama was scheduled for an appointment but is now listed as a no-show
		bbp3.put("pk", "3");
		bbp3.put("status", "active");
		bbp3.put("dept", "INT");
		bbp3.put("category", "cp");
		bbp3.put("visitDate", new RCalendar("d",-7).toTimestamp());
		bbp3.put("doctorID", "who");
		bbp3.put("letterDueDate", new RCalendar("d",-7).toTimestamp());
		bbp3.put("firstPossReminder", today.toTimestamp());
		bbp3.put("lastLetter", null);
		bbp3.put("newLetter", null);
		bbp.add(bbp3);
		Map<String, Object> bbp4 = new LinkedHashMap<String, Object>();
		bbp4.put("PID", "katarina"); //katarina was scheduled for an appointment previously, but no longer appears on the agenda. This implies that the appointment was cancelled/no show.
		bbp4.put("pk", "4");
		bbp4.put("status", "active");
		bbp4.put("dept", "INT");
		bbp4.put("category", "cp");
		bbp4.put("visitDate", new RCalendar("d",-7).toTimestamp());
		bbp4.put("doctorID", "who");
		bbp4.put("letterDueDate", new RCalendar("d",-7).toTimestamp());
		bbp4.put("firstPossReminder", today.toTimestamp());
		bbp4.put("lastLetter", null);
		bbp4.put("newLetter", null);
		bbp.add(bbp4);
		Map<String, Object> bbp5 = new LinkedHashMap<String, Object>();
		bbp5.put("PID", "sarahjane"); //sarah is scheduled for an appointment but has a different appointment date in the new schedule (rescheduled)
		bbp5.put("pk", "5");
		bbp5.put("status", "active");
		bbp5.put("dept", "INT");
		bbp5.put("category", "cp");
		bbp5.put("visitDate", new RCalendar("d",-7).toTimestamp());
		bbp5.put("doctorID", "who");
		bbp5.put("letterDueDate", new RCalendar("d",-7).toTimestamp());
		bbp5.put("firstPossReminder", today.toTimestamp());
		bbp5.put("lastLetter", null);
		bbp5.put("newLetter", null);
		bbp.add(bbp5);
		Map<String, Object> bbp6 = new LinkedHashMap<String, Object>();
		bbp6.put("PID", "peri"); //peri has status noRemind; her letter is not due yet
		bbp6.put("pk", "6");
		bbp6.put("status", "noRemind");
		bbp6.put("dept", "INT");
		bbp6.put("category", "cp");
		bbp6.put("visitDate", new RCalendar("d",-7).toTimestamp());
		bbp6.put("doctorID", "who");
		bbp6.put("letterDueDate", new RCalendar("m",3).toTimestamp());
		bbp6.put("firstPossReminder", null);
		bbp6.put("lastLetter", "1");
		bbp6.put("newLetter", null);
		bbp.add(bbp6);
		Map<String, Object> bbl1 = new LinkedHashMap<String, Object>();
		bbl1.put("pk", "1");
		bbl1.put("PID", "peri");
		bbl1.put("complete", "true");
		bbl1.put("visitDate", new RCalendar("d",-7).toTimestamp());
		bbl1.put("doctorID", "who");
		bbl1.put("startDate", new RCalendar("m",-3).toTimestamp());
		bbl1.put("finishDate", new RCalendar("m",-3).toTimestamp());
		bbl1.put("dept", "INT");
		bbl1.put("author", "who");
		bbl1.put("lastChangeDate", new RCalendar("m",-3).toTimestamp());
		bbl1.put("brief_id", "n1984");
		bbl.add(bbl1);
		Map<String, Object> bbp7 = new LinkedHashMap<String, Object>();
		bbp7.put("PID", "ray"); //ray is a no-show archived in the system. Ray is due for a letter but didn't show up for the appointment, so no letter can be sent.
		bbp7.put("pk", "7");
		bbp7.put("status", "no_show");
		bbp7.put("dept", "INT");
		bbp7.put("category", "cp");
		bbp7.put("visitDate", new RCalendar("m",-3).toTimestamp());
		bbp7.put("doctorID", "who");
		bbp7.put("letterDueDate", new RCalendar("m",-3).toTimestamp());
		bbp7.put("firstPossReminder", null);
		bbp7.put("lastLetter", null);
		bbp7.put("newLetter", null);
		bbp.add(bbp7);
		Map<String, Object> bbp8 = new LinkedHashMap<String, Object>();
		bbp8.put("PID", "river"); //river has a letter that is incomplete. no newer letter is available, so she should get a reminder.
		bbp8.put("pk", "8");
		bbp8.put("status", "inc");
		bbp8.put("dept", "INT");
		bbp8.put("category", "cp");
		bbp8.put("visitDate", new RCalendar("d",-100).toTimestamp());
		bbp8.put("doctorID", "who");
		bbp8.put("letterDueDate", new RCalendar("d",-40).toTimestamp());
		bbp8.put("firstPossReminder", today);
		bbp8.put("lastLetter", null);
		bbp8.put("newLetter", "2");
		bbp.add(bbp8);
		Map<String, Object> bbl2 = new LinkedHashMap<String, Object>();
		bbl2.put("pk", "2");
		bbl2.put("PID", "river");
		bbl2.put("complete", "false");
		bbl2.put("visitDate", new RCalendar("d",-100).toTimestamp());
		bbl2.put("doctorID", "who");
		bbl2.put("startDate", new RCalendar("d",-100).toTimestamp());
		bbl2.put("finishDate", null);
		bbl2.put("dept", "INT");
		bbl2.put("author", "who");
		bbl2.put("lastChangeDate", new RCalendar("d",-100).toTimestamp());
		bbl2.put("brief_id", "n2012");
		bbl.add(bbl2);
		Map<String, Object> bbp9 = new LinkedHashMap<String, Object>();
		bbp9.put("PID", "liz");
		bbp9.put("pk", "8");
		bbp9.put("status", "active");
		bbp9.put("dept", "INT");
		bbp9.put("category", "cp");
		bbp9.put("visitDate", new RCalendar("d",-100).toTimestamp());
		bbp9.put("doctorID", "who");
		bbp9.put("letterDueDate", new RCalendar("d",-100).toTimestamp());
		bbp9.put("firstPossReminder", new RCalendar("d",-100).toTimestamp());
		bbp9.put("lastLetter", null);
		bbp9.put("newLetter", null);
		bbp.add(bbp9);
	}
}