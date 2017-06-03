package bbtrial.nl.logicgate.ace;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrialBBStorage implements BBStorageI {

	private QueryConnection storage;
	private SQLObjects so;
		
	public TrialBBStorage(){
		storage = new QueryConnection();
		so = new SQLObjects();
	}
	
	/**
	 * Gets the list of active patients out of the database
	 * @return patients - list of (active) stored patients
	 */
	@Override
	public List<Patient> getActivePatients() {
		List<Patient> activePatients = new ArrayList<Patient>();
		String query = "SELECT * FROM bb_patients WHERE status='active'";
		if(establishConnection()){
			List<Map<String,Object>> result = storage.getQuery().executeQuery(query);
			for(Map<String,Object> row : result){
				activePatients.add(makePatient(row));
			}
		} else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot get active patients from storage");}
		close();
		return activePatients;
	}
	
	/**
	 * Handles patients who did not show up for their appointment.
	 * @param ns (patient who did not show)
	 * @param patients (list of active patients from storage)
	 * @return list of active patients with no-show removed
	 */
	@Override
	public List<Patient> noShow(List<Patient> noShows, List<Patient> patients) {
		if(establishConnection()){
			for(Patient ns : noShows){
				String query = "UPDATE bb_patients SET status='no_show'" +
				" WHERE pk='" + ns.getDBID() + "'";
				storage.getQuery().executeUpdate(query);
			}
			close();
		} else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot update DB with no-shows");}
		patients.removeAll(noShows);
		return patients;
	}
	
	@Override
	public void addDuplicate(Patient p, String dbidMatch) {
		String today = "'" + new RCalendar().toString() + "'";
		boolean newConn = false;
		String query = "INSERT INTO bb_duplicates VALUES (" +
		today + ", " +
		so.objectToSQLValue(p.getPID()) + ", " + 
		so.objectToSQLValue(p.getDept()) + ", " +
		so.objectToSQLValue(p.getDoctorID()) + ", " +
		so.objectToSQLValue(p.getCategory()) + ", " + 
		so.objectToSQLValue(p.getVisitDate()) + ", " +
		so.objectToSQLValue(dbidMatch) + ")";
		if(!storage.isConnected()){ //can use an existing connection or make its own
			newConn = establishConnection();
		}
		storage.getQuery().executeInsert(query);
		if(newConn){
			close();
		}
	}
	
	/**
	 * Changes the visit date of a patient to the specified date
	 */
	@Override
	public void rescheduled(RCalendar newDate, Patient p){
		String query = "UPDATE bb_patients SET visitDate=" + 
				so.objectToSQLValue(newDate) + " WHERE pk='" + p.getDBID() + "'";;
		if(establishConnection()){
			storage.getQuery().executeUpdate(query);
			close();
		} else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot change visitDate for DBID " + p.getDBID());}
	}
	
	@Override
	public RCalendar getLastCheckDate() {
		RCalendar lastCheckDate = null;
		String query = "SELECT lastCheckDate FROM bb_system";
		if(establishConnection()){
			List<Map<String,Object>> result = storage.getQuery().executeQuery(query);
			Object date = result.get(0).get("lastCheckDate");			
			if(Timestamp.class.isInstance(date)){ //ensure that the casting is safe
			lastCheckDate = new RCalendar((Timestamp) date);
			} else {
				new WriteFile().appendFile(BriefBot.ERROR_FILE, "weird badness: lastCheckDate is not an SQL timestamp! \n");
			}
			close();
		} else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot get last check date from DB");}
		return lastCheckDate;
	}

	/**
	 * Returns the maxDaysIncomplete, which is the number of days a letter
	 * is allowed to be incomplete before it is considered abandoned. The
	 * default is zero, meaning that incomplete letters are not accepted.
	 */
	@Override
	public int getMaxDaysIncomplete() {
		String query = "SELECT maxDaysIncomplete FROM bb_system";
		int maxDaysIncomplete = 0;
		if(establishConnection()){
			List<Map<String,Object>> result = storage.getQuery().executeQuery(query);
			Object mdi = result.get(0).get("maxDaysIncomplete");
			try{
				maxDaysIncomplete = Integer.parseInt(mdi.toString());
			}
			catch(Exception e){//probably a NumberFormatException or NullPointer
				new WriteFile().appendFile(BriefBot.ERROR_FILE, "weird badness: maxDaysIncomplete is not parseable! " + e + "\n");
			}
			close();
		} else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot get max days incomplete from DB");}
		return maxDaysIncomplete;
	}
	
	/**
	 * Gets all patients from storage, except no-shows
	 */
	@Override
	public List<Patient> getAllPatients() {
		List<Patient> allPatients = new ArrayList<Patient>();
		String query = "SELECT * FROM bb_patients WHERE status!='no_show'";
		if(establishConnection()){
			List<Map<String,Object>> result = storage.getQuery().executeQuery(query);
			for(Map<String,Object> row : result){
				allPatients.add(makePatient(row));
			}
		} else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot get patients from storage");}
		close();
		return allPatients;
	}
	
	/**
	 * Gets all necessary patient information from the DB. This is the pair
	 * to createPatientInDB
	 * @param row
	 * @return
	 */
	private Patient makePatient(Map<String,Object> row) {
		Patient p = new Patient(row.get("PID").toString().trim());
		p.setDBID(row.get("pk").toString().trim());
		p.setStatus(row.get("status").toString().trim());
		p.setDept(so.objectToString(row.get("dept")));
		p.setCategory(so.objectToString(row.get("category")));
		p.setVisitDate(so.sqlTimestampToRCalendar(row.get("visitDate")));
		p.setDoctorID((so.objectToString(row.get("doctorID"))));
		p.setLetterDueDate(so.sqlTimestampToRCalendar(row.get("letterDueDate")));
		p.setFirstPossibleReminderDate(so.sqlTimestampToRCalendar(row.get("firstPossReminder")));
		String lastLetter = "";
		List<Map<String,Object>> resultLast = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> resultNew = new ArrayList<Map<String,Object>>();
		if(row.get("lastLetter")!=null){
			lastLetter = "SELECT * FROM bb_letters WHERE pk='"
				+ row.get("lastLetter").toString().trim() + "'";
			resultLast = storage.getQuery().executeQuery(lastLetter);
		}
		if(!resultLast.isEmpty()){
			p.setLastLetter(makeLetter(resultLast.get(0)));
		}
		String newLetter = ""; 
		if(row.get("newLetter")!=null){
			newLetter = "SELECT * FROM bb_letters WHERE pk='" 
				+ row.get("newLetter").toString().trim() + "'";;
			resultNew = storage.getQuery().executeQuery(newLetter);
		}
		if(!resultNew.isEmpty()){
			p.setNewLetter(makeLetter(resultNew.get(0)));
		}		
		return p;
	}

	/**
	 * Gets all necessary information about the patient letters from the database
	 * This is the pair to createLetterInDB
	 * @param row
	 * @return
	 */
	private Letter makeLetter(Map<String, Object> row) {
		Letter l = new Letter();
		l.setPID(row.get("PID").toString().trim());
		l.setComplete(Boolean.parseBoolean(row.get("complete").toString().trim())); //convoluted, but the simplest thing that seems to work
		l.setVisitDate(so.sqlTimestampToRCalendar(row.get("visitDate")));
		l.setStartDate(so.sqlTimestampToRCalendar(row.get("startDate")));
		l.setFinishDate(so.sqlTimestampToRCalendar(row.get("finishDate")));
		l.setDoctorID(so.objectToString(row.get("doctorID")));
		return l;
	}
	
	public List<Patient> mergeNewPatients(List<Patient> newPatients){
		if(establishConnection()){
			for(Patient patient : newPatients){
				String dbid = createPatientInDB(patient);
				patient.setDBID(dbid);
			}
			close();
		}
		return newPatients;
	}
	
	/**
	 * Creates a new patient in the database. Returns the unique ID
	 * of the patient in the database. Patients are created with the
	 * status 'active' in the database.
	 * @param patient
	 * @return dbid unique ID of the patient in the database
	 */
	private String createPatientInDB(Patient patient){
		String lastLetterKey = createLetterInDB(patient.getLastLetter());
		String newLetterKey = createLetterInDB(patient.getNewLetter());
		String query = "INSERT INTO bb_patients VALUES (" +
			so.objectToSQLValue(patient.getPID()) + ", " +
			so.objectToSQLValue(patient.getDept()) + ", " +
			so.objectToSQLValue(patient.getCategory()) + ", " +
			so.objectToSQLValue(patient.getVisitDate()) + ", " +
			so.objectToSQLValue(patient.getDoctorID()) + ", " +
			so.objectToSQLValue(patient.getLetterDueDate()) + ", " +
			so.objectToSQLValue(patient.getFirstPossibleReminderDate()) + ", " +
			so.objectToSQLValue(lastLetterKey) + ", " + 
			so.objectToSQLValue(newLetterKey) + ", " +
			null +
			", 'active')";
		boolean newConn = false;
		try {
			if(!storage.isConnected()){ //can use an existing connection or establish its own
				newConn = establishConnection();
			}
			List<String> key = storage.getQuery().executeInsert(query);
			if(newConn){
				close();
			}
			return key.get(0); //key should have only one entry, since we made only one insert.
		} catch (Exception e) {
			new WriteFile().appendFile(BriefBot.ERROR_FILE, e.toString() + query + "\n");
			return "0";
		}
	}
	

	protected String createLetterInDB(Letter letter) {
		if(letter==null){return null;}
		boolean newConn = false;
		String complete = "'0'";
		if(letter.isComplete()){
			complete = "'1'";
		}
		String query = "INSERT INTO bb_letters VALUES (" +
					so.objectToSQLValue(letter.getPID()) + ", " +
					complete + ", " + 
					so.objectToSQLValue(letter.getVisitDate()) + ", " + 
					so.objectToSQLValue(letter.getDoctorID()) + ", " + 
					so.objectToSQLValue(letter.getStartDate()) + ", " +  
					so.objectToSQLValue(letter.getFinishDate()) + ", " + 
					so.objectToSQLValue(letter.getDepartment()) + ", " + 
					so.objectToSQLValue(letter.getAuthor()) + ", " +
					so.objectToSQLValue(letter.getLastChangeDate()) + ", " +
					so.objectToSQLValue(letter.getBrief_id()) + ")";
		try {
			if(!storage.isConnected()){ //can use an existing connection or create its own
				newConn = establishConnection();
			}
			List<String> key = storage.getQuery().executeInsert(query);
			if(newConn){
				close();
			}
			return key.get(0); //key should have only one entry, since we made only one insert.
		} catch (Exception e) {
			new WriteFile().appendFile(BriefBot.ERROR_FILE, e.toString() + query + "\n");
			return "0";
		}
	}
	
	public void finish(List<Patient> patients){
		if(establishConnection()){
			for(Patient patient : patients){
				String dbid;
				String query;
				if(patient.getNewLetter().isComplete()
						/* N-- does not reliably convert letters to definitive. 
						   For now, consider all N-- letters to be complete.
						   TODO remove this line if N-- becomes reliable. */
						|| patient.getNewLetter().getBrief_id().startsWith("n") 	
					){ //all finished patients have a newLetter, no need to check the null condition
					query = "UPDATE bb_patients SET status='finished'";
				} else {
					query = "UPDATE bb_patients SET status='inc'";
				}
				if(patient.getDBID()==null){ //no dbid means the patient is not yet in the database
					dbid = createPatientInDB(patient);
					//createPatient will also add any letters, so no need to add them here
				} else {
					dbid = patient.getDBID();
					//a patient is finished because they got a new letter which won't be in the DB yet
					String letterKey = createLetterInDB(patient.getNewLetter());
					query += ", newLetter=" + so.objectToSQLValue(letterKey);
				}
				query += " WHERE pk='" + dbid + "'";
				storage.getQuery().executeUpdate(query);
			}
			close();
		} else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot update DB with finished patients");}
	}
	
	public void doNotRemind(List<Patient> patients){
		if(establishConnection()){
			for(Patient patient : patients){
				String dbid;
				String query = "UPDATE bb_patients SET status='noRemind'";
				if(patient.getDBID()==null){ //no dbid means the patient is not yet in the database
					dbid = createPatientInDB(patient);
				} else {
					dbid = patient.getDBID();
					if(patient.getNewLetter()!=null){
						String letterKey = createLetterInDB(patient.getNewLetter());
						query += ", newLetter=" + so.objectToSQLValue(letterKey);
					}
				}
				query += " WHERE pk='" + dbid + "'";
				storage.getQuery().executeUpdate(query);
			}
			close();
		} else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot update DB with no-remind");}
	}
	
	@Override
	public List<Patient> getIncomplete() {
		if(establishConnection()){
			String query = "SELECT * FROM bb_patients WHERE status='inc'";
			List<Patient> storedIncomplete = new ArrayList<Patient>();
			storage.getQuery().executeQuery(query);
			List<Map<String,Object>> result = storage.getQuery().executeQuery(query);
			for(Map<String,Object> row : result){
				storedIncomplete.add(makePatient(row));
			}
			close();
			return storedIncomplete;
		} 
		else {
			new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot get incomplete from DB");
			return null; }
	}
	
	@Override
	public void noteAbandonedLetter(List<Patient> incomplete){
		if(establishConnection()){
			String query1 = "UPDATE bb_patients SET unfinishedLetter = newLetter";
			String query2 = "UPDATE bb_patients SET newLetter = null";
			for(Patient p : incomplete){
				storage.getQuery().executeUpdate(query1 + " WHERE pk='" + p.getDBID() + "'");
				storage.getQuery().executeUpdate(query2 + " WHERE pk='" + p.getDBID() + "'");
			}
			close();
		}  else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot connect to DB to move letter to inc");}
	}
	
	@Override
	public void changeStatusToActive(List<Patient> makeActive) {
		if(establishConnection()){
			for(Patient patient : makeActive){
				String query = "UPDATE bb_patients SET status='active'" +
						" WHERE pk='" + patient.getDBID() + "'";
				storage.getQuery().executeUpdate(query);
			}
			close();
		}  else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot change patients to active");}
	}
	
	/**
	 * Opens a database connection
	 * @return
	 */
	public boolean openConnection(){
		return establishConnection();
	}
	
	@Override
	public void setLastCheckDate(RCalendar checkDate) {
		if(establishConnection()){
			String query = "UPDATE bb_system SET lastCheckDate='" 
				+ checkDate.toString() + "'";
			storage.getQuery().executeUpdate(query);
			close();
		} else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot set last check date");}
	}
	
	@Override
	public void reminderDates(List<Patient> todaysPatients) {
		String today = new RCalendar().toString();
		if(establishConnection()){
			for(Patient patient : todaysPatients){
				String query = "INSERT INTO bb_reminderDates VALUES ('" +
					patient.getDBID() + "', '" + today + "')";
				storage.getQuery().executeInsert(query); 
			}
			close();
		} else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot update DB with reminder dates");}
		
	}
	
	@Override
	public void cleanup(){
		// no cleanup needed for bbstorage
	}
	

	private boolean establishConnection(){
		return storage.establishConnection("storage");
	}

	public boolean close() {
		return storage.close();
	}

}
