package project.db;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

public interface Table<V, K> {
    /**
     * @return the name of the table.
     */
    String getTableName();

    /**
     * Creates the database table.
     * @return true if the table has been created successfully, false otherwise.
     */
    boolean createTable();

    /**
     * Drops the database table.
     * @return true if there is a table to be dropped, false otherwise.
     */
    boolean dropTable();

    /**
     * Inserts a new object to the database.
     * @param value the object that needs to be saved into the database.
     * @return true if the object was successfully saved into the database, false otherwise.
     */
    boolean insertToTable(final V value);

    /**
     * Updates an already existing object in the database.
     * @param value the new object values that have to be assigned.
     * @return true if the object has been updated successfully, false otherwise.
     */
    boolean updateTable(final V value, final K primaryKey);

    /**
     * Deletes an object from the database.
     * @param primaryKey the primary key that identifies the object that needs to be removed.
     * @return true if the object has been deleted successfully, false otherwise.
     */
    boolean deleteFromTable(final K primaryKey);

    List<V> findResultSet(final ResultSet result);

    List<V> findAll();

    Optional<V> findByPrimaryKey(final K primaryKey);

}
