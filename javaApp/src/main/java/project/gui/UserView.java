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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.util.*;

/**
 * This class is used to display the user view.
 * It is used by the User to:
 * - view his/her medical reports
 * - view the doctors available in the hospital
 * - book an appointment with a doctor
 * - view his/her appointments
 * - update his/her account information
 */
public class UserView extends JFrame implements FrameLauncher {

    // Enum used to switch between the different views and keep track of the current one.
    private enum UserViewType {
        USER_HOME,
        USER_REPORTS,
        USER_CHECKING_REPORT,
        USER_DOCTORS,
        USER_BOOKING,
        USER_CHECKING_APPOINTMENT,
        USER_ACCOUNT
    }

    private static Connection connection;
    private static User user;
    private static String query;
    private static String conditions = "";
    private static UserViewType view = UserViewType.USER_HOME;
    private static Object[] lateBinding = new Object[]{};
    private static final List<Pair<String,String>> orderBy = new ArrayList<>();
    private static Integer[] tableSettings = new Integer[]{};
    private static Integer selectedRow = -1;

    private static JFrame thisFrame;
    private static JFrame callerFrame;
    private JPanel panelMain;
    private JButton tableBtn1;
    private JLabel manageLbl;
    private JTable sqlTable;
    private JButton tableBtn2;
    private JButton tableBtn3;
    private JTabbedPane selectionPane;
    private JPanel userHome;
    private JButton reportsBtn;
    private JButton doctorsBtn;
    private JButton appointmentsBtn;
    private JButton accountBtn;
    private JButton logoutBtn;
    private JPanel visualizeRequired;
    private JTextField txtField1;
    private JTextField txtField2;
    private JTextField txtField3;
    private JPanel jdate;
    private JDateChooser dateChooser;
    private JComboBox comboBox;
    private JRadioButton maleRB;
    private JRadioButton femaleRB;
    private JTextField txtField4;
    private JTextField txtField5;
    private JPasswordField password;
    private JPasswordField verifyPassword;
    private JButton confirmBtn;
    private JButton backBtn;
    private JLabel userName;
    private JTabbedPane userTable;
    private JPanel userInfo;
    private JPanel table;
    private JButton selectBtn;
    private JButton clearBtn;
    private JTextField nameTxt;
    private JTextField surnameTxt;
    private JTextField CFTxt;
    private JTextField emailTxt;
    private JTextField birthdayTxt;
    private JLabel tableLbl;
    private JLabel timeSlotsLbl;
    private JPanel viewData;
    private JTextField documentTxt1;
    private JTextField documentTxt2;
    private JTextArea txtArea1;
    private JTextArea txtArea2;
    private JTextArea txtArea3;
    private JButton documentBtn;
    private JPanel dataFields;
    private JTextField dataField1;
    private JTextField dataField2;
    private JTextField dataField3;
    private JTextArea dataArea1;
    private JTextArea dataArea3;
    private JTextArea dataArea4;
    private JButton dataBackBtn;
    private JTextArea dataArea2;
    private JCheckBox onlyAvailableCB;

