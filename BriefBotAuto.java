package bbtrial.nl.logicgate.ace;



public class BriefBotAuto implements BriefBotLauncher {

	private AgendaI agenda;
	private DoctorsI doctorsI;
	private BBStorageI bBStorageI;
	private LettersI letters;
	private ReminderGeneratorsI reminderGenerator;
	
	public BriefBotAuto(){
		agenda = null;
		doctorsI = null;
		bBStorageI = null;
		letters = null;
		reminderGenerator = null;
		agenda = new TrialAgenda();
		doctorsI = new TrialDoctors();
		bBStorageI = new TrialBBStorage();
		letters = new TrialLetters();
		reminderGenerator = new TrialReminderGenerator();
	}

	/**
	 * @return the agenda
	 */
	public AgendaI getAgendaI() {
		if(agenda == null){
			agenda = new TrialAgenda();
		}
		return agenda;
	}

	/**
	 * @return the doctorsI
	 */
	public DoctorsI getDoctorsI() {
		if(doctorsI == null){
			doctorsI = new TrialDoctors();
		}
		return doctorsI;
	}

	/**
	 * @return the bBStorageI
	 */
	public BBStorageI getBBStorageI() {
		if(bBStorageI == null){
			bBStorageI = new TrialBBStorage();
		}
		return bBStorageI;
	}

	/**
	 * @return the letters
	 */
	public LettersI getLettersI() {
		if(letters == null){
			letters = new TrialLetters();
		}
		return letters;
	}

	/**
	 * @return the reminderGenerator
	 */
	public ReminderGeneratorsI getReminderGenerator() {
		if(reminderGenerator == null){
			reminderGenerator = new TrialReminderGenerator();
		}
		return reminderGenerator;
	}

}

	
