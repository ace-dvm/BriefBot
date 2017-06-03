package bbtrial.nl.logicgate.ace;

import java.util.HashMap;

import junit.framework.TestCase;

public class DoctorTest extends TestCase {
	
	public void testPatientLetterDueDate(){
		Doctor doctor = new Doctor("strangelove");
		PatientCategoryPreference pcp1 = new PatientCategoryPreference("T1");
		pcp1.setDueBase("lastLetter");
		pcp1.setDueUnit("y");
		pcp1.setDueOffset(1);
		pcp1.setDue2Base("visitDate");
		pcp1.setDue2Unit("d");
		pcp1.setDue2Offset(0);
		PatientCategoryPreference pcp2 = new PatientCategoryPreference("T2");
		pcp2.setDueBase("visitDate");
		pcp2.setDueUnit("w");
		pcp2.setDueOffset(4);
		pcp2.setDue2Base("lastLetter");
		pcp2.setDue2Unit("w");
		pcp2.setDue2Offset(6);
		PatientCategoryPreference pcp3 = new PatientCategoryPreference("T3");
		pcp3.setDueBase("visitDate");
		pcp3.setDueUnit("w");
		pcp3.setDueOffset(-4);
		HashMap<String, PatientCategoryPreference> pcpMap = new HashMap<String, PatientCategoryPreference>();
		pcpMap.put("T1", pcp1);
		pcpMap.put("T2", pcp2);
		pcpMap.put("T3", pcp3);
		doctor.setPatientCategoryPreferences(pcpMap);
		Patient liz = new Patient("liz");
		liz.setCategory("T1");
		Letter lizl = new Letter();
		lizl.setFinishDate(new RCalendar("2011-3-21"));
		liz.setLastLetter(lizl);
		doctor.patientLetterDueDate(liz);
		assertTrue(new RCalendar("2012-3-21").equals(liz.getLetterDueDate()));
		//liz should be 1 year after the finish date of the letter
		Patient jo = new Patient("jo");
		jo.setCategory("T1");
		jo.setVisitDate(new RCalendar("2011-6-1"));
		doctor.patientLetterDueDate(jo);
		assertTrue(new RCalendar("2011-6-1").equals(jo.getLetterDueDate()));
		//jo should be = visit date
		Patient sarahjane = new Patient("sarahjane");
		sarahjane.setCategory("T2");
		sarahjane.setVisitDate(new RCalendar("2011-1-1"));
		Letter l2 = new Letter();
		l2.setFinishDate(new RCalendar("2011-2-2"));
		sarahjane.setLastLetter(l2);
		doctor.patientLetterDueDate(sarahjane);
		assertTrue(new RCalendar("2011-1-29").equals(sarahjane.getLetterDueDate()));
		//sarahjane should be 4 weeks after visit date
		Patient harry = new Patient("harry");
		harry.setCategory("T2");
		harry.setLastLetter(l2);
		doctor.patientLetterDueDate(harry);
		assertTrue(new RCalendar("2011-3-16").equals(harry.getLetterDueDate()));
		//harry should be 6 weeks after finish date
		Patient leela = new Patient("leela");
		leela.setCategory("T3");
		leela.setVisitDate(new RCalendar("2011-3-3"));
		doctor.patientLetterDueDate(leela);
		assertTrue(new RCalendar("2011-2-3").equals(leela.getLetterDueDate()));
		//leela should be 4 weeks before the visit date
		Patient k9 = new Patient("k9");
		k9.setCategory("T3");
		doctor.patientLetterDueDate(k9);		
		assertTrue(new RCalendar().equals(k9.getLetterDueDate()));
	}

