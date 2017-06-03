package bbtrial.nl.logicgate.ace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Access to information about participating doctors.
 * This is an interface to BriefBot's DB, which we will populate by
 * hand. Maybe someday we'll make a web interface for populating it.
 * @author skmedlock
 *
 */

public class TrialDoctors implements DoctorsI {

	private QueryConnection docDB;
	private SQLObjects so;
	private HashMap<String, PatientCategoryPreference> defaultPCPs;
	private NextReminderCalculator defaultNRC;
		
	public TrialDoctors(){
		docDB = new QueryConnection();
		so = new SQLObjects();
		defaultPCPs = new HashMap<String, PatientCategoryPreference>();
		defaultNRC = new NextReminderCalculator();
	}
	
	@Override
	public List<Doctor> fillDoctors() {
		List<Doctor> doctors = null;
		String query = "SELECT * FROM bb_doctors";
		if(establishConnection()){
			String defaultPCPQuery = "SELECT * FROM bb_categoryPrefs WHERE doctorID='defaultdoc'";
			List<Map<String,Object>> pcpresult = docDB.getQuery().executeQuery(defaultPCPQuery);		
			defaultPCPs = makePatientCategoryPreferences(defaultPCPs, pcpresult);
			String defaultNRCQuery = "SELECT * FROM bb_reminderPrefs WHERE doctorID='defaultdoc'";
			List<Map<String,Object>> defaultNRCMap = docDB.getQuery().executeQuery(defaultNRCQuery);
			makeNRC(defaultNRC, defaultNRCMap.get(0)); //we expect exactly one entry with the name defaultdoc
			doctors = new ArrayList<Doctor>();
			List<Map<String,Object>> result = docDB.getQuery().executeQuery(query);
			for(Map<String,Object> docdata : result){
				doctors.add(mapToDoctor(docdata));
			}
			close();
		} else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Doctors could not connect to get doctor list");}
		return doctors;
	}

	@Override
	public boolean haveChanges() {
		if(establishConnection()){
			int bb_changeSize = docDB.getQuery().executeCount("bb_changes");
			if(bb_changeSize > 0){
				return true;
			} else {return false;}
		} else {
			new WriteFile().appendFile(BriefBot.ERROR_FILE, "Doctors could not connect to check for changes");
			return false;
		}
	}
	
	@Override
	public List<Patient> changeDoctors(List<Patient> newPatients) {
		//bb_change: old_dr | new_dr | first_day
		//where old_dr and new_dr contain the doctorID of the old and new drs
		//hopefully there'll be a 1:1 mapping
		String query = "SELECT * FROM bb_changes";
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		if(establishConnection()){
			result = docDB.getQuery().executeQuery(query);
			close();
		} else { new WriteFile().appendFile(BriefBot.ERROR_FILE, "Doctors could not connect to get changes"); }
		HashMap<String,String> pairs = new HashMap<String,String>();
		HashMap<String,RCalendar> old_firstDay = new HashMap<String,RCalendar>();
		HashMap<String,RCalendar> new_firstDay = new HashMap<String,RCalendar>();
		for(Map<String,Object> row : result){
			//map pairs in both directions
			pairs.put(so.objectToString(row.get("old_dr")), so.objectToString(row.get("new_dr")));
			pairs.put(so.objectToString(row.get("new_dr")), so.objectToString(row.get("old_dr")));
			//make a list of old doctors and the date
			old_firstDay.put(so.objectToString(row.get("old_dr")), so.sqlTimestampToRCalendar(row.get("first_day")));
			//make a list of new doctors and the date
			new_firstDay.put(so.objectToString(row.get("new_dr")), so.sqlTimestampToRCalendar(row.get("first_day")));
		}
		Set<String> old_dr = old_firstDay.keySet();
		Set<String> new_dr = new_firstDay.keySet();
		for(Patient patient : newPatients){
			if(old_dr.contains(patient.getDoctorID()) //the doctor is on the list of old doctors
					&& (patient.getVisitDate().equals(old_firstDay.get(patient.getDoctorID())) //the visit date is the new doctor's first day
							|| patient.getVisitDate().after(old_firstDay.get(patient.getDoctorID())))){ //or the visit date is after the new doctor's first day
				patient.setDoctorID(pairs.get(patient.getDoctorID())); //set the doctorID to the new doctor
			}
			if(new_dr.contains(patient.getDoctorID()) //the doctor is on the list of new doctors
					&& patient.getVisitDate().before(new_firstDay.get(patient.getDoctorID()))){ //and the visit date is before his first day
				patient.setDoctorID(pairs.get(patient.getDoctorID())); //set the doctorID to the old doctor
			}
		}
		return newPatients;
	}

	
	@Override
	public List<Map<String, Object>> getLetterInfo() {
		String query = "SELECT * FROM bb_doctors";
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		if(establishConnection()){
			 result = docDB.getQuery().executeQuery(query);
			 close();
		} else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Doctors could not connect to get letter info");}
		return result;
	}
	
