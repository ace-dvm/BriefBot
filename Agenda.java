package bbtrial.nl.logicgate.ace;

import java.util.List;

/**
 * Gets the lists of new patients
 * Has a localAgenda object which connects to the hospital Agenda
 * @author skmedlock
 *
 */

public class Agenda {

	private AgendaI localAgenda;
	private List<Patient> newPatients;
	private List<Patient> noShows;
	
	public Agenda(AgendaI localAgenda, Doctors doctors){
		this.localAgenda = localAgenda;
		newPatients = null;
		noShows = null;
		fillPatients(doctors);
	}

	/**
	 * @return the newPatients
	 */
	public List<Patient> getNewPatients() {
		return newPatients;
	}

	/**
	 * @return the noShows
	 */
	public List<Patient> getNoShows() {
		return noShows;
	}
	
	private void fillPatients(Doctors doctors){
		newPatients = localAgenda.getNewPatients(doctors); 
			//newPatients are those added since BriefBot last ran. 
			//it's OK if this duplicates some patients from the last run.
		noShows = localAgenda.getNoShows(doctors); 
			//noShows are patients who did not show up for their appointment
	}

	public void close() {
		localAgenda.close();
	}
	
	
}
