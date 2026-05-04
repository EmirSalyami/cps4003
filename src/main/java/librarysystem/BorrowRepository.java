package librarysystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowRepository {

    public void insertBorrow(int numRecordId, int numBookId, int numMemberId, String strBorrow, String strDue,
        String strStatus)
        throws Exception {
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO borrow_records (record_id, book_id, member_id, borrow_date, due_date, return_status) "
                    + "VALUES (?,?,?,?,?,?)");
            ps.setInt(1, numRecordId);
            ps.setInt(2, numBookId);
            ps.setInt(3, numMemberId);
            ps.setString(4, strBorrow);
            ps.setString(5, strDue);
            ps.setString(6, strStatus);
            ps.executeUpdate();
            ps.close();
            conn.close();
        }
    }

    public List<BorrowRecord> findAllBorrows() throws Exception {
        List<BorrowRecord> lst = new ArrayList<>();
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT record_id, book_id, member_id, borrow_date, due_date, return_status FROM borrow_records "
                    + "ORDER BY record_id");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BorrowRecord r = mapRow(rs);
                lst.add(r);
            }
            rs.close();
            ps.close();
            conn.close();
        }
        return lst;
    }

    public List<BorrowRecord> searchByMemberId(int numMemberId) throws Exception {
        List<BorrowRecord> lst = new ArrayList<>();
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT record_id, book_id, member_id, borrow_date, due_date, return_status FROM borrow_records "
                    + "WHERE member_id = ? ORDER BY record_id");
            ps.setInt(1, numMemberId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BorrowRecord r = mapRow(rs);
                lst.add(r);
            }
            rs.close();
            ps.close();
            conn.close();
        }
        return lst;
    }

    public List<BorrowRecord> searchByBookId(int numBookId) throws Exception {
        List<BorrowRecord> lst = new ArrayList<>();
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT record_id, book_id, member_id, borrow_date, due_date, return_status FROM borrow_records "
                    + "WHERE book_id = ? ORDER BY record_id");
            ps.setInt(1, numBookId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BorrowRecord r = mapRow(rs);
                lst.add(r);
            }
            rs.close();
            ps.close();
            conn.close();
        }
        return lst;
    }

    public void updateBorrowStatus(int numRecordId, String strStatus) throws Exception {
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement("UPDATE borrow_records SET return_status = ? WHERE record_id = ?");
            ps.setString(1, strStatus);
            ps.setInt(2, numRecordId);
            ps.executeUpdate();
            ps.close();
            conn.close();
        }
    }

    public void deleteBorrow(int numRecordId) throws Exception {
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM borrow_records WHERE record_id = ?");
            ps.setInt(1, numRecordId);
            ps.executeUpdate();
            ps.close();
            conn.close();
        }
    }

    public List<BorrowRecord> filterByBorrowDateRange(LocalDate fromDate, LocalDate toDate, boolean sortAsc)
        throws Exception {
        List<BorrowRecord> lst = new ArrayList<>();
        String strOrder = "";
        if (sortAsc) {
            strOrder = " ORDER BY borrow_date ASC ";
        } else {
            strOrder = " ORDER BY borrow_date DESC ";
        }
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT record_id, book_id, member_id, borrow_date, due_date, return_status FROM borrow_records "
                    + "WHERE borrow_date BETWEEN ? AND ? " + strOrder);
            ps.setString(1, fromDate.toString());
            ps.setString(2, toDate.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BorrowRecord r = mapRow(rs);
                lst.add(r);
            }
            rs.close();
            ps.close();
            conn.close();
        }
        return lst;
    }

    public List<BorrowRecord> findOverdueRecords() throws Exception {
        List<BorrowRecord> lst = new ArrayList<>();
        LocalDate today = LocalDate.now();
        String strToday = today.toString();
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT record_id, book_id, member_id, borrow_date, due_date, return_status FROM borrow_records "
                    + "WHERE (UPPER(return_status) = 'BORROWED' AND due_date < ?) "
                    + "OR UPPER(return_status) = 'OVERDUE' "
                    + "ORDER BY due_date ASC");
            ps.setString(1, strToday);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BorrowRecord r = mapRow(rs);
                lst.add(r);
            }
            rs.close();
            ps.close();
            conn.close();
        }
        return lst;
    }

    public BorrowRecord findByRecordId(int numId) throws Exception {
        BorrowRecord r = null;
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT record_id, book_id, member_id, borrow_date, due_date, return_status FROM borrow_records "
                    + "WHERE record_id = ?");
            ps.setInt(1, numId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                r = mapRow(rs);
            }
            rs.close();
            ps.close();
            conn.close();
        }
        return r;
    }

    private BorrowRecord mapRow(ResultSet rs) throws Exception {
        BorrowRecord r = new BorrowRecord();
        r.setNumRecordId(rs.getInt("record_id"));
        r.setNumBookId(rs.getInt("book_id"));
        r.setNumMemberId(rs.getInt("member_id"));
        r.setStrBorrowDate(rs.getString("borrow_date"));
        r.setStrDueDate(rs.getString("due_date"));
        r.setStrReturnStatus(rs.getString("return_status"));
        return r;
    }
}
