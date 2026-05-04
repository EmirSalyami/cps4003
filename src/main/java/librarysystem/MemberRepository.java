package librarysystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MemberRepository {

    public void insertMember(int numMemberId, String strName, String strEmail, String strType) throws Exception {
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO members (member_id, member_name, email, membership_type) VALUES (?,?,?,?)");
            ps.setInt(1, numMemberId);
            ps.setString(2, strName);
            ps.setString(3, strEmail);
            ps.setString(4, strType);
            ps.executeUpdate();
            ps.close();
            conn.close();
        }
    }

    public List<Member> findAllMembers() throws Exception {
        List<Member> lst = new ArrayList<>();
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT member_id, member_name, email, membership_type FROM members ORDER BY member_id");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Member m = new Member();
                m.setNumMemberId(rs.getInt("member_id"));
                m.setStrMemberName(rs.getString("member_name"));
                m.setStrEmail(rs.getString("email"));
                m.setStrMembershipType(rs.getString("membership_type"));
                lst.add(m);
            }
            rs.close();
            ps.close();
            conn.close();
        }
        return lst;
    }

    public Member findMemberById(int numId) throws Exception {
        Member m = null;
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps =
                conn.prepareStatement("SELECT member_id, member_name, email, membership_type FROM members WHERE member_id = ?");
            ps.setInt(1, numId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                m = new Member();
                m.setNumMemberId(rs.getInt("member_id"));
                m.setStrMemberName(rs.getString("member_name"));
                m.setStrEmail(rs.getString("email"));
                m.setStrMembershipType(rs.getString("membership_type"));
            }
            rs.close();
            ps.close();
            conn.close();
        }
        return m;
    }

    public List<Member> searchMembersByName(String strPart) throws Exception {
        List<Member> lst = new ArrayList<>();
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT member_id, member_name, email, membership_type FROM members "
                    + "WHERE member_name LIKE ? ORDER BY member_id");
            ps.setString(1, "%" + strPart + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Member m = new Member();
                m.setNumMemberId(rs.getInt("member_id"));
                m.setStrMemberName(rs.getString("member_name"));
                m.setStrEmail(rs.getString("email"));
                m.setStrMembershipType(rs.getString("membership_type"));
                lst.add(m);
            }
            rs.close();
            ps.close();
            conn.close();
        }
        return lst;
    }

    public void updateMember(int numMemberId, String strName, String strEmail, String strType) throws Exception {
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE members SET member_name = ?, email = ?, membership_type = ? WHERE member_id = ?");
            ps.setString(1, strName);
            ps.setString(2, strEmail);
            ps.setString(3, strType);
            ps.setInt(4, numMemberId);
            ps.executeUpdate();
            ps.close();
            conn.close();
        }
    }

    public void deleteMember(int numMemberId) throws Exception {
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM members WHERE member_id = ?");
            ps.setInt(1, numMemberId);
            ps.executeUpdate();
            ps.close();
            conn.close();
        }
    }

    public List<Member> searchMembersFilterByType(String strTypePart) throws Exception {
        List<Member> lst = new ArrayList<>();
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT member_id, member_name, email, membership_type FROM members "
                    + "WHERE membership_type LIKE ? ORDER BY member_name ASC");
            ps.setString(1, "%" + strTypePart + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Member m = new Member();
                m.setNumMemberId(rs.getInt("member_id"));
                m.setStrMemberName(rs.getString("member_name"));
                m.setStrEmail(rs.getString("email"));
                m.setStrMembershipType(rs.getString("membership_type"));
                lst.add(m);
            }
            rs.close();
            ps.close();
            conn.close();
        }
        return lst;
    }
}
