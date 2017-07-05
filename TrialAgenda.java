package bbtrial.nl.logicgate.ace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Parses the Agenda
 * @author skmedlock
 *
 */

public class TrialAgenda implements AgendaI{

	private ArrayList<Patient> newPatientList;
	private ArrayList<Patient> noShowPatientList;
	private BBStorage bbstorage;
	private Doctors doctors;
	private File lastFile;
	private File nextFile;
		
	public TrialAgenda(){
		fakeAgenda(); //added for demo 
		newPatientList = new ArrayList<Patient>();
		noShowPatientList = new ArrayList<Patient>();
		bbstorage = new BBStorage(new TrialBBStorage());	
		doctors = null;
		lastFile = findFile("LAST2WKS_BB_INT", new RCalendar());
		nextFile = findFile("NEXT2WKS_BB_INT", new RCalendar());
	}

	//constructor for loading old files
	public TrialAgenda(File lastFile, File nextFile){
		newPatientList = new ArrayList<Patient>();
		noShowPatientList = new ArrayList<Patient>();
		bbstorage = new BBStorage(new TrialBBStorage());
		doctors = null;
		this.lastFile = lastFile;
		this.nextFile = nextFile;
	}

	/**
	 * Constructor for testing
	 * @param bbstorage
	 */
	public TrialAgenda(BBStorage bbstorage){
		newPatientList = new ArrayList<Patient>();
		noShowPatientList = new ArrayList<Patient>();
		this.bbstorage = bbstorage;
	}
	
	/**
	 * Constructor for testing
	 * @param bbstorage
	 * @param doctors
	 */
	public TrialAgenda(BBStorage bbstorage, Doctors doctors){
		newPatientList = new ArrayList<Patient>();
		noShowPatientList = new ArrayList<Patient>();
		this.bbstorage = bbstorage;
		this.doctors = doctors;
	}
	
	@Override
	public List<Patient> getNewPatients(Doctors doctors) {
		this.doctors = doctors;
		if(newPatientList.size()==0){
			fillLists();
		}
		return newPatientList;
	}

	@Override
	public List<Patient> getNoShows(Doctors doctors) {
		this.doctors = doctors;
		if(noShowPatientList.size()==0 && newPatientList.size()==0){
			fillLists();
		}
		return noShowPatientList;
	}

	private void fillLists(){
			newPatientList.addAll(readFuturePatientList(nextFile));
			newPatientList.addAll(readPastPatientList(lastFile)); 
	}
	
	
	//this could probably be refactored with readPastPatientList, but doesn't seem worth it now.
	protected List<Patient> readFuturePatientList(File nextFile) {
		List<Patient> futurePatients = new ArrayList<Patient>();
		String excluded = "";
		if(nextFile==null){ //if there's no new file, log an error
			new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot find a new NEXT2WKS file \n");
			return futurePatients; //return an empty futurePatients list
		}
		List<String> nextStrings = new ReadFile().readLines(nextFile);
		WriteFile writer = new WriteFile();
		HashSet<String> newDoctors = new HashSet<String>();
		for(String nString : nextStrings){
			try {
				String[] poliArray = nString.split(";");
				String dept = poliArray[0].trim();
				//[1] is the doctor's code
				//[2] is the full string indicating the doctor/dept
				String doctorID = poliArray[2].trim();
				//[3] is the patient number
				String pNr = poliArray[3].trim();
				//[4] is CP or NP
				String type = poliArray[4].trim();
				//[5] is the visit date and time
				String visit = poliArray[5].trim();
				Patient p = new Patient(pNr);
				p.setDept(dept);
				p.setCategory(type.toLowerCase());
				int day = Integer.valueOf(visit.substring(0, 2));
				int rMonth = Integer.valueOf(visit.substring(3, 5));
				int year = Integer.valueOf(visit.substring(6, 10));
				p.setVisitDate(new RCalendar(year, rMonth, day));
				p.setDoctorID(doctorID);
				//check to make sure we've created a sensible patient.
				if(p.getVisitDate() != null){
					if(p.getCategory().equalsIgnoreCase("cp")){
						if(doctors.hasDoctor(doctorID)){
							futurePatients.add(p);
						} else { //doctor is not on our list. Check for new Drs or aliases.
							excluded += nString + "\n";
							newDoctors.add(p.getDoctorID());
						}
					} else { //not a CP, exclude from futurePatients
						excluded += nString + "\n";
					}
				} else {//if the patient does not have a valid patient number and visit date, record an error
					writer.appendFile(BriefBot.ERROR_FILE, nString);
				}
			} catch (Exception e){
				//null pointers and stuff can happen due to crappy data. Log and move on.
				writer.appendFile(BriefBot.ERROR_FILE, e.toString() + "nString: " + nString + "\n");
			}
		}
		writer.appendFile(new File("excludedNEXT" + new RCalendar().toString() + ".txt"), excluded);
		for(String s : newDoctors){
			writer.appendFile(BriefBot.ERROR_FILE, "Check for new doctor: " + s + "\n");
		}
		return futurePatients;
	}

