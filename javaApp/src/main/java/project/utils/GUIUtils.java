package project.utils;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GUIUtils<K> {

    public GUIUtils() {};

    /**
     * Method that creates a dialog window to inform the user
     * about an event that was fired by the application.
     * @param text the text to be displayed.
     * @param e the exception to be displayed.
     */
    public static void exceptionToast(final String text, final Exception e) {
        JOptionPane.showMessageDialog(null, text + "\n" + (e == null ? "" : e));
    }

    /**
     * Method that creates a `yes or no` window to ask the user
     * if he wants to proceed with the action.
     * @param text the text to be displayed.
     * @return true if the user pressed yes, false otherwise.
     */
    public static boolean yesNoToast(final String text) {
        int reply = JOptionPane.showConfirmDialog(null, text, null, JOptionPane.YES_NO_OPTION);

        return reply == JOptionPane.YES_OPTION ? true : false;
    }

    /**
     * Method that creates a multi choice window to ask the user
     * to choose between multiple options.
     * @param text the text to be displayed.
     * @param options the options to be displayed.
     * @return the index of the option chosen by the user:
     * - 0 if the user pressed the first option
     * - 1 if the user pressed the second option
     * - -1 if the user pressed the cancel button
     */
    public static Integer multipleChoiceToast(final String text, final String[] options) {
        Integer reply = JOptionPane.showOptionDialog(null, text, null, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        return reply;
    }

    /**
     * Method that creates a dialog window to inform the user
     * that a field that was required is empty.
     * @param obj the object to be checked.
     * @param name the name of the object.
     * @return true if the object is empty, false otherwise.
     */
    public static boolean emptyFieldToast(final Object obj, final String name) {
        if (obj instanceof String && ((String)obj).isEmpty()) {
            GUIUtils.exceptionToast(Utils.smartStringCapitalize(name)
                    + " text field is empty, enter a valid "
                    + Utils.checkIfAcronymAndCapitalize(name) + ".", null);
            return true;
        } else if (obj instanceof String && (((String)obj).equals("?") || ((String)obj).equals("-1"))) {
            String msg = ((String)obj).equals("?") ? "gender" : "role";
            GUIUtils.exceptionToast("No " + msg + " has been chosen, please select a valid " + msg + ".", null);
            return true;
        } else if (obj==null) {
            GUIUtils.exceptionToast("No birthday has been chosen, please select a valid birthday.", null);
            return true;
        }
        return false;
    }

    /**
     * Method that creates a dialog window to inform the user
     * that the CF field is not valid.
     * @param cf the CF to be checked.
     * @param name the name of the user.
     * @param surname the surname of the user.
     * @param birthday the birthday of the user.
     * @param gender the gender of the user.
     * @param field the field to be cleared.
     * @return true if the CF is not valid, false otherwise.
     */
    public static boolean CFFieldNotValidatedToast(final String cf, final String name, final String surname, final Date birthday, final Character gender, final JTextField field) {
        if(!Utils.CFValidator(cf, name, surname, birthday, gender)) {
            GUIUtils.exceptionToast("CF text field doesn't match user's information, enter a valid TIN.", null);
            GUIUtils.clearField(field);
            return true;
        }
        return false;
    }

    /**
     * Method that creates a dialog window to inform the user
     * that the email field is not valid.
     * @param email the email to be checked.
     * @param field the field to be cleared.
     * @return true if the email is not valid, false otherwise.
     */
    public static boolean emailNotValidatedToast(final String email, final JTextField field) {
        if (!Utils.emailValidator(email)) {
            GUIUtils.exceptionToast("Email field does not match any valid email composition.", null);
            GUIUtils.clearField(field);
            return true;
        }
        return false;
    }

    /**
     * Method that creates a dialog window to inform the user
     * that the password field is not valid.
     * @param password the password to be checked.
     * @param field the field to be cleared.
     * @return true if the password is not valid, false otherwise.
     */
    public static boolean passwordNotValidatedToast(final String password, final JTextField field) {
        if (!Utils.passwordValidator(password)) {
            GUIUtils.exceptionToast("Password must contains characters: \'a-z,A-Z,0-9 and at least one special character.\'", null);
            GUIUtils.clearField(field);
            return true;
        }
        return false;
    }

    /**
     * Method that fills a `JComboBox` with the elements of a list.
     * @param comboBox the `JComboBox` to be filled.
     * @param list the list to be used to fill the `JComboBox`.
     */
    public void fillJComboBox(JComboBox<String> comboBox, ArrayList<K> list) {
        comboBox.removeAllItems();
        for (K item : list) {
            comboBox.addItem(item.toString());
        }
    }

    /**
     * Method used to hide a column of a `JTable` by its index.
     * @param table the `JTable` to be used.
     * @param idx the index of the column to be hidden.
     */
    public static void hideTableColumnByIdx(JTable table, int idx) {
        table.getColumnModel().getColumn(idx).setMinWidth(0);
        table.getColumnModel().getColumn(idx).setMaxWidth(0);
        table.getColumnModel().getColumn(idx).setWidth(0);
    }

    /**
     * Method used to change the text of all the visible `JLabel`
     * components of a given `Container`.
     * @param c the `Container` to be used.
     * @param newLabels the new labels to be used.
     */
    public static void modifyLabelText(final Container c, final String[] newLabels) {
        ArrayList<Component> compList = getAllComponents(c);
        int i = 0;

        for (Component comp : compList) {
            if (comp instanceof JLabel && comp.isVisible()) {
                if (i == newLabels.length) {
                    break;
                }
                ((JLabel)comp).setText(newLabels[i]);
                i++;
            }
        }
    }

    /**
     * Method used to clear the fields of a given `JComponent`
     * based on its type.
     * @param field the `JComponent` to be used.
     * @return true if the field has been cleared, false otherwise.
     */
    public static boolean clearField(JComponent field) {
        if (field instanceof JTextField) {
            ((JTextField)field).setText("");
            return true;
        }
        if (field instanceof JTextArea) {
            ((JTextArea)field).setText("");
            return true;
        }
        if (field instanceof JRadioButton) {
            ((JRadioButton)field).setSelected(false);
            return true;
        }
        if (field instanceof JDateChooser) {
            ((JDateChooser)field).setCalendar(null);
            return true;
        }
        if (field instanceof JComboBox) {
            ((JComboBox)field).setSelectedIndex(-1);
            return true;
        }
        if (field instanceof JCheckBox) {
            ((JCheckBox)field).setSelected(false);
            return true;
        }
        return false;
    }

    /**
     * Method used to block or permit the editing of a given
     * `JComponent`.
     * @param field the `JComponent` to be used.
     * @param edit the boolean value to be used.
     * @return
     */
    public static boolean editField(JComponent field, boolean edit) {
        if (field instanceof JTextField) {
            ((JTextField)field).setEditable(edit);
            return true;
        }
        if (field instanceof JTextArea) {
            ((JTextArea)field).setEditable(edit);
            return true;
        }
        if (field instanceof JComboBox) {
            ((JComboBox)field).setEditable(edit);
            return true;
        }
        return false;
    }

    /**
     * Method used to hide the tabs of a given `JTabbedPane`.
     * @param tabbedPane the `JTabbedPane` to be used.
     */
    public static void hideTabsInTabbedPane(JTabbedPane tabbedPane) {
        tabbedPane.setUI(new javax.swing.plaf.metal.MetalTabbedPaneUI() {
            @Override
            protected int calculateTabAreaHeight(int tab_placement, int run_count, int max_tab_height) {
                return -1;
            }
            protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex){}
        });
    }

    /**
     * Method used to hide or show a given `JComponent` based on its type.
     * @param field the `JComponent` to be used.
     * @param hide the boolean value to be used.
     * @return true if the field has been hidden or shown, false otherwise.
     */
    public static boolean hideShowField(JComponent field, boolean hide) {
        if (field instanceof JLabel && !((JLabel)field).getText().equals("")) {
            ((JLabel)field).setVisible(hide);
            return true;
        }
        if (field instanceof JTextField) {
            ((JTextField)field).setVisible(hide);
            return true;
        }
        if (field instanceof JRadioButton) {
            ((JRadioButton)field).setVisible(hide);
            return true;
        }
        if (field instanceof JDateChooser) {
            ((JDateChooser)field).setVisible(hide);
            return true;
        }
        if (field instanceof JComboBox) {
            ((JComboBox)field).setVisible(hide);
            return true;
        }
        if (field instanceof JTextArea) {
            ((JTextArea)field).setVisible(hide);
            return true;
        }
        if (field instanceof JCheckBox) {
            ((JCheckBox)field).setVisible(hide);
            return true;
        }
        return false;
    }

    /**
     * Method used to clear all the fields of a given `Container`
     * and to permit the editing of all the `JComponent`.
     * @param c the `Container` to be used.
     */
    public static void clearAll(final Container c) {
        for (Component comp : getAllComponents(c)) {
            if (comp instanceof JComponent) {
                editField((JComponent) comp, true);
                clearField((JComponent) comp);
            }
        }
    }

    /**
     * Method used to clear the fields of a given `Container`
     * by their index in the container.
     * @param c the `Container` to be used.
     * @param idx the index of the fields to be cleared.
     */
    public static void clearFieldByIndex(final Container c, Integer idx) {
        Integer i = 0;
        for (Component comp : getAllComponents(c)) {
            if (comp instanceof JComponent && comp instanceof JTextField) {
                if (i == idx) {
                    clearField((JComponent) comp);
                    break;
                }
                i++;

            }
        }
    }

    /**
     * Method used to hide or show all the fields of a given `Container`
     * based on a sequence of 0 and 1 (0 = hide, 1 = show).
     * In this way, with the usage of other utilities methods, it is possible
     * to create custom views from the same `Container`.
     * Sequence fill rules:
     * - if the sequence is empty, all the fields will be shown
     * - if the sequence is shorter than the number of fields, the sequence
     *   will be filled with 0
     * @param c the `Container` to be used.
     * @param sequence the sequence of 0 and 1 to be used.
     */
    public static void hideShowWithSequence(final Container c, String sequence) {
        ArrayList<Component> compList = getAllComponents(c);
        Integer idx = 0;

        if (sequence.isEmpty()) {
            sequence = Utils.fillRight(sequence, compList.size(), "1");
        } else if (sequence.length() < compList.size()) {
            sequence = Utils.fillRight(sequence, compList.size(), "0");
        }

        for(Component comp : compList) {
            if (comp instanceof JComponent) {
                boolean hide = sequence.charAt(idx) == '0' ? false : true;
                if(hideShowField((JComponent) comp, hide)) {
                    idx++;
                    if (idx == sequence.length()) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Method that returns recursively all the `Component` of a given
     * `Container`.
     * @param c the `Container` to be used.
     * @return an `ArrayList` of `Component`.
     */
    public static ArrayList<Component> getAllComponents(final Container c) {
        Component[] comps = c.getComponents();
        ArrayList<Component> compList = new ArrayList<>();
        for (Component comp : comps) {
            compList.add(comp);
            if (comp instanceof Container && !(comp instanceof JComboBox)) {
                compList.addAll(getAllComponents((Container) comp));
            }
        }
        return compList;
    }

    /**
     * Method that removes all the `Component` of a given `Container`.
     * @param c the `Container` to be used.
     */
    public static void removeAllComponents(final Container c) {
        Component[] comps = c.getComponents();
        for (Component comp : comps) {
            c.remove(comp);
        }
    }

    /**
     * Method that fills a given `JTable` with the data of a query.
     * If the query returns no data, a message in the table will be shown.
     * @param table the `JTable` to be used.
     * @param model the `DefaultTableModel` to be used.
     * @param colName the array of column names to be used.
     * @param rowData the array of row data to be used.
     */
    public static void fillTableWithSQLResultSet(JTable table, DefaultTableModel model, String[] colName, ArrayList<Object[]> rowData) {
        model.setColumnIdentifiers(colName);

        for (Object[] row : rowData) {
            model.addRow(row);
        }

        if (model.getRowCount() == 0) {
            JLabel label = new JLabel("No records available");
            label.setSize(label.getPreferredSize());
            table.add(label);
            table.setFillsViewportHeight(true);
        }
    }

    /**
     * Method that automatically fills a given `JTable` with the data from a
     * given query, with the order specified in the `orderBy` parameter and
     * excluding the columns specified in the `exclude` parameter.
     * @param connection the `Connection` to be used.
     * @param table the `JTable` to be used.
     * @param query the query to be used.
     * @param orderBy the list of `Pair` of the column name and the sorting order to be used.
     * @param exclude the array of `Integer` to be used.
     */
    public static void showQueryInTable(final Connection connection, final JTable table, final String query,
                                        final Object[] lateBinding, final List<Pair<String,String>> orderBy,
                                        final Integer[] exclude) {
        // Clears the model.
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        // Clears any previous selection.
        table.clearSelection();
        // Removes all the components from the table (in case the `No records available` label was added
        // to the table in a previous call).
        GUIUtils.removeAllComponents(table);
        // Creates a new order query based on the `orderBy` parameter.
        String queryOrdered = SQLUtils.orderQueryByTableSortingOrder(query,orderBy,"");
        // Retrieves the data from the database.
        Pair<String[], ArrayList<Object[]>> data = SQLUtils.showDataInTable(connection, queryOrdered, lateBinding);
        // Fills the table with the data.
        GUIUtils.fillTableWithSQLResultSet(table, model,data.getX(),data.getY());

        // Hides the columns specified in the `exclude` parameter.
        for (Integer i : exclude) {
            GUIUtils.hideTableColumnByIdx(table,i);
        }
    }
}
