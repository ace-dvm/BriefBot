package bbtrial.nl.logicgate.ace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

public class TrialReminderGenerator implements ReminderGeneratorsI {

	private EmailSender emailSender;
	private String admin;
	
	public TrialReminderGenerator(){
		String smtpServer = null;
		int smtpPort = 0;
		String smtpAccount = null;
        String smtpPassword = "";
        boolean enableTLS = false;
        String defaultFrom = "\"Snelle Cor\"";
		emailSender = new SmtpEmailSender(smtpServer, smtpPort, smtpAccount,
	            smtpPassword, enableTLS, defaultFrom);
		admin = "";
	}
	
	@Override
	public String generateReminderString(String salutation, List<Patient> patients) {
		RCalendar today = new RCalendar();
		List<Patient> np = new ArrayList<Patient>();
		List<Patient> cpp = new ArrayList<Patient>();
		List<Patient> cpf = new ArrayList<Patient>();
		for(Patient patient : patients){
			if(patient.getCategory().equalsIgnoreCase("NP")
				&& (patient.getVisitDate().before(today)
				|| patient.getVisitDate().equals(today))){
					np.add(patient);
				}
			if(patient.getCategory().equalsIgnoreCase("CP")
				&& (patient.getVisitDate().before(today)
				|| patient.getVisitDate().equals(today))){
				cpp.add(patient);
			}
			if(patient.getCategory().equalsIgnoreCase("CP")
				&& patient.getVisitDate().after(today)){
				cpf.add(patient);
			}
		}
		String todayString = dayOfWeekToDutch(today.getStringDayOfWeek()) + " " + today.getDayOfMonth() + " " + today.get3LetterMonth();
		String reminderString = salutation + "\n"
			+ "\nLaatste controle voor brieven: " + todayString + "\n\n";
		if(np.isEmpty()){
			reminderString += "Geen nieuwe pati\u00EBnten met een openstaande brief -- Goed gedaan! \n\n";
		} else {
			np = sortByVisitDate(np);
			reminderString += "Nieuwe pati\u00EBnten met een openstaande brief:\n"
				+ "pati\u00EBnt nr...tijd sinds afspraak...afspraak datum \n";
			for(Patient patient : np){
				reminderString += npString(patient);
			}
			reminderString += "\n";
		}
		if(cpp.isEmpty() && cpf.isEmpty()){
			reminderString += "Geen chronische pati\u00EBnten met een openstaande brief -- Goed gedaan! \n\n";
		} else {
			reminderString += "Chronische pati\u00EBnten met een openstaande brief\n";
				if(!cpp.isEmpty()){
					cpp = sortByLastLetterDate(cpp);
					reminderString += " met een afspraak in het verleden:\n"
						+ "pati\u00EBnt nr.......laatste brief.......afspraak datum \n";
					for(Patient patient : cpp){
						reminderString += cpString(patient);
					}
					reminderString += "\n";
				}
				if(!cpf.isEmpty()){
					cpf = sortByLastLetterDate(cpf);
					reminderString += " met een geplande afspraak:\n"
						+ "pati\u00EBnt nr.......laatste brief.......afspraak datum \n";
					for(Patient patient : cpf){
						reminderString += cpString(patient);
					}
					reminderString += "\n";
				}
		}
		reminderString += "Met vriendelijke groeten,\n Snelle Cor";
		return reminderString;
	}

	private String dayOfWeekToDutch(String stringDayOfWeek) {
		if(stringDayOfWeek.equalsIgnoreCase("sunday")){return "zondag";}
		if(stringDayOfWeek.equalsIgnoreCase("monday")){return "maandag";}
		if(stringDayOfWeek.equalsIgnoreCase("tuesday")){return "dinsdag";}
		if(stringDayOfWeek.equalsIgnoreCase("wednesday")){return "wondsdag";}
		if(stringDayOfWeek.equalsIgnoreCase("thursday")){return "donderdag";}
		if(stringDayOfWeek.equalsIgnoreCase("friday")){return "vrijdag";}
		if(stringDayOfWeek.equalsIgnoreCase("saturday")){return "zaterdag";}
		return null;
	}
	
