package bbtrial.nl.logicgate.ace;

import java.util.List;

public interface LettersI {

	List<Patient> findLetters(List<Patient> newPatients, Doctors doctors);

	List<Patient> checkForNewLetters(List<Patient> patients, Doctors doctors);

	boolean close();

}
