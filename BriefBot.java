package bbtrial.nl.logicgate.ace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * BriefBot is a simple program which takes in a list of patients from an Agenda,
 * checks if they have a letter within a certain time after their visit,
 * and emails a collected list of patients in need of letters to each doctor.
 * This is my first production Java program. If you want to do something similar
 * in your practice, odds are you can write better code than me, and you should do so. 
 * This code is provided for reference.
 * @author ace medlock
 *
 */
public class BriefBot {

	public static int MAX_DAYS_INC;
	private Agenda agenda;
	private List<Patient> newPatients;
	private BBStorage bbstorage;  //stored patients
	private Doctors doctors; //participating doctors
	private Letters letters; //patient letters
	private Map<String,String> reminderStrings;
	private Reminders reminders;
	public static int[] TODAY = new int[0];
	public static File ERROR_FILE;
		
	/**
	 * Runs BriefBot. Optional arguments of:
	 * lastFileName
	 * nextFileName
	 * today's year
	 * today's month (Java months, Jan = 0)
	 * today's day
	 * Args are all or none: they either are all present or all absent.
	 * @param args
	 */	
// TO RUN FROM MAIN: java -jar BriefBot.jar lastFileName nextFileName YYYY M(M) D(D) <- months are java months
	public static void main(String[] args) {
		String lastFileName = null;
		String nextFileName = null;
		if(args.length>0){
			lastFileName = args[0];
			nextFileName = args[1];
			TODAY = new int[3];
			TODAY[0] = Integer.parseInt(args[2]);
			TODAY[1] = Integer.parseInt(args[3]);
			TODAY[2] = Integer.parseInt(args[4]);
		}
		ERROR_FILE = new File("errors" + new RCalendar().toString() + ".txt");
		new BriefBot(new BriefBotAuto(), lastFileName, nextFileName);
	}
		
