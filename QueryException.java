package bbtrial.nl.logicgate.ace;

import java.util.List;

public class QueryException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private static String EOL = System.getProperty("line.separator");

	public QueryException(String query, List<?> parameters, Exception cause) {
		super(toMsg(query, parameters, cause.getMessage()), cause);
	}

	public QueryException(String query, List<?> parameters, String msg) {
		super(toMsg(query, parameters, msg));
	}

	private static String toMsg(String query, List<?> parameters, String msg) {
		return msg + EOL + query + EOL + parameters;
	}
}

