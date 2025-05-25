package com.autotrader.autotraderbackend.config;

import com.autotrader.autotraderbackend.repository.CarListingRepository;
import com.autotrader.autotraderbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Test service to cleanup resources after tests
 * Implements AutoCloseable to support try-with-resources pattern
 */
@Service
public class TestResourceCleanupService implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(TestResourceCleanupService.class);
    
    @Autowired
    private CarListingRepository carListingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DataSource dataSource;
    
    @Value("${spring.jpa.hibernate.ddl-auto:none}")
    private String hibernateDdlAuto;
    
    /**
     * Cleans up all test resources
     */
    public void cleanupResources() {
        // Skip cleanup if hibernate is configured to drop tables
        if ("create-drop".equals(hibernateDdlAuto)) {
            logger.info("Skipping explicit repository cleanup as Hibernate is configured to drop tables (ddl-auto=create-drop)");
            return;
        }
        
        // Clean up stored car listings for tests
        // Using deleteAll() is fine for tests as the database is ephemeral
        try {
            // Only attempt to delete if the table exists
            if (tableExists("car_listings")) {
                carListingRepository.deleteAll();
            }
        } catch (Exception e) {
            logger.warn("Exception during carListingRepository.deleteAll() in cleanup: {}", e.getMessage());
        }
        
        // Clean up stored users for tests
        try {
            // Only attempt to delete if the table exists
            if (tableExists("users")) {
                userRepository.deleteAll();
            }
        } catch (Exception e) {
            logger.warn("Exception during userRepository.deleteAll() in cleanup: {}", e.getMessage());
        }
        
        logger.info("Test resources cleanup attempt finished.");
    }
    
    /**
     * Check if a table exists in the database
     * @param tableName the name of the table to check
     * @return true if the table exists, false otherwise
     */
    private boolean tableExists(String tableName) {
        try {
            DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
            ResultSet tables = metaData.getTables(null, null, tableName, null);
            return tables.next();
        } catch (SQLException e) {
            logger.warn("Error checking if table {} exists: {}", tableName, e.getMessage());
            return false;
        }
    }
    
    /**
     * Implementation of AutoCloseable interface
     * Automatically called when used with try-with-resources
     */
    @Override
    public void close() {
        cleanupResources();
    }
}
