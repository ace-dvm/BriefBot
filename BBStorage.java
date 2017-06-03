package bbtrial.nl.logicgate.ace;

import java.util.ArrayList;
import java.util.List;

public class BBStorage {

	private BBStorageI localBBStorage;
	private List<Patient> activePatients;
	private RCalendar lastCheckDate;
	
	public BBStorage(BBStorageI localBBStorage){
		activePatients = new ArrayList<Patient>();
		this.localBBStorage = localBBStorage;
		lastCheckDate = null;
	}

	/**
	 * Retrieves patients who are active (in need of a letter
	 * as of the last time letters were checked).
	 * @return activePatients
	 */
	public List<Patient> getActivePatients() {
		if(activePatients.isEmpty()){
			activePatients = localBBStorage.getActivePatients();
		}
		return activePatients;
	}

	/**
	 * Gets the last date that BriefBot ran out of the DB
	 * @return
	 */
	public RCalendar getLastCheckDate() {
		if(this.lastCheckDate==null){ //if it hasn't been set yet, get it from localStorage
			this.lastCheckDate = localBBStorage.getLastCheckDate();
		}
		return lastCheckDate;
		
	}
	
	/**
	 * Sets the date that BriefBot ran in the DB
	 * @param includeStrings
	 */
	public void setLastCheckDate(RCalendar checkDate){
		this.lastCheckDate = checkDate;
		localBBStorage.setLastCheckDate(checkDate);
	}
	
	/**
	 * Handles a patient who didn't show for their appointment.
	 * They are flagged as "no_show" in BriefBot's database, and
	 * (if they are on the list of active patients) removed from 
	 * that list.
	 * @param sp
	 */
	public void noShow(List<Patient> noShows) {
		activePatients = localBBStorage.noShow(noShows, activePatients);
		//keep in mind that the patient may not exist yet in the localBBStorage
	}

	/**
	 * Stores information about a patient identified as a duplicate.
	 * This may be a duplicate entry for the same patient visit,
	 * or it may be that the same patient returned for another visit
	 * with the same doctor while still active in BriefBot.
	 * @param np
	 */
	public void addDuplicate(Patient p, String dbidMatch) {
		localBBStorage.addDuplicate(p, dbidMatch);
	}

	/*
	 * Stores information about patients who are in the Agenda but
	 * excluded from BriefBot. Generally this is because the doctor
	 * is not recognized by BriefBot.
	 * @param p
	 */
/*	public void addExcluded(List<Patient> p) {
		localBBStorage.addExcluded(p);		
	}*/
	
	/**
	 * Gets all patients from storage, except no-shows
	 */
	public List<Patient> getAllPatients(){
		return localBBStorage.getAllPatients();
	}

	/**
	 * Handles a list of patients who are finished (i.e. a 
	 * letter was found which is sufficient for this visit). 
	 * They are flagged as "finished" in BriefBot's database, 
	 * and (if they are on the list of active patients) 
	 * removed from the list of active patients.
	 * @param finished
	 */
	public void finish(List<Patient> finished) {
		localBBStorage.finish(finished);
		activePatients.removeAll(finished);
	}
	
	/**
	 * Handles a list of patients who will never get a reminder.
	 * They will be registered in the system but will not remain
	 * active. The system will not check for additional letters
	 * pertaining to this patient visit.
	 * @param noReminder
	 */
	public void doNotRemind(List<Patient> noReminder) {
		localBBStorage.doNotRemind(noReminder);
		activePatients.removeAll(noReminder);
	}

	/**
	 * Gets patients whose letters were at status=incomplete
	 * as of the last time BriefBot ran
	 * @return incomplete
	 */
	public List<Patient> getIncomplete() {
		return localBBStorage.getIncomplete();
	}

	/**
	 * Makes a record of abandoned letters (letters
	 * which have been incomplete for longer than the
	 * max days incomplete).
	 * @param abandoned: patients with abandoned letters
	 */
	public void noteAbandonedLetter(List<Patient> abandoned) {
		localBBStorage.noteAbandonedLetter(abandoned);
	}
	
	/**
	 * Changes the status of a list of patients to "Active" in the database
	 * so that patients are eligible for reminders
	 * @param makeActive
	 */
	public void changeStatusToActive(List<Patient> makeActive) {
		localBBStorage.changeStatusToActive(makeActive);
	}
	
	/**
	 * Records in the database which patients had a reminder sent
	 * about them.
	 * @param todaysPatients
	 */
	public void reminderDates(List<Patient> todaysPatients) {
		localBBStorage.reminderDates(todaysPatients);
	}

//not used
//	/**
//	 * Opens a connection to the local storage
//	 */
//	public boolean openConnection(){
//		return localBBStorage.openConnection();
//	}

	/**
	 * Closes the connection to local Storage
	 */
	public void close() {
		localBBStorage.close();
	}

	/**
	 * Gets the maxiumum number of days a letter may remain incomplete
	 * before it is considered abandoned.
	 * @return maxDaysIncomplete
	 */
	public int getMaxDaysIncomplete() {
		return localBBStorage.getMaxDaysIncomplete();
	}

	/**
	 * Adds new patients from the agenda to localStorage
	 * @param agendaPatients
	 * @return agendaPatients with dbid's assigned
	 */
	public List<Patient> mergeNewPatients(List<Patient> agendaPatients) {
		return localBBStorage.mergeNewPatients(agendaPatients);
	}

	/**
	 * Changes the visit date of the specified patient in 
	 * local storage.
	 * @param visitDate
	 * @param sp
	 */
	public void rescheduled(RCalendar visitDate, Patient sp) {
		localBBStorage.rescheduled(visitDate, sp);
	}

	/**
	 * Perform any maintenance on the localStorage - normalizing, etc.
	 */
	public void cleanup() {
		localBBStorage.cleanup();
	}



}
