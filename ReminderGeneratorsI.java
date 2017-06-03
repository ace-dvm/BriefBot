package bbtrial.nl.logicgate.ace;

import java.util.List;
import java.util.Map;

public interface ReminderGeneratorsI {

	String generateReminderString(String salutation, List<Patient> patients);

	void send(Map<String, String> reminderStrings);

	void sendErrorLog();


}
