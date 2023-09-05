package project.db.tables;

import project.db.Table;
import project.model.Building;
import project.utils.GUIUtils;
import project.utils.Pair;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BuildingsTable implements Table<Building,Pair<Integer,String>> {

    public static final String TABLE_NAME = "Building";
    private final Connection connection;

    public BuildingsTable(final Connection connection) {
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
                            "id int AUTO_INCREMENT PRIMARY KEY," +
                            "name varchar(100) NOT NULL," +
                            "address varchar(50) NOT NULL," +
                            "city varchar(50) NOT NULL," +
                            "cap int NOT NULL," +
                            "province varchar(50) NOT NULL," +
                            "region varchar(50) NOT NULL," +
                            "phone varchar(50) NOT NULL," +
                            "UNIQUE KEY building_name (name)" +
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
    public boolean insertToTable(Building value) {
        final String query = "INSERT INTO " + TABLE_NAME +
                " (name,address,city,cap,province,region,phone)" +
                " VALUES (?,?,?,?,?,?,?)";
        try (final var preparedStatement = this.connection.prepareStatement(query)) {
            preparedStatement.setString(1, value.getName());
            preparedStatement.setString(2, value.getAddress());
            preparedStatement.setString(3, value.getCity());
            preparedStatement.setInt(4, value.getCap());
            preparedStatement.setString(5, value.getProvince());
            preparedStatement.setString(6, value.getRegion());
            preparedStatement.setString(7, value.getPhone());
            preparedStatement.executeUpdate();
            return true;
        } catch (final SQLIntegrityConstraintViolationException e) {
            GUIUtils.exceptionToast("Constraint violation with new building parameters:", e);
            return false;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("An error as occurred during a new building insertion:", e);
            return false;
        }
    }

    @Override
    public boolean updateTable(Building value, Pair<Integer,String> primaryKey) {
        final String query = "UPDATE " + TABLE_NAME +
                " SET name=?,address=?,city=?,cap=?,province=?,region=?,phone=?" +
                " WHERE id=? or name=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, value.getName());
            statement.setString(2, value.getAddress());
            statement.setString(3, value.getCity());
            statement.setInt(4, value.getCap());
            statement.setString(5, value.getProvince());
            statement.setString(6, value.getRegion());
            statement.setString(7, value.getPhone());
            statement.setInt(8, primaryKey.getX());
            statement.setString(9, primaryKey.getY());
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL update error", e);
            return false;
        }
    }

    @Override
    public boolean deleteFromTable(Pair<Integer,String> primaryKey) {
        final String query = "DELETE FROM " + TABLE_NAME + " WHERE id=? or name=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, primaryKey.getX());
            statement.setString(2, primaryKey.getY());
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL delete error", e);
            return false;
        }
    }

    @Override
    public List<Building> findResultSet(ResultSet result) {
        final List<Building> buildings = new ArrayList<>();
        try {
            while (result.next()) {
                final int id = result.getInt("id");
                final String name = result.getString("name");
                final String address = result.getString("address");
                final String city = result.getString("city");
                final Integer cap = result.getInt("cap");
                final String province = result.getString("province");
                final String region = result.getString("region");
                final String phone = result.getString("phone");
                final Building building = new Building(id, name, address, city, cap, province, region, phone);
                buildings.add(building);
            }
        } catch (final SQLException e) {
            GUIUtils.exceptionToast("SQL result set error", e);
        }
        return buildings;
    }

    @Override
    public List<Building> findAll() {
        List<Building> buildings = new ArrayList<>();
        try (final Statement statement = this.connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
            buildings = findResultSet(resultSet);
        } catch (final SQLException e) { }
        return buildings;
    }

    @Override
    public Optional<Building> findByPrimaryKey(Pair<Integer,String> primaryKey) {
        Optional<Building> building = Optional.empty();
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE id=? or name=?";
        try (final java.sql.PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setInt(1, primaryKey.getX());
            statement.setString(2, primaryKey.getY());
            final ResultSet resultSet = statement.executeQuery();
            final List<Building> buildings = findResultSet(resultSet);
            if (!buildings.isEmpty()) {
                building = Optional.of(buildings.get(0));
            }
        } catch (final SQLException e) { }
        return building;
    }
}
