package bbtrial.nl.logicgate.ace;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

public class RCalendarTest extends TestCase {

	public void testDefault(){
		RCalendar r = new RCalendar();
		Calendar cal = new GregorianCalendar();
		Calendar rCal = Calendar.getInstance();
		rCal.setTimeInMillis(r.getMillis());
		assertEquals(cal.get(Calendar.DATE), rCal.get(Calendar.DATE));
	}
	
	public void testString(){
		RCalendar xmas = new RCalendar("2008-12-25");
		assertEquals("2008-12-25", xmas.toString());
	}
	
	public void testNonsenseString(){
		RCalendar yuk;
		try{
			yuk = new RCalendar("yukko gummo");
		} catch (Exception e){
			yuk = null;
		}
		assertEquals(null, yuk);
	}
	
	public void testIntIntInt(){
		RCalendar xmas = new RCalendar(2008,12,25);
		assertEquals("2008-12-25", xmas.toString());
	}
	

	public void testCalendar(){
		Calendar cal = Calendar.getInstance();
		//11 = december in Java land
		cal.set(2008, 11, 25,0, 0, 0);
		RCalendar rcal = new RCalendar(cal);
		System.out.println(rcal.toString());
		assertEquals("2008-12-25", rcal.toString());
	}
	
	public void testRCalendar(){
		RCalendar one = new RCalendar("2008-12-25");
		RCalendar two = new RCalendar(one);
		assertTrue(one.equals(two));
	}
	
	public void testTimestamp(){
		Timestamp t = java.sql.Timestamp.valueOf("2009-08-21 02:10:05");
		RCalendar aug = new RCalendar(t);
		Calendar calAug = Calendar.getInstance();
		calAug.setTimeInMillis(aug.getMillis());
		assertEquals(7, calAug.get(Calendar.MONTH));
	}
	
	//value of DWMY will change with current date. Appears to work fine.
	
	public void testSetDayOfMonth(){
		RCalendar r = new RCalendar("2011-8-4");
		r.setDayOfMonth(1);
		assertEquals("2011-8-1", r.toString());
	}
	
	public void testSetWeekOfMonth(){
		RCalendar r = new RCalendar("2011-7-7");
		r.setWeekOfMonth(4);
		assertEquals("2011-7-21", r.toString());
		RCalendar r2 = new RCalendar("2011-7-7");
		r2.setWeekOfMonth(1);
		assertEquals("2011-6-30", r2.toString());
	}
	
	public void testAddDWMY(){
		RCalendar r = new RCalendar("2008-12-25");
		RCalendar nye = new RCalendar(r, "d", 6);
		assertEquals("2008-12-31", nye.toString());
		RCalendar r2 = new RCalendar("2008-12-25");
		r2.add(0,0,-4,-2);
		assertEquals("2008-11-25", r2.toString());
	}
	
	public void setToLastDayOfMonth(){
		//setting day to -0 should give us the last day of the previous month
		RCalendar r = new RCalendar("2011-7-7");
		r.setDayOfMonth(-0);
		assertEquals("2011-6-30", r.toString());
		RCalendar r2 = new RCalendar("2011-7-7");
		r2.setDayOfMonth(-4);
		assertEquals("2011-6-26", r2.toString());
	}
	
	public void testGetAndSetStringDayOfWeek(){
		RCalendar r = new RCalendar("2011-6-30");
		assertEquals("thursday", r.getStringDayOfWeek());
		r.setStringDayOfWeek("monday");
		assertEquals("2011-6-27", r.toString());
		r.setStringDayOfWeek("friday");
		assertEquals("2011-7-1", r.toString());
	}
}
