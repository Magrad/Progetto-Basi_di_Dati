package project.gui;

import com.toedter.calendar.JDateChooser;
import project.db.tables.*;
import project.model.*;
import project.utils.GUIUtils;
import project.utils.Pair;
import project.utils.SQLUtils;
import project.utils.Utils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class is used to display the employee view.
 * It is used by the Medic to:
 * - view or add patients medical records
 * - view or manage their weekly agenda
 * - view and manage appointments
 * - view and update their account information
 */
public class MedicView extends JFrame implements FrameLauncher {

    // Enum used to switch between the different views and keep track of the current one.
    private enum ViewMode {
        VIEW_HOME,
        VIEW_PATIENTS,
        VIEW_PATIENT_MEDICAL_RECORD,
        INSERT_TO_PATIENT,
        VIEW_AGENDA,
        VIEW_APPOINTMENTS,
        VIEW_INFO
    }

    private final String MODIFY = "Modify";
    private final String ADD = "Add";

    private static Connection connection;
    private static Employee employee;
    private static String query;
    private static String conditions = "";
    private static Integer selectedRow = -1;
    private static ViewMode view = ViewMode.VIEW_HOME;
    private static Object[] lateBinding = new Object[]{};
    private static final List<Pair<String,String>> orderBy = new ArrayList<>();
    private static Integer[] tableSettings = new Integer[]{};
    private boolean initialize = true;

    private static JFrame thisFrame;
    private static JFrame callerFrame;
    private JPanel panelMain;
    private JTabbedPane selectionPane;
    private JPanel userHome;
    private JButton patientsBtn;
    private JButton agendaBtn;
    private JButton appointmentsBtn;
    private JButton accountBtn;
    private JButton logoutBtn;
    private JPanel accountSettings;
    private JTextField txtField1;
    private JTextField txtField2;
    private JTextField txtField3;
    private JPanel jdate;
    private JDateChooser dateChooser;
    private JRadioButton maleRB;
    private JRadioButton femaleRB;
    private JLabel roleLbl;
    private JComboBox comboBox;
    private JTextField txtField4;
    private JTextField txtField5;
    private JPasswordField passwordField;
    private JPasswordField verifyPasswordField;
    private JButton updateBtn;
    private JButton backBtn;
    private JTabbedPane userTable;
    private JPanel userInfo;
    private JTextField CFTxt;
    private JTextField nameTxt;
    private JTextField surnameTxt;
    private JTextField birthdayTxt;
    private JTextField emailTxt;
    private JPanel table;
    private JLabel tableLbl;
    private JTable sqlTable;
    private JButton selectBtn;
    private JButton clearBtn;
    private JPanel viewData;
    private JTextArea txtArea1;
    private JTextArea txtArea2;
    private JTextArea txtArea3;
    private JButton documentBtn;
    private JTextField documentTxt2;
    private JTextField documentTxt1;
    private JLabel employeeName;
    private JPanel dataFields;
    private JComboBox dayCB;
    private JTextField dataField1;
    private JTextField dataField2;
    private JTextField dataField3;
    private JTextField dataField4;
    private JTextField dataField5;
    private JTextArea dataArea1;
    private JTextArea dataArea2;
    private JTextArea dataArea3;
    private JButton switchViewBtn;
    private JButton dataBackBtn;
    private JButton confirmBtn;
    private JComboBox buildingCB;
    private JComboBox drugsCB;
    private JTextArea dataArea4;

