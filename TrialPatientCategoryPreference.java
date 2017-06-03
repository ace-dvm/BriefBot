package bbtrial.nl.logicgate.ace;

public class TrialPatientCategoryPreference {

	public RCalendar calculateLetterDueDate(Patient p, String dueUnit, int dueOffset) {
		//there are no local rules for calculateLetterDueDate
		return null;
	}

	public RCalendar calculateFirstAllowedReminderDate(Patient p, String remindUnit, int remindOffset) {
		/* By default, if the letter is due in the future, 
		 * BriefBot stores the patient and issues a reminder
		 * when it's due. For CP, though, it's possible that
		 * the patient will not visit again (died, moved, changed
		 * condition, etc.) and that there will never be a letter
		 * due for this visit. So, if the letter due date falls
		 * more than a week after the visit date, 
		 * BriefBot will NOT issue a reminder for that patient-visit.
		 * The patient will have to visit again and re-enter the system.
		 */
		if(p.getCategory().equalsIgnoreCase("cp")){
			RCalendar weekAfterVisit = new RCalendar(p.getVisitDate(), "w", 1);
			if(p.getLetterDueDate().after(weekAfterVisit)){
				return null;
			} else {
				return new RCalendar(p.getVisitDate(), remindUnit, remindOffset);
			}
		} else return null;
	}

	

}
