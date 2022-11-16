package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import entity.ToyEntity;
import entity.TransactionEntity;
import exception.DataAccessException;
import jdbc.JdbcConnectionManager;

public class TransactionRepository {
	
	private final static String SELECT = "SELECT t.id, t.transaction_number, t.cart_number, t.room, t.grand_total, t.date_created, t.status";
	private final static String SELECT_ALL = SELECT + " FROM transactions t";
	private final static String SELECT_BY_USER_ID = SELECT_ALL + " FROM transaction t, cart_item c, champ_user u"
													+ " WHERE t.cart_number = c.cart_number AND c.user_number = u.user_number";
	private final static String INSERT_TRANSACTION_DETAILS = "INSERT INTO transaction (id, transaction_number, cart_number, room, grand_total, date_created, status) VALUES (?,?,?,?,?,?,?)";
	private final static String UPDATE_TRANSACTION_STATUS = "UPDATE transaction SET status = ? WHERE transaction_number = ?";
	
	private final static int COLUMN_ID = 1;
	private final static int COLUMN_TRANSACTION_NUMBER = 2;
	private final static int COLUMN_CART_NUMBER = 3;
	private final static int COLUMN_ROOM = 4;
	private final static int COLUMN_GRAND_TOTAL = 5;
	private final static int COLUMN_DATE_CREATED = 6;
	private final static int COLUMN_STATUS = 7;
	
    private JdbcConnectionManager jdbcConnectionManager;

    public TransactionRepository(JdbcConnectionManager jdbcConnectionManager) {
        this.jdbcConnectionManager = jdbcConnectionManager;
    }
    
    public List<TransactionEntity> findAll() {
    	try {
            final Connection connection = jdbcConnectionManager.getConnection();
            final PreparedStatement findAllQuery = connection.prepareStatement(SELECT_ALL);
            final ResultSet resultSet = findAllQuery.executeQuery();
            final List<TransactionEntity> transactions = new ArrayList<>();
            while (resultSet.next()) {
                toTransaction(resultSet, transactions);
            }
            return transactions;
        } catch (Exception e) {
            throw DataAccessException.instance("Failed to retrieve transactions: " + e.getMessage());
        }
    }
    
    public List<TransactionEntity> findByUserId() {
    	try {
            final Connection connection = jdbcConnectionManager.getConnection();
            final PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_ID);
            final ResultSet resultSet = statement.executeQuery();
            final List<TransactionEntity> transactions = new ArrayList<>();
            while (resultSet.next()) {
                toTransaction(resultSet, transactions);
            }

            return transactions;
        } catch (Exception e) {
            throw DataAccessException.instance("Failed to retrieve transactions: " + e.getMessage());
        }
    }
    
    public void updateTransactionStatus(String transactionNumber, String status) {
    	try {
			PreparedStatement statement = jdbcConnectionManager.getConnection().prepareStatement(UPDATE_TRANSACTION_STATUS);
			statement.setString(1, status);
			statement.setString(2, transactionNumber);
			
			int numberRowsAffected = statement.executeUpdate();
    		if (numberRowsAffected > 1)
    			throw new RuntimeException("Number of rows is greater than 1.");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void insertTransaction(int id, String transactionNumber, String cartNumber, String room, 
    							  float grandTotal, String dateCreated, String status) {
		try {
			final Connection connection = jdbcConnectionManager.getConnection();
            final PreparedStatement statement = connection.prepareStatement(INSERT_TRANSACTION_DETAILS);
	    	statement.setInt(COLUMN_ID, id);
	    	statement.setString(COLUMN_TRANSACTION_NUMBER, transactionNumber);
	    	statement.setString(COLUMN_CART_NUMBER, cartNumber);
	    	statement.setString(COLUMN_ROOM, room);
	    	statement.setFloat(COLUMN_GRAND_TOTAL, grandTotal);
	    	statement.setString(COLUMN_DATE_CREATED, dateCreated);
	    	statement.setString(COLUMN_STATUS, status);
	    	
	    	final ResultSet resultSet = statement.executeQuery();
	    	
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

	private void toTransaction(final ResultSet resultSet, final List<TransactionEntity> transactions)
			throws SQLException {
		TransactionEntity transaction = new TransactionEntity
			(
				resultSet.getInt(COLUMN_ID),
				resultSet.getString(COLUMN_TRANSACTION_NUMBER),
				resultSet.getString(COLUMN_CART_NUMBER),
				resultSet.getString(COLUMN_ROOM),
				resultSet.getFloat(COLUMN_GRAND_TOTAL),
				resultSet.getString(COLUMN_DATE_CREATED),
				resultSet.getString(COLUMN_STATUS)
			);
		transactions.add(transaction);
	}
}
