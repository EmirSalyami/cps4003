package librarysystem;

public class Member {

    private int numMemberId;
    private String strMemberName;
    private String strEmail;
    private String strMembershipType;

    public Member() {
    }

    public Member(int numMemberId, String strMemberName, String strEmail, String strMembershipType) {
        this.numMemberId = numMemberId;
        this.strMemberName = strMemberName;
        this.strEmail = strEmail;
        this.strMembershipType = strMembershipType;
    }

    public int getNumMemberId() {
        return numMemberId;
    }

    public void setNumMemberId(int numMemberId) {
        this.numMemberId = numMemberId;
    }

    public String getStrMemberName() {
        return strMemberName;
    }

    public void setStrMemberName(String strMemberName) {
        this.strMemberName = strMemberName;
    }

    public String getStrEmail() {
        return strEmail;
    }

    public void setStrEmail(String strEmail) {
        this.strEmail = strEmail;
    }

    public String getStrMembershipType() {
        return strMembershipType;
    }

    public void setStrMembershipType(String strMembershipType) {
        this.strMembershipType = strMembershipType;
    }

    @Override
    public String toString() {
        String strLines = "";
        strLines = strLines + "MEMBER ID: " + numMemberId + System.lineSeparator();
        strLines = strLines + "MEMBER NAME: " + strMemberName + System.lineSeparator();
        strLines = strLines + "EMAIL: " + strEmail + System.lineSeparator();
        strLines = strLines + "MEMBER TYPE: " + strMembershipType + System.lineSeparator();
        return strLines;
    }
}
