package bbtrial.nl.logicgate.ace;

import java.util.List;

/**
 * BBStorageI objects contain the information about BriefBot's working storage
 * Normally this will be a database
 * @author skmedlock
 *
 */

public interface BBStorageI {

	public RCalendar getLastCheckDate();

//	public void addExcluded(List<Patient> p);

	public List<Patient> getActivePatients();

	public List<Patient> noShow(List<Patient> noShows, List<Patient> patients);

	public void addDuplicate(Patient p, String dbidMatch);

	public void finish(List<Patient> finished);

	public void setLastCheckDate(RCalendar checkDate);

	public void reminderDates(List<Patient> todaysPatients);

	public void doNotRemind(List<Patient> noReminder);

	public List<Patient> getIncomplete();

	public void noteAbandonedLetter(List<Patient> incomplete);

	public void changeStatusToActive(List<Patient> makeActive);
	
	public boolean openConnection();
	
	public boolean close();

	public int getMaxDaysIncomplete();

	public List<Patient> mergeNewPatients(List<Patient> newPatients);

	public List<Patient> getAllPatients();

	public void rescheduled(RCalendar visitDate, Patient sp);

	public void cleanup();


	

	//this will either contain basic login information for the DB,
	//or specific queries for this DB
	
}
