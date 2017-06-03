package bbtrial.nl.logicgate.ace;

import java.util.HashMap;

/**
 * Doctor is a container object for information about an individual doctor.
 * @author skmedlock
 *
 */

public class Doctor {
	private String doctorID;
	private String salutation;
	private String email;
	private HashMap<String, PatientCategoryPreference> patientCategoryPreferences; //one letterDueCalculator per patient type
	private NextReminderCalculator nrc; //next reminder date can be calculated on object creation
	
	public Doctor(String doctorID){
		this.doctorID = doctorID;
	}
	
	/**
	 * Determines the date that the letter for this visit is due,
	 * according to the doctor's preferences for this patient category.
	 * @param patient
	 */
	public void patientLetterDueDate(Patient patient) {
		RCalendar r = patientCategoryPreferences.get(patient.getCategory()).calculateLetterDueDate(patient);		
		patient.setLetterDueDate(r);
	}

	/**
	 * Determines the first possible reminder date for this patient.
	 * If the doctor does not want reminders for this patient category,
	 * firstPossibleReminderDate will be set to null. 
	 * @param patient
	 */
	public void firstPossibleReminderDate(Patient patient) {
		//TODO simplify this by dynamically loading a PCP based on the name of the
		//category pref in the database. Then we don't have to have a default and local,
		//we can just have a couple defaults and the option to make more.
		PatientCategoryPreference pcp = patientCategoryPreferences.get(patient.getCategory());
		if(pcp != null){
			RCalendar r = pcp.calculateFirstAllowedReminderDate(patient);
			patient.setFirstPossibleReminderDate(r);
		}
		//TODO test that remindBase is loaded as "null" and not empty string or string "NULL"
	}
	
	/**
	 * @return the salutation
	 */
	public String getSalutation() {
		return salutation;
	}

	/**
	 * @param salutation the salutation to set
	 */
	public void setSalutation(String salutation) {
		this.salutation = salutation;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the doctorID
	 */
	public String getDoctorID() {
		return doctorID;
	}

	/**
	 * @param patientCategoryPreferences the patientCategoryPreferences to set
	 */
	public void setPatientCategoryPreferences(
			HashMap<String, PatientCategoryPreference> patientCategoryPreferences) {
		this.patientCategoryPreferences = patientCategoryPreferences;
	}


	/**
	 * @param nrc the nrc to set
	 */
	public void setNrc(NextReminderCalculator nrc) {
		this.nrc = nrc;
	}

	/**
	 * @return the nrc
	 */
	public NextReminderCalculator getNrc() {
		return nrc;
	}

}