	/**
	 * Gets the list of patients with appointments in the past,
	 * as of the last dump from the poli Agenda
	 */
	protected List<Patient> readPastPatientList(File lastFile){
		List<Patient> pastPatients = new ArrayList<Patient>();
		String excluded = "";
		if(lastFile==null){ //if null, then there's no suitable file.
			new WriteFile().appendFile(BriefBot.ERROR_FILE, "Cannot find a new LAST2WKS file\n");
			return pastPatients; //return an empty pastPatients list
		}
		List<String> lastStrings = new ReadFile().readLines(lastFile);
		WriteFile writer = new WriteFile();
		HashSet<String> newDoctors = new HashSet<String>();
		for(String lString : lastStrings){
			try {				
				String[] poliArray = lString.split(";");
				String dept = poliArray[0].trim();
				//[1] is the doctor's code, similar to drID but not necessarily unique
				//[2] is the full string indicating the doctor/dept
				//This is used as the doctor's unique ID in BriefBot.
				String doctorID = poliArray[2].trim();
				//[3] is the patient number
				String pNr = poliArray[3].trim();
				//[4] is CP or NP
				String type = poliArray[4].trim();
				//[5] is the visit status: blank, NS, VC, EC
				String status = poliArray[5].trim();
				//[6] is the visit date and time
				String visit = poliArray[6].trim();
				Patient p = new Patient(pNr);
				p.setDept(dept);
				p.setCategory(type.toLowerCase());
				int day = Integer.valueOf(visit.substring(0, 2));
				int rMonth = Integer.valueOf(visit.substring(3, 5));
				int year = Integer.valueOf(visit.substring(6, 10));
				p.setVisitDate(new RCalendar(year, rMonth, day));
				p.setDoctorID(doctorID);				
				//check to ensure we've created a sensible patient
				if(p.getVisitDate() != null){
					if(doctors.hasDoctor(doctorID)){ //if the doctor is recognized
						//determine if patient is a no-show
						if(status.equals("") || status.equals("NS") || status.equals("VB")){
							noShowPatientList.add(p);
						} else {
							pastPatients.add(p);
						}
					} else { //the doctor is not recognized. Add doctor to a list to hand-check
						excluded += lString + "\n";
						newDoctors.add(p.getDoctorID());
						}
				} else { //if the patient does not have a valid visit date, record an error
					writer.appendFile(BriefBot.ERROR_FILE, lString);
					}
			} catch (Exception e){
				//null pointers and stuff can happen due to crappy data. Log and move on.
				writer.appendFile(BriefBot.ERROR_FILE, e.toString() + "lString: " + lString + "\n");
			}
		}
		writer.appendFile(new File("excludedLAST" + new RCalendar().toString() + ".txt"), excluded);
		for(String s : newDoctors){
			writer.appendFile(BriefBot.ERROR_FILE, "Check for new doctor: " + s + "\n");
		}
		return pastPatients;
	}

	/**
	 * Finds the proper files.  These are named
	 * LAST2WKS_BB_INTYYYY-MM-DD.txt and NEXT2WKS_BB_INTYYYY-MM-DD.txt
	 * and are sent to the BriefBot home directory q1week.
	 * The search starts with today's date and works backwards to the last check date.
	 */
	protected File findFile(String string, RCalendar date){		
		while(bbstorage.getLastCheckDate().before(date)){
			File file = new File(string + date.getYear() + "-" + date.getZeroMonth() + "-" + date.getZeroDay() + ".txt");
			if(file.exists()){
				return file;
			} else {
				date.add(0,0,0,-1);
			}
		}
		return null;
	}
	
	public boolean close(){
		return true;
	}
	