    public MedicView() {
        if (initialize) {
            initializeComboBoxes();
            initialize = false;
        }

        employeeName.setText(getEmployeeNameAndRole());
        initializeEmployeeData();

        // Adds a date chooser to the date panel, in order for the employee
        // to select a date.
        dateChooser = new JDateChooser();
        jdate.add(dateChooser);

        GUIUtils.hideTabsInTabbedPane(selectionPane);
        GUIUtils.hideTabsInTabbedPane(userTable);

        // Add a listener to the table, in order to keep track of the selected row.
        sqlTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Get the column name of the column clicked.
                String col = sqlTable.getModel().getColumnName(sqlTable.columnAtPoint(e.getPoint())).toLowerCase();
                // Checks if it is the first time the users clicks on a column,
                // if so it will sort ascending, if the user clicks on the same
                // column again it will sort descending, if the user clicks on
                // a different column it will sort ascending.
                if (orderBy.isEmpty()) {
                    orderBy.add(new Pair<>(col, " ASC"));
                } else if (orderBy.get(0).getX().equals(col)) {
                    if (orderBy.get(0).getY().equals(" ASC")) {
                        orderBy.removeAll(orderBy);
                        orderBy.add(new Pair<>(col, " DESC"));
                    } else {
                        orderBy.removeAll(orderBy);
                        orderBy.add(new Pair<>(col, " ASC"));
                    }
                } else {
                    orderBy.removeAll(orderBy);
                    orderBy.add(new Pair<>(col, " ASC"));
                }

                // Reload the table with the new order by clause.
                GUIUtils.showQueryInTable(connection, sqlTable, query + conditions, lateBinding, orderBy, tableSettings);
            }
        });
        // Add a listener to the `sqlTable` table, in order to get the selected row.
        ListSelectionModel selectionModel = sqlTable.getSelectionModel();
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(!selectionModel.isSelectionEmpty()) {
                    selectedRow = selectionModel.getMinSelectionIndex();
                }
            }
        });
        // Add a listener to the `dataField1`, `dataField2` and
        // `dataField3` text fields, in order to update the table
        // in real time when the user types something.
        dataField1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (view.equals(ViewMode.VIEW_PATIENTS)) {
                    loadTableViewByIndex(view);
                }
            }
        });
        dataField2.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (view.equals(ViewMode.VIEW_PATIENTS)) {
                    loadTableViewByIndex(view);
                }
            }
        });
        dataField3.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (view.equals(ViewMode.VIEW_PATIENTS)) {
                    loadTableViewByIndex(view);
                }
            }
        });

        // Add a listener to the `patientsBtn` button, in order to
        // load the `patients` view, with the corresponding table.
        patientsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                orderBy.removeAll(orderBy);
                GUIUtils.clearAll(selectionPane);
                view = ViewMode.VIEW_PATIENTS;
                createUIComponents(view);
            }
        });
        // Add a listener to the `agendaBtn` button, in order to
        // load the `agenda` view, with the corresponding table.
        agendaBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                orderBy.removeAll(orderBy);
                GUIUtils.clearAll(selectionPane);
                view = ViewMode.VIEW_AGENDA;
                createUIComponents(view);
            }
        });
        // Add a listener to the `appointmentsBtn` button, in order to
        // load the `appointments` view, with the corresponding table.
        appointmentsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                orderBy.removeAll(orderBy);
                GUIUtils.clearAll(selectionPane);
                view = ViewMode.VIEW_APPOINTMENTS;
                createUIComponents(view);
            }
        });
        // Add a listener to the `accountBtn` button, in order to
        // load the `account` view, containing the employee's
        // personal information.
        accountBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                orderBy.removeAll(orderBy);
                GUIUtils.clearAll(selectionPane);
                initializeEmployeeData();
                view = ViewMode.VIEW_INFO;
                createUIComponents(view);
            }
        });
        // Add a listener to the `confirmBtn` button, in order to
        // add a new medical record to the database or to create
        // a new agenda for a specific week day.
        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (view.equals(ViewMode.INSERT_TO_PATIENT) && createNewMedicalRecord()) {
                    GUIUtils.clearAll(dataFields);
                    view = ViewMode.VIEW_PATIENTS;
                    createUIComponents(view);
                }
                if (view.equals(ViewMode.VIEW_AGENDA) && createAgendaAndRelativesTimeSlots()) {
                    GUIUtils.clearAll(dataFields);
                    view = ViewMode.VIEW_AGENDA;
                    createUIComponents(view);
                }
            }
        });
        // Add a listener to the `updateBtn` button, in order to
        // update an appointment's note or the employee's personal
        // information.
        updateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // If the user is in the `appointments` view, then
                // update the appointment's note.
                if (view.equals(ViewMode.VIEW_APPOINTMENTS) && insertFeedbackInSelectedBooking()) {
                    GUIUtils.clearAll(accountSettings);
                    loadTableViewByIndex(view);
                }
                if (view.equals(ViewMode.VIEW_INFO) && updateUserInfo()) {
                    initializeEmployeeData();
                    GUIUtils.clearAll(accountSettings);
                }
            }
        });
        // Add a listener to the `backBtn` button, in order to
        // go back to the home view.
        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                orderBy.removeAll(orderBy);
                GUIUtils.clearAll(selectionPane);
                confirmBtn.setVisible(true);
                initializeEmployeeData();
                selectionPane.setSelectedIndex(0);
                userTable.setSelectedIndex(0);
                view = ViewMode.VIEW_HOME;
            }
        });
        // Add a listener to the `dataBackBtn` button, in order to
        // go back to the home view.
        dataBackBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                orderBy.removeAll(orderBy);
                GUIUtils.clearAll(selectionPane);
                confirmBtn.setVisible(true);
                initializeEmployeeData();
                selectionPane.setSelectedIndex(0);
                userTable.setSelectedIndex(0);
                view = ViewMode.VIEW_HOME;
            }
        });
        // Add a listener to the `drugsCB` combo box, in order to
        // add a new drug to the prescription upon selection.
        drugsCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (drugsCB.getSelectedItem() != null) {
                    if (dataArea2.getText().contains(drugsCB.getSelectedItem().toString())) {
                        // Adds one more of the already selected drug to the prescription.
                        String[] lines = dataArea2.getText().split("\n");
                        for (String row : lines) {
                            if (row.contains(drugsCB.getSelectedItem().toString())) {
                                int quantity = Utils.parseInt(row.substring(row.indexOf("x") + 1));
                                dataArea2.setText(dataArea2.getText().replace(row, drugsCB.getSelectedItem() + " x" + (quantity + 1)));
                            }
                        }
                        return;
                    }

                    dataArea2.setText(dataArea2.getText() + drugsCB.getSelectedItem() + " x1\n");
                }
            }
        });
        // Add a listener to the `selectBtn` button, in order to
        // select a row from the table.
        selectBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedRow == -1) {
                    GUIUtils.exceptionToast("Please select a row", null);
                    return;
                }

                if (view.equals(ViewMode.VIEW_PATIENTS)) {
                    if (viewOrCreateMedicalRecords() == 1) {
                        createUIComponents(view);
                        dataArea1.setText("Description:\n\nDiagnosis:\n\n");
                    }
                } else if (view.equals(ViewMode.VIEW_PATIENT_MEDICAL_RECORD) && viewMedicalRecord()) {
                    dataArea1.setEditable(false);
                    dataArea2.setEditable(false);
                    dataArea3.setEditable(false);
                    createUIComponents(view);
                }
                if (view.equals(ViewMode.VIEW_AGENDA) && modifyAgendaSelected()) {
                    confirmBtn.setText(MODIFY);
                }
                if (view.equals(ViewMode.VIEW_APPOINTMENTS)) {
                    // If the user is in the `appointments` view, then
                    // let him/her choose whether to confirm or reject
                    // the selected booking.
                    int reply = GUIUtils.multipleChoiceToast("What do you want to do?", new String[]{"Booking confirmation", "Booking rejection"});

                    // If the user has chosen to close the dialog, then
                    // don't go any further.
                    if (reply == -1) {
                        return;
                    }

                    // If the employee has chosen to confirm the booking,
                    // then the `feedback` field will contain the
                    // information about the booking confirmation
                    // (i.e. the booking confirmation, the date and
                    // the time of the appointment and the ambulatory
                    // where the appointment will take place based on
                    // the building and ward in which the employee is
                    // working at that day).
                    BookingsTable bookingsTable = new BookingsTable(connection);
                    query = BookingsTable.getBookingsByEmployeeCFQuery;
                    query = SQLUtils.orderQueryByTableSortingOrder(query, orderBy, "");
                    Optional<Booking> bookingOpt = bookingsTable.getBookingAtRowWithStatementWithCF(query, selectedRow, employee.getCF());

                    if (bookingOpt.isEmpty()) {
                        GUIUtils.exceptionToast("No booking found", null);
                        return;
                    }
                    Booking booking = bookingOpt.get();

                    TimeSlotsTable timeSlotsTable = new TimeSlotsTable(connection);
                    Optional<TimeSlot> timeSlotOpt = timeSlotsTable.findByPrimaryKey(
                            new Pair<>(booking.getTimeslot_id(),
                                    new Object[]{new Date(), "00:00", ""})
                    );

                    if (timeSlotOpt.isEmpty()) {
                        GUIUtils.exceptionToast("No time slot found", null);
                        return;
                    }
                    TimeSlot timeSlot = timeSlotOpt.get();

                    AgendasTable agendasTable = new AgendasTable(connection);
                    Optional<Agenda> agendaOpt = agendasTable.findByPrimaryKey(
                            new Pair<>(timeSlot.getAgenda_day(),
                                    timeSlot.getAgenda_employee_cf())
                    );

                    if (agendaOpt.isEmpty()) {
                        GUIUtils.exceptionToast("No agenda found", null);
                        return;
                    }
                    Agenda agenda = agendaOpt.get();

                    AmbulatoryTable ambulatoryTable = new AmbulatoryTable(connection);
                    Optional<Ambulatory> ambulatoryOpt = ambulatoryTable.findByPrimaryKey(
                            new Pair<>(agenda.getAmbulatory_id(),
                                    new Pair<>(-1, -1))
                    );

                    if (ambulatoryOpt.isEmpty()) {
                        GUIUtils.exceptionToast("No ambulatory found", null);
                        return;
                    }
                    Ambulatory ambulatory = ambulatoryOpt.get();

                    WardsTable wardsTable = new WardsTable(connection);
                    Optional<Ward> wardOpt = wardsTable.findByPrimaryKey(
                            new Pair<>(ambulatory.getWard_id(),
                                    new Pair<>(-1, ""))
                    );

                    if (wardOpt.isEmpty()) {
                        GUIUtils.exceptionToast("No ward found", null);
                        return;
                    }
                    Ward ward = wardOpt.get();

                    BuildingsTable buildingsTable = new BuildingsTable(connection);
                    Optional<Building> buildingOpt = buildingsTable.findByPrimaryKey(
                            new Pair<>(ward.getBuilding_id(), "")
                    );

                    if (buildingOpt.isEmpty()) {
                        GUIUtils.exceptionToast("No building found", null);
                        return;
                    }
                    Building building = buildingOpt.get();

                    dateChooser.setDate(booking.getDay());

                    if (reply == 0) {
                        dataArea4.setText("Your booking has been confirmed. See you on " + booking.getDay() + " at " + timeSlot.getSlot() + "\n" +
                                "Ambulatory: " + ambulatory.getRoom_number() + "\nWard: " + ward.getType() +
                                " (sector: " + ward.getSector() + ")\nBuilding: " + building.getName());
                    } else {
                        // If the employee has chosen to reject the booking,
                        // then the `feedback` field will contain the
                        // information about the booking rejection.
                        dataArea4.setText("Your booking has been rejected. Please contact us for more information.");


                        timeSlotsTable.updateTable(
                                new TimeSlot(
                                        timeSlot.getId(),
                                        timeSlot.getDay(),
                                        timeSlot.getAgenda_day(),
                                        timeSlot.getAgenda_employee_cf(),
                                        timeSlot.getSlot(),
                                        true),
                                new Pair<>(timeSlot.getId(), new Object[]{new Date(), "00:00", ""})
                        );
                    }
                }
            }
        });
        clearBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedRow = -1;
                confirmBtn.setText(ADD);
                orderBy.removeAll(orderBy);
                GUIUtils.clearAll(selectionPane);
                sqlTable.clearSelection();

                switch (view) {
                    case VIEW_PATIENTS:
                    case VIEW_PATIENT_MEDICAL_RECORD:
                    case INSERT_TO_PATIENT:
                        view = ViewMode.VIEW_PATIENTS;
                        createUIComponents(view);
                        confirmBtn.setVisible(false);
                        view = ViewMode.VIEW_PATIENTS;
                        break;
                    case VIEW_AGENDA:
                        createUIComponents(view);
                        break;
                    case VIEW_APPOINTMENTS:
                        createUIComponents(view);
                        break;
                }
            }
        });
        switchViewBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUIUtils.clearAll(thisFrame);
                AdminView adminView = new AdminView(connection, employee);
                adminView.Launcher("Medic View", callerFrame);
                thisFrame.dispose();
            }
        });
        logoutBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                thisFrame.dispose();
                callerFrame.setVisible(true);
            }
        });
    }

    /**
     * Constructor for the MedicView class.
     * @param provider the connection provider.
     * @param employee the employee that is using the view.
     */
    public  MedicView(Connection provider, Employee employee) {
        this.connection = provider;
        this.employee = employee;
    }

    @Override
    public void Launcher(String name, JFrame caller) {
        callerFrame = caller;
        thisFrame = new JFrame(name);
        thisFrame.setContentPane(new MedicView().panelMain);
        thisFrame.pack();
        thisFrame.setLocationRelativeTo(null);
        thisFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        thisFrame.setResizable(false);
        thisFrame.setVisible(true);
    }

    /**
     * Method that sets the user information in the top left corner of the
     * frame.
     * @return the string to be displayed. Combines the employee's name and
     * role.
     */
    private String getEmployeeNameAndRole() {
        if (employee.getPermissions() == 1) {
            switchViewBtn.setVisible(true);
        }

        PositionsTable positionsTable = new PositionsTable(connection);
        Optional<Position> position = positionsTable.findByPrimaryKey(new Pair<>(employee.getRole_id(), new Pair<>("","")));
        String role = position.isEmpty() ? "Employee" : position.get().getRole();
        String specialization = position.isEmpty() || position.get().getSpecialization().isEmpty() ? ""
                : " (" + position.get().getSpecialization() + ")";

        return "Logged in: " + employee.getName() + " - " + role + specialization;
    }

    /**
     * Method that initializes all the data of the employee.
     */
    private void initializeEmployeeData() {
        CFTxt.setText(employee.getCF());
        nameTxt.setText(employee.getName());
        surnameTxt.setText(employee.getSurname());
        birthdayTxt.setText(Utils.dateToString(employee.getBirthday()));
        emailTxt.setText(employee.getEmail());
    }

    /**
     * Method that initializes all the combo boxes of the frame.
     */
    private void initializeComboBoxes() {
        String[] days = new String[]{"Monday","Tuesday","Wednesday","Thursday","Friday"};
        for (String day : days) {
            dayCB.addItem(day);
        }

        BuildingsTable buildingsTable = new BuildingsTable(connection);
        List<Building> buildings = buildingsTable.findAll();
        for (Building building : buildings) {
            buildingCB.addItem(building.getName());
        }

        TreatmentsTable treatmentsTable = new TreatmentsTable(connection);
        List<Treatment> treatments = treatmentsTable.findAll();
        for (Treatment treatment : treatments) {
            drugsCB.addItem(treatment.getName());
        }
    }

    /**
     * Method that initializes all the components of the frame, based
     * on the view that is currently selected.
     * @param view the view that is currently selected.
     */
    private void createUIComponents(ViewMode view) {
        selectionPane.setSelectedIndex(1);
        userTable.setSelectedIndex(1);

        if (view.equals(ViewMode.VIEW_PATIENTS)) {
            GUIUtils.hideShowWithSequence(dataFields, "0");
            GUIUtils.hideShowWithSequence(dataFields, "0000111111");
            GUIUtils.modifyLabelText(dataFields,
                    new String[] {"Patient's CF",
                            "Patient's Name",
                            "Patient's Surname"
                    });
            confirmBtn.setVisible(false);
            loadTableViewByIndex(view);
        }
        if (view.equals(ViewMode.VIEW_AGENDA)) {
            GUIUtils.hideShowWithSequence(dataFields, "0");
            GUIUtils.hideShowWithSequence(dataFields, "11111111111111");
            GUIUtils.modifyLabelText(dataFields,
                    new String[] {"Hospital",
                            "Day",
                            "Starts to work (hh:mm)",
                            "Starts break (hh:mm)",
                            "Ends break (hh:mm)",
                            "Ends to work (hh:mm)",
                            "Appointments duration (hh:mm)"
                    });
            loadTableViewByIndex(view);
        }
        if (view.equals(ViewMode.VIEW_APPOINTMENTS)) {
            selectionPane.setSelectedIndex(2);
            GUIUtils.hideShowWithSequence(accountSettings, "0");
            GUIUtils.hideShowWithSequence(accountSettings, "000000111000000000000011");
            GUIUtils.modifyLabelText(accountSettings,
                    new String[] {"Day",
                            "Feedback"
                    });
            loadTableViewByIndex(view);
        }
        if (view.equals(ViewMode.INSERT_TO_PATIENT)) {
            GUIUtils.hideShowWithSequence(dataFields, "0");
            GUIUtils.hideShowWithSequence(dataFields, "0000111111000011111111");
            GUIUtils.modifyLabelText(dataFields,
                    new String[] {"Patient's CF",
                            "Patient's Name",
                            "Patient's Surname",
                            "Description and Diagnosis",
                            "Drugs",
                            "Treatments",
                            "Follow up visits"
                    });
            confirmBtn.setVisible(true);
        }
        if (view.equals(ViewMode.VIEW_PATIENT_MEDICAL_RECORD)) {
            GUIUtils.hideShowWithSequence(dataFields, "0");
            GUIUtils.hideShowWithSequence(dataFields, "0000111111000011001111");
            GUIUtils.modifyLabelText(dataFields,
                    new String[] {"Patient's CF",
                            "Patient's Name",
                            "Patient's Surname",
                            "Description and Diagnosis",
                            "Treatments",
                            "Follow up visits"
                    });
        }
        if (view.equals(ViewMode.VIEW_INFO)) {
            selectionPane.setSelectedIndex(2);
            GUIUtils.hideShowWithSequence(accountSettings, "0");
            GUIUtils.hideShowWithSequence(accountSettings, "00000000000011111111");
            GUIUtils.modifyLabelText(accountSettings,
                    new String[] {"Phone",
                            "Email",
                            "Password",
                            "Verify Password"
                    });
            userTable.setSelectedIndex(0);
            txtField4.setText(employee.getPhone().orElse(""));
            txtField5.setText(employee.getEmail());
        }
    }

    /**
     * Method that load the correct view of the `sqlTable` table, depending on
     * the `view` parameter.
     * @param view the current view.
     */
    private void loadTableViewByIndex(ViewMode view) {
        if (view.equals(ViewMode.VIEW_PATIENTS)) {
            String cf = dataField1.getText();
            String name = dataField2.getText();
            String surname = dataField3.getText();
            int fieldsFilled = Utils.booleanToInt(!cf.isEmpty()) +
                    Utils.booleanToInt(!name.isEmpty()) +
                    Utils.booleanToInt(!surname.isEmpty());

            query = UsersTable.getUsersQuery;
            conditions = "";

            if (!cf.isEmpty()) {
                conditions += " WHERE cf LIKE '%" + cf + "%'";
            }
            if (!name.isEmpty()) {
                conditions += fieldsFilled < 2 ? " WHERE" : " AND";
                conditions += " name LIKE '%" + name + "%'";
            }
            if (!surname.isEmpty()) {
                conditions += fieldsFilled < 2 ? " WHERE" : " AND";
                conditions += " surname LIKE '%" + surname + "%'";
            }
            lateBinding = new Object[]{};
            tableSettings = new Integer[]{};
            GUIUtils.showQueryInTable(connection, sqlTable, query + conditions, lateBinding, orderBy, tableSettings);
        }
        if (view.equals(ViewMode.VIEW_AGENDA)) {
            query = AgendasTable.getAgendaByEmployeeCFQuery;
            conditions = "";
            lateBinding = new String[]{employee.getCF()};
            tableSettings = new Integer[]{};
            GUIUtils.showQueryInTable(connection, sqlTable, query + conditions, lateBinding, orderBy, tableSettings);
        }
        if (view.equals(ViewMode.VIEW_APPOINTMENTS)) {
            Date date = dateChooser.getDate();

            query = BookingsTable.getBookingByEmployeeCFQuery;
            conditions = "";

            if (date != null) {
                conditions += " AND date = '" + Utils.dateToString(date) + "'";
            }
            lateBinding = new String[]{employee.getCF()};
            tableSettings = new Integer[]{};
            GUIUtils.showQueryInTable(connection, sqlTable, query + conditions, lateBinding, orderBy, tableSettings);
        }
    }

    /**
     * Method used to load all the medical records of a patient
     * in the table.
     * @return
     * - -1 if no user is found or the multiple choice dialog is closed
     * - 0 if the user wants to view the medical records
     * - 1 if the user wants to create a new medical record
     */
    private Integer viewOrCreateMedicalRecords() {
        UsersTable usersTable = new UsersTable(connection);
        query = "SELECT * FROM " + UsersTable.TABLE_NAME;
        query = SQLUtils.orderQueryByTableSortingOrder(query + conditions, orderBy, "");
        Optional<User> opt = usersTable.getUserAtRowWithStatement(query, selectedRow);

        if (opt.isEmpty()) {
            GUIUtils.exceptionToast("No user found", null);
            return -1;
        }

        User user = opt.get();

        int reply = GUIUtils.multipleChoiceToast("Patient: " + user.getName() + " " + user.getSurname(),
                new String[]{"View medical reports", "Insert new medical report"});

        if (reply == -1) {
            return reply;
        }

        query = MedicalReportsTable.getUserMedicalRecordsQuery;
        lateBinding = new String[]{user.getCF()};
        GUIUtils.showQueryInTable(connection, sqlTable, query, lateBinding, orderBy, tableSettings);

        dataField1.setText(user.getCF());
        dataField2.setText(user.getName());
        dataField3.setText(user.getSurname());
        dataField1.setEditable(false);
        dataField2.setEditable(false);
        dataField3.setEditable(false);

        if (reply == 0) {
            // Show medical records.
            view = ViewMode.VIEW_PATIENT_MEDICAL_RECORD;
        } else if (reply == 1) {
            // Insert new medical record.
            view = ViewMode.INSERT_TO_PATIENT;
        }
        return reply;
    }

    /**
     * Loads the medical record of the selected patient and
     * shows it in the current view.
     * @return true if the operation was successful, false otherwise.
     */
    private boolean viewMedicalRecord() {
        MedicalReportsTable medicalReportsTable = new MedicalReportsTable(connection);
        String dataQuery = "SELECT * FROM " + MedicalReportsTable.TABLE_NAME;
        dataQuery = SQLUtils.orderQueryByTableSortingOrder(dataQuery, orderBy, "");
        Optional<MedicalReport> medicalReportOpt = medicalReportsTable.getMedicalReportAtRowWithStatement(dataQuery, selectedRow);

        if (medicalReportOpt.isEmpty()) {
            GUIUtils.exceptionToast("No medical report found", null);
            return false;
        }

        MedicalReport medicalReport = medicalReportOpt.get();

        // Fills `dataArea1` with the description and diagnosis of the medical report.
        dataArea1.setText("Description:\n" + medicalReport.getDescription() + "\n\nDiagnosis:\n" + medicalReport.getDiagnosis());

        PrescriptionsTable prescriptionsTable = new PrescriptionsTable(connection);
        List<Prescription> prescriptions = prescriptionsTable.findPrescriptionByMedicalReportID(medicalReport.getId());

        // If a prescription related to the current medical record is
        // present in the database, it shows all the treatments prescribed
        // and the possible follow up needed for more checkups.
        if (!prescriptions.isEmpty()) {
            dataArea2.setText("Treatments:\n");
            for (Prescription prescription : prescriptions) {
                TreatmentsTable treatmentsTable = new TreatmentsTable(connection);

                if (prescription.getTreatment_id().isPresent()) {
                    Optional<Treatment> treatmentOpt = treatmentsTable.findByPrimaryKey(
                            new Pair<>(prescription.getTreatment_id().get(), "")
                    );

                    if (!treatmentOpt.isEmpty()) {
                        Treatment treatment = treatmentOpt.get();

                        dataArea2.setText(dataArea2.getText() + treatment.getName() + " x" + prescription.getQuantity().orElse(1) + "\n");
                    }
                }
            }
            dataArea3.setText("Follow up:\n" + prescriptions.get(0).getFollowup().orElse(""));
        }
        return true;
    }

    /**
     * Updates the fields of the current view according to the
     * selected agenda.
     * @return true if the operation was successful, false otherwise.
     */
    private boolean modifyAgendaSelected() {
        AgendasTable agendasTable = new AgendasTable(connection);

        query = AgendasTable.getAgendaByEmployeeCFQuery;
        query = SQLUtils.orderQueryByTableSortingOrder(query, orderBy, "");
        Optional<Agenda> agendaOpt = agendasTable.getAgendaAtRowWithStatement(query, selectedRow, employee.getCF());

        if (agendaOpt.isEmpty()) {
            GUIUtils.exceptionToast("No agenda found", null);
            return false;
        }

        Agenda agenda = agendaOpt.get();

        if (!GUIUtils.yesNoToast("Are you sure you want to modify this agenda?")) {
            return false;
        }

        AmbulatoryTable ambulatoryTable = new AmbulatoryTable(connection);
        Optional<Ambulatory> ambulatoryOpt = ambulatoryTable.findByPrimaryKey(
                new Pair<>(agenda.getAmbulatory_id(),
                        new Pair<>(-1, -1))
        );

        if (ambulatoryOpt.isEmpty()) {
            GUIUtils.exceptionToast("No ambulatory found", null);
            return false;
        }

        Ambulatory ambulatory = ambulatoryOpt.get();
        WardsTable wardsTable = new WardsTable(connection);
        Optional<Ward> wardOpt = wardsTable.findByPrimaryKey(
                new Pair<>(ambulatory.getWard_id(),
                        new Pair<>(-1, ""))
        );

        if (wardOpt.isEmpty()) {
            GUIUtils.exceptionToast("No ward found", null);
            return false;
        }

        Ward ward = wardOpt.get();
        BuildingsTable buildingsTable = new BuildingsTable(connection);
        Optional<Building> buildingOpt = buildingsTable.findByPrimaryKey(
                new Pair<>(ward.getBuilding_id(), "")
        );

        if (buildingOpt.isEmpty()) {
            GUIUtils.exceptionToast("No building found", null);
            return false;
        }

        Building building = buildingOpt.get();

        buildingCB.setSelectedItem(building.getName());
        dayCB.setSelectedItem(agenda.getDay());
        dataField1.setText(agenda.getDaystart());
        dataField2.setText(agenda.getBreakstart());
        dataField3.setText(agenda.getBreakend());
        dataField4.setText(agenda.getDayend());
        dataField5.setText(agenda.getDeltatime());

        return true;
    }

    /**
     * Creates a new medical record from the data inserted in the form.
     * @return true if the medical record was created successfully, false otherwise.
     */
    private boolean createNewMedicalRecord() {
        final String patient_cf = dataField1.getText();
        final String employee_cf = employee.getCF();
        final Date date = Utils.localDatetoDate(LocalDate.now());
        final int descriptionLength = "Description:\n".length();
        final int diagnosisLength = "Diagnosis:\n".length();
        final int idxDescription = dataArea1.getText().indexOf("Description:\n");
        final int idxDiagnosis = dataArea1.getText().indexOf("Diagnosis:\n");
        final String description = dataArea1.getText().substring(idxDescription+descriptionLength, idxDiagnosis);
        final String diagnosis = dataArea1.getText().substring(idxDiagnosis+diagnosisLength, dataArea1.getText().length());

        // Check if the integrity of the data automatically inserted
        // in the text area has been compromised.
        if (idxDescription == -1 || idxDiagnosis == -1) {
            GUIUtils.exceptionToast("Please restore `Description` and `Diagnosis` fields as they were.\n" +
                    "Start typing from the beginning of the new line for each.", null);
            return false;
        }

        // Checks if all the required fields are filled.
        if (GUIUtils.emptyFieldToast(patient_cf, "Patient CF") ||
                GUIUtils.emptyFieldToast(employee_cf, "Employee CF") ||
                GUIUtils.emptyFieldToast(description.trim(), "Description") ||
                GUIUtils.emptyFieldToast(diagnosis.trim(), "Diagnosis")) {
            return false;
        }

        MedicalReportsTable medicalReportsTable = new MedicalReportsTable(connection);
        if (medicalReportsTable.insertToTable(
                new MedicalReport(
                        0,
                        patient_cf,
                        employee_cf,
                        date,
                        description,
                        diagnosis
                )
        ));

        final String treatments = dataArea2.getText();
        List<String> treatmentsList = new ArrayList<>();
        final String followUp = dataArea3.getText();

        // Upon selection of a drug from the `drugsCB` combo box,
        // the drug is added to the `dataArea2` text area followed
        // by the quantity of the drug.
        if (!treatments.isEmpty()) {
            treatmentsList = Arrays.asList(treatments.split("\n"));
            Optional<Treatment> treatmentOpt = Optional.empty();
            int quantity = 0;

            for (String treatment : treatmentsList) {
                String quantityString = " x";
                int quantityLenght = quantityString.length();
                quantity = Utils.parseInt(treatment.substring(treatment.indexOf(quantityString) + quantityLenght));
                treatment = treatment.substring(0, treatment.indexOf(quantityString));

                TreatmentsTable treatmentsTable = new TreatmentsTable(connection);
                treatmentOpt = treatmentsTable.findByPrimaryKey(
                        new Pair<>(0, treatment)
                );

                if (treatmentOpt.isEmpty() || quantity <= 0) {
                    GUIUtils.exceptionToast("There is an error with the treatments please clear all data and retry.", null);
                    return false;
                }

                PrescriptionsTable prescriptionsTable = new PrescriptionsTable(connection);
                if (!prescriptionsTable.insertToTable(
                        new Prescription(
                                0,
                                medicalReportsTable.findUserMaxID(patient_cf)-1,
                                patient_cf,
                                treatmentOpt.isEmpty() ? Optional.empty() : Optional.of(treatmentOpt.get().getId()),
                                treatmentOpt.isEmpty() ? Optional.empty() : Optional.of(quantity),
                                followUp.isEmpty() ? Optional.empty() : Optional.of(followUp)
                        ))) {
                    medicalReportsTable.deleteFromTable(
                            new Pair<>(medicalReportsTable.findUserMaxID(patient_cf)-1,
                                    patient_cf));
                }
            }
        }

        // If both `treatments` and `followUp` fields are empty,
        // then no prescription will be inserted in the database.
        if (treatments.isEmpty() && !followUp.isEmpty()) {
            PrescriptionsTable prescriptionsTable = new PrescriptionsTable(connection);
            if (!prescriptionsTable.insertToTable(
                    new Prescription(
                            0,
                            medicalReportsTable.findUserMaxID(patient_cf)-1,
                            patient_cf,
                            Optional.empty(),
                            Optional.empty(),
                            followUp.isEmpty() ? Optional.empty() : Optional.of(followUp)
                    )
            )) {
                GUIUtils.exceptionToast("Error while inserting prescription", null);
                return false;
            };
        }

        GUIUtils.exceptionToast("Medical report successfully inserted", null);
        return true;
    }

    /**
     * Once the employee has selected the building, the day and the
     * time slots for the agenda, this method initializes the agenda
     * in the database and creates the relative time slots according
     * to the selected week day.
     */
    private boolean createAgendaAndRelativesTimeSlots() {
        String day = dayCB.getSelectedItem() == null ? "" : dayCB.getSelectedItem().toString();
        String hospital = buildingCB.getSelectedItem() == null ? "" :buildingCB.getSelectedItem().toString();
        String sWork = dataField1.getText();
        String sBreak = dataField2.getText();
        String eBreak = dataField3.getText();
        String eWork = dataField4.getText();
        String duration = dataField5.getText();
        int appointmentDuration = 0;
        int step = 0;

        if (day.isEmpty() || hospital.isEmpty()) {
            GUIUtils.exceptionToast("Please select a day and a building", null);
            return false;
        }

        if (GUIUtils.emptyFieldToast(sWork, "Start of work") ||
                GUIUtils.emptyFieldToast(sBreak, "Start of break") ||
                GUIUtils.emptyFieldToast(eBreak, "End of break") ||
                GUIUtils.emptyFieldToast(eWork, "End of work") ||
                GUIUtils.emptyFieldToast(duration, "Visits duration")) {
            return false;
        }

        if(!sWork.isEmpty() && buildingCB.getSelectedItem() != null && !day.isEmpty()) {
            // Check if all the fields are filled with the correct format
            // and calculates the appointment duration.
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                Date sWorkDate = sdf.parse(sWork);
                step++;
                Date sBreakDate = sdf.parse(sBreak);
                step++;
                Date eBreakDate = sdf.parse(eBreak);
                step++;
                Date eWorkDate = sdf.parse(eWork);
                step++;
                Date durationDate = sdf.parse(duration);
                step++;
                sWork = sdf.format(sWorkDate);
                sBreak = sdf.format(sBreakDate);
                eBreak = sdf.format(eBreakDate);
                eWork = sdf.format(eWorkDate);
                duration = sdf.format(durationDate).equals("00:00") ? "01:00" : sdf.format(durationDate);
                appointmentDuration = TimeUnit.MILLISECONDS.toHours(durationDate.getTime()) > 0 ? (int) TimeUnit.MILLISECONDS.toHours(durationDate.getTime()) % 24 : 0;
                appointmentDuration += (int) (60 - (TimeUnit.MILLISECONDS.toMinutes(durationDate.getTime()) % 60) * -1);
            } catch (Exception e) {
                GUIUtils.exceptionToast("Invalid time format at field: " + (step + 1), null);
                GUIUtils.clearFieldByIndex(dataFields, step);
                return false;
            }

            // Select building at row index.
            BuildingsTable buildingsTable = new BuildingsTable(connection);
            Optional<Building> buildingOpt = buildingsTable.findByPrimaryKey(new Pair<>(-1, hospital));

            if (buildingOpt.isEmpty()) {
                GUIUtils.exceptionToast("Building not found", null);
                return false;
            }
            Building buildingObj = buildingOpt.get();

            PositionsTable positionsTable = new PositionsTable(connection);
            WardsTable wardsTable = new WardsTable(connection);

            // Select ward type based on employee specialization.
            Optional<Position> positionOpt = positionsTable.findByPrimaryKey(
                    new Pair<>(employee.getRole_id(),
                            new Pair<>("","")
                    ));

            if (positionOpt.isEmpty()) {
                GUIUtils.exceptionToast("Position not found", null);
                return false;
            }
            Position positionObj = positionOpt.get();
            Optional<Ward> wardOpt = wardsTable.findByPrimaryKey(
                    new Pair<>(0,
                            new Pair<>(buildingObj.getId(),
                                    Character.toString((char) (positionObj.getId()-2) + 'A'))
                    ));

            if (wardOpt.isEmpty()) {
                GUIUtils.exceptionToast("Ward not found", null);
                return false;
            }
            Ward wardObj = wardOpt.get();

            // Select ambulatory number by first available.
            AmbulatoryTable ambulatoriesTable = new AmbulatoryTable(connection);
            Optional<Ambulatory> ambulatoryOpt = ambulatoriesTable.findFirstAmbulatoryAvailableByWardId(
                    wardObj.getId(),
                    day
            );

            if (ambulatoryOpt.isEmpty()) {
                GUIUtils.exceptionToast("No available ambulatories found, please try another hospital", null);
                return false;
            }

            Ambulatory ambulatoryObj = ambulatoryOpt.get();
            AgendasTable agendasTable = new AgendasTable(connection);
            TimeSlotsTable timeSlotsTable = new TimeSlotsTable(connection);

            // If the employee requires to modify an agenda,
            // all the time slots related to it need to be
            // deleted and then re-inserted with the new
            // values.
            if (confirmBtn.getText().equals(MODIFY)) {
                timeSlotsTable.deleteAllTimeSlotsOfEmployeeByDay(
                        employee.getCF(),
                        day
                );

                if (!agendasTable.updateTable(
                        new Agenda(
                                day,
                                employee.getCF(),
                                ambulatoryObj.getId(),
                                sWork,
                                sBreak,
                                eBreak,
                                eWork,
                                duration),
                        new Pair<>(day, employee.getCF())
                )) {
                    return false;
                };
            } else {
                if (!agendasTable.insertToTable(
                        new Agenda(
                                day,
                                employee.getCF(),
                                ambulatoryObj.getId(),
                                sWork,
                                sBreak,
                                eBreak,
                                eWork,
                                duration
                        )
                )) {
                    return false;
                };
            }

            // Create time slots for the selected day starting from
            // the current day until the end of the month.
            // (This approach is used to avoid the creation of to
            // many time slots in the database, but can be easily
            // changed to create all the time slots for the entire
            // year or as the requirements of the project suggest).
            LocalDate start = LocalDate.now().minusDays(LocalDate.now().getDayOfMonth() - 1);
            LocalDate end = start.plusMonths(1);
            List<LocalDate> days = Utils.getDatesBetween(LocalDate.now(), end);

            for (LocalDate date : days) {
                String weekDay = Utils.smartStringCapitalize(date.getDayOfWeek().toString().toLowerCase());
                if (weekDay.equals(day)) {
                    String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                    ArrayList<String> slots = Utils.getTimeSlots(
                            LocalTime.parse(sWork),
                            LocalTime.parse(eWork),
                            LocalTime.parse(sBreak),
                            LocalTime.parse(eBreak),
                            appointmentDuration
                    );

                    for (String slot : slots) {
                        if (!timeSlotsTable.insertToTable(
                                new TimeSlot(0,
                                        Utils.localDatetoDate(date),
                                        day,
                                        employee.getCF(),
                                        slot,
                                        true
                                )
                        )) {
                            return false;
                        };
                    }
                }
            }
            GUIUtils.exceptionToast("Agenda created successfully", null);
            return true;
        }
        return false;
    }

    /**
     * The employee can accept or reject an appointment.
     * On accepting, an automatic message will be shown
     * in a text area containing the appointment's details,
     * and the ambulatory in which the appointment will
     * take place.
     * On rejecting, an automatic message will be shown
     * in a text area containing the steps that the patient
     * should follow to know the reason of the rejection.
     * @return true if the operation is successful, false otherwise.
     */
    private boolean insertFeedbackInSelectedBooking() {
        String feedback = dataArea4.getText();

        if (feedback.isEmpty()) {
            GUIUtils.exceptionToast("Please insert a feedback or restore the " +
                    "automatic one", null);
            return false;
        }

        BookingsTable bookingsTable = new BookingsTable(connection);
        query = SQLUtils.orderQueryByTableSortingOrder(query, orderBy, "");
        Optional<Booking> bookingOpt = bookingsTable.getBookingAtRowWithStatementWithCF(query, selectedRow, employee.getCF());

        if (bookingOpt.isEmpty()) {
            GUIUtils.exceptionToast("Booking not found", null);
            return false;
        }
        Booking booking = bookingOpt.get();

        if (!bookingsTable.updateTable(
                new Booking(
                        booking.getId(),
                        booking.getDay(),
                        booking.getTimeslot_id(),
                        booking.getUser_cf(),
                        booking.getEmployee_cf(),
                        feedback),
                new Object[]{
                        booking.getDay(),
                        booking.getTimeslot_id(),
                        booking.getUser_cf(),
                        booking.getEmployee_cf()
                }
        )) {
            GUIUtils.exceptionToast("Can not modify the booking, please try again", null);
            return false;
        };
        GUIUtils.exceptionToast("Booking updated", null);

        return true;
    }

    /**
     * Once the employee has decided to update his/her
     * personal information, this method will be called
     * to update the database with the new values inserted
     * and check if they are valid.
     * @return true if the operation is successful, false otherwise.
     */
    private boolean updateUserInfo() {
        String phone = txtField4.getText();
        String email = txtField5.getText();
        String password = passwordField.getText();
        String verifyPassword = verifyPasswordField.getText();

        if (GUIUtils.emptyFieldToast(email, "Email") ||
                GUIUtils.emptyFieldToast(password, "Password") ||
                GUIUtils.emptyFieldToast(verifyPassword, "Verify Password")) {
            return false;
        }

        if(!Utils.emailValidator(email)) {
            GUIUtils.exceptionToast("Email field is empty.\n" +
                    "Email must contains characters: \'a-z,A-Z,0-9 and at least one special character.\'", null);
            GUIUtils.clearField(emailTxt);
            return false;
        }
        if (!Utils.passwordValidator(password)){
            GUIUtils.exceptionToast("Password field is empty.\n" +
                    "Password must contains characters: \'a-z,A-Z,0-9 and at least one special character.\'", null);
            GUIUtils.clearField(passwordField);
            GUIUtils.clearField(verifyPasswordField);
            return false;
        }

        EmployeesTable employeesTable = new EmployeesTable(connection);
        Optional<Employee> employeeOpt = employeesTable.findByPrimaryKey(employee.getCF());

        if (employeeOpt.isEmpty()) {
            GUIUtils.exceptionToast("Employee not found", null);
            return false;
        }

        Employee newEmployee = new Employee(
                employeeOpt.get().getCF(),
                employeeOpt.get().getName(),
                employeeOpt.get().getSurname(),
                employeeOpt.get().getBirthday(),
                employeeOpt.get().getGender(),
                employeeOpt.get().getRole_id(),
                phone.isEmpty() ? Optional.empty() : Optional.of(phone),
                email,
                Utils.passwordEncoder(password),
                employeeOpt.get().getPermissions()
        );
        if (!employeesTable.updateTable(
                newEmployee,
                employeeOpt.get().getCF()
        )) {
            GUIUtils.exceptionToast("Can not modify the employee, please try again", null);
            return false;
        };
        GUIUtils.exceptionToast("Employee updated", null);
        employee = newEmployee;

        return true;
    }
}
