package com.unicorn.store.data;

import com.unicorn.store.model.Unicorn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Optional;

public class UnicornRepository  {
    private static final String DB_CONNECTION = System.getenv("DB_CONNECTION_URL");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");
    private static final Logger logger = LoggerFactory.getLogger(UnicornRepository.class);
    private static Connection connection;

    static {
        logger.info("START: Database Connection initialization");
        connection = getDatabaseConnection();
        logger.info("END: Database Connection initialization");
    }

    private static Connection getDatabaseConnection() {
        try {
            if (connection == null || !connection.isValid(1)) {
            	// we will just retry once here to keep it simple
            	// for prod systems, use retries with exponential backoff
            	connection = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
            }
        } catch (SQLException e) {
            logger.error("Error while connecting to the database", e);
        }
        return connection;
    }

    public Unicorn save(Unicorn unicorn) {
        var connection = getDatabaseConnection();
        try {
            var statement = connection.prepareStatement("INSERT INTO unicorns(name, age, size, type) VALUES(?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, unicorn.getName());
            statement.setString(2, unicorn.getAge());
            statement.setString(3, unicorn.getSize());
            statement.setString(4, unicorn.getType());
            statement.execute();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    unicorn.setId(generatedKeys.getString(1));
                } else {
                    throw new SQLException("Creating unicorn failed: No id returned");
                }
            }
            return unicorn;
        } catch (SQLException sqlException) {
            var errorMsg = "Error while saving unicorn";
            logger.error(errorMsg, sqlException);
            throw new RuntimeException(errorMsg, sqlException);
        }
    }
    
    public Unicorn update(Unicorn unicorn) {
        var connection = getDatabaseConnection();
        try {
            var statement = connection.prepareStatement("UPDATE unicorns set name=?, age=?, size=?, type=? where id=?");
            statement.setString(1, unicorn.getName());
            statement.setString(2, unicorn.getAge());
            statement.setString(3, unicorn.getSize());
            statement.setString(4, unicorn.getType());
            statement.setString(5, unicorn.getId());
            statement.execute();
            
            if (statement.getUpdateCount() < 1) {
            	throw new RuntimeException("Error: the unicorn could not be updated with the provided information");
            }
            return unicorn;
        } catch (SQLException sqlException) {
            logger.error("Error while updating unicorn", sqlException);
            throw new RuntimeException("Error while updating unicorn", sqlException);
        }
    }    

    public Optional<Unicorn> findById(String unicornId) {
        var connection = getDatabaseConnection();
        try {
            var statement = connection.prepareStatement("Select * from unicorns where id = ?");
            statement.setString(1, unicornId);
            var result = statement.executeQuery();

            if(result.next()){
                var unicorn = new Unicorn();
                unicorn.setId(result.getString("id"));
                unicorn.setName(result.getString("name"));
                unicorn.setAge(result.getString("age"));
                unicorn.setType(result.getString("type"));
                unicorn.setSize(result.getString("size"));
                return Optional.of(unicorn);
            }
            return Optional.empty();
        } catch (SQLException sqlException) {
            var errorMsg = "Error while retrieving unicorn";
            logger.error(errorMsg, sqlException);
            throw new RuntimeException(errorMsg, sqlException);
        }
    }

    public void delete(Unicorn unicorn) {
        var connection = getDatabaseConnection();
        try {
            var statement = connection.prepareStatement("DELETE FROM unicorns WHERE id = ?");
            statement.setString(1, unicorn.getId());
            statement.execute();
        } catch (SQLException sqlException) {
            var errorMsg = "Error while deleting unicorn";
            logger.error(errorMsg, sqlException);
            throw new RuntimeException(errorMsg, sqlException);
        }
    }
}