	public BriefBot(BriefBotLauncher bb, String lastFileName, String nextFileName){
			logMsg("launch started \n");
		doctors = new Doctors(bb.getDoctorsI()); //get doctor info, calculates when a reminder will go out for each doctor
			logMsg("Doctors loaded");		
		if(lastFileName!=null && nextFileName!=null){
			agenda = new Agenda(new TrialAgenda(new File(lastFileName), 
					new File(nextFileName)), doctors);
		} else {
			agenda = new Agenda(bb.getAgendaI(), doctors);
		}
			logMsg("Agenda loaded");		
		bbstorage = new BBStorage(bb.getBBStorageI());
		MAX_DAYS_INC = bbstorage.getMaxDaysIncomplete();
			logMsg("BBStorage loaded");
		eliminateNoShows(); //remove patients who didn't show for their appt from stored patients
			logMsg("no-shows removed");
		newPatients = deduplicateNewWithSelf(); //remove duplicates from new patient list
		newPatients = changeDoctors(newPatients); //check for schedule changes, update the patients' doctors accordingly
		newPatients = deduplicateNewWithStored(newPatients); //remove patients who are already in stored patients from new patients		
			logMsg("newPatients deduplicated");
		letters = new Letters(bb.getLettersI());
			logMsg("Letters loaded. Checking for letters for new patients...");
		findLettersNewPatients(); //try to find a letter for this visit. Assign the most recent letter to lastLetter
			logMsg("Calculating letter due dates...");		
		calculateLetterDue(); //calculate when each new patient has a letter due.
							  //note and store any patients who are already finished (have a letter which postdates this visit)
		calculateFirstPossibleReminderDate(); //calculate when a reminder should go out for this patient based on doctor preferences for this patient type
											  //if this visit does not qualify for a reminder, note in storage and remove from list
			logMsg("Checking incomplete letters...");
		checkIncompleteStoredPatients();
			logMsg("Storing finished new patients");
		storeFinishedNewPatients();
			logMsg("Looking for letters for stored patients");
		findLettersStoredPatients(); //find letters for stored patients
		                      //mark patients with letters as finished
			logMsg("Storing new patients");
		mergeNewPatients();
		reminderStrings = new HashMap<String,String>();
		reminders = new Reminders(bb.getReminderGenerator());
			logMsg("Generating reminders");		
		generateReminderStrings();
		remind();
		cleanup();
			logMsg("done");		
}

	
	/**
	 * If the PID, visit date, and doctorID of a patient in the no-show list
	 * matches a patient in our active patients, that patient is flagged
	 * as a no-show and removed from the active patients.
	 */
	private void eliminateNoShows() {
		List<Patient> agendaNoShows = agenda.getNoShows();
		List<Patient> storedNoShows = new ArrayList<Patient>();
			logMsg("No shows: " + agendaNoShows.size() + "\n");
			logMsg("Active patients from last time: " + bbstorage.getActivePatients().size() + "\n");
		for(Patient ns : agendaNoShows){
			boolean found = false;			
			Iterator<Patient> it = bbstorage.getActivePatients().iterator();
			//NOTE that it uses active patients, so noRemind are ignored. This is probably OK.
			while(it.hasNext() && found==false){
				Patient sp = it.next();
				if(ns.getPID().equalsIgnoreCase(sp.getPID()) 
						&& ns.getVisitDate().equals(sp.getVisitDate())
						&& ns.getDoctorID().equalsIgnoreCase(sp.getDoctorID())){
					storedNoShows.add(sp);
					found = true;
				}
			}
		}
			logMsg("Removing " + storedNoShows.size() + " no-shows from storage\n");
		bbstorage.noShow(storedNoShows);
		storedNoShows.clear();
		//check for undocumented no-shows
		for(Patient sp : bbstorage.getActivePatients()){
			//if the visit date was scheduled between the last run and today
System.out.println(sp.getPID());			
			if(sp.getVisitDate().betweenOneInclusive(bbstorage.getLastCheckDate(), new RCalendar())){
//System.out.println("Visit was in last week: "+sp.getPID()+" : "+sp.getVisitDate());
				//then the patient should be on this list of new patients
				//if not, they didn't show up but weren't marked as a no-show
				boolean match = false;
				Iterator<Patient> npIt = agenda.getNewPatients().iterator();
				while(!match && npIt.hasNext()){
					Patient np = npIt.next();
					if(sp.getPID().equalsIgnoreCase(np.getPID())
							&& sp.getVisitDate().equals(np.getVisitDate())
							&& sp.getDoctorID().equalsIgnoreCase(np.getDoctorID())){
//TODO may be more efficient to start dedup here, since we're matching already
//System.out.println("... matches Agenda "+np.getPID()+" : "+np.getVisitDate());
						match = true;
					}
				}
				if(!match){
//System.out.println("No matching agenda patient. Add to no-shows.");					
					storedNoShows.add(sp);
				}
			}
		}
			logMsg("Undocumented no-shows found: " + storedNoShows.size() + "\n");
		bbstorage.noShow(storedNoShows);
	}

