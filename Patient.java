package bbtrial.nl.logicgate.ace;


/**
 * Patient objects are containers for patient-specific data.
 * Each Patient object represents a single patient-visit which may
 * require a letter.
 * @author skmedlock
 *
 */

public class Patient {

	private String pid;
	private String dept; //string indicating what service the patient was seen by
	private String category; //string code indicating what kind of patient this is
	private RCalendar visitDate;
	private Letter lastLetter; //most recent letter that was BEFORE the visit date
	private RCalendar letterDueDate; //should be calculated based on this doctor's preferences for this patient type
	private Letter newLetter; //a letter for this visitDate, by definition after the visitDate
	private String doctorID; //string code indicating which doctor is responsible for a letter for this visit
	private RCalendar firstPossibleReminderDate; //date when reminder is allowed per doctor's preferences
	private String dbid;
	private String status;
	
	public Patient(String pid){
		this.pid = pid;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the visitDate
	 */
	public RCalendar getVisitDate() {
		return visitDate;
	}

	/**
	 * @param visitDate the visitDate to set
	 */
	public void setVisitDate(RCalendar visitDate) {
		this.visitDate = visitDate;
	}

	/**
	 * @return the lastLetter
	 */
	public Letter getLastLetter() {
		return lastLetter;
	}

	/**
	 * @param lastLetter the lastLetter to set
	 */
	public void setLastLetter(Letter lastLetter) {
		this.lastLetter = lastLetter;
	}

	/**
	 * @return the newLetter
	 */
	public Letter getNewLetter() {
		return newLetter;
	}

	/**
	 * @param newLetter the newLetter to set
	 */
	public void setNewLetter(Letter newLetter) {
		this.newLetter = newLetter;
	}

	/**
	 * @return the doctorID
	 */
	public String getDoctorID() {
		return doctorID;
	}

	/**
	 * @param doctorID the doctorID to set
	 */
	public void setDoctorID(String doctorID) {
		this.doctorID = doctorID;
	}

	/**
	 * @return the pid
	 */
	public String getPID() {
		return pid;
	}
	
	/**
	 * @param letterDueDate the letterDueDate to set
	 */
	public void setLetterDueDate(RCalendar letterDueDate) {
		this.letterDueDate = letterDueDate;
	}

	/**
	 * @return the letterDueDate
	 */
	public RCalendar getLetterDueDate() {
		return letterDueDate;
	}

	/**
	 * @param firstPossibleReminderDate the firstPossibleReminderDate to set
	 */
	public void setFirstPossibleReminderDate(RCalendar firstPossibleReminderDate) {
		this.firstPossibleReminderDate = firstPossibleReminderDate;
	}

	/**
	 * @return the firstPossibleReminderDate
	 */
	public RCalendar getFirstPossibleReminderDate() {
		return firstPossibleReminderDate;
	}

	/**
	 * @param dbid the dbid to set
	 */
	public void setDBID(String dbid) {
		this.dbid = dbid;
	}

	/**
	 * @return the dbid
	 */
	public String getDBID() {
		return dbid;
	}

	/**
	 * @param dept the dept to set
	 */
	public void setDept(String dept) {
		this.dept = dept;
	}

	/**
	 * @return the dept
	 */
	public String getDept() {
		return dept;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
}
