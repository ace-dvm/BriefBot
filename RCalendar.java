package bbtrial.nl.logicgate.ace;

/**
 * RCalendar is the Calendar for BriefBot.  It smooths over several of the
 * peculiarities of Java's Calendar, while retaining Calendar's functions.
 * RCalendar accepts and presents months in conventional (Jan = 1) rather than
 * Java (Jan = 0) counting.  
 * It also ensures that hours, minutes, seconds, and milliseconds are set to 0.
 * @author S. Medlock
 */

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class RCalendar {

		private long millis;

		/**
		 * Constructor for a default RCalendar object with today's year, month, and day;
		 * with hr:min:sec set to OO:OO:OO.
		 */
		
		public RCalendar(){
			Calendar cal = zeroTime(new GregorianCalendar()); 
			if(BriefBot.TODAY.length == 3){
				cal.set(BriefBot.TODAY[0], BriefBot.TODAY[1], BriefBot.TODAY[2]);
			}
			millis = cal.getTimeInMillis();
		}
		
		private Calendar zeroTime(Calendar cal){
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return cal;
		}
		
		/**
		 * Takes a String date in YYYY-mM-dD format and constructs an RCalendar object
		 * @param String rCal
		 */
		public RCalendar(String rCal) {
			Calendar cal = zeroTime(new GregorianCalendar());
			try {
				String[] rCalArray = rCal.split("-");
				String yearString = rCalArray[0];
				int year = Integer.parseInt(yearString);
				int month = Integer.parseInt(rCalArray[1]);
				int day = Integer.parseInt(rCalArray[2]);
				if(yearString.length()==4
						&&(month>0&&month<=12)
						&&(day>0&&day<=31)){		
					cal.set(year, month-1, day);}
				else{throw new IllegalArgumentException();}
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(rCal);
			}
			millis = cal.getTimeInMillis();
		}
		
		/**
		 * Takes a date as 3 integers (YYYY, mm, dd) and constructs an RCalendar object
		 * @param year
		 * @param month
		 * @param day
		 */
		public RCalendar(int year, int month, int day){
			Calendar cal = zeroTime(new GregorianCalendar());
			if((year<=9999 && year>=1000)
					&&(month>0 && month<=12)
					&&(day>0 && day<=31)){		
				cal.set(year, month-1, day);}
			else{throw new IllegalArgumentException(year+"-"+month+"-"+day);}
			millis = cal.getTimeInMillis();
		}

		/**
		 * Sets the year, month, and day of the current RCalendar equal
		 * to the year, month, and day of the Calendar object.
		 * @param cal
		 */
		private void setYearMonthDay(Calendar cal) {
			Calendar zeroCal = zeroTime(new GregorianCalendar());
			zeroCal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
			zeroCal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
			zeroCal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
			millis = zeroCal.getTimeInMillis();
		}
		
		/**
		 * Construct an RCalendar object from a Calendar object
		 * @param cal
		 */
		public RCalendar(Calendar cal) {
			setYearMonthDay(cal);
		}
		
		/**
		 * Construct an RCalendar object from another RCalendar object
		 * @param rCal
		 */
		public RCalendar(RCalendar rCal){
			millis = rCal.getMillis();
		}
		
		/**
		 * Construct and RCalendar object from an SQL Timestamp object
		 * @param Timestamp
		 */
		public RCalendar(Timestamp t){
			long milliseconds = t.getTime();
			Calendar cal = new GregorianCalendar();
			cal.clear();
			cal.setTime(new java.util.Date(milliseconds));
			setYearMonthDay(cal);
		}
		
		/**
		 * Construct an RCalendar which is equal to today's date
		 * plus some integer number of DATE (days), WEEK_OF_MONTH,
		 * MONTH, or YEAR.  Use negative numbers to subtract.
		 * @param String d, w, m, or y indicates that the amount
		 * should be added to days, weeks, months, or years
		 * @param amount is the number of d,w,m, or y to be added 
		 */
		public RCalendar(String dwmy, int amount){
			setYearMonthDay(millisPlusAmount(
					zeroTime(new GregorianCalendar()).getTimeInMillis(),
					dwmy, amount));
		}
		
		/**
		 * Construct an RCalendar which is equal to the date
		 * of the parameter r, plus some integer number of
		 * DATE (days), WEEK_OF_MONTH, MONTH, or YEAR.  
		 * Use negative numbers to subtract.
		 * @param r
		 * @param dwmy
		 * @param amount
		 */
		public RCalendar(RCalendar r, String dwmy, int amount){
			setYearMonthDay(millisPlusAmount(r.getMillis(), dwmy, amount));
		}
		
		private Calendar millisPlusAmount(long millis, String dwmy, int amount){
			Calendar cal = new GregorianCalendar();
			cal.clear();
			cal.setTime(new java.util.Date(millis));
			if(dwmy.equals("d")){cal.add(Calendar.DATE, amount);}
			if(dwmy.equals("w")){cal.add(Calendar.WEEK_OF_YEAR, amount);}
			if(dwmy.equals("m")){cal.add(Calendar.MONTH, amount);}
			if(dwmy.equals("y")){cal.add(Calendar.YEAR, amount);}
			return cal;
		}
		
		
		/**
		 * Gets the millis value of this RCalendar object, similar to
		 * Calendar.getTimeInMillis
		 * @return millis
		 */
		public long getMillis(){
			return millis;
		}
		
		public int getYear(){
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millis);
			return cal.get(Calendar.YEAR);
		}
		
		public void setYear(int year){
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millis);
			cal.set(Calendar.YEAR, year);
			millis = cal.getTimeInMillis();
		}
		
		/**
		 * Gets the number of the month in usual Jan = 1 form
		 * @return
		 */
		public int getRMonth(){
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millis);
			return (cal.get(Calendar.MONTH) + 1);
		}
		
		/**
		 * Gets the number of the month as a string
		 * with a leading zero for single-digit months
		 * @return
		 */
		public String getZeroMonth(){
			String month = "" + getRMonth();
			if(month.length() == 1){month = "0" + month;}
			return month;
		}
		
		/**
		 * Sets the number of the month, taking the month
		 * in usual Jan = 1 numbering
		 * @param month
		 */
		public void setRMonth(int month){
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millis);
			cal.set(Calendar.MONTH, (month-1));
			millis = cal.getTimeInMillis();
		}
		
		public int getWeekOfMonth(){
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millis);
			return cal.get(Calendar.WEEK_OF_MONTH);
		}
		
		public void setWeekOfMonth(int week){
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millis);
			cal.set(Calendar.WEEK_OF_MONTH, week);
			millis = cal.getTimeInMillis();
		}
		
		public int getDayOfMonth(){
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millis);
			return cal.get(Calendar.DAY_OF_MONTH);
		}
		
		public String getZeroDay(){
			String day = "" + getDayOfMonth();
			if(day.length() == 1){day = "0" + day;}
			return day;
		}
		
		public void setDayOfMonth(int day){
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millis);
			cal.set(Calendar.DAY_OF_MONTH, day);
			millis = cal.getTimeInMillis();
		}
		
		public int getIntDayOfWeek(){
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millis);
			return cal.get(Calendar.DAY_OF_WEEK);
		}
		
		public void setIntDayOfWeek(int intDayOfWeek){
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millis);
			cal.set(Calendar.DAY_OF_WEEK, intDayOfWeek);
			millis = cal.getTimeInMillis();
		}
		
		/**
		 * Gets the English string for the day of the week
		 * for this RCalendar.
		 * @return
		 */
		public String getStringDayOfWeek(){
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millis);
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			if(dayOfWeek == 1){return "sunday";}
			if(dayOfWeek == 2){return "monday";}
			if(dayOfWeek == 3){return "tuesday";}
			if(dayOfWeek == 4){return "wednesday";}
			if(dayOfWeek == 5){return "thursday";}
			if(dayOfWeek == 6){return "friday";}
			if(dayOfWeek == 7){return "saturday";}
			return "";
		}
		
		/**
		 * Sets the day of the week of this RCalendar using
		 * a String = the English day of the week.
		 * @param dayOfWeek
		 */
		public void setStringDayOfWeek(String dayOfWeek){
			if(dayOfWeek.equalsIgnoreCase("sunday")){setIntDayOfWeek(1);}
			if(dayOfWeek.equalsIgnoreCase("monday")){setIntDayOfWeek(2);}
			if(dayOfWeek.equalsIgnoreCase("tuesday")){setIntDayOfWeek(3);}
			if(dayOfWeek.equalsIgnoreCase("wednesday")){setIntDayOfWeek(4);}
			if(dayOfWeek.equalsIgnoreCase("thursday")){setIntDayOfWeek(5);}
			if(dayOfWeek.equalsIgnoreCase("friday")){setIntDayOfWeek(6);}
			if(dayOfWeek.equalsIgnoreCase("saturday")){setIntDayOfWeek(7);}
		}
		
		/**
		 * Returns the 3 letter abbreviation for the month
		 * of this RCalendar instance.
		 * (standard Dutch abbreviations)
		 * @return
		 */
		public String get3LetterMonth(){
			if(this.getRMonth()==1){return "jan";}
			if(this.getRMonth()==2){return "feb";}
			if(this.getRMonth()==3){return "mrt";}
			if(this.getRMonth()==4){return "apr";}
			if(this.getRMonth()==5){return "mei";}
			if(this.getRMonth()==6){return "jun";}
			if(this.getRMonth()==7){return "jul";}
			if(this.getRMonth()==8){return "aug";}
			if(this.getRMonth()==9){return "sep";}
			if(this.getRMonth()==10){return "okt";}
			if(this.getRMonth()==11){return "nov";}
			if(this.getRMonth()==12){return "dec";}
			return "";
		}
		
		/**
		 * Adds the specified amount of time to this RCalendar instance.
		 * Use negative numbers to subtract.
		 * @param years
		 * @param months
		 * @param weeks
		 * @param days
		 */
		public void add(int years, int months, int weeks, int days){
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millis);
			cal.add(Calendar.YEAR, years);
			cal.add(Calendar.MONTH, months);
			cal.add(Calendar.WEEK_OF_MONTH, weeks);
			cal.add(Calendar.DAY_OF_MONTH, days);
			millis = cal.getTimeInMillis();
		}
		
		/**
		 * Returns true if the current date-time of this RCalendar is before
		 * the date-time of the parameter RCalendar.
		 * @param r
		 * @return
		 */
		public boolean before(RCalendar c){
			if(millis < c.getMillis()){return true;}
			else{return false;}
		}
		
		/**
		 * Returns true if the current date-time of this RCalendar is after
		 * the date-time of the parameter RCalendar.
		 * @param r
		 * @return
		 */
		public boolean after(RCalendar c){
			if(millis > c.getMillis()){return true;}
			else{return false;}
		}	
		
		/**
		 * Checks if the date-time of this RCalendar is between two other RCalendar date-times.
		 * It is allowed to be equal to one (inclusive) but not the other (exclusive).
		 * @param inclusive
		 * @param exclusive
		 * @return true if this RCalendar is between the two dates
		 */
		public boolean betweenOneInclusive(RCalendar inclusive, RCalendar exclusive){
			if(this.equals(inclusive)){
				return true; //if it is equal to the inclusive date, just return true
			}
			RCalendar first = null;
			RCalendar second = null;
			if(inclusive.before(exclusive)){ //if inclusive is before exclusive, inclusive is first 
				first = inclusive;
				second = exclusive;
			} else { //else the other way 'round
				first = exclusive;
				second = inclusive;
			}
			//if it is after the earlier date and before the later date, it is between the two. Return true.
			if(this.after(first) && this.before(second)){
				return true;
			}
			//it is not equal to the inclusive or between the two dates. Return false.
			return false;
		}
		
		/**
		 * Returns the number of days which have elapsed between
		 * the date represented by this RCalendar and today's date.
		 * @return int
		 */
		public int daysSince() {
			long nowLong = new RCalendar().getMillis();
			long elapsedDays = (nowLong - millis)/(1000 * 60 * 60 * 24);
			//being RCalendars ensures that they will divide to a whole number of days
			return (int) elapsedDays;
		}
		
		/**
		 * Gets a string representation of this RCalendar object
		 * in DD MMM YYYY format (e.g. 01 Jul 1999)
		 * @return String
		 */
		public String getDDMMMYYYY(){
			return getZeroDay() + " " 
				+ get3LetterMonth() 
				+ " " + getYear();
		}
		
		/**
		 * Overrides equals.  Returns true if the time in millis for both
		 * RCalendar objects is the same (i.e. they represent the same time).
		 * @param r
		 * @return boolean
		 */
		public boolean equals(RCalendar r){
			if(this.getMillis() == r.getMillis()){return true;}
			else{return false;}
		}
		
		/**
		 * Returns the time represented by this RCalendar as a sql Timestamp object.
		 * This method was added for the demo.
		 * @return Timestamp
		 */
		public Timestamp toTimestamp(){
			return Timestamp.valueOf(this.getYear() + "-" + this.getZeroMonth() + "-" + this.getZeroDay() + " 00:00:000");
		}
		
		/**
		 * Overrides toString so that RCalendar objects print in the usual real-world
		 * year-month-day form.  Does not use leading 0's for single-digit months and days.
		 * @return String YYYY-M(M)-D(D)
		 */
		public String toString(){
			Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis(millis);
			return "" + cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH)+1) + "-" + cal.get(Calendar.DAY_OF_MONTH);
		}
}
