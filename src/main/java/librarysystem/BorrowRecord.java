package librarysystem;

public class BorrowRecord {

    private int numRecordId;
    private int numBookId;
    private int numMemberId;
    private String strBorrowDate;
    private String strDueDate;
    private String strReturnStatus;

    public BorrowRecord() {
    }

    public BorrowRecord(int numRecordId, int numBookId, int numMemberId, String strBorrowDate, String strDueDate,
        String strReturnStatus) {
        this.numRecordId = numRecordId;
        this.numBookId = numBookId;
        this.numMemberId = numMemberId;
        this.strBorrowDate = strBorrowDate;
        this.strDueDate = strDueDate;
        this.strReturnStatus = strReturnStatus;
    }

    public int getNumRecordId() {
        return numRecordId;
    }

    public void setNumRecordId(int numRecordId) {
        this.numRecordId = numRecordId;
    }

    public int getNumBookId() {
        return numBookId;
    }

    public void setNumBookId(int numBookId) {
        this.numBookId = numBookId;
    }

    public int getNumMemberId() {
        return numMemberId;
    }

    public void setNumMemberId(int numMemberId) {
        this.numMemberId = numMemberId;
    }

    public String getStrBorrowDate() {
        return strBorrowDate;
    }

    public void setStrBorrowDate(String strBorrowDate) {
        this.strBorrowDate = strBorrowDate;
    }

    public String getStrDueDate() {
        return strDueDate;
    }

    public void setStrDueDate(String strDueDate) {
        this.strDueDate = strDueDate;
    }

    public String getStrReturnStatus() {
        return strReturnStatus;
    }

    public void setStrReturnStatus(String strReturnStatus) {
        this.strReturnStatus = strReturnStatus;
    }

    @Override
    public String toString() {
        String strLines = "";
        strLines = strLines + "RECORD ID: " + numRecordId + System.lineSeparator();
        strLines = strLines + "BOOK ID: " + numBookId + System.lineSeparator();
        strLines = strLines + "MEMBER ID: " + numMemberId + System.lineSeparator();
        strLines = strLines + "BORROW DATE: " + strBorrowDate + System.lineSeparator();
        strLines = strLines + "DUE DATE: " + strDueDate + System.lineSeparator();
        strLines = strLines + "STATUS: " + strReturnStatus + System.lineSeparator();
        return strLines;
    }
}