	/**
	 * This code added for demo
	 */
	private void fakeAgenda() {
		RCalendar date = new RCalendar("d", -1);
		File future = new File("NEXT2WKS_BB_INT" + date.getYear() + "-" + date.getZeroMonth() + "-" + date.getZeroDay() + ".txt");
		RCalendar fdate = new RCalendar(date, "d", 6); // date for future appointments (CP eligible for reminder)
		RCalendar fdate2 = new RCalendar(date, "d", 14); // date for future appointments (no patients eligible for reminder: appointment is too far in the future)
		RCalendar pdate = new RCalendar(date, "d", -7); // date for all past appointments in this agenda demo
		RCalendar kdate = new RCalendar("d",-7); //for Kamelion. Make sure this matches the date in FakeStorage.
//ian is eligible for a reminder; the rest are not (NP never are; CP only if their future appointment is in the next 7 days)
		String ftext = 
			"GER       ;STR       ;strangelove                    ;ian;CP        ;" + fdate.getZeroDay() + "-" + fdate.getZeroMonth() + "-" + fdate.getYear() + " 13:00;G              \n"
		  + "GER       ;STR       ;strangelove                    ;rose;NP        ;" + fdate.getZeroDay() + "-" + fdate.getZeroMonth() + "-" + fdate.getYear() + " 08:00;G              \n"
		  + "INT       ;SIR       ;sirnotappearing                ;donna;NP        ;" + fdate2.getZeroDay() + "-" + fdate2.getZeroMonth() + "-" + fdate2.getYear() + " 08:00;G              \n"
		  + "INT       ;WHO       ;who		                      ;barbara;CP        ;" + fdate2.getZeroDay() + "-" + fdate2.getZeroMonth() + "-" + fdate2.getYear() + " 12:10;G              ";
		new WriteFile().overWriteFile(future, ftext);
		File past = new File("LAST2WKS_BB_INT" + date.getYear() + "-" + date.getZeroMonth() + "-" + date.getZeroDay() + ".txt");
//Steven is a CP who has no prior letter (gets reminder)
//Martha is an NP who has no letter (will get reminder after 60d)
//Amy is an NP who has a letter (no reminder)
//Rory is an NP who has a letter, but it is too old (will get reminder after 60d)
//kama was in bbstorage already but is now listed as a no-show (no reminder)
//dodo is a CP who has a prior letter, but it is too old
//jamie is a CP who has a prior letter (no reminder)
//liz is a CP who is already in bbstorage but visited again
		String ptext =
			   "GER       ;STR    ;strangelove                  ;steven;CP        ;VC  ;" + pdate.getZeroDay() + "-" + pdate.getZeroMonth() + "-" + pdate.getYear() + " 11:30;A             \n"
			+  "INT       ;WHO    ;who                          ;martha;NP        ;EC  ;" + pdate.getZeroDay() + "-" + pdate.getZeroMonth() + "-" + pdate.getYear() + " 10:00;A             \n"
			+  "INT       ;WHO    ;who                          ;amy;NP        ;EC  ;" + pdate.getZeroDay() + "-" + pdate.getZeroMonth() + "-" + pdate.getYear() + " 10:00;A             \n"
			+  "INT       ;WHO    ;who                          ;rory;NP        ;EC  ;" + pdate.getZeroDay() + "-" + pdate.getZeroMonth() + "-" + pdate.getYear() + " 10:00;A             \n"
			+  "INT       ;WHO    ;who                          ;kama;CP        ;NS  ;" + kdate.getZeroDay() + "-" + kdate.getZeroMonth() + "-" + kdate.getYear() + " 13:00;G             \n"
			+  "INT       ;SIR    ;sirnotappearing              ;polly;CP        ;VC  ;" + pdate.getZeroDay() + "-" + pdate.getZeroMonth() + "-" + pdate.getYear() + " 16:00;A             \n"
			+  "INT       ;WHO    ;who                          ;dodo;CP        ;VB  ;" + pdate.getZeroDay() + "-" + pdate.getZeroMonth() + "-" + pdate.getYear() + " 14:30;A             \n"
			+  "INT       ;WHO    ;who                          ;jamie;CP        ;VB  ;" + pdate.getZeroDay() + "-" + pdate.getZeroMonth() + "-" + pdate.getYear() + " 14:30;A             \n"
			+  "INT       ;WHO    ;who                          ;liz;CP        ;VB  ;" + pdate.getZeroDay() + "-" + pdate.getZeroMonth() + "-" + pdate.getYear() + " 14:30;A             ";

		new WriteFile().overWriteFile(past, ptext);
		}
	
}
