package project.db.tables;

import project.db.Table;
import project.model.Ward;
import project.utils.GUIUtils;
import project.utils.Pair;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class WardsTable implements Table<Ward, Pair<Integer,Pair<Integer,String>>> {

    public static final String TABLE_NAME = "Ward";
    private final Connection connection;

    public WardsTable(final Connection connection) {
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
                            "building_id int NOT NULL," +
                            "sector varchar(50) NOT NULL," +
                            "type varchar(50) NOT NULL," +
                            "PRIMARY KEY (building_id,sector)," +
                            "FOREIGN KEY (building_id) REFERENCES " + BuildingsTable.TABLE_NAME + "(id)," +
                            "UNIQUE KEY ward_id (id)" +
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
    public boolean insertToTable(Ward value) {
        final String query = "INSERT INTO " + TABLE_NAME +
                " (id,building_id,sector,type)" +
                " VALUES (?,?,?,?)";
        try (final var preparedStatement = this.connection.prepareStatement(query)) {
            preparedStatement.setInt(1, getMaxId());
            preparedStatement.setInt(2, value.getBuilding_id());
            preparedStatement.setString(3, value.getSector());
            preparedStatement.setString(4, value.getType());
            preparedStatement.executeUpdate();
            return true;
        } catch (final SQLIntegrityConstraintViolationException e) {
            GUIUtils.exceptionToast("Constraint violation with new ward parameters:", e);
            return false;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("An error as occurred during a new ward insertion:", e);
            return false;
        }
    }

    @Override
    public boolean updateTable(Ward value, Pair<Integer, Pair<Integer, String>> primaryKey) {
        final Integer id = primaryKey.getX();
        final Integer building_id = primaryKey.getY().getX();
        final String sector = primaryKey.getY().getY();

        final String query = "UPDATE " + TABLE_NAME +
                " SET id=?, building_id=?, sector=?, type=?" +
                " WHERE id=? AND building_id=? AND sector=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, value.getId());
            statement.setInt(2, value.getBuilding_id());
            statement.setString(3, value.getSector());
            statement.setString(4, value.getType());
            statement.setInt(5, id);
            statement.setInt(6, building_id);
            statement.setString(7, sector);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL update error", e);
            return false;
        }
    }

    @Override
    public boolean deleteFromTable(Pair<Integer, Pair<Integer, String>> primaryKey) {
        final Integer id = primaryKey.getX();
        final Integer building_id = primaryKey.getY().getX();
        final String sector = primaryKey.getY().getY();

        final String query = "DELETE FROM " + TABLE_NAME + " WHERE id=? OR (building_id=? AND sector=?)";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setInt(2, building_id);
            statement.setString(3, sector);
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL delete error", e);
            return false;
        }
    }

    @Override
    public List<Ward> findResultSet(ResultSet result) {
        final List<Ward> wards = new ArrayList<>();
        try {
            while (result.next()) {
                final int id = result.getInt("id");
                final int building_id = result.getInt("building_id");
                final String sector = result.getString("sector");
                final String type = result.getString("type");
                final Ward ward = new Ward(id, building_id, sector, type);
                wards.add(ward);
            }
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL result set error", e);
        }
        return wards;
    }

    @Override
    public List<Ward> findAll() {
        List<Ward> wards = new ArrayList<>();
        try (final Statement statement = this.connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
            wards = findResultSet(resultSet);
        } catch (final SQLException e) { }
        return wards;
    }

    @Override
    public Optional<Ward> findByPrimaryKey(Pair<Integer, Pair<Integer, String>> primaryKey) {
        final Integer id = primaryKey.getX();
        final Integer building_id = primaryKey.getY().getX();
        final String sector = primaryKey.getY().getY();

        Optional<Ward> ward = Optional.empty();
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE id=? OR (building_id=? AND sector=?)";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setInt(2, building_id);
            statement.setString(3, sector);
            final ResultSet resultSet = statement.executeQuery();
            final List<Ward> wards = findResultSet(resultSet);
            if (!wards.isEmpty()) {
                ward = Optional.of(wards.get(0));
            }
        } catch (final SQLException e) { }
        return ward;
    }

    /**
     * @return the max id in the table + 1
     */
    private Integer getMaxId() {
        return findAll().size() + 1;
    }
}
