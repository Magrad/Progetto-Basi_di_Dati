package project.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class containing utility methods.
 */
public final class Utils {
    private Utils() {}
    private final static BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public final static String USERNAME = "root";
    public final static String PASSWORD = "";
    public final static String DBNAME = "ospedale.bo";

    /**
     * Method to check if a given string conforms to
     * the email format.
     * @param email the string to check.
     * @return true if the string is a valid email, false otherwise.
     */
    public static boolean emailValidator(String email) {
        return email.matches("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");
    }

    /**
     * Method to check if a given string conforms to
     * the password format:
     * - at least 8 characters long
     * - at least one lowercase letter
     * - at least one uppercase letter
     * - at least one number
     * - at least one special character
     * @param password the string to check.
     * @return true if the string is a valid password, false otherwise.
     */
    public static boolean passwordValidator(String password) {
        if (password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*])(.{8,})")) {
            return true;
        } else if (password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(.{8,})")){
            return true;
        } else if (password.matches("^(?=.*[a-z])(?=.*[0-9])(.{8,})")){
            return false;
        } else if (password.matches("^(?=.*[A-Z])(?=.*[0-9])(.{8,})")){
            return false;
        }

        return false;
    }

    /**
     * Method that creates a valid CF from the given parameters.
     * @param name the name of the person.
     * @param surname the surname of the person.
     * @param birthday the birthday of the person.
     * @param gender the gender of the person.
     * @return the CF of the person.
     */
    public static String CFgenerator(String name, String surname, Date birthday, Character gender) {
        final Character[] months = {'A','B','C','D','E','H','L','M','P','R','S','T'};

        Calendar cal=Calendar.getInstance();
        cal.setTime(birthday);

        String s=getConsonants(surname).toUpperCase();
        String n=getConsonants(name).toUpperCase();
        int year=Integer.parseInt((""+cal.get(Calendar.YEAR)).substring(2,4));
        Character month=months[cal.get(Calendar.MONTH)];
        int day=cal.get(Calendar.DAY_OF_MONTH)+(gender=='F'?40:0);

        // 3 first letters of the person's surname (consonants first, then vowels)
        // 3 first letters of the person's name (consonants first, then vowels)
        // 2 last digits of the person's birth year
        // 1 letter corresponding to the person's birth month mapped to the `months` array
        // 2 last digits of the person's birthday (if the person is female add 40 to the day)
        return s+n+parseInt(""+year)+month+parseInt(""+day);
    }

    /**
     * Method that checks if a given CF is valid based on user data.
     * @param cf the user's CF.
     * @param name the user's name.
     * @param surname the user's surname.
     * @param birthday the user's birthday.
     * @param gender the user's gender
     * @return true if the CF is valid, false otherwise.
     */
    public static boolean CFValidator(String cf, String name, String surname, Date birthday, Character gender) {
        final int BIRTH_LOCATION_PLUS_LAST_RND_CHAR = 4 + 1;

        String generatedCF = CFgenerator(name,surname,birthday,gender);
        String CFimportant = cf.substring(0, cf.length()-BIRTH_LOCATION_PLUS_LAST_RND_CHAR);
        if (!CFimportant.equals(generatedCF)) return false;

        return true;
    }

    /**
     * Method that returns an encrypted password based on the
     * BCryptPasswordEncoder.
     * @param password the password to encrypt.
     * @return the encrypted password.
     */
    public static String passwordEncoder(final String password) {
        return bCryptPasswordEncoder.encode(password);
    }

    /**
     * Method that checks if a given password matches a given
     * encrypted password.
     * @param password the password to check.
     * @param encryptedPassword the encrypted password to check against.
     * @return true if the passwords match, false otherwise.
     */
    public static boolean passwordMatches(final String password, final String encryptedPassword) {
        return bCryptPasswordEncoder.matches(password,encryptedPassword);
    }

    /**
     * Method that returns a better `parseInt` method:
     * - if the string is empty, return -1
     * - if the string contains only letters, return -1
     * @param str the string to parse.
     * @return the parsed integer.
     */
    public static int parseInt(final String str) {
        if (str.isEmpty()) return -1;
        if (str.matches("[a-zA-Z]+")) return -1;

        return Integer.parseInt(str);
    }

    /**
     * Method that fills a string with a given character
     * until it reaches a given length.
     * @param str the string to fill.
     * @param length the length to reach.
     * @param finalChar the character to fill with.
     * @return the filled string.
     */
    public static String fillRight(final String str, final int length, final String finalChar) {
        String new_str = str;
        for (int i = 0; i < length - str.length(); i++) {
            new_str += finalChar;
        }
        return new_str;
    }

    /**
     * Method that capitalizes a string in a smart way:
     * - if the string is empty, return the string
     * - if the string is composed only of consonants, return the string in uppercase
     * - otherwise, capitalize the first letter and return the string
     * @param str
     * @return
     */
    public static String smartStringCapitalize(final String str) {
        if (str.isEmpty()) return str;

        return str.toUpperCase().matches("[QWRTYPSDFGHJKLZXCVBNM]+") ? str.toUpperCase()
                : str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Method that checks if a string is an acronym and capitalizes it if so.
     * @param str the string to check.
     * @return the capitalized string if it's an acronym, the string otherwise.
     */
    public static String checkIfAcronymAndCapitalize(final String str) {
        return str.toUpperCase().matches("[QWRTYPSDFGHJKLZXCVBNM]+") ? str.toUpperCase()
                : str;
    }

    /**
     * Method that returns the first word of a string after a given substring.
     * @param str the string to check.
     * @param subStr the substring to check.
     * @return the first word of the string after the substring.
     */
    public static String getNameAfterSubstring(final String str,final String subStr) {
        String removeBefore = str
                            .toLowerCase()
                            .substring(str
                                    .toLowerCase()
                                    .indexOf(subStr) + subStr.length());
        return removeBefore.split(" ")[0];
    }

    /**
     * Method that generates a random person's name, surname and
     * gender from two text files in this project.
     * @return a pair of values containing:
     * - in the getter `.getX()` the person's name and surname
     * - in the getter `.getY()` the gender of the person
     */
    public static Pair<String,Character> generateRandomFullname() {
        String name = "";
        String surname = "";
        Character gender = '?';
        try {
            // Get random name and surname from text files in this project.
            final String namesPath = "/src/main/java/project/text/names.txt";
            final String surnamesPath = "/src/main/java/project/text/surnames.txt";
            final String[] names = readFile(namesPath).split("\n");
            final String[] surnames = readFile(surnamesPath).split("\n");
            int rndName = (int) (Math.random() * names.length);
            name = names[rndName];
            surname = surnames[(int) (Math.random() * surnames.length)];
            gender = (rndName < 743 ? 'M' : 'F');
        } catch (final Exception e) {
            GUIUtils.exceptionToast("An error occurred while generating random name and surname", null);
            return new Pair<>("", '?');
        }
        return new Pair<String,Character>(name+" "+surname,gender);
    }

    /**
     * Method that creates a list of all the possible positions
     * available in the hospital.
     * @return the list of all the possible positions.
     */
    public static ArrayList<Pair<String, String>> generateAllPossiblePositions() {
        final String[] roles = {"Doctor", "Nurse"};
        final String[] specializations = {
                "Anesthetist",
                "Cardiologist",
                "Surgeon",
                "Diabetologist",
                "Dermatologist",
                "Endocrinologist",
                "Gastroenterologist",
                "Neurologist",
                "Nephrologist",
                "Internal Medicine",
                "Radiologist",
                "Psychiatrist",
                "Urologist",
                "ENT",
                "Orthopedic"
        };

        ArrayList<Pair<String, String>> positions = new ArrayList<>();
        for (final String specialization : specializations) {
            positions.add(new Pair<>(roles[0], specialization));
        }
        // The nurse doesn't have a specialization.
        positions.add(new Pair<>(roles[1], ""));

        return positions;
    }

    /**
     * Method that transforms a boolean into an integer.
     * @param bool the boolean to transform.
     * @return the integer.
     */
    public static Integer booleanToInt(final boolean bool) {
        return bool ? 1 : 0;
    }

    /**
     * Method that transforms a java.sql.Date variable into a java.util.Date.
     * @param sqlDate the java.sql.Date to transform.
     * @return the java.util.Date.
     */
    public static java.util.Date sqlDateToDate(final java.sql.Date sqlDate) {
        return sqlDate == null ? null : new java.util.Date(sqlDate.getTime());
    }

    /**
     * Method that transforms a java.util.Date variable into a java.sql.Date.
     * @param date the java.util.Date to transform.
     * @return the java.sql.Date.
     */
    public static java.sql.Date dateToSqlDate(final java.util.Date date) {
        return date == null ? null : new java.sql.Date(date.getTime());
    }

    /**
     * Method that transforms a java.util.Date variable into a string.
     * @param date the java.util.Date to transform.
     * @return the string.
     */
    public static String dateToString(final java.util.Date date) {
        return date == null ? "" : new SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN).format(date);
    }

    /**
     * Method that transforms a java.time.LocalDate variable into a
     * java.util.Date.
     * @param localDate the java.time.LocalDate to transform.
     * @return the java.util.Date.
     */
    public static java.util.Date localDatetoDate(final java.time.LocalDate localDate) {
        return localDate == null ? null : java.util.Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Method that transforms a string into a java.sql.Time.
     * @param time the string to transform.
     * @return the java.sql.Time.
     */
    public static java.sql.Time stringToSqlTime(final String time) {
        if (time.isEmpty()) {
            GUIUtils.exceptionToast("Time cannot be empty", null);
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            return new java.sql.Time(sdf.parse(time).getTime());
        } catch (Exception e) {
            GUIUtils.exceptionToast("Time format is not valid", null);
            return null;
        }
    }

    /**
     * Method that transforms a java.sql.Time variable into a string.
     * @param time the java.sql.Time to transform.
     * @return the string.
     */
    public static String sqlTimeToString(final java.sql.Time time) {
        return time == null ? "" : time.toString();
    }

    /**
     * Method that generates a Date object from a day, month and year.
     * @param day the day.
     * @param month the month.
     * @param year the year.
     * @return the Date object.
     */
    public static java.util.Date buildDate(final int day, final int month, final int year) {
        try {
            final String dateFormatString = "dd/MM/yyyy";
            final String dateString = day + "/" + month + "/" + year;
            final Date date = new SimpleDateFormat(dateFormatString, Locale.ITALIAN).parse(dateString);
            return date;
        } catch (final ParseException e) {
            return null;
        }
    }

    /**
     * Method that creates a list of all the possible time slots by a given
     * start time, end time, start break, end break and appointment duration.
     * @param startTime the start time.
     * @param endTime the end time.
     * @param startBreak the start break.
     * @param endBreak the end break.
     * @param slotSizeInMinutes the appointment duration.
     * @return the list of all the possible time slots.
     */
    public static ArrayList<String> getTimeSlots(LocalTime startTime, LocalTime endTime, LocalTime startBreak,
                                                 LocalTime endBreak, int slotSizeInMinutes) {
        ArrayList<String> timeSlots = new ArrayList<>();
        // Until the current time is before the end time, add the current time
        // to the list of time slots if it is not in the break time, and then
        // increase the current time by the appointment duration.
        for (LocalTime time = startTime, nextTime; time.isBefore(endTime); time = nextTime) {
            if (time.isBefore(startBreak) || time.isAfter(endBreak)) {
                timeSlots.add(time.toString());
            }
            nextTime = time.plusMinutes(slotSizeInMinutes);
            if (nextTime.isAfter(endTime)) {
                break; // Time slot crosses end time.
            }
        }
        return timeSlots;
    }

    /**
     * Method that creates a list of all the dates between two given dates.
     * @param startDate the start date.
     * @param endDate the end date.
     * @return the list of all the dates between the two given dates.
     */
    public static List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) {
            return startDate.datesUntil(endDate).collect(Collectors.toList());
    }

    /**
     * Method that is used to read from a file and return its content as a
     * string.
     * @param path the path of the file.
     * @return the content of the file.
     * @throws Exception if the file is not found.
     */
    public static String readFile(final String path) throws Exception {
        final String absolutePath = new File("").getAbsolutePath();
        final File file = new File(absolutePath + path);
        final Scanner sc = new Scanner(file);
        String text = "";
        while (sc.hasNextLine()) {
            text += sc.nextLine() + "\n";
        }
        sc.close();
        return text;
    }

    /**
     * Method that is used to verify a CF.
     * @param s the name or surname to extract the 3 letters from.
     * @return the 3 letters.
     */
    private static String getConsonants(String s) {
        String out="";
        s=s.toLowerCase();

        // Add the first 3 consonants to the output string.
        for (int i=0 ; i<s.length(); i++) {
            char ch = s.charAt(i);
            if(out.length() >= 3) { i=s.length(); }
            else if(ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u' ) {
                // Does nothing.
            }else if(ch != ' ') {
                out+=ch;
            }
        }

        // If the output string is less than 3 characters, add the
        // remaining letters to the output string.
        if(out.length() < 3) {
            for (int i=0 ; i<s.length(); i++){
                char ch = s.charAt(i);
                if(out.length() >= 3) { i=s.length(); }
                else if(ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u' ) {
                    out+=ch;
                }
            }
        }

        return out;
    }
}