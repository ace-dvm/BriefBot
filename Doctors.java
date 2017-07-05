package bbtrial.nl.logicgate.ace;

import java.util.List;
import java.util.Map;

/**
 * Gets information about the doctors
 * @author skmedlock
 *
 */

public class Doctors {

	private DoctorsI localDoctors;
	private List<Doctor> doctors;

	public Doctors(DoctorsI localDoctors){
		this.localDoctors = localDoctors;
		doctors = localDoctors.fillDoctors();
	}

	public boolean hasDoctor(String doctorID) {
		if(getDoctorByID(doctorID)!=null){
			return true;
		} else {
			return false;
		}
	}

	public Doctor getDoctorByID(String doctorID) {
		for(Doctor doctor : doctors){
			if(doctorID.equals(doctor.getDoctorID())){				
				return doctor;
			}
		}
		return null;
	}

	public List<Doctor> getDoctors() {
		return doctors;
	}

	public void close() {
		localDoctors.close();
	}

	/**
	 * Returns a list of additional information about the doctors
	 * to use in finding letters which are theirs. Since the type
	 * and number of data elements needed to make this classification
	 * will vary from place to place, it just returns the query result.
	 * @return letterInfo (a list of Map objects consisting of the column
	 * name and value)
	 */
	public List<Map<String, Object>> getLetterInfo() {
		return localDoctors.getLetterInfo();
	}

	public void lastReminders(List<Doctor> todaysDoctors) {
		localDoctors.lastReminders(todaysDoctors);
	}

	public boolean haveChanges() {
		return localDoctors.haveChanges();
	}

	public List<Patient> changeDoctors(List<Patient> newPatients) {
		return localDoctors.changeDoctors(newPatients);
	}

	
}
