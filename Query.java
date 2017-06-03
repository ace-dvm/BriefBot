package bbtrial.nl.logicgate.ace;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

public class Query {
	private Connection conn;

    private PrintStream err;

    public Query(DataSource ds) {
        this(ds, System.err);
    }

    public Query(DataSource ds, PrintStream err) {
        try {
            this.conn = ds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.err = err;
    }

    public Query(Connection conn, PrintStream err) {
        this.conn = conn;
        this.err = err;
    }

    /**
     * @param query
     *            the SQL Query to execute
     * 
     * @return List of "rows" represented as a Map of Column Name to Column
     *         Value
     */
    public List<Map<String, Object>> executeQuery(String query) {
        return executeQuery(query, Collections.EMPTY_LIST);
    }

    /**
     * @param query
     *            the SQL Query to execute
     * @param params
     *            the List<?> containing the parameters for the query
     * 
     * @return List of "rows" represented as a Map of Column Name to Column
     *         Value
     */
    public List<Map<String, Object>> executeQuery(String query, List<?> params) {
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try {
            pStmt = prepareStatement(query, params);
            rs = pStmt.executeQuery();
            return mapRows(rs);
        } catch (SQLException sqlEx) {
            throw new QueryException(query, params, sqlEx);
        } finally {
            close(pStmt, rs);
        }
    }

    /**
     * Returns the record count for the specified table
     * @param table
     * @return
     */
    public int executeCount(String table){
    	String query = "SELECT COUNT(*) FROM " + table;
    	int records = 0;
    	PreparedStatement pStmt = null;
    	ResultSet rs = null;
    	List<?> params = Collections.EMPTY_LIST;
    	try {
    		pStmt = prepareStatement(query, params);
    		rs = pStmt.executeQuery();
    		//this result should always be a single int value
    		rs.next();
    		records = rs.getInt(1);
    	} catch (SQLException sqlEx) {
            throw new QueryException(query, params, sqlEx);
        } finally {
            close(pStmt, rs);
        }
    	return records;
    }
    
    /**
     * @param query
     *            the SQL Query to execute
     * @param params
     *            the List<?> containing the parameters for the query
     * 
     * @return either a row count, or 0 for SQL commands
     */
    public int executeUpdate(String query) {
        return executeUpdate(query, Collections.EMPTY_LIST);
    }

    /**
     * Executes an INSERT query
     * Returns the primary keys of the affected rows as a list of strings
     * @param query
     */
    public List<String> executeInsert(String query) {
    	return executeTransaction(query);
    }

    /**
     * Executes a transactional query, returns the keys
     * of the inserted rows in a list.
     * @param query
     */
    private List<String> executeTransaction(String query) {
    	List<String> keys = null;
    	try {
    		//Switch to manual transaction mode by setting
    		//autocommit to false. Note that this starts the first 
    		//manual transaction.
    		conn.setAutoCommit(false);
    		Statement stmt = conn.createStatement();
    		stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
    		conn.commit(); //This commits the transaction and starts a new one.
    		keys = listKeys(stmt.getGeneratedKeys());
    		stmt.close(); //This turns off the transaction.
    	}
    	catch (SQLException ex) {
    		new WriteFile().appendFile(BriefBot.ERROR_FILE, "Unable to run query: " 
    				+ query + "\n" + ex.getMessage() + "\n");
    		try {
    			conn.rollback();
    		}
    		catch (SQLException se) {
    			new WriteFile().appendFile(BriefBot.ERROR_FILE, "Unable to roll back: " 
    					+ se.getMessage() + "\n");
    		}
    	}
    	return keys;
    }

    
    /**
     * Converts the values of a result set to a list of Strings.
     * Meant to be used when there is only a single-column result.
     * @param rs
     * @return keys as a list of strings
     * @throws SQLException
     */
	private List<String> listKeys(ResultSet rs) throws SQLException {
		int cols = rs.getMetaData().getColumnCount();
        List<String> rows = new ArrayList<String>();
        while (rs.next()) {
            for (int i = 1; i <= cols; i++) {
            	rows.add(rs.getObject(i).toString());
            }
        }
		return rows;
	}

	/**
     * @param query
     *            the SQL Query to execute
     * @param params
     *            the List<?> containing the parameters for the query
     * 
     * @return the row count (or 0 for SQL statements which return nothing)
     */
    public int executeUpdate(String query, List<?> params) {
        PreparedStatement pStmt = null;
        try {
        	conn.setAutoCommit(false);
            pStmt = prepareStatement(query, params);
            int result = pStmt.executeUpdate();
            conn.commit();
            return result;
        } catch (SQLException sqlEx) {
        	new WriteFile().appendFile(BriefBot.ERROR_FILE, "Unable to run query: " 
    				+ query + "\n" + sqlEx.getMessage() + "\n");
            throw new QueryException(query, params, sqlEx);
        } finally {
            close(pStmt);
        }
    }



    // 

    private PreparedStatement prepareStatement(String query, List<?> params)
        throws SQLException {
        if (query == null) {
            throw new IllegalArgumentException("query string may not be null");
        }
        if (query.length() == 0) {
            throw new IllegalArgumentException("query string may not be empty");
        }
        PreparedStatement pStmt = conn.prepareStatement(query);
        int numParams = pStmt.getParameterMetaData().getParameterCount();

        if (numParams != params.size()) {
            final String msg = "Expected " + numParams + " parameters, got "
                + params.size();
            throw new IllegalArgumentException(msg);
        }

        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            pStmt.setObject(i + 1, param);
        }
        return pStmt;
    }

    private List<Map<String, Object>> mapRows(ResultSet rs) throws SQLException {
        int cols = rs.getMetaData().getColumnCount();
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        while (rs.next()) {
            Map<String, Object> rowVals = new LinkedHashMap<String, Object>(
                cols);
            for (int i = 1; i <= cols; i++) {
                String columnName = rs.getMetaData().getColumnName(i);                
                Object columValue = rs.getObject(i);
                rowVals.put(columnName, columValue);
            }
            rows.add(rowVals);
        }
        return rows;
    }

    private void close(Statement stmt, ResultSet rs) {
        close(rs);
        close(stmt);
    }

    private void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Throwable t) {
                t.printStackTrace(err);
            }
        }
    }

    private void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Throwable t) {
                t.printStackTrace(err);
            }
        }
    }
}