	private List<Patient> sortByVisitDate(List<Patient> unsorted){
		List<Patient> sorted = new ArrayList<Patient>();
		sorted.add(unsorted.get(0));
		unsorted.remove(0);
		for(Patient p : unsorted){
			int index = 0;
			while(index < sorted.size()){
				if(p.getVisitDate().before(sorted.get(index).getVisitDate())){
					sorted.add(index, p);
					break;
				}
				index++;
			}
			if(index==sorted.size()){ //if we didn't break out of the while loop
				sorted.add(p);
			}
		}
		return sorted;
	}
	
	private List<Patient> sortByLastLetterDate(List<Patient> unsorted){
		List<Patient> sorted = new ArrayList<Patient>();
		sorted.add(unsorted.get(0));
		unsorted.remove(0);
		for(Patient p : unsorted){
			int index = 0;
			while(index < sorted.size()){
				if(p.getLastLetter()==null || p.getLastLetter().getFinishDate()==null){
					sorted.add(0, p);
					break;
				}
				if(sorted.get(index).getLastLetter()!=null //skip patients without letters
						&& sorted.get(index).getLastLetter().getFinishDate()!=null){ //skip unfinished letters
					if(p.getLastLetter().getFinishDate().before(sorted.get(index).getLastLetter().getFinishDate())){
						sorted.add(index, p);
						break;
					}
				}
				index++;
			}
			if(index==sorted.size()){ //if we didn't break out of the while loop
				sorted.add(p);
			}
		}
		return sorted;
	}

	private String npString(Patient patient){
		String days = Integer.toString(patient.getVisitDate().daysSince());
		days = daySpace(days);
		return patient.getPID() + ".........." 
		     + days + " dagen........."
			 + patient.getVisitDate().getDDMMMYYYY() + "\n";
	}
	
	private String daySpace(String days){
		int length = days.length();
		switch(length) {
			case 1: days = "   " + days; break;
			case 2: days = "  " + days; break;
			case 3: days = " " + days; break;
		}
		return days;
	}

	private String cpString(Patient patient) {
		String response = patient.getPID();
		if(patient.getLastLetter()==null){
			response += ".......nooit een brief.....";
		} else {
			RCalendar from = patient.getLastLetter().getFinishDate();
			if(from==null){
				response += "...geen definitieve brief..";
			} else {
				response += "........" + from.getDDMMMYYYY() + "........";
			}
		}
		response += patient.getVisitDate().getDDMMMYYYY() + "\n";
		return response;
	}

	@Override
	public void send(Map<String, String> reminderStrings) {
		List<String> emails = new ArrayList<String>(reminderStrings.keySet());
		String intString = "";
		for(String email : emails){
			String to = email;
			intString += to + "\n" + reminderStrings.get(email);
			String subject = "openstaande brieven";
			try {
				emailSender.sendMail(to, subject, reminderStrings.get(email));
			} catch (AddressException e) {
				new WriteFile().appendFile(BriefBot.ERROR_FILE, "Address exception: " + to + "\n" + reminderStrings.get(email) + "\n");
			} catch (MessagingException e) {
				new WriteFile().appendFile(BriefBot.ERROR_FILE, "Messaging exception: " + to + "\n" + reminderStrings.get(email) + "\n");
			}
		}
		new WriteFile().appendFile(BriefBot.ERROR_FILE, intString);
	}

	@Override
	public void sendErrorLog() {
		String errors = new ReadFile().readFileAsString(BriefBot.ERROR_FILE);
		try {
			emailSender.sendMail(admin, "BriefBot error log", errors);
		} catch (AddressException e) {
			new WriteFile().appendFile(BriefBot.ERROR_FILE, "Address exception: " + admin + "\n");
		} catch (MessagingException e) {
			new WriteFile().appendFile(BriefBot.ERROR_FILE, "Messaging exception: " + admin + "\n");
		}
		
	}

}
