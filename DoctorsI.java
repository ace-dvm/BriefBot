package bbtrial.nl.logicgate.ace;

import java.util.List;
import java.util.Map;

/**
 * Interface for getting stored information about doctors. Normally this
 * will be a database.
 * @author skmedlock
 *
 */

public interface DoctorsI {

	public List<Doctor> fillDoctors();
	
	public boolean close();

	public List<Map<String, Object>> getLetterInfo();

	public void lastReminders(List<Doctor> todaysDoctors);

	public boolean haveChanges();

	public List<Patient> changeDoctors(List<Patient> newPatients);

}
