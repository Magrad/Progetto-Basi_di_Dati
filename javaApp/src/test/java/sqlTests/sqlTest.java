package sqlTests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project.db.ConnectionProvider;
import project.db.tables.*;
import project.utils.Utils;

public class sqlTest {
    final static ConnectionProvider connectionProvider = new ConnectionProvider(Utils.USERNAME, Utils.PASSWORD, Utils.DBNAME);
    final static BuildingsTable buildingsTable = new BuildingsTable(connectionProvider.getMySQLConnection());
    final static WardsTable wardsTable = new WardsTable(connectionProvider.getMySQLConnection());
    final static AmbulatoryTable ambulatoryTable = new AmbulatoryTable(connectionProvider.getMySQLConnection());
    final static PositionsTable positionsTable = new PositionsTable(connectionProvider.getMySQLConnection());
    final static EmployeesTable employeesTable = new EmployeesTable(connectionProvider.getMySQLConnection());
    final static AgendasTable agendasTable = new AgendasTable(connectionProvider.getMySQLConnection());
    final static TimeSlotsTable timeSlotsTable = new TimeSlotsTable(connectionProvider.getMySQLConnection());
    final static UsersTable usersTable = new UsersTable(connectionProvider.getMySQLConnection());
    final static TreatmentsTable treatmentsTable = new TreatmentsTable(connectionProvider.getMySQLConnection());
    final static PrescriptionsTable prescriptionsTable = new PrescriptionsTable(connectionProvider.getMySQLConnection());
    final static MedicalReportsTable medicalReportsTable = new MedicalReportsTable(connectionProvider.getMySQLConnection());
    final static BookingsTable bookingsTable = new BookingsTable(connectionProvider.getMySQLConnection());

    @BeforeEach
    void setUp() throws Exception {
        bookingsTable.dropTable();
        timeSlotsTable.dropTable();
        agendasTable.dropTable();
        ambulatoryTable.dropTable();
        wardsTable.dropTable();
        buildingsTable.dropTable();
        positionsTable.dropTable();
        prescriptionsTable.dropTable();
        medicalReportsTable.dropTable();
        treatmentsTable.dropTable();
        usersTable.dropTable();
        employeesTable.dropTable();

        buildingsTable.createTable();
        wardsTable.createTable();
        ambulatoryTable.createTable();
        positionsTable.createTable();
        employeesTable.createTable();
        agendasTable.createTable();
        timeSlotsTable.createTable();
        usersTable.createTable();
        treatmentsTable.createTable();
        medicalReportsTable.createTable();
        bookingsTable.createTable();
        prescriptionsTable.createTable();
    }

    @AfterEach
    void tearDown() throws Exception {
        bookingsTable.dropTable();
        timeSlotsTable.dropTable();
        agendasTable.dropTable();
        ambulatoryTable.dropTable();
        wardsTable.dropTable();
        buildingsTable.dropTable();
        positionsTable.dropTable();
        prescriptionsTable.dropTable();
        medicalReportsTable.dropTable();
        treatmentsTable.dropTable();
        usersTable.dropTable();
        employeesTable.dropTable();
    }

    @Test
    void alreadyCreatedTest() {
        assertFalse(buildingsTable.createTable());
        assertFalse(wardsTable.createTable());
        assertFalse(ambulatoryTable.createTable());
        assertFalse(positionsTable.createTable());
        assertFalse(employeesTable.createTable());
        assertFalse(agendasTable.createTable());
        assertFalse(timeSlotsTable.createTable());
        assertFalse(usersTable.createTable());
        assertFalse(treatmentsTable.createTable());
        assertFalse(medicalReportsTable.createTable());
        assertFalse(bookingsTable.createTable());
        assertFalse(prescriptionsTable.createTable());
    }

    @Test
    void DropExistingTablesTest() {
        assertTrue(bookingsTable.dropTable());
        assertTrue(timeSlotsTable.dropTable());
        assertTrue(agendasTable.dropTable());
        assertTrue(ambulatoryTable.dropTable());
        assertTrue(wardsTable.dropTable());
        assertTrue(buildingsTable.dropTable());
        assertTrue(positionsTable.dropTable());
        assertTrue(prescriptionsTable.dropTable());
        assertTrue(medicalReportsTable.dropTable());
        assertTrue(treatmentsTable.dropTable());
        assertTrue(usersTable.dropTable());
        assertTrue(employeesTable.dropTable());
    }
}
