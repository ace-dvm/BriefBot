package bbtrial.nl.logicgate.ace;

import junit.framework.TestCase;

public class NextReminderCalculatorTest extends TestCase {

	public void testGetNextReminderDate(){
		NextReminderCalculator nrcNever = new NextReminderCalculator();
		nrcNever.setNever(true);
		NextReminderCalculator nrcWeek = new NextReminderCalculator();
		nrcWeek.setN(4);
		nrcWeek.setUnit("w");
		NextReminderCalculator nrc4ofMo0 = new NextReminderCalculator();
		nrc4ofMo0.setN(4);
		nrc4ofMo0.setUnit("ofMo");
		NextReminderCalculator nrc30ofMo3 = new NextReminderCalculator();
		nrc30ofMo3.setN(30);
		nrc30ofMo3.setUnit("ofMo");
		nrc30ofMo3.setM(3);
		NextReminderCalculator nrcTues = new NextReminderCalculator();
		nrcTues.setUnit("tuesday");
		NextReminderCalculator nrcLastThurs = new NextReminderCalculator();
		nrcLastThurs.setN(-1);
		nrcLastThurs.setUnit("thursday");
		nrcLastThurs.setM(2);
		NextReminderCalculator nrcFirstThurs = new NextReminderCalculator();
		nrcFirstThurs.setN(1);
		nrcFirstThurs.setUnit("thursday");
		assertNull(nrcNever.getNextReminderDate());
		assertTrue(new RCalendar().equals(nrcWeek.getNextReminderDate()));
//these will change over time, worked on 18 Jul 2011
//		assertTrue(new RCalendar("2011-7-4").equals(nrc4ofMo0.getNextReminderDate()));
//		assertTrue(new RCalendar("2011-7-30").equals(nrc30ofMo3.getNextReminderDate()));
//		assertTrue(new RCalendar("2011-7-19").equals(nrcTues.getNextReminderDate()));
//		assertTrue(new RCalendar("2011-7-28").equals(nrcLastThurs.getNextReminderDate()));
//		assertTrue(new RCalendar("2011-7-7").equals(nrcFirstThurs.getNextReminderDate()));
		nrcWeek.setPrevious(new RCalendar("2011-5-1"));
		nrc4ofMo0.setPrevious(new RCalendar("2011-5-1"));
		nrc30ofMo3.setPrevious(new RCalendar("2011-5-1"));
		nrcTues.setPrevious(new RCalendar("2011-5-1"));
		nrcLastThurs.setPrevious(new RCalendar("2011-5-1"));
		nrcFirstThurs.setPrevious(new RCalendar("2011-5-1"));
		assertNull(nrcNever.getNextReminderDate());
		assertTrue(new RCalendar("2011-5-29").equals(nrcWeek.getNextReminderDate()));
		assertTrue(new RCalendar("2011-5-4").equals(nrc4ofMo0.getNextReminderDate()));
		assertTrue(new RCalendar("2011-8-30").equals(nrc30ofMo3.getNextReminderDate()));
		assertTrue(new RCalendar("2011-5-3").equals(nrcTues.getNextReminderDate()));
		assertTrue(new RCalendar("2011-7-28").equals(nrcLastThurs.getNextReminderDate()));
		assertTrue(new RCalendar("2011-5-5").equals(nrcFirstThurs.getNextReminderDate()));
	}
	
}
