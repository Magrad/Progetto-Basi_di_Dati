package project.db.tables;

import project.db.Table;
import project.model.Ambulatory;
import project.utils.GUIUtils;
import project.utils.Pair;

import java.sql.*;
import java.util.*;

public class AmbulatoryTable implements Table<Ambulatory,Pair<Integer,Pair<Integer,Integer>>> {

    public static final String TABLE_NAME = "Ambulatorie";
    private final Connection connection;

    public AmbulatoryTable(final Connection connection) {
        this.connection = Objects.requireNonNull(connection);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public boolean createTable() {
        try (final Statement statement = this.connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE " + TABLE_NAME + " (" +
                            "id int NOT NULL," +
                            "room_number int NOT NULL," +
                            "ward_id int NOT NULL," +
                            "PRIMARY KEY (room_number, ward_id)," +
                            "FOREIGN KEY (ward_id) REFERENCES " + WardsTable.TABLE_NAME + "(id)," +
                            "UNIQUE KEY ambulatory_id (id)" +
                            ")");
            return true;
        } catch (final SQLException e) {
            return false;
        }
    }

    @Override
    public boolean dropTable() {
        try (final Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("DROP TABLE " + TABLE_NAME);
            return true;
        } catch (final SQLException e) {
            return false;
        }
    }

    @Override
    public boolean insertToTable(Ambulatory value) {
        final String query = "INSERT INTO " + TABLE_NAME +
                " (id,room_number,ward_id)" +
                " VALUES (?,?,?)";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, getMaxId());
            statement.setInt(2, value.getRoom_number());
            statement.setInt(3, value.getWard_id());
            statement.executeUpdate();
            return true;
        } catch (final SQLIntegrityConstraintViolationException e) {
            GUIUtils.exceptionToast("Constraint violation with new ambulatory parameters:", e);
            return false;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("An error as occurred during a new ambulatory insertion:", e);
            return false;
        }
    }

    @Override
    public boolean updateTable(Ambulatory value, Pair<Integer, Pair<Integer, Integer>> primaryKey) {
        Integer id = primaryKey.getX();
        Integer room_number = primaryKey.getY().getX();
        Integer ward_id = primaryKey.getY().getY();

        final String query = "UPDATE " + TABLE_NAME +
                " SET id=?, room_number=?, ward_id=?," +
                " WHERE id=? OR (room_number=? AND ward_id=?)";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, value.getId());
            statement.setInt(2, value.getRoom_number());
            statement.setInt(3, value.getWard_id());
            statement.setInt(4, id);
            statement.setInt(5, room_number);
            statement.setInt(6, ward_id);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL update error", e);
            return false;
        }
    }

    @Override
    public boolean deleteFromTable(Pair<Integer, Pair<Integer, Integer>> primaryKey) {
        Integer id = primaryKey.getX();
        Integer room_number = primaryKey.getY().getX();
        Integer ward_id = primaryKey.getY().getY();

        final String query = "DELETE FROM " + TABLE_NAME + " WHERE id=? OR (room_number=? AND ward_id=?)";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setInt(2, room_number);
            statement.setInt(3, ward_id);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL delete error", e);
            return false;
        }
    }

    @Override
    public List<Ambulatory> findResultSet(ResultSet result) {
        final List<Ambulatory> ambulatories = new ArrayList<>();
        try {
            while (result.next()) {
                final int id = result.getInt("id");
                final int room_number = result.getInt("room_number");
                final int ward_id = result.getInt("ward_id");
                final Ambulatory ambulatory = new Ambulatory(id, room_number, ward_id);
                ambulatories.add(ambulatory);
            }
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL result set error", e);
        }
        return ambulatories;
    }

    @Override
    public List<Ambulatory> findAll() {
        List<Ambulatory> ambulatories = new ArrayList<>();
        try (final Statement statement = this.connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
            ambulatories = findResultSet(resultSet);
        } catch (final SQLException e) { }
        return ambulatories;
    }

    @Override
    public Optional<Ambulatory> findByPrimaryKey(Pair<Integer, Pair<Integer, Integer>> primaryKey) {
        Integer id = primaryKey.getX();
        Integer room_number = primaryKey.getY().getX();
        Integer ward_id = primaryKey.getY().getY();

        Optional<Ambulatory> ambulatory = Optional.empty();
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE id=? OR (room_number=? AND ward_id=?)";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setInt(2, room_number);
            statement.setInt(3, ward_id);
            final ResultSet resultSet = statement.executeQuery();
            final List<Ambulatory> ambulatories = findResultSet(resultSet);
            if (!ambulatories.isEmpty()) {
                ambulatory = Optional.of(ambulatories.get(0));
            }
        } catch (final SQLException e) { }
        return ambulatory;
    }

    /**
     * Retrieves the first ambulatory available for a given ward and day.
     * This method is used to assign a new agenda to an ambulatory.
     * @param ward_id
     * @param day
     * @return
     */
    public Optional<Ambulatory> findFirstAmbulatoryAvailableByWardId(Integer ward_id, String day) {
        Optional<Ambulatory> ambulatory = Optional.empty();
        final String query = "SELECT * FROM " + TABLE_NAME + " AS a WHERE ward_id=?" +
                " AND id NOT IN" +
                " (SELECT ambulatory_id FROM " + AgendasTable.TABLE_NAME + " AS ag" +
                " WHERE day=?" +
                ")";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, ward_id);
            statement.setString(2, day);
            final ResultSet resultSet = statement.executeQuery();
            final List<Ambulatory> ambulatories = findResultSet(resultSet);
            if (!ambulatories.isEmpty()) {
                ambulatory = Optional.of(ambulatories.get(0));
            }
        } catch (final SQLException e) { }
        return ambulatory;
    }

    /**
     * Gets the maximum id in the table and adds 1 to it,
     * in this way we can insert a new ambulatory with a unique
     * increasing id.
     * @return the new id.
     */
    private Integer getMaxId() {
        return findAll().size() + 1;
    }
}