	/**
	 * If a patient has the same PID, doctorID, and visitDate as 
	 * any other patient, we log the patient as a duplicate and don't 
	 * process it further. If the PID and doctorID are the same but not the
	 * visit date, the patient may have rescheduled or be back for another
	 * visit. If the patient is active it is still logged as a duplicate,
	 * otherwise it is treated as a new instance.
	 * @return
	 */
	private List<Patient> deduplicateNewWithSelf() {
		List<Patient> agendaPatients = agenda.getNewPatients();
			logMsg("Total new patients from agenda: " + agendaPatients.size() + "\n");
		//First deduplicate the list with itself
		List<Patient> duplicates = new ArrayList<Patient>();
		int index = 0;
		int itIndex = 0;
//System.out.println("BEGIN DEBUG");
//System.out.println("AGENDA BEFORE DEDUP:");
//for(Patient p : agendaPatients){
//	System.out.println(p.getPID()+" : "+p.getVisitDate());
//}
		while(index < agendaPatients.size()){
			itIndex = index+1;
			Patient p1 = agendaPatients.get(index);
			while(itIndex < agendaPatients.size()){
				Patient p2 = agendaPatients.get(itIndex);
//System.out.println("p1=" + p1.getPID() + ", p2=" + p2.getPID());
				if(p1.getPID().equalsIgnoreCase(p2.getPID())
						&& p1.getDoctorID().equalsIgnoreCase(p2.getDoctorID())){
//System.out.println("p1 and p2 have the same PID and doctorID");
					//determine if the visit date is the same
					if(p1.getVisitDate().equals(p2.getVisitDate())){
//System.out.println("Visit dates are the same, queue p1 for removal");
						//if the visit date is the same, just remove one copy
						duplicates.add(p1);
					} else {
//System.out.println("Visit dates are different");						
						if(p1.getVisitDate().after(p2.getVisitDate())){
//System.out.println("p1's visit date is after p2's, queue p1 for removal");
							duplicates.add(p1);
						} else {
//System.out.println("p1's visit date is not after p2's, queue p2 for removal");
							duplicates.add(p2);
						}
					}
				}				
				itIndex++;
			}
			index++;
		}
//System.out.println("Duplicates to be removed:");		
//for (Patient p : duplicates){
//	System.out.println(p.getPID());
//}
		agendaPatients.removeAll(duplicates);
			logMsg("Found " + duplicates.size() + " duplicates within the newPatient list\n");
		return agendaPatients;
	}

	public List<Patient> changeDoctors(List<Patient> agendaPatients){
		if(doctors.haveChanges()){
			agendaPatients = doctors.changeDoctors(agendaPatients);
		}
		return agendaPatients;
	}
		
		
	public List<Patient> deduplicateNewWithStored(List<Patient> agendaPatients){
		List<Patient> duplicates = new ArrayList<Patient>();
		Iterator<Patient> itNP = agendaPatients.iterator();
		List<Patient> storedPatients = bbstorage.getAllPatients();
		while(itNP.hasNext()){
			Patient np = itNP.next();
			boolean dup = false;
			Iterator<Patient> itSP = storedPatients.iterator();
			while(itSP.hasNext() && !dup){
				Patient sp = itSP.next();
//System.out.println("np="+np.getPID() + ", sp="+sp.getPID());
				if(np.getPID().equalsIgnoreCase(sp.getPID())
						&& np.getDoctorID().equalsIgnoreCase(sp.getDoctorID())){
					//same PID and doctorID. Figure out if it's the same visit or a new one
//System.out.println("np and sp have the same PID and doctorID");
					if(np.getVisitDate().equals(sp.getVisitDate())){
//System.out.println("Visit dates are the same, queue p1 for removal");
						//same visit, it just hasn't been cleared from the agenda since the last run.
						//remove the patient from the newPatients list
						duplicates.add(np);
						dup=true;
					} else {
//System.out.println("Visit dates are different: np="+np.getVisitDate()+", sp="+sp.getVisitDate());
						//different visit date. Figure out if the original appointment happened yet
						if(sp.getVisitDate().after(new RCalendar())){
//System.out.println(sp.getVisitDate()+">"+new RCalendar()+", Regard this as a reschedule.");
							//the original appointment is still in the future. Regard this as a rescheduled appointment.
							bbstorage.addDuplicate(sp, sp.getDBID());
							bbstorage.rescheduled(np.getVisitDate(), sp);
							duplicates.add(np);
							dup=true;
						} else {
//System.out.print(sp.getVisitDate()+">"+new RCalendar());							
							//the original appointment already happened. the new visit is probably a return visit.
							//figure out if the patient is active
							if(sp.getStatus().equalsIgnoreCase("active")){
//System.out.println(", status = active");								
								//if active, register the agenda patient as a duplicate
								duplicates.add(np);
								bbstorage.addDuplicate(np, sp.getDBID());
								dup=true;
							} //else The previous patient-visit was finished.
							// This is not a duplicate as far as BB is concerned.
						}
					}
				}
			}
		}
//System.out.println("Duplicates to be removed:");		
//for(Patient p : duplicates){
//	System.out.println(p.getPID());
//}
		agendaPatients.removeAll(duplicates);
			logMsg("Found " + duplicates.size() + " duplicated with stored patients\n");
			logMsg("Nonduplicated new patients: " + agendaPatients.size() + "\n");
//System.out.println("Agenda after dedup:");
//for(Patient p : agendaPatients){
//	System.out.println(p.getPID()+" : "+p.getVisitDate());
//}
		return agendaPatients;
	}
	
