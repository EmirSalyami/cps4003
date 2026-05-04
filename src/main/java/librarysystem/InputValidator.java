package librarysystem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class InputValidator {

    private static final Pattern EMAIL_STANDARD =
        Pattern.compile("^[\\w%+.-]+@[A-Za-z0-9][A-Za-z0-9.-]*\\.[A-Za-z]{2,}$");

    private InputValidator() {
    }

    public static boolean isBookIdNumeric(String strId) {
        if (strId == null) {
            return false;
        }
        strId = strId.trim();
        if (strId.length() == 0) {
            return false;
        }
        try {
            Integer.parseInt(strId);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static boolean isEmailOk(String strEmail) {
        if (strEmail == null) {
            return false;
        }
        strEmail = strEmail.trim();
        if (strEmail.length() == 0) {
            return false;
        }
        boolean matches = EMAIL_STANDARD.matcher(strEmail).matches();
        return matches;
    }

    public static boolean isTitleNotEmpty(String strTitle) {
        if (strTitle == null) {
            return false;
        }
        strTitle = strTitle.trim();
        if (strTitle.length() == 0) {
            return false;
        }
        return true;
    }

    public static LocalDate parseBorrowDate(String strDate) throws DateTimeParseException {
        String trimmed = strDate.trim();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate d = LocalDate.parse(trimmed, fmt);
        return d;
    }

    public static boolean isDueAfterBorrow(LocalDate borrow, LocalDate due) {
        if (due == null) {
            return false;
        }
        if (borrow == null) {
            return false;
        }
        boolean after = due.isAfter(borrow);
        return after;
    }

    public static boolean isStatusOk(String strStatus) {
        if (strStatus == null) {
            return false;
        }
        strStatus = strStatus.trim();
        if (strStatus.equalsIgnoreCase("Returned")) {
            return true;
        }
        if (strStatus.equalsIgnoreCase("Overdue")) {
            return true;
        }
        if (strStatus.equalsIgnoreCase("Borrowed")) {
            return true;
        }
        return false;
    }
}
