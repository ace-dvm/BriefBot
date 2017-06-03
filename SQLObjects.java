package bbtrial.nl.logicgate.ace;

import java.sql.Timestamp;

/**
 * Converts the objects returned by 
 * SQL queries to various data types. 
 * Returns null if the object is null.
 * @author skmedlock
 *
 */

public class SQLObjects {

	/**
	 * Just like calling .toString(), except returns null if the object itself is null
	 * @param object
	 * @return string the toString() or null
	 */
	public String objectToString(Object o) {
		if(o==null){
			return null;
		} else {			
			return o.toString().trim();
		}
	}
	
	/**
	 * Accepts any object with a toString that returns an appropriate
	 * SQL string, and returns it with single quotes. If the object is
	 * null, returns a null. Escapes quote characters.
	 * @param Object o Any object where 'toString()' is an acceptable SQL value
	 * @return String ' + toString() + '
	 */
	public String objectToSQLValue(Object o){
		if(o==null){
			return null;
		} else {
			String t = o.toString().trim().replaceAll("'", "''");
			return "'" + t + "'";
		}
	}
	
	/**
	 * if object is a Timestamp, returns an RCalendar. Otherwise returns null.
	 * @param object - any object, but preferably a java.sql.Timestamp object
	 * @return RCalendar
	 */
	public RCalendar sqlTimestampToRCalendar(Object object) {
		if(Timestamp.class.isInstance(object)){
			return new RCalendar((Timestamp) object);
		}
		return null;
	}
	
	/**
	 * Takes any object and tries to make an integer out of it.
	 * Works best if you give it objects that resemble integers,
	 * otherwise returns zero.
	 * @param o
	 * @return the integer represented by the object, or zero.
	 */
	public int objectToint(Object o) {
		if(o==null) return 0;
		try{
			if(Integer.class.isInstance(o)){
				Integer integer = (Integer) o;
				return integer.intValue(); 
			}
			return Integer.parseInt(o.toString().trim());
		} catch(Exception e){
			return 0;
		}
	}
	
	
}
