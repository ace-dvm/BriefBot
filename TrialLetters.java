package bbtrial.nl.logicgate.ace;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrialLetters implements LettersI {

	private QueryConnection nDB;
	private QueryConnection pbDB;
	private HashMap<String, String> pbWhere; //<doctorID, WHERE clause>
	private HashMap<String, String> nWhere; //<doctorID, WHERE clause>
	private HashMap<Patient, PatientLetter> pletMap;
	private SQLObjects so;
	
	/**
	 * PatientLetter is a container for specific but unprocessed information
	 * about the letters for this patient. Its purpose is to make efficient use
	 * of the database connection by storing information for later processing.
	 * @author skmedlock
	 *
	 */
	private class PatientLetter{
		String pid;
		String nQueryLast;
		String pbQueryLast;
		String nQueryNew;
		String pbQueryNew;
		Map<String,Object> nResultLast;
		Map<String,Object> pbResultLast;
		Map<String,Object> nResultNew;
		Map<String,Object> pbResultNew;
		
		protected PatientLetter(String pid){
			this.pid = pid;
		}
	}
			
	public TrialLetters(){
		nDB = new QueryConnection();
		pbDB = new QueryConnection();
		pbWhere = new HashMap<String, String>();
		nWhere = new HashMap<String, String>();
		pletMap = new HashMap<Patient, PatientLetter>();
		so = new SQLObjects();
	}
	
	/**
	 * Finds both lastLetter and newLetter for patients just added from the agenda.
	 */
	@Override
	public List<Patient> findLetters(List<Patient> newPatients, Doctors doctors) {
		if(pbWhere.isEmpty()){
			fillWhereClauses(doctors);
		}
		//create all the queries
		for(Patient patient : newPatients){
			PatientLetter plet = new PatientLetter(patient.getPID());
			createNQueries(patient, plet);
			createPBQueries(patient, plet);
			pletMap.put(patient, plet);
		}
		//run the queries
		if(establishConnection()){
			for(Patient patient: newPatients){				
				List<Map<String, Object>> nLResult = nDB.getQuery().executeQuery(pletMap.get(patient).nQueryLast);
				if(nLResult.size()==1){ //should contain 1 or 0 rows.
					pletMap.get(patient).nResultLast = nLResult.get(0);
				}
				List<Map<String, Object>> pLResult = pbDB.getQuery().executeQuery(pletMap.get(patient).pbQueryLast);
				if(pLResult.size()==1){ //should contain 1 or 0 rows.
					pletMap.get(patient).pbResultLast = pLResult.get(0);
				}
				List<Map<String, Object>> nNResult = nDB.getQuery().executeQuery(pletMap.get(patient).nQueryNew);
				if(nNResult.size()==1){ //should contain 1 or 0 rows.
					pletMap.get(patient).nResultNew = nNResult.get(0);
				}
				List<Map<String, Object>> pNResult = pbDB.getQuery().executeQuery(pletMap.get(patient).pbQueryNew);
				if(pNResult.size()==1){ //should contain 1 or 0 rows.
					pletMap.get(patient).pbResultNew = pNResult.get(0);
				}
			}
			close();
		} else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot connect to letters databases to find letters");}
		//process the query results
		List<Patient> np = new ArrayList<Patient>();
		for(Patient patient : newPatients){
			findLastLetter(patient);
			findNewLetter(patient);
			np.add(patient);
		}
		return np;
	}

	/**
	 * Assigns a last letter if one exists
	 * @param patient
	 */
	private boolean findLastLetter(Patient patient) {
		//check if letter is an incomplete PBletter. If so, look for a complete letter.
		//TODO If we can trust n's letter status (concept/definitive) then we can also add a check for completeness for n letters here.
		if(pletMap.get(patient).pbResultLast != null){ //we have a PB letter
			if(so.objectToint(pletMap.get(patient).pbResultLast.get("stadiumnummer"))<60 //it is incomplete
					&& isAbandoned(so.sqlTimestampToRCalendar(pletMap.get(patient).pbResultLast.get("mutatiedatum")))){ //it is too old
				PatientLetter plet = new PatientLetter(patient.getPID());
				String pbQueryStem = createPBQueryStem(patient, plet);
				plet.pbQueryLast = pbQueryStem + 
				" AND initiatiedatum < CAST('" + patient.getVisitDate().toString() + "' AS datetime)" +
				" AND stadiumnummer='60' ORDER BY initiatiedatum DESC";
				if(establishConnection()){
					List<Map<String,Object>> pResult = pbDB.getQuery().executeQuery(plet.pbQueryLast);
					if(pResult.size()==1){ //size should be 0 or 1
						plet.pbResultLast = pResult.get(0);
					}
					close();
				} else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot establish connection to look for complete letters");}
			}
		}
		if(pletMap.get(patient).nResultLast != null && pletMap.get(patient).pbResultLast != null){
			//we have a candidate letter from each database. Pick the newest one.
			RCalendar nStart = so.sqlTimestampToRCalendar(pletMap.get(patient).nResultLast.get("aanmaakDT"));
			RCalendar pStart = so.sqlTimestampToRCalendar(pletMap.get(patient).pbResultLast.get("initiatiedatum"));
			if(nStart.after(pStart)){
				patient.setLastLetter(makeNLetter(pletMap.get(patient).nResultLast, patient));
				return true;
			} else { //pStart must be newer
				patient.setLastLetter(makePBLetter(pletMap.get(patient).pbResultLast, patient));
				return true;
			}
		} else if(pletMap.get(patient).nResultLast != null){
			patient.setLastLetter(makeNLetter(pletMap.get(patient).nResultLast, patient));
			return true;
		} else if(pletMap.get(patient).pbResultLast != null){
			patient.setLastLetter(makePBLetter(pletMap.get(patient).pbResultLast, patient));
			return true;
		} //else they are both null. There are no eligible last letters.
		return false;
	}

	/**
	 * Assigns a new letter, if one exists
	 * @param patient
	 * @return true if a new letter is found
	 */
	private boolean findNewLetter(Patient patient) {
		if(pletMap.get(patient).nResultNew != null && pletMap.get(patient).pbResultNew != null){
			//we have a candidate letter from each database. Take the one with the oldest start date.
			RCalendar nStart = so.sqlTimestampToRCalendar(pletMap.get(patient).nResultNew.get("aanmaakDT"));
			RCalendar pStart = so.sqlTimestampToRCalendar(pletMap.get(patient).pbResultNew.get("initiatiedatum"));
			if(nStart.before(pStart)){
				patient.setNewLetter(makeNLetter(pletMap.get(patient).nResultNew, patient));
				return true;
			} else { //pStart must be older
				patient.setNewLetter(makePBLetter(pletMap.get(patient).pbResultNew, patient));
				return true;
			}
		} else if(pletMap.get(patient).nResultNew != null){
			patient.setNewLetter(makeNLetter(pletMap.get(patient).nResultNew, patient));
			return true;
		} else if(pletMap.get(patient).pbResultNew != null){
			patient.setNewLetter(makePBLetter(pletMap.get(patient).pbResultNew, patient));
			return true;
		} //else they are both null. There are no eligible new letters.
		return false;
	}
	
	/**
	 * Creates a Letter object from a nMetagegevens database entry
	 * @param nResult
	 * @param patient
	 * @return
	 */
	private Letter makeNLetter(Map<String, Object> nResult, Patient patient) {
		Letter nl = new Letter();
		nl.setPID(patient.getPID());
		String complete = so.objectToString(nResult.get("status"));
		if(complete.equals("D")){
			nl.setComplete(true);
		} //else it remains false
		nl.setDoctorID(patient.getDoctorID());
		if(Timestamp.class.isInstance(nResult.get("aanmaakDT"))){ //safe casting
			nl.setStartDate(new RCalendar((Timestamp) nResult.get("aanmaakDT")));
		}
		nl.setDepartment(so.objectToString(nResult.get("specialismeID")));
		nl.setAuthor(so.objectToString(nResult.get("ondertekenaar")));
		if(Timestamp.class.isInstance(nResult.get("aanpassingDT"))){ //safe casting
			RCalendar mut = new RCalendar((Timestamp) nResult.get("aanpassingDT"));
			nl.setLastChangeDate(mut);
			if(nl.isComplete()){
				nl.setFinishDate(mut);
			}
		}
		nl.setBrief_id("n" + nResult.get("nBriefID"));
		return nl;
	}
	
	/**
	 * Creates a Letter object from a pb database entry
	 * @param pbResult
	 * @param patient
	 * @return
	 */
	private Letter makePBLetter(Map<String, Object> pbResult, Patient patient) {
		Letter pbl = new Letter();
		pbl.setPID(patient.getPID());
		if(so.objectToString(pbResult.get("stadiumnummer")).equals("60")){
			pbl.setComplete(true);
		}
		if(Timestamp.class.isInstance(pbResult.get("einddatum_behandeling"))){ //safe casting
			pbl.setVisitDate(new RCalendar((Timestamp) pbResult.get("einddatum_behandeling")));
		}
		pbl.setDoctorID(patient.getDoctorID());
		if(Timestamp.class.isInstance(pbResult.get("initiatiedatum"))){ //safe casting
			pbl.setStartDate(new RCalendar((Timestamp) pbResult.get("initiatiedatum")));
		}
		if(Timestamp.class.isInstance(pbResult.get("bevestigingsdatum"))){ //safe casting
			if(pbl.isComplete()){
				pbl.setFinishDate(new RCalendar((Timestamp) pbResult.get("bevestigingsdatum")));
			}
		}
		pbl.setAuthor(so.objectToString(pbResult.get("codreg_auteuromschrijving")));
		pbl.setDepartment(so.objectToString(pbResult.get("briefinstellingscode")));
		if(Timestamp.class.isInstance(pbResult.get("mutatiedatum"))){ //safe casting
			pbl.setLastChangeDate(new RCalendar((Timestamp) pbResult.get("mutatiedatum")));
		}
		pbl.setBrief_id("p" + so.objectToString(pbResult.get("patientenbrief_vwz")));
		return pbl;
	}

	/**
	 * @param patient
	 * @param plet
	 */
	private void createNQueries(Patient patient, PatientLetter plet) {
		String queryStem = createNQueryStem(patient, plet);
		plet.nQueryLast = queryStem;
		plet.nQueryNew = queryStem;
		if(patient.getCategory().equalsIgnoreCase("np")){
//Code for dealing with NP's possibly transferred to other depts.
			try {
				int indexOfCloseParen = plet.nQueryNew.lastIndexOf(")"); //get index of last close paren
				String queryNew = plet.nQueryNew.substring(0, indexOfCloseParen);
				queryNew += "OR specialismeID='0')"; //specialismIDs removed 
				plet.nQueryNew = queryNew;
			} catch (StringIndexOutOfBoundsException e) {
				// TODO Auto-generated catch block
				new WriteFile().appendFile(BriefBot.ERROR_FILE, 
						e.toString() + "patient: " + patient.getPID() + "\n" +
						"query: + " + plet.nQueryNew + "\n");
			}
//end special NP code			
		}
		if(patient.getVisitDate()!=null){
			plet.nQueryLast += " AND aanmaakDT < CAST('" + patient.getVisitDate().toString() + "' AS datetime)";
			plet.nQueryNew += " AND aanmaakDT >= CAST('" + patient.getVisitDate().toString() + "' AS datetime)";
			}
		plet.nQueryLast += " ORDER BY aanmaakDT DESC, status DESC"; //newest first, finished on top (noted as Concept or Definitief)
		plet.nQueryNew += " ORDER BY aanmaakDT ASC, status DESC"; //oldest (closest to visit date) first, finished on top
	}
	/**
	 * @param patient
	 * @param plet
	 * @return
	 */
	private String createNQueryStem(Patient patient, PatientLetter plet) {
		String queryStem = "SELECT TOP 1 * FROM n " +
			"WHERE patientID='" + plet.pid + "' ";		
		if(nWhere.get(patient.getDoctorID())==null){ //if nWhere for this doctor is null, we expect no letters in n
			queryStem += " AND (specialismeID='0')"; //should ensure that no results are returned
		} else {
			queryStem += nWhere.get(patient.getDoctorID());
		}
		return queryStem;
	}
	
	private void createPBQueries(Patient patient, PatientLetter plet){
		String queryStem = createPBQueryStem(patient, plet);
		plet.pbQueryLast = queryStem;
		plet.pbQueryNew = queryStem;
		if(patient.getCategory().equalsIgnoreCase("np")){
//code for dealing with NP possibly transferred to other depts
			try {
				int indexOfCloseParen = plet.pbQueryNew.lastIndexOf(")"); //get index of last close paren
				String queryNew = plet.pbQueryNew.substring(0, indexOfCloseParen);
				queryNew += "OR briefinstellingomschrijving LIKE '%poli%')";//try accepting all polikliniek letters
				plet.pbQueryNew = queryNew;
			} catch (StringIndexOutOfBoundsException e) {
				// TODO Auto-generated catch block
				new WriteFile().appendFile(BriefBot.ERROR_FILE, 
						e.toString() + "patient: " + patient.getPID() + "\n" +
						"query: + " + plet.pbQueryNew + "\n");
			}
//end NP code			
		}
		if(patient.getVisitDate()!=null){
			plet.pbQueryLast += " AND initiatiedatum < CAST('" + patient.getVisitDate().toString() + "' AS datetime)";
			plet.pbQueryNew += " AND initiatiedatum >= CAST('" + patient.getVisitDate().toString() + "' AS datetime)";
		}
		plet.pbQueryLast += " ORDER BY initiatiedatum DESC, stadiumnummer DESC"; //newest letter on top, finished letters on top
		plet.pbQueryNew += " ORDER BY initiatiedatum ASC, stadiumnummer DESC"; //oldest letter (closest to visit) on top, finished letters on top
	}
	
	/**
	 * @param patient
	 * @param plet
	 * @return
	 */
	private String createPBQueryStem(Patient patient, PatientLetter plet) {
		String queryStem = "SELECT TOP 1 * FROM pb " +
			"WHERE patientnummer='" + plet.pid + "' ";
		if(pbWhere.get(patient.getDoctorID())==null){ //if pbWhere for this doctor is null, we expect no letters from PatBrief 
			queryStem += " AND briefinstellingscode='0'"; //should ensure that no results are returned
//TODO this is kind of a hack. We should handle this more gracefully.
		} else {
			queryStem += pbWhere.get(patient.getDoctorID());
		}
		return queryStem;
	}

	/**
	 * @param doctors
	 */
	private void fillWhereClauses(Doctors doctors) {
		List<Map<String,Object>> whereClauses = doctors.getLetterInfo();
		for(Map<String,Object> row : whereClauses){
			String pb1 = so.objectToString(row.get("pbwhere"));
			String pb2 = so.objectToString(row.get("pb_auteur"));
			String pbWhereString = null;
			if(pb1!=null){
				pbWhereString = " AND (" + pb1;
				if(pb2!=null){
					pbWhereString += " " + pb2; 
				}
				pbWhereString += ")";
			}
			pbWhere.put(so.objectToString(row.get("doctorID")), pbWhereString);
			String n1 = so.objectToString(row.get("nwhere"));
			String n2 = so.objectToString(row.get("n_ondertek"));
			//pbWhere = " AND (pbwhere pb_auteur)"
			String nWhereString = null;
			if(n1!=null){
				nWhereString = " AND (" + n1;
				if(n2!=null){
					nWhereString += " " + n2; 
				}
				nWhereString += ")";
			}
			nWhere.put(so.objectToString(row.get("doctorID")), nWhereString);
		}
	}

	/**
	 * Checks for new letters. Intended to be used with stored patients.
	 */
	@Override
	public List<Patient> checkForNewLetters(List<Patient> patients, Doctors doctors) {
		List<Patient> finished = new ArrayList<Patient>();
		if(pbWhere.isEmpty()){
			//shouldn't need this, but won't hurt
			fillWhereClauses(doctors);
		}
		//create all the queries
		for(Patient patient : patients){
			PatientLetter plet = new PatientLetter(patient.getPID());
			createNQueries(patient, plet);
			createPBQueries(patient, plet);
			pletMap.put(patient, plet);
		}
		//run the newLetter queries
		if(establishConnection()){
			for(Patient patient: patients){
				List<Map<String, Object>> nNResult = nDB.getQuery().executeQuery(pletMap.get(patient).nQueryNew);
				if(nNResult.size()==1){//should contain 1 or 0 rows.
						pletMap.get(patient).nResultNew = nNResult.get(0);
					}
				List<Map<String, Object>> pNResult = pbDB.getQuery().executeQuery(pletMap.get(patient).pbQueryNew);
				if(pNResult.size()==1){ //should contain 1 or 0 rows.
					if(so.objectToint(pNResult.get(0).get("stadiumnummer"))<60
							&& isAbandoned(so.sqlTimestampToRCalendar(pNResult.get(0).get("mutatiedatum")))){
						//NOTE that this uses last-modified date rather than start date
						pletMap.get(patient).pbQueryNew = pletMap.get(patient).pbQueryNew.replace("ORDER BY", "AND stadiumnummer='60' ORDER BY");
						pNResult = pbDB.getQuery().executeQuery(pletMap.get(patient).pbQueryNew);
					}
				}
				if(pNResult.size()==1){
					pletMap.get(patient).pbResultNew = pNResult.get(0);
				}
			}
			close();
		} else {new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot establish connection to look for new letters");}
		for(Patient patient : patients){
			if(findNewLetter(patient)){
				finished.add(patient);
			}
		}
		return finished;
	}
		
	private boolean isAbandoned(RCalendar r){
		RCalendar expires = new RCalendar(r, "d", BriefBot.MAX_DAYS_INC);
		if(expires.before(new RCalendar())){ //if it expires before today (the expiry date has passed)
			return true;
		} else { return false; }
	}
	
	private boolean establishConnection(){
		if(nDB.establishConnection("ndb") && pbDB.establishConnection("pbdb")){
			return true;
		} else { return false; }
	}
	
	public boolean close(){
		if(nDB.close() && pbDB.close()){
			return true;
		} else { return false; }
	}	
}
