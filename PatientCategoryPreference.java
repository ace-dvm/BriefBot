package bbtrial.nl.logicgate.ace;

public class PatientCategoryPreference {

	private String category; //patient category that this rule is for
	private String dueBase;  //indicates which date to calculate from
	private int dueOffset;   //indicates the amount of units we should add or subtract from that date
	private String dueUnit;  //indicates which units to add or subtract from that date
	private String due2Base; //same as above, but a secondary rule to use if the first fails
	private int due2Offset;  //same as above, but a secondary rule to use if the first fails
	private String due2Unit; //same as above, but a secondary rule to use if the first fails
	private String remindBase;//indicates which date to calculate from
	private int remindOffset; //indicates how much to add or subtract from that date
	private String remindUnit;//indicates what unit to add or subtract from that date
	
	public PatientCategoryPreference(String category) {
		this.category = category;
		dueBase = null;
		dueUnit = null;
		due2Base = null;
		due2Unit = null;
		remindBase = null;
		remindUnit = null;
	}

	/**
	 * Uses the information in a Patient object to determine when a letter is due
	 * for that patient.
	 */
	public RCalendar calculateLetterDueDate(Patient p){
		if(dueBase.equalsIgnoreCase("lastLetter")){
			if(p.getLastLetter()==null){
				return calculateByRule2(p);
			} else {
				return calculateFromLastLetter(p.getLastLetter(), dueUnit, dueOffset);
			}
		}
		if(dueBase.equalsIgnoreCase("visitDate")){
			if(p.getVisitDate()==null){				
				return calculateByRule2(p);
			} else {				
				return new RCalendar(p.getVisitDate(), dueUnit, dueOffset);
			}
		}
		if(dueBase.equalsIgnoreCase("local")){
			LocalPCP local = loadLocal();
			return local.calculateLetterDueDate(p, dueUnit, dueOffset);
		}
		//this statement can only be reached if "dueBase" is not set or is set to something strange.
		return null;
	}
	
	private RCalendar calculateByRule2(Patient p) {
		if(due2Base!=null){
			if(due2Base.equalsIgnoreCase("lastLetter")){
				if(p.getLastLetter()==null){
					return null;
				} else {
					return calculateFromLastLetter(p.getLastLetter(), due2Unit, due2Offset);
				}			
			}
			if(due2Base.equalsIgnoreCase("visitDate")){
				if(p.getVisitDate()==null){
					return null;
				} else {
					return new RCalendar(p.getVisitDate(), due2Unit, due2Offset);
				}
			}
		}
		//if unable to calculate, return due date as today.
		return new RCalendar();
	}

	private RCalendar calculateFromLastLetter(Letter lastLetter, String unit, int offset){
		RCalendar base;
		if(lastLetter.getFinishDate() != null){
			base = lastLetter.getFinishDate();
		} else {
			base = lastLetter.getStartDate();
		}
		return new RCalendar(base, unit, offset);
	}
	
	public RCalendar calculateFirstAllowedReminderDate(Patient p){
		if(remindBase==null){
			return null;
		}
		if(remindBase.equalsIgnoreCase("lastLetter")){
			if(p.getLastLetter() != null){
				return calculateFromLastLetter(p.getLastLetter(), remindUnit, remindOffset);
			}
		}
		if(remindBase.equalsIgnoreCase("visitDate")){
			if(p.getVisitDate() != null){
				return new RCalendar(p.getVisitDate(), remindUnit, remindOffset);
			}
		}
		if(remindBase.equalsIgnoreCase("letterDueDate")){
			if(p.getLetterDueDate() != null){
				return new RCalendar(p.getLetterDueDate(), remindUnit, remindOffset);
			}
		}
		if(remindBase.equalsIgnoreCase("local")){
			//allows a more complex rule to be added by dynamically loading the class LocalPCP
			LocalPCP local = loadLocal();
			return local.calculateFirstAllowedReminderDate(p, remindUnit, remindOffset);
		}
		//if unable to calculate, set the first allowed reminder to today.
		return new RCalendar();
	}

	
	@SuppressWarnings("unchecked")
	private LocalPCP loadLocal() {
		String stem = this.getClass().getName().replaceFirst("PatientCategoryPreference", "");
		ClassLoader classLoader = this.getClass().getClassLoader();
		try {
	        Class<LocalPCP> local = (Class<LocalPCP>) classLoader.loadClass(stem + "LocalPCP");
	        return local.newInstance();
	    } catch (Exception e) {
	    	new WriteFile().appendFile(BriefBot.ERROR_FILE, "Problem loading LocalPCP \n" + e);
	    }
		//something has gone wrong, return null
		return null;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param dueBase the dueBase to set
	 */
	public void setDueBase(String dueBase) {
		this.dueBase = dueBase;
	}

	/**
	 * @param dueOffset the dueOffset to set
	 */
	public void setDueOffset(int dueOffset) {
		this.dueOffset = dueOffset;
	}

	/**
	 * @param dueUnit the dueUnit to set
	 */
	public void setDueUnit(String dueUnit) {
		if(dueUnit != null){
			this.dueUnit = dueUnit.substring(0, 1);			
		}
	}

	/**
	 * @param dueBase the dueBase to set
	 */
	public void setDue2Base(String due2Base) {
		this.due2Base = due2Base;
	}

	/**
	 * @param dueOffset the dueOffset to set
	 */
	public void setDue2Offset(int due2Offset) {
		this.due2Offset = due2Offset;
	}

	/**
	 * @param dueUnit the dueUnit to set
	 */
	public void setDue2Unit(String due2Unit) {
		if(due2Unit != null){
			this.due2Unit = due2Unit.substring(0, 1);
		}
	}	
	
	/**
	 * @param remindBase the remindBase to set
	 */
	public void setRemindBase(String remindBase) {
		this.remindBase = remindBase;
	}

	/**
	 * @param remindOffset the remindOffset to set
	 */
	public void setRemindOffset(int remindOffset) {
		this.remindOffset = remindOffset;
	}

	/**
	 * @param remindUnit the remindUnit to set
	 */
	public void setRemindUnit(String remindUnit) {
		if(remindUnit != null){
			this.remindUnit = remindUnit.substring(0, 1);
		}
	}
	
}
