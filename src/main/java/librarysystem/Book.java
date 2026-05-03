package librarysystem;

public class Book {

    private int numBookId;
    private String strTitle;
    private String strAuthor;
    private String strCategory;
    private String strAvailabilityStatus;

    public Book() {
    }

    public Book(int numBookId, String strTitle, String strAuthor, String strCategory, String strAvailabilityStatus) {
        this.numBookId = numBookId;
        this.strTitle = strTitle;
        this.strAuthor = strAuthor;
        this.strCategory = strCategory;
        this.strAvailabilityStatus = strAvailabilityStatus;
    }

    public int getNumBookId() {
        return numBookId;
    }

    public void setNumBookId(int numBookId) {
        this.numBookId = numBookId;
    }

    public String getStrTitle() {
        return strTitle;
    }

    public void setStrTitle(String strTitle) {
        this.strTitle = strTitle;
    }

    public String getStrAuthor() {
        return strAuthor;
    }

    public void setStrAuthor(String strAuthor) {
        this.strAuthor = strAuthor;
    }

    public String getStrCategory() {
        return strCategory;
    }

    public void setStrCategory(String strCategory) {
        this.strCategory = strCategory;
    }

    public String getStrAvailabilityStatus() {
        return strAvailabilityStatus;
    }

    public void setStrAvailabilityStatus(String strAvailabilityStatus) {
        this.strAvailabilityStatus = strAvailabilityStatus;
    }

    @Override
    public String toString() {
        String strLines = "";
        strLines = strLines + "BOOK ID: " + numBookId + System.lineSeparator();
        strLines = strLines + "TITLE: " + strTitle + System.lineSeparator();
        strLines = strLines + "AUTHOR: " + strAuthor + System.lineSeparator();
        strLines = strLines + "CATEGORY: " + strCategory + System.lineSeparator();
        strLines = strLines + "STATUS: " + strAvailabilityStatus + System.lineSeparator();
        return strLines;
    }
}