    public UserView() {
        userName.setText(getUserNameAndRole());

        // Adds a date chooser to the date panel, in order for the user to
        // select a date and filter the table by it.
        dateChooser = new JDateChooser();
        dateChooser.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                loadTableViewByIndex(view);
            }
        });
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
        ListSelectionModel selectionModel = sqlTable.getSelectionModel();
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(!selectionModel.isSelectionEmpty()) {
                    selectedRow = selectionModel.getMinSelectionIndex();
                }
            }
        });

        initializeUserInfo();
        initializeComboBoxes();
        userTable.setSelectedIndex(0);

        // Add a listener to the `reportsBtn` button, in order to switch to the
        // reports view.
        reportsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUIUtils.clearAll(selectionPane);
                view=UserViewType.USER_REPORTS;
                createUIComponents(view);
            }
        });
        // Add a listener to the `doctorsBtn` button, in order to switch to the
        // doctors view.
        doctorsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUIUtils.clearAll(selectionPane);
                view=UserViewType.USER_DOCTORS;
                createUIComponents(view);
            }
        });
        // Add a listener to the `appointmentsBtn` button, in order to switch to the
        // appointments view.
        appointmentsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUIUtils.clearAll(selectionPane);
                view=UserViewType.USER_CHECKING_APPOINTMENT;
                createUIComponents(view);
            }
        });
        // Add a listener to the `accountBtn` button, in order to switch to the
        // account view.
        accountBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUIUtils.clearAll(selectionPane);
                view=UserViewType.USER_ACCOUNT;
                createUIComponents(view);
            }
        });
        // Add a listener to the `txtField1`,`txtField2`,`txtField3` text fields,
        // in order to filter the table by the text inserted in the text fields.
        txtField1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                loadTableViewByIndex(view);
            }
        });
        txtField2.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                loadTableViewByIndex(view);
            }
        });
        txtField3.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                loadTableViewByIndex(view);
            }
        });

        // Add a listener to the `onlyAvailableCB` check box, in order to filter
        // the table by the only doctors that have an available appointment.
        onlyAvailableCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadTableViewByIndex(view);
            }
        });

        // Add a listener to the `comboBox` combo box, in order to filter the
        // table by the selected item in the combo box.
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (comboBox.getSelectedItem() != null) {
                    if (view.equals(UserViewType.USER_BOOKING)) {
                        loadTableViewByIndex(view);
                    }
                }
            }
        });
        // Add a listener to the `confirmBtn` button, in order to update the
        // user's information.
        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (view.equals(UserViewType.USER_ACCOUNT)) {
                    String phone = txtField4.getText();
                    String email = txtField5.getText();
                    String psw = password.getText();
                    String verifyPsw = verifyPassword.getText();

                    if (GUIUtils.emptyFieldToast(email, "Email") ||
                            GUIUtils.emptyFieldToast(password, "Password") ||
                            GUIUtils.emptyFieldToast(verifyPassword, "Verify Password")) {
                        return;
                    }

                    if(!Utils.emailValidator(email)) {
                        GUIUtils.exceptionToast("Email field is empty.\n" +
                                "Email must contains characters: \'a-z,A-Z,0-9 and at least one special character.\'", null);
                        GUIUtils.clearField(emailTxt);
                        return;
                    }
                    if (!Utils.passwordValidator(psw)){
                        GUIUtils.exceptionToast("Password field is empty.\n" +
                                "Password must contains characters: \'a-z,A-Z,0-9 and at least one special character.\'", null);
                        GUIUtils.clearField(password);
                        GUIUtils.clearField(verifyPassword);
                        return;
                    }

                    UsersTable usersTable = new UsersTable(connection);
                    Optional<User> userOpt = usersTable.findByPrimaryKey(user.getCF());

                    if (userOpt.isEmpty()) {
                        GUIUtils.exceptionToast("User not found.", null);
                        return;
                    }

                    User newUser = new User(
                            userOpt.get().getCF(),
                            userOpt.get().getName(),
                            userOpt.get().getSurname(),
                            userOpt.get().getBirthday(),
                            userOpt.get().getGender(),
                            !phone.isEmpty() ? Optional.of(phone) : Optional.empty(),
                            email,
                            Utils.passwordEncoder(psw)
                    );
                    if (!usersTable.updateTable(newUser, userOpt.get().getCF())) {
                        GUIUtils.exceptionToast("Error while updating user.", null);
                        return;
                    }

                    user = usersTable.findByPrimaryKey(userOpt.get().getCF()).get();
                    GUIUtils.clearAll(selectionPane);
                    selectionPane.setSelectedIndex(0);
                    userTable.setSelectedIndex(0);
                    initializeUserInfo();

                    GUIUtils.exceptionToast("User updated successfully.", null);
                }
            }
        });
        // Add a listener to the `selectBtn` button, in order to select a row
        // in the table.
        // If no row is selected, a toast message will be shown.
        // Based on the current view, the action of the button will change.
        selectBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedRow == -1) {
                    GUIUtils.exceptionToast("Please select a row", null);
                    return;
                }

                if (view.equals(UserViewType.USER_BOOKING) && checkingTimeSlotsForBooking()) {
                    GUIUtils.exceptionToast("Appointment booked successfully.", null);
                    GUIUtils.clearAll(dataFields);
                    view = UserViewType.USER_HOME;
                    selectionPane.setSelectedIndex(0);
                    userTable.setSelectedIndex(0);

                }
                if ((view.equals(UserViewType.USER_REPORTS) || view.equals(UserViewType.USER_CHECKING_REPORT))
                    && viewMedicalReports()) {
                    orderBy.clear();
                    view = UserViewType.USER_CHECKING_REPORT;
                    createUIComponents(view);
                }
                if (view.equals(UserViewType.USER_DOCTORS) && selectDoctorForBooking()) {
                    orderBy.clear();
                    view = UserViewType.USER_BOOKING;
                    createUIComponents(view);
                }
                if (view.equals(UserViewType.USER_CHECKING_APPOINTMENT) && viewAppointmentData()) {
                    createUIComponents(view);
                }
            }
        });
        // Add a listener to the `clearBtn` button, in order to clear the
        // selection of the table and the data fields.
        clearBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sqlTable.clearSelection();

                if (view.equals(UserViewType.USER_CHECKING_REPORT)) {
                    GUIUtils.clearAll(selectionPane);
                    orderBy.clear();
                    view = UserViewType.USER_REPORTS;
                    createUIComponents(view);
                    selectBtn.setVisible(true);
                }
                if(view.equals(UserViewType.USER_CHECKING_APPOINTMENT)) {
                    GUIUtils.clearAll(selectionPane);
                    createUIComponents(view);
                    selectBtn.setVisible(true);
                }
                if(view.equals(UserViewType.USER_BOOKING)) {
                    GUIUtils.clearAll(selectionPane);
                    createUIComponents(view);
                    selectBtn.setVisible(true);
                }
            }
        });
        // Add a listener to the `backBtn` button, in order to go back to the
        // home view.
        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUIUtils.clearAll(selectionPane);
                view=UserViewType.USER_HOME;
                tableSettings = new Integer[]{};
                lateBinding = new Object[]{};
                orderBy.clear();
                selectionPane.setSelectedIndex(0);
                userTable.setSelectedIndex(0);
            }
        });
        // Add a listener to the `dataBackBtn` button, with the same action of
        // the `backBtn` button.
        dataBackBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUIUtils.clearAll(selectionPane);
                view=UserViewType.USER_HOME;
                tableSettings = new Integer[]{};
                lateBinding = new Object[]{};
                orderBy.clear();
                selectionPane.setSelectedIndex(0);
                userTable.setSelectedIndex(0);
            }
        });
        // Add a listener to the `logoutBtn` button, in order to log out the
        // user and go back to the login view.
        logoutBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUIUtils.clearAll(selectionPane);
                thisFrame.dispose();
                callerFrame.setVisible(true);
            }
        });
    }

    /**
     * Constructor for the UserView class.
     * @param provider the connection provider.
     * @param user the user that is currently logged in.
     */
    public UserView(Connection provider, User user) {
        this.connection = Objects.requireNonNull(provider);
        this.user = user;
    }

    @Override
    public void Launcher(String name, JFrame caller) {
        callerFrame = caller;
        thisFrame = new JFrame(name);
        thisFrame.setContentPane(new UserView().panelMain);
        thisFrame.pack();
        thisFrame.setLocationRelativeTo(null);
        thisFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        thisFrame.setResizable(false);
        thisFrame.setVisible(true);
    }

    /**
     * @return the name of the user that is currently logged in.
     */
    private String getUserNameAndRole() {
        return "Logged in: " + user.getName() + " " + user.getSurname();
    }

    /**
     * Initializes the user's information in the home page.
     */
    private void initializeUserInfo() {
        nameTxt.setText(user.getName());
        surnameTxt.setText(user.getSurname());
        CFTxt.setText(user.getCF());
        emailTxt.setText(user.getEmail());
        birthdayTxt.setText(Utils.dateToString(user.getBirthday()));
    }

    /**
     * Initializes the combo boxes used in the UserView.
     */
    private void initializeComboBoxes() {
        String comboBoxQuery = TimeSlotsTable.getAllTimeslots;
        TimeSlotsTable timeSlotsTable = new TimeSlotsTable(connection);
        List<String> timeSlots = timeSlotsTable.findTimeSlotsAvailableByUserQuery(comboBoxQuery + " ORDER BY t.slot ASC");

        Object item = comboBox.getSelectedItem() == null ? "" : comboBox.getSelectedItem();
        comboBox.removeAllItems();
        comboBox.addItem("");

        for (String timeSlot : timeSlots) {
            comboBox.addItem(timeSlot);
            if (item.toString().equals(timeSlot)) {
                comboBox.setSelectedItem(item);
            }
        }
    }

    /**
     * Method that initializes all the components of the frame, based
     * on the view that is currently selected.
     * @param view the view that is currently selected.
     */
    private void createUIComponents(UserViewType view) {
        selectionPane.setSelectedIndex(1);
        userTable.setSelectedIndex(1);
        confirmBtn.setVisible(false);

        loadTableViewByIndex(view);

        if (view.equals(UserViewType.USER_REPORTS)) {
            GUIUtils.hideShowWithSequence(visualizeRequired, "0");
            GUIUtils.hideShowWithSequence(visualizeRequired, "111100111");
            GUIUtils.modifyLabelText(visualizeRequired,
                    new String[] {"Doctor's Name",
                            "Doctor's Surname",
                            "Date"
                    });
        }
        if (view.equals(UserViewType.USER_DOCTORS)) {
            GUIUtils.hideShowWithSequence(visualizeRequired, "0");
            GUIUtils.hideShowWithSequence(visualizeRequired, "11111100000000000000001");
            GUIUtils.modifyLabelText(visualizeRequired,
                    new String[] {"Name",
                            "Surname",
                            "Specialization"
                    });
        }
        if (view.equals(UserViewType.USER_BOOKING)) {
            GUIUtils.hideShowWithSequence(visualizeRequired, "0");
            GUIUtils.hideShowWithSequence(visualizeRequired, "11111111100011");
            GUIUtils.modifyLabelText(visualizeRequired,
                    new String[] {"Employee's CF",
                            "Employee's name",
                            "Employee's surname",
                            "Date",
                            "Timeslot"
                    });
        }
        if (view.equals(UserViewType.USER_ACCOUNT)) {
            GUIUtils.hideShowWithSequence(visualizeRequired, "0");
            GUIUtils.hideShowWithSequence(visualizeRequired, "0000000000000011111111");
            GUIUtils.hideShowField(comboBox, false);
            GUIUtils.hideShowField(timeSlotsLbl, false);
            GUIUtils.modifyLabelText(visualizeRequired,
                    new String[] {"Phone",
                            "Email",
                            "Password",
                            "Verify Password"
                    });
            confirmBtn.setVisible(true);
            confirmBtn.setText("Update");
            txtField4.setText(user.getPhone().orElse(""));
            txtField5.setText(user.getEmail());
            userTable.setSelectedIndex(0);
        }
        if (view.equals(UserViewType.USER_CHECKING_REPORT)) {
            selectionPane.setSelectedIndex(2);
            GUIUtils.hideShowWithSequence(dataFields, "0");
            GUIUtils.hideShowWithSequence(dataFields, "");
            GUIUtils.modifyLabelText(visualizeRequired,
                    new String[] {"Name",
                            "Surname",
                            "Day",
                            "Description and Diagnosis",
                            "Treatments",
                            "Follow up visits"
                    });
        }
        if (view.equals(UserViewType.USER_CHECKING_APPOINTMENT)) {
            selectionPane.setSelectedIndex(2);
            GUIUtils.hideShowWithSequence(dataFields, "0");
            GUIUtils.hideShowWithSequence(dataFields, "11111111");
            GUIUtils.modifyLabelText(visualizeRequired,
                    new String[] {"Name",
                            "Surname",
                            "Day",
                            "Feedback"
                    });
        }
    }

    /**
     * Method that load the correct view of the `sqlTable` table, depending on
     * the `view` parameter.
     * @param view the current view.
     */
    private void loadTableViewByIndex(UserViewType view) {
        userTable.setSelectedIndex(1);
        if (view.equals(UserViewType.USER_REPORTS)) {
            tableLbl.setText("Medical Records");
            String name = txtField1.getText();
            String surname = txtField2.getText();
            Date date = dateChooser.getDate();

            query = MedicalReportsTable.getUserMedicalRecordsQuery;
            conditions = "";

            if (!name.isEmpty()) {
                conditions += " AND name LIKE '%" + name + "%'";
            }
            if (!surname.isEmpty()) {
                conditions += " AND surname LIKE '%" + surname + "%'";
            }
            if (date != null) {
                conditions += " AND day = '" + Utils.dateToSqlDate(date) + "'";
            }

            tableSettings = new Integer[]{};
            lateBinding = new Object[]{user.getCF()};
            GUIUtils.showQueryInTable(connection, sqlTable, query + conditions, lateBinding, orderBy, tableSettings);
        }
        if (view.equals(UserViewType.USER_DOCTORS)) {
            tableLbl.setText("Doctors");
            String name = txtField1.getText();
            String surname = txtField2.getText();
            String specialization = txtField3.getText();

            query = onlyAvailableCB.isSelected() ? EmployeesTable.getAvailableMedicsWithRespectiveSpecialization :
                    EmployeesTable.getMedicsWithRespectiveSpecialization;
            conditions = "";

            if (!name.isEmpty()) {
                conditions += " AND name LIKE '%" + name + "%'";
            }
            if (!surname.isEmpty()) {
                conditions += " AND surname LIKE '%" + surname + "%'";
            }
            if (!specialization.isEmpty()) {
                conditions += " AND specialization LIKE '%" + specialization + "%'";
            }

            tableSettings = new Integer[]{};
            lateBinding = new Object[]{};
            GUIUtils.showQueryInTable(connection, sqlTable, query + conditions, lateBinding, orderBy, tableSettings);
        }
        if (view.equals(UserViewType.USER_BOOKING)) {
            tableLbl.setText("Schedule appointment");

            String cf = txtField1.getText();
            String name = txtField2.getText();
            String surname = txtField3.getText();
            Date date = dateChooser.getDate();
            String timeslot = "";

            if(comboBox.getSelectedItem() != null) {
                timeslot = comboBox.getSelectedItem().toString();
            }

            query = TimeSlotsTable.getTimeslotsAvailable;
            conditions = "";

            if (!cf.isEmpty()) {
                conditions += " AND cf LIKE '%" + cf + "%'";
            }
            if (!name.isEmpty()) {
                conditions += " AND name LIKE '%" + name + "%'";
            }
            if (!surname.isEmpty()) {
                conditions += " AND surname LIKE '%" + surname + "%'";
            }
            if (date != null) {
                conditions += " AND day = '" + Utils.dateToSqlDate(date) + "'";
            }
            if (!timeslot.isEmpty()) {
                conditions += " AND slot = '" + timeslot + "'";
            }

            tableSettings = new Integer[]{};
            lateBinding = new Object[]{};
            GUIUtils.showQueryInTable(connection, sqlTable, query + conditions, lateBinding, orderBy, tableSettings);
        }
        if (view.equals(UserViewType.USER_CHECKING_APPOINTMENT)) {
            tableLbl.setText("Appointments");

            String name = txtField1.getText();
            String surname = txtField2.getText();
            String specialization = txtField3.getText();
            Date date = dateChooser.getDate();
            String timeslot = "";

            if(comboBox.getSelectedItem() != null) {
                timeslot = comboBox.getSelectedItem().toString();
            }

            query = BookingsTable.getBookingByUserCFQuery;
            conditions = "";

            if (!name.isEmpty()) {
                conditions += " AND e.name LIKE '%" + name + "%'";
            }
            if (!surname.isEmpty()) {
                conditions += " AND e.surname LIKE '%" + surname + "%'";
            }
            if (!specialization.isEmpty()) {
                conditions += " AND p.specialization LIKE '%" + specialization + "%'";
            }
            if (date != null) {
                conditions += " AND t.day = '" + Utils.dateToSqlDate(date) + "'";
            }
            if (!timeslot.isEmpty()) {
                conditions += " AND t.slot = '" + timeslot + "'";
            }

            tableSettings = new Integer[]{};
            lateBinding = new Object[]{user.getCF()};

            GUIUtils.showQueryInTable(connection, sqlTable, query + conditions, lateBinding, orderBy, tableSettings);
        }
    }

    /**
     * Method that load the correct view of the `dataFields` panel, depending on
     * chosen medical report to view.
     * @return true if the operation was successful, false otherwise.
     */
    private boolean viewMedicalReports() {
        MedicalReportsTable medicalReportsTable = new MedicalReportsTable(connection);
        Optional<MedicalReport> medicalReportOpt = medicalReportsTable
                .getMedicalReportsByUserCFAtRow(
                        user.getCF(),
                        conditions,
                        !orderBy.isEmpty() ? orderBy.get(0) : null,
                        selectedRow
                );

        if (medicalReportOpt.isEmpty()) {
            GUIUtils.exceptionToast("No medical report found", null);
            return false;
        }

        MedicalReport medicalReport = medicalReportOpt.get();

        EmployeesTable employeesTable = new EmployeesTable(connection);
        Optional<Employee> employeeOpt = employeesTable.findByPrimaryKey(medicalReport.getEmployee_cf());

        if (employeeOpt.isEmpty()) {
            GUIUtils.exceptionToast("No employee found", null);
            return false;
        }

        dataField1.setText(employeeOpt.get().getName());
        dataField2.setText(employeeOpt.get().getSurname());
        dataField3.setText(medicalReport.getDay().toString());
        dataArea1.setText("Description:\n" + medicalReport.getDescription());
        dataArea2.setText("Diagnosis:\n" + medicalReport.getDiagnosis());

        PrescriptionsTable prescriptionsTable = new PrescriptionsTable(connection);
        List<Prescription> prescriptions = prescriptionsTable.findPrescriptionByMedicalReportID(medicalReport.getId());

        if (!prescriptions.isEmpty()) {
            dataArea3.setText("Treatments:\n");
            for (Prescription prescription : prescriptions) {
                TreatmentsTable treatmentsTable = new TreatmentsTable(connection);

                if (prescription.getTreatment_id().isPresent()) {
                    Optional<Treatment> treatmentOpt = treatmentsTable.findByPrimaryKey(
                            new Pair<>(prescription.getTreatment_id().get(), "")
                    );

                    if (!treatmentOpt.isEmpty()) {
                        Treatment treatment = treatmentOpt.get();

                        dataArea3.setText(dataArea3.getText() + treatment.getName() + " x" + prescription.getQuantity().orElse(1) + "\n");
                    }
                }
            }
            dataArea4.setText("Follow up:\n" + prescriptions.get(0).getFollowup().orElse(""));
        }
        dataArea1.setEditable(false);
        dataArea2.setEditable(false);
        dataArea3.setEditable(false);
        dataArea4.setEditable(false);
        return true;
    }

    /**
     * Method that, once the user has selected a doctor, prompts a dialog
     * window to check if the user would like to book an appointment with
     * him/her.
     * @return true if the operation was successful, false otherwise.
     */
    private boolean selectDoctorForBooking() {
        EmployeesTable employeesTable = new EmployeesTable(connection);

        String tmpQuery = onlyAvailableCB.isSelected() ? EmployeesTable.getMedicsWithAgenda :
                EmployeesTable.getMedics;
        tmpQuery = SQLUtils.orderQueryByTableSortingOrder(tmpQuery + conditions, orderBy,"");
        Optional<Employee> employeeOpt = employeesTable.getEmployeeAtRowWithStatement(tmpQuery, selectedRow);

        if (employeeOpt.isEmpty()) {
            GUIUtils.exceptionToast("No employee found", null);
            return false;
        }

        Employee employee = employeeOpt.get();

        if (!GUIUtils.yesNoToast("Would you like to look at the bookings available for " +
                employee.getName() + " " + employee.getSurname() +
                "?")) {
            return false;

        }

        txtField1.setText(employee.getCF());
        txtField2.setText(employee.getName());
        txtField3.setText(employee.getSurname());
        return true;
    }

    /**
     * Method that books an appointment for the user, once he/she has selected
     * a day and a time slot associated to a doctor.
     * @return true if the operation was successful, false otherwise.
     */
    private boolean checkingTimeSlotsForBooking() {
        TimeSlotsTable timeSlotsTable = new TimeSlotsTable(connection);

        String tmpQuery = TimeSlotsTable.getSelectedTimeslotData;
        tmpQuery = SQLUtils.orderQueryByTableSortingOrder(tmpQuery + conditions, orderBy, "");
        Optional<TimeSlot> timeSlotOpt = timeSlotsTable.getTimeSlotAtRowWithStatement(tmpQuery, selectedRow);

        if (timeSlotOpt.isEmpty()) {
            GUIUtils.exceptionToast("No time slot found.", null);
            return false;
        }

        TimeSlot timeSlot = timeSlotOpt.get();

        EmployeesTable employeesTable = new EmployeesTable(connection);
        Optional<Employee> employeeOpt = employeesTable.findByPrimaryKey(timeSlot.getAgenda_employee_cf());

        if (employeeOpt.isEmpty()) {
            GUIUtils.exceptionToast("No employee found.", null);
            return false;
        }

        if (!GUIUtils.yesNoToast("Would you like to book an appointment with " +
                employeeOpt.get().getName() + " " + employeeOpt.get().getSurname() + "\n" +
                "At " + timeSlot.getDay() + " " + timeSlot.getSlot() +
                "?")) {
            return false;
        }

        BookingsTable bookingsTable = new BookingsTable(connection);

        Booking booking = new Booking(
                0,
                timeSlot.getDay(),
                timeSlot.getId(),
                user.getCF(),
                employeeOpt.get().getCF(),
                "");
        if(!bookingsTable.insertToTable(booking)) {
            GUIUtils.exceptionToast("Something went wrong, please try again later.", null);
            return false;
        }

        if (!timeSlotsTable.updateTable(
                new TimeSlot(
                        timeSlot.getId(),
                        timeSlot.getDay(),
                        timeSlot.getAgenda_day(),
                        timeSlot.getAgenda_employee_cf(),
                        timeSlot.getSlot(),
                        false),
                new Pair<>(timeSlot.getId(),
                        new Object[]{
                                timeSlot.getDay(),
                                timeSlot.getSlot(),
                                timeSlot.getAgenda_employee_cf()
                        })
        )) {
            GUIUtils.exceptionToast("Something went wrong, please try again later.", null);

            bookingsTable.deleteFromTable(new Object[]{
                    booking.getDay(),
                    booking.getTimeslot_id(),
                    booking.getUser_cf(),
                    booking.getEmployee_cf()
            });
            return false;
        }

        return true;
    }

    private boolean viewAppointmentData() {
        createUIComponents(view);

        // Retrieves the selected row from the table and gets the
        // corresponding booking from the database.
        query = BookingsTable.getBookingsByUserCFQuery;
        query = SQLUtils.orderQueryByTableSortingOrder(query, orderBy, "");
        BookingsTable bookingsTable = new BookingsTable(connection);
        Optional<Booking> bookingOpt = bookingsTable.getBookingAtRowWithStatementWithCF(
                query,
                selectedRow,
                user.getCF()
        );

        if (bookingOpt.isEmpty()) {
            GUIUtils.exceptionToast("No booking found.", null);
            return false;
        }
        Booking booking = bookingOpt.get();

        EmployeesTable employeesTable = new EmployeesTable(connection);
        Optional<Employee> employeeOpt = employeesTable.findByPrimaryKey(booking.getEmployee_cf());

        if (employeeOpt.isEmpty()) {
            GUIUtils.exceptionToast("No employee found.", null);
            return false;
        }
        Employee employee = employeeOpt.get();

        // If the booking can be retrieved, the data fields will be
        // filled with the booking's information.
        // If the booking has been confirmed or rejected by the
        // employee, the user will be able to read the given
        // feedback.
        dataField1.setText(employee.getName());
        dataField2.setText(employee.getSurname());
        dataField3.setText(booking.getDay().toString());
        dataArea1.setText(booking.getFeedback());
        return true;
    }
}
