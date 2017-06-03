package bbtrial.nl.logicgate.ace;

/**
 * NextReminderCalculator calculates when the next reminder can be sent to this doctor.
 * @author skmedlock
 *
 */

public class NextReminderCalculator {
	
	private boolean never;
	private int n;
	private String unit;
	private int m;
	private RCalendar previous;

	public NextReminderCalculator(){
		never = false;
		n = 0;
		unit = "";
		m = 0;
		previous = null;
	}

	public RCalendar getNextReminderDate() {
		if(never){
			return null;
		}
		if(unit.equals("d") || unit.equals("w") || unit.equals("m")){
			//add n units to the previous reminder date
			if(previous==null){
				return new RCalendar();
			} else {
				return new RCalendar(previous, unit, n);
			}
		}
		//set the month
		RCalendar next;
		if(previous==null){
			next = new RCalendar();
		} else {
			next = new RCalendar(previous, "m", m);
		}
		if(unit.equals("ofMo")){
			//set the day
			next.setDayOfMonth(n);
			return next;
		}
		//unit is a day of the week
		int days = (Math.abs(n)-1)*7;
		if(n>0){ 
			next.setDayOfMonth(1);
			int dif = intDOW(unit)-next.getIntDayOfWeek();
			if(dif<0){
				days = days + (7 + dif); //if dif is neg, add days to make up for week 1  
			} else {
				days = days + dif; //if dif is pos, add dif
			}
			next.add(0,0,0,days);
		}
		if(n<0){ //count from end of month
			next.add(0,1,0,0); next.setDayOfMonth(-0); //set to last day of month
			int dif = next.getIntDayOfWeek()-intDOW(unit);
			if(dif<0){
				days = days + (7 + dif); //if dif is neg, add days to make up for last week  
			} else {
				days = days - dif; //if dif is pos, subtract dif
			}
			next.add(0,0,0,-days);
		}
		if(n==0){
			next.setStringDayOfWeek(unit);
		}
		return next;
	}
	
	/**
	 * Integer values for string days of week
	 */
	private int intDOW(String strDOW){
		if(strDOW.equalsIgnoreCase("sunday")){return 1;}
		if(strDOW.equalsIgnoreCase("monday")){return 2;}
		if(strDOW.equalsIgnoreCase("tuesday")){return 3;}
		if(strDOW.equalsIgnoreCase("wednesday")){return 4;}
		if(strDOW.equalsIgnoreCase("thursday")){return 5;}
		if(strDOW.equalsIgnoreCase("friday")){return 6;}
		if(strDOW.equalsIgnoreCase("saturday")){return 7;}
		return 0;
	}
	
	/**
	 * @param never the never to set
	 */
	public void setNever(boolean never) {
		this.never = never;
	}
	
	/**
	 * Returns TRUE if this doctor NEVER gets a reminder
	 */
	public boolean isNever(){
		return never;
	}

	/**
	 * @param n the n to set
	 */
	public void setN(int n) {
		this.n = n;
	}

	/**
	 * @param unit the unit to set
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * @param m the m to set
	 */
	public void setM(int m) {
		this.m = m;
	}

	/**
	 * @param previous the previous to set
	 */
	public void setPrevious(RCalendar previous) {
		this.previous = previous;
	}

	public RCalendar getLastReminderDate() {
		return previous;
	}
	
	
}
