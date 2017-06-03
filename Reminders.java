package bbtrial.nl.logicgate.ace;

import java.util.List;
import java.util.Map;

public class Reminders {

	private ReminderGeneratorsI rGen;
	
	public Reminders(ReminderGeneratorsI rGen){
		this.rGen = rGen;
	}

	public String generateReminderString(String salutation, List<Patient> patients) {
		return rGen.generateReminderString(salutation, patients);
	}

	public void send(Map<String, String> reminderStrings) {
		rGen.send(reminderStrings);
	}

	public void sendErrorLog() {
		rGen.sendErrorLog();
		
	}
	
}
