package bbtrial.nl.logicgate.ace;

import java.util.List;

public interface AgendaI {

	/**
	 * @param doctors 
	 * @return the newPatients
	 */
	public List<Patient> getNewPatients(Doctors doctors);

	/**
	 * @param doctors 
	 * @return the noShows
	 */
	public List<Patient> getNoShows(Doctors doctors);

	public boolean close();
}
