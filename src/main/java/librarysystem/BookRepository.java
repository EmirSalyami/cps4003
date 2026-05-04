package librarysystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BookRepository {

    public void insertBook(int numBookId, String strTitle, String strAuthor, String strCategory, String strStatus)
        throws Exception {
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO books (book_id, title, author, category, availability_status) VALUES (?,?,?,?,?)");
            ps.setInt(1, numBookId);
            ps.setString(2, strTitle);
            ps.setString(3, strAuthor);
            ps.setString(4, strCategory);
            ps.setString(5, strStatus);
            ps.executeUpdate();
            ps.close();
            conn.close();
        }
    }

    public List<Book> findAllBooks() throws Exception {
        List<Book> lst = new ArrayList<>();
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT book_id, title, author, category, availability_status FROM books ORDER BY book_id");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Book b = new Book();
                b.setNumBookId(rs.getInt("book_id"));
                b.setStrTitle(rs.getString("title"));
                b.setStrAuthor(rs.getString("author"));
                b.setStrCategory(rs.getString("category"));
                b.setStrAvailabilityStatus(rs.getString("availability_status"));
                lst.add(b);
            }
            rs.close();
            ps.close();
            conn.close();
        }
        return lst;
    }

    public Book findBookById(int numId) throws Exception {
        Book b = null;
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT book_id, title, author, category, availability_status FROM books WHERE book_id = ?");
            ps.setInt(1, numId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                b = new Book();
                b.setNumBookId(rs.getInt("book_id"));
                b.setStrTitle(rs.getString("title"));
                b.setStrAuthor(rs.getString("author"));
                b.setStrCategory(rs.getString("category"));
                b.setStrAvailabilityStatus(rs.getString("availability_status"));
            }
            rs.close();
            ps.close();
            conn.close();
        }
        return b;
    }

    public List<Book> searchBooksByTitle(String strPart) throws Exception {
        List<Book> lst = new ArrayList<>();
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT book_id, title, author, category, availability_status FROM books "
                    + "WHERE title LIKE ? ORDER BY book_id");
            ps.setString(1, "%" + strPart + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Book b = new Book();
                b.setNumBookId(rs.getInt("book_id"));
                b.setStrTitle(rs.getString("title"));
                b.setStrAuthor(rs.getString("author"));
                b.setStrCategory(rs.getString("category"));
                b.setStrAvailabilityStatus(rs.getString("availability_status"));
                lst.add(b);
            }
            rs.close();
            ps.close();
            conn.close();
        }
        return lst;
    }

    public List<Book> searchBooksByAuthor(String strPart) throws Exception {
        List<Book> lst = new ArrayList<>();
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT book_id, title, author, category, availability_status FROM books "
                    + "WHERE author LIKE ? ORDER BY book_id");
            ps.setString(1, "%" + strPart + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Book b = new Book();
                b.setNumBookId(rs.getInt("book_id"));
                b.setStrTitle(rs.getString("title"));
                b.setStrAuthor(rs.getString("author"));
                b.setStrCategory(rs.getString("category"));
                b.setStrAvailabilityStatus(rs.getString("availability_status"));
                lst.add(b);
            }
            rs.close();
            ps.close();
            conn.close();
        }
        return lst;
    }

    public void updateBook(int numBookId, String strTitle, String strAuthor, String strCategory, String strStatus)
        throws Exception {
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE books SET title = ?, author = ?, category = ?, availability_status = ? WHERE book_id = ?");
            ps.setString(1, strTitle);
            ps.setString(2, strAuthor);
            ps.setString(3, strCategory);
            ps.setString(4, strStatus);
            ps.setInt(5, numBookId);
            ps.executeUpdate();
            ps.close();
            conn.close();
        }
    }

    public void deleteBook(int numBookId) throws Exception {
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE book_id = ?");
            ps.setInt(1, numBookId);
            ps.executeUpdate();
            ps.close();
            conn.close();
        }
    }

    public List<Book> searchBooksAdvanced(String strTitlePart, String strAuthorPart, String strCategoryPart,
        boolean sortAsc)
        throws Exception {
        List<Book> lst = new ArrayList<>();

        String sql = "";
        sql = sql + "SELECT book_id, title, author, category, availability_status FROM books WHERE 1=1 ";

        List<String> lstParams = new ArrayList<>();

        if (strTitlePart != null) {
            if (strTitlePart.trim().length() > 0) {
                sql = sql + " AND title LIKE ? ";
                String pat = "%" + strTitlePart.trim() + "%";
                lstParams.add(pat);
            }
        }

        if (strAuthorPart != null) {
            if (strAuthorPart.trim().length() > 0) {
                sql = sql + " AND author LIKE ? ";
                String pat = "%" + strAuthorPart.trim() + "%";
                lstParams.add(pat);
            }
        }

        if (strCategoryPart != null) {
            if (strCategoryPart.trim().length() > 0) {
                sql = sql + " AND category LIKE ? ";
                String pat = "%" + strCategoryPart.trim() + "%";
                lstParams.add(pat);
            }
        }

        if (sortAsc) {
            sql = sql + " ORDER BY title ASC ";
        } else {
            sql = sql + " ORDER BY title DESC ";
        }

        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            int numIndex = 1;
            int pos = 0;
            while (pos < lstParams.size()) {
                String val = lstParams.get(pos);
                ps.setString(numIndex, val);
                numIndex = numIndex + 1;
                pos = pos + 1;
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Book b = new Book();
                b.setNumBookId(rs.getInt("book_id"));
                b.setStrTitle(rs.getString("title"));
                b.setStrAuthor(rs.getString("author"));
                b.setStrCategory(rs.getString("category"));
                b.setStrAvailabilityStatus(rs.getString("availability_status"));
                lst.add(b);
            }
            rs.close();
            ps.close();
            conn.close();
        }
        return lst;
    }

    public void updateAvailabilityOnly(int numBookId, String strStatus) throws Exception {
        synchronized (DatabaseHelper.DB_LOCK) {
            Connection conn = DatabaseHelper.openConnection();
            PreparedStatement ps = conn.prepareStatement("UPDATE books SET availability_status = ? WHERE book_id = ?");
            ps.setString(1, strStatus);
            ps.setInt(2, numBookId);
            ps.executeUpdate();
            ps.close();
            conn.close();
        }
    }
}
