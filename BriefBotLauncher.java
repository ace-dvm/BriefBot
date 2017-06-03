package bbtrial.nl.logicgate.ace;


public interface BriefBotLauncher {

	/**
	 * @return the agenda
	 */
	public AgendaI getAgendaI();

	/**
	 * @return the doctorDB
	 */
	public DoctorsI getDoctorsI();

	/**
	 * @return the bbdb
	 */
	public BBStorageI getBBStorageI();

	/**
	 * @return the letters
	 */
	public LettersI getLettersI();

	/**
	 * @return the reminderGenerator
	 */
	public ReminderGeneratorsI getReminderGenerator();


}