	public void testFirstPossibleReminderDate(){
		Doctor doctor = new Doctor("strangelove");
		PatientCategoryPreference pcp1 = new PatientCategoryPreference("T1");
		PatientCategoryPreference pcp2 = new PatientCategoryPreference("T2");
		PatientCategoryPreference pcp3 = new PatientCategoryPreference("T3");
		PatientCategoryPreference pcp4 = new PatientCategoryPreference("T4");
		pcp1.setRemindBase("lastLetter");
		pcp1.setRemindUnit("d");
		pcp1.setRemindOffset(6);
		//6 days after the last letter
		pcp2.setRemindBase("visitDate");
		pcp2.setRemindUnit("w");
		pcp2.setRemindOffset(6);
		//6 weeks after the visitDate
		pcp3.setRemindBase("letterDueDate");
		pcp3.setRemindUnit("d");
		pcp3.setRemindOffset(30);
		//30 days after the letterDueDate
		pcp4.setRemindBase("letterDueDate");
		pcp4.setRemindUnit("w");
		pcp4.setRemindOffset(-2);
		//2 weeks before letter due date
		HashMap<String, PatientCategoryPreference> pcpMap = new HashMap<String, PatientCategoryPreference>();
		pcpMap.put("T1", pcp1);
		pcpMap.put("T2", pcp2);
		pcpMap.put("T3", pcp3);
		pcpMap.put("T4", pcp4);
		doctor.setPatientCategoryPreferences(pcpMap);
		Patient liz = new Patient("liz");
		liz.setCategory("T1");
		Letter lizl = new Letter();
		lizl.setFinishDate(new RCalendar("2011-3-21"));
		liz.setLastLetter(lizl);
		liz.setVisitDate(new RCalendar("2011-2-2"));
		liz.setLetterDueDate(new RCalendar("2011-9-21"));
		doctor.firstPossibleReminderDate(liz);
		assertTrue(new RCalendar("2011-3-27").equals(liz.getFirstPossibleReminderDate()));
		//change liz's category
		liz.setCategory("T2");
		doctor.firstPossibleReminderDate(liz);
		assertTrue(new RCalendar("2011-3-16").equals(liz.getFirstPossibleReminderDate()));
		liz.setCategory("T3");
		doctor.firstPossibleReminderDate(liz);
		assertTrue(new RCalendar("2011-10-21").equals(liz.getFirstPossibleReminderDate()));
		liz.setCategory("T4");
		doctor.firstPossibleReminderDate(liz);
		assertTrue(new RCalendar("2011-9-7").equals(liz.getFirstPossibleReminderDate()));
		Patient katarina = new Patient("katarina");
		katarina.setCategory("T1");
		doctor.firstPossibleReminderDate(katarina);
		assertTrue(new RCalendar().equals(katarina.getFirstPossibleReminderDate()));
		//katarina has no info to use
		liz.setFirstPossibleReminderDate(null);
		liz.setCategory("NA");
		doctor.firstPossibleReminderDate(liz);
		assertNull(liz.getFirstPossibleReminderDate());
		//set liz's category to something that doesn't exist
	}
	
	public void testFirstPossibleReminderDateLocal(){
		Doctor who = new Doctor("who");
		PatientCategoryPreference pcp = new PatientCategoryPreference("cp");
		HashMap<String, PatientCategoryPreference> pcpMap = new HashMap<String, PatientCategoryPreference>();
		pcp.setRemindBase("local");
		pcp.setRemindUnit("w");
		pcp.setRemindOffset(-1);
		pcpMap.put("cp", pcp);
		who.setPatientCategoryPreferences(pcpMap);
		//alice has a letter for this visit, expect null
		Patient alice = new Patient("alice");
		alice.setCategory("cp");
		alice.setVisitDate(new RCalendar("2011-7-7"));
		alice.setLetterDueDate(new RCalendar("2011-8-8"));
		//the caterpillar is just due for a letter. Expect 2011-6-30
		Patient caterpillar = new Patient("caterpillar");
		caterpillar.setCategory("cp");
		caterpillar.setVisitDate(new RCalendar("2011-7-7"));
		caterpillar.setLetterDueDate(new RCalendar("2011-7-10"));
		who.firstPossibleReminderDate(alice);
		who.firstPossibleReminderDate(caterpillar);
		assertNull(alice.getFirstPossibleReminderDate());
		assertTrue(caterpillar.getFirstPossibleReminderDate().equals(new RCalendar("2011-6-30")));
	}
}
