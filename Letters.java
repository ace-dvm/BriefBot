package bbtrial.nl.logicgate.ace;

import java.util.List;

/**
 * Letters gets information about patient localLetters
 * @author skmedlock
 *
 */

public class Letters {

	private LettersI localLetters;
	
	public Letters(LettersI localLetters){
		this.localLetters = localLetters;
	}

	/**
	 * Finds the most recent letter dated BEFOREs the patient's visit
	 * date.
	 * @param newPatients
	 * @param doctors 
	 */
	public List<Patient> findLetters(List<Patient> newPatients, Doctors doctors) {
		newPatients = localLetters.findLetters(newPatients, doctors);
		return newPatients;
	}

	/**
	 * Checks a list of patients for new letters.
	 * If a new letter is found, it adds to the patient to the list
	 * of Finished patients, which is returned.
	 * @param patients
	 * @param doctors 
	 * @return finished patients
	 */
	public List<Patient> checkForNewLetters(List<Patient> patients, Doctors doctors) {
		return localLetters.checkForNewLetters(patients, doctors);
	}

	public void close() {
		localLetters.close();
	}

}
