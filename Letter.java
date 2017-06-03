package bbtrial.nl.logicgate.ace;

/**
 * Letter is a container object for information about a specific patient letter
 * @author skmedlock
 *
 */

public class Letter {

	private String pid;
	private boolean complete;
	private RCalendar visitDate;
	private String doctorID;
	private RCalendar startDate;
	private RCalendar finishDate;
	private String department;
	private String author;
	//these values are not used, but are recorded for auditing
	private RCalendar lastChangeDate;
	private String brief_id; //the letter's unique ID in the *hospital* db

	public Letter(){
		pid = null;
		complete = false;
		visitDate = null;
		doctorID = null;
		startDate = null;
		finishDate = null;
		department = null;
		author = null;
		lastChangeDate = null;
		brief_id = null;
	}
	
	/**
	 * @return the pid
	 */
	public String getPID() {
		return pid;
	}
	/**
	 * @param pid the pid to set
	 */
	public void setPID(String pid) {
		this.pid = pid;
	}
	/**
	 * @return the complete
	 */
	public boolean isComplete() {
		return complete;
	}
	/**
	 * @param complete the complete to set
	 */
	public void setComplete(boolean complete) {
		this.complete = complete;
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
	 * @return the startDate
	 */
	public RCalendar getStartDate() {
		return startDate;
	}
	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(RCalendar startDate) {
		this.startDate = startDate;
	}
	/**
	 * @return the finishDate
	 */
	public RCalendar getFinishDate() {
		return finishDate;
	}
	/**
	 * @param finishDate the finishDate to set
	 */
	public void setFinishDate(RCalendar finishDate) {
		this.finishDate = finishDate;
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
	 * @return the lastChangeDate
	 */
	public RCalendar getLastChangeDate() {
		return lastChangeDate;
	}
	/**
	 * @param lastChangeDate the lastChangeDate to set
	 */
	public void setLastChangeDate(RCalendar lastChangeDate) {
		this.lastChangeDate = lastChangeDate;
	}
	/**
	 * @return the department
	 */
	public String getDepartment() {
		return department;
	}
	/**
	 * @param department the department to set
	 */
	public void setDepartment(String department) {
		this.department = department;
	}
	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}
	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}
	/**
	 * @return the brief_id
	 */
	public String getBrief_id() {
		return brief_id;
	}
	/**
	 * @param briefId the brief_id to set
	 */
	public void setBrief_id(String briefId) {
		brief_id = briefId;
	}
		


}
