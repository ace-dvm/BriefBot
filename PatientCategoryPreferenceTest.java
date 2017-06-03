package bbtrial.nl.logicgate.ace;

import junit.framework.TestCase;

public class PatientCategoryPreferenceTest extends TestCase {
	
	public void testCalculateLetterDueDate(){
		//takes Patient
		//returns RCalendar
	}
	
	public void testCalculateFirstAllowedReminderDate(){
		PatientCategoryPreference pcp = new PatientCategoryPreference("cp");
		pcp.setRemindBase("local");
		Patient p = new Patient("alice");
		p.setCategory("cp");
		pcp.calculateFirstAllowedReminderDate(p);
	}

}