	/**
	 * Find letters relevant for this visit.
	 */
	private void findLettersNewPatients(){
		newPatients = letters.findLetters(newPatients, doctors);
		//this is purely for reporting
			int lettersFound = 0;
			for(Patient p : newPatients){
				if (p.getNewLetter()!=null){
					lettersFound++;
				}
			}
				logMsg("New letters found: " + lettersFound + "\n");
	}
	
	/**
	 * An abandoned letter is one which has no finish date and 
	 * the start date is older than the cutoff.
	 * @param l (a Letter)
	 * @return
	 */
	private boolean isAbandoned(Letter l){
		if(l.getFinishDate()!=null){
			return false;
		} else { //finish date is null
			if(l.getStartDate()==null){
				return true; //no finish or start date, this letter has a problem.
			} else {
				RCalendar expires = new RCalendar(l.getStartDate(), "d", MAX_DAYS_INC);
				if(expires.before(new RCalendar())){ //if it expires before today (the expiry date has passed)
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * For each new patient, calculate when the letter for this visit is due.
	 */
	private void calculateLetterDue(){
String record = "";	
		Iterator<Patient> it = newPatients.iterator();
System.out.println("Number of newPatients: " + newPatients.size());
		while(it.hasNext()){
try{
record = "";			
record += "While: ";			
			Patient patient = it.next();
record += "paient nr: " + patient.getPID();
record += " doctor: " + patient.getDoctorID();
			Doctor doctor = doctors.getDoctorByID(patient.getDoctorID());
record += "  found...";
if(doctor==null){
	it.remove();
	new WriteFile().appendFile(ERROR_FILE, "null doctor: " + record +"\n");
} else {
			//a newLetter is a letter which satisfies the requirements for this visit.
			//If the patient already has one, they are finished in BriefBot.
			doctor.patientLetterDueDate(patient);
record += "due: " + patient.getLetterDueDate() + "\n";			
	}		
} catch (Exception e){
	new WriteFile().appendFile(ERROR_FILE, e.toString() + record +"\n");
}
		}
	}
	
	/**
	 * For each new patient, calculate when a reminder is allowed according
	 * to the doctor's preferences.
	 */
	private void calculateFirstPossibleReminderDate() {
		ArrayList<Patient> noReminder = new ArrayList<Patient>();
		Iterator<Patient> it = newPatients.iterator();
		while(it.hasNext()){
			Patient patient = it.next();
			Doctor doctor = doctors.getDoctorByID(patient.getDoctorID());
			doctor.firstPossibleReminderDate(patient);
			//if the reminder date is set to null, then this patient is never eligible for a reminder.
			if(patient.getFirstPossibleReminderDate()==null){
				noReminder.add(patient);
				it.remove();
			}
		}
			logMsg("Reminder date is null: " + noReminder.size() + "\n");
		bbstorage.doNotRemind(noReminder);
	}

	/**
	 * Finds Finished patients in BriefBot whose letters are incomplete.
	 * If the incomplete letter appears to be abandoned, it moves the 
	 * pointer to the incomplete letter to unfinishedLetter and marks
	 * the patient as Active.
	 */
	private void checkIncompleteStoredPatients(){
		List<Patient> storedIncomplete = bbstorage.getIncomplete();		
			logMsg("Number of incomplete letters in storage: " + storedIncomplete.size() + "\n");
		List<Patient> withAbandonedLetters = new ArrayList<Patient>();		
		for(Patient p : storedIncomplete){
			if(isAbandoned(p.getNewLetter())){
				p.setNewLetter(null);
				withAbandonedLetters.add(p);
			}
		}
		//if the letter is not yet old enough to be abandoned, ignore it 'til next time
		bbstorage.noteAbandonedLetter(withAbandonedLetters);
				//makes a record of the old letter in the database
		bbstorage.changeStatusToActive(withAbandonedLetters);
			logMsg("Patients changed to active due to inc letters: " + withAbandonedLetters.size() + "\n");
	}
	
	private void storeFinishedNewPatients(){
		ArrayList<Patient> finished = new ArrayList<Patient>();
		for(Patient p : newPatients){
			if(p.getNewLetter()!=null){
				finished.add(p);
			}
		}
		bbstorage.finish(finished);
			logMsg("Finished new patients: " + finished.size() + "\n");
		newPatients.removeAll(finished);
		logMsg("Active new patients: " + newPatients.size() + "\n");
	}
	
	/**
	 * Checks for new letters for the visitDate 
	 * for the patients already registered
	 * in BriefBot.
	 */
	private void findLettersStoredPatients() {
			logMsg("Searching for letters for " + bbstorage.getActivePatients().size() + " stored active patients" + "\n");
		 List<Patient> finished = letters.checkForNewLetters(bbstorage.getActivePatients(), doctors);
		 	logMsg("Found " +  finished.size() + " letters" + "\n");
		 bbstorage.finish(finished);
		 	logMsg(bbstorage.getActivePatients().size() + " remaining active stored patients" + "\n");
	}
	
	/**
	 * Merges the new patient list with the active patients in storage.
	 */
	private void mergeNewPatients(){
//The dedup process has been consolidated to the deduplicate method. We'll leave this around for a little while in case we need it.
//		ArrayList<Patient> dedup = new ArrayList<Patient>();
//		HashMap<Patient, List<Patient>> dup = new HashMap<Patient, List<Patient>>();
//		reportString += "Deduping new patients. Starting with " + newPatients.size() + " new patients" + "\n";
//System.out.println("Deduping new patients. Starting with " + newPatients.size() + " new patients");
//		int size = newPatients.size();
//		int duplicateCounter = 0;
//		while(size > 0){
//			List<Patient> duplicates = new ArrayList<Patient>();
//			Patient p0 = newPatients.get(0);
//			newPatients.remove(0);
//			Iterator<Patient> it = newPatients.iterator();
//				while(it.hasNext()){
//					Patient p1 = it.next();
//					if(p0.getPID().equals(p1.getPID()) &&
//						doctors.getDoctorByID(p0.getDoctorID()).getEmail().equals(doctors.getDoctorByID(p1.getDoctorID()).getEmail())){
//						//same PID and doctor, compare the visit dates
//						if(p0.getVisitDate().after(p1.getVisitDate())){
//							//p1 is the oldest, p0 is a duplicate
//							duplicates.add(p0);
//							duplicateCounter++;
//							p0 = p1;
//						} else {
//							//p0 is the oldest, p1 is a duplicate; or they have the same visit date.
//							duplicates.add(p1);
//						}
//						it.remove(); /*p1 has now been moved to duplicates
//									   or assigned to p0. Remove it from newPatients.*/
//					}
//				}
//				/*at the end of this iteration, p0 should contain 
//				  the patient object with the oldest visit date, and
//				  duplicates should contain any of its duplicates.
//				  */
//				dedup.add(p0);
//				if(!duplicates.isEmpty()){
//					dup.put(p0, duplicates);
//				}
//				/*reset size so the while loop will exit when all 
//				  patients on newPatients have been checked.*/
//				size = newPatients.size();
//		}
//		reportString += "Found " + duplicateCounter + " duplicates and " + dedup.size() + " unique patients" +"\n";
//System.out.println("Found " + duplicateCounter + " duplicates and " + dedup.size() + " unique patients");
//		bbstorage.mergeNewPatients(dup);
//		//now we want to log the duplicates with a link to the active/finished patient they're duplicating
//		Set<Patient> originals = dup.keySet(); //get the active/finished patients who have duplicates
//		for(Patient p : originals){ //for each patient who has a duplicate
//			List<Patient> duplicates = dup.get(p); //get its list of duplicates
//			for(Patient patient : duplicates){ //and put each duplicate in the database
//				bbstorage.addDuplicate(patient, p.getDBID()); //with a link to the dbid of the active/finished patient
//			}
//		}
		bbstorage.mergeNewPatients(newPatients);
			logMsg("Active patients now in storage: " + bbstorage.getActivePatients().size()+ "\n");
	}
	
	/**
	 * Generates the text of each email to be sent today, 
	 * and pairs it with the appropriate email address
	 */
	private void generateReminderStrings() {
		RCalendar today = new RCalendar();
		List<Doctor> todaysDoctors = new ArrayList<Doctor>();
		List<Patient> todaysPatients = new ArrayList<Patient>();
		List<Patient> todaysEligible = new ArrayList<Patient>();
		//list patients whose firstPossibleReminderDate is today or already passed
		for(Patient patient : bbstorage.getActivePatients()){			
			if(patient.getFirstPossibleReminderDate().equals(today)
					|| patient.getFirstPossibleReminderDate().before(today)){
				todaysEligible.add(patient);
			}
		}
			logMsg("Eligible patients today: " +  todaysEligible.size() + "\n");
		for(Doctor doctor : doctors.getDoctors()){
			//if the doctor is scheduled to get a reminder today...
			RCalendar nextR = doctor.getNrc().getNextReminderDate();
			RCalendar lastR = doctor.getNrc().getLastReminderDate();
			if(nextR!=null){ //nextR=null implies the doctor never gets a reminder
				if(lastR==null){//if the doctor has no previous reminder
					//then add the doctor if the nextReminderDate is today or in the past
//TODO double-check that nextReminderDate can never be set to before the lastCheckDate
					if(nextR.betweenOneInclusive(today, bbstorage.getLastCheckDate())){
						todaysDoctors.add(doctor);
					}
				//if the doctor has a previous reminder
				//check to see if the nextR is between today and the last reminder.
				} else if(nextR.betweenOneInclusive(today, lastR)){
					todaysDoctors.add(doctor);
				}
			}
		}
		int countReminders = 0;
		if(!todaysDoctors.isEmpty()){
			//A doctor may have more than one doctorID if he sees patients in more than one department.
			//We still want him to get only one email, so we make a collection of unique email
			//addresses and salutation strings.
			HashMap<String, String> emailSalutation = new HashMap<String,String>();
			for(Doctor doctor : todaysDoctors){
				emailSalutation.put(doctor.getEmail(), doctor.getSalutation());
				//if there are different salutations associated with the same email, 
				//this will choose a more or less random one. Hopefully any of them will do.
			}
			for(String email : emailSalutation.keySet()){ 
				//for each unique email address
				//make a list of doctorIDs matched to this email address
				List<Doctor> sameDoctor = new ArrayList<Doctor>();
				for(Doctor doctor : todaysDoctors){
					if(doctor.getEmail().equals(email)){
						sameDoctor.add(doctor);
					}
				}
				//find the patients matching to each doctorID
				List<Patient> patients = new ArrayList<Patient>();
				for(Doctor doctor : sameDoctor){
					for(Patient p : todaysEligible){
						if(doctor.getDoctorID().equals(p.getDoctorID())){
							countReminders++;
							patients.add(p);
						}						
					}
				}
				todaysPatients.addAll(patients);
				reminderStrings.put(email, reminders.generateReminderString(emailSalutation.get(email), patients));
			}
			//register that the doctors got a reminder today
				logMsg("Patients getting reminders today: " + countReminders + "\n");
			doctors.lastReminders(todaysDoctors);
			//register that the patients got a reminder about them
			bbstorage.reminderDates(todaysPatients);
		}
	}

	private void remind(){
		reminders.send(reminderStrings);
	}
		
	private void cleanup() {
		bbstorage.setLastCheckDate(new RCalendar());
		agenda.close();
		doctors.close();
		letters.close();
		bbstorage.cleanup();
		bbstorage.close();
		reminders.sendErrorLog();
	}
	
	private void logMsg(String msg){
		new WriteFile().appendFile(ERROR_FILE, msg);
	}
}
	