	private Doctor mapToDoctor(Map<String, Object> docdata) {
		String doctorID = so.objectToString(docdata.get("doctorID"));
		Doctor d = new Doctor(doctorID);
		d.setEmail(so.objectToString(docdata.get("email")));
		d.setSalutation(so.objectToString(docdata.get("salutation")));
		String type = (so.objectToString(docdata.get("type")));
		HashMap<String, PatientCategoryPreference> patientCategoryPreferences = new HashMap<String, PatientCategoryPreference>();
		boolean newConn = false;
		if(!docDB.isConnected()){ //can use an existing connection or create its own
			newConn = establishConnection();
		}
		//first check for dr-specific prefs
		List<Map<String,Object>> categoryPrefs = docDB.getQuery().executeQuery("SELECT * FROM bb_categoryPrefs WHERE doctorID='" + doctorID + "'");
		List<Map<String,Object>> reminderPrefs = docDB.getQuery().executeQuery("SELECT * FROM bb_reminderPrefs WHERE doctorID='" + doctorID + "'");
		if((categoryPrefs==null || categoryPrefs.isEmpty()) && type!=null){
			//if there are no dr-specific prefs, try prefs by type
			categoryPrefs = docDB.getQuery().executeQuery(
					"SELECT * FROM bb_categoryPrefs WHERE doctorID='" + type + "'");
		}
		if((reminderPrefs==null || reminderPrefs.isEmpty()) && type!=null){
			//if there are no dr-specific prefs, try prefs by type
			reminderPrefs = docDB.getQuery().executeQuery(
					"SELECT * FROM bb_reminderPrefs WHERE doctorID='" + type + "'");
		}
		if(newConn){
			close();
		}
		if(categoryPrefs==null || categoryPrefs.isEmpty()){
			patientCategoryPreferences = defaultPCPs;
		} else {
			patientCategoryPreferences = makePatientCategoryPreferences(patientCategoryPreferences, categoryPrefs);
		}
		d.setPatientCategoryPreferences(patientCategoryPreferences);
		if(reminderPrefs==null || reminderPrefs.isEmpty()){
			d.setNrc(defaultNRC);
		} else {
			NextReminderCalculator nrc = new NextReminderCalculator();
			Map<String, Object> reminderPref = reminderPrefs.get(0); //we expect exactly one per doctor		
			d.setNrc(makeNRC(nrc, reminderPref));
		}
		return d;
	}

	/**
	 * @param nrc
	 * @param reminderPref
	 */
	private NextReminderCalculator makeNRC(NextReminderCalculator nrc,
			Map<String, Object> reminderPref) {
		nrc.setNever(Boolean.parseBoolean(reminderPref.get("never").toString().trim()));
		if(!nrc.isNever()){ //don't bother with the rest if it's "never"
			nrc.setN(so.objectToint(reminderPref.get("nth")));
			nrc.setUnit(interpretUnit(so.objectToString(reminderPref.get("unit"))));
			nrc.setM(so.objectToint(reminderPref.get("mth")));
			nrc.setPrevious(so.sqlTimestampToRCalendar(reminderPref.get("lastReminder")));
		}
		return nrc;
	}

	/**
	 * @param patientCategoryPreferences
	 * @param categoryPrefs
	 */
	private HashMap<String, PatientCategoryPreference> makePatientCategoryPreferences(
			HashMap<String, PatientCategoryPreference> patientCategoryPreferences,
			List<Map<String, Object>> categoryPrefs) {
		for(Map<String,Object> pref : categoryPrefs){
			PatientCategoryPreference pcp = new PatientCategoryPreference(so.objectToString(pref.get("category")));
			pcp.setDueBase(so.objectToString(pref.get("dueBase")));
			pcp.setDueOffset(so.objectToint(pref.get("dueOffset")));
			pcp.setDueUnit(so.objectToString(pref.get("dueUnit")));
			pcp.setDue2Base(so.objectToString(pref.get("due2Base")));
			pcp.setDue2Offset(so.objectToint(pref.get("due2Offset")));
			pcp.setDue2Unit(so.objectToString(pref.get("due2Unit")));
			pcp.setRemindBase(so.objectToString(pref.get("remindBase")));
			pcp.setRemindOffset(so.objectToint(pref.get("remindOffset")));
			pcp.setRemindUnit(so.objectToString(pref.get("remindUnit")));
			patientCategoryPreferences.put(pcp.getCategory(), pcp);
		}
		return patientCategoryPreferences;
	}
	
	private String interpretUnit(String u) {
		if(u.equalsIgnoreCase("ma")){return "monday";}
        if(u.equalsIgnoreCase("di")){return "tuesday";}
        if(u.equalsIgnoreCase("wo")){return "wednesday";}
        if(u.equalsIgnoreCase("do")){return "thursday";}
        if(u.equalsIgnoreCase("vr")){return "friday";}
        if(u.equalsIgnoreCase("za")){return "saturday";}
        if(u.equalsIgnoreCase("zo")){return "sunday";}
		return u;
	}

	@Override
	public void lastReminders(List<Doctor> todaysDoctors) {
		String today = new RCalendar().toString();
		if(establishConnection()){
			for(Doctor doctor : todaysDoctors){
				String query = "UPDATE bb_reminderPrefs SET lastReminder='" +
				today + "' WHERE doctorID='" + doctor.getDoctorID() + "'";
				docDB.getQuery().executeUpdate(query);
			}
			close();
		} else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Doctors could not connect to set last reminder");}
	}
	
	
	private boolean establishConnection(){
		return docDB.establishConnection("docDB");
		}

	public boolean close() {
		return docDB.close();
	}



}
