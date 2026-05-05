package librarysystem;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public final class ConsoleApp {

    private ConsoleApp() {
    }

    public static void runConsole(Scanner scan) {
        int numPick = 0;
        BookRepository repoBooks = new BookRepository();
        MemberRepository repoMembers = new MemberRepository();
        BorrowRepository repoBorrow = new BorrowRepository();

        while (true) {
            System.out.println("");
            System.out.println("===== MAIN MENU =====");
            System.out.println("1 Manage Books");
            System.out.println("2 Manage Members");
            System.out.println("3 Manage Borrowing Records");
            System.out.println("4 Search Records");
            System.out.println("5 Exit System");

            numPick = 0;
            System.out.print("Enter choice number: ");
            if (scan.hasNextInt()) {
                numPick = scan.nextInt();
            } else {
                scan.nextLine();
            }
            scan.nextLine();

            if (numPick == 5) {
                System.out.println("Good bye.");
                return;
            }
            if (numPick == 1) {
                menuBooks(scan, repoBooks);
            }
            if (numPick == 2) {
                menuMembers(scan, repoMembers);
            }
            if (numPick == 3) {
                menuBorrow(scan, repoBorrow);
            }
            if (numPick == 4) {
                menuSearch(scan, repoBooks, repoMembers, repoBorrow);
            }
        }
    }

    private static void menuBooks(Scanner scan, BookRepository repoBooks) {
        int numPick = 0;
        boolean keepGoing = true;
        keepGoing = true;
        while (keepGoing) {
            System.out.println("");
            System.out.println("--- Manage Books ---");
            System.out.println("1 Show all books");
            System.out.println("2 Search books by title");
            System.out.println("3 Search books by author");
            System.out.println("4 Search books by numeric ID");
            System.out.println("5 Add book");
            System.out.println("6 Update book");
            System.out.println("7 Delete book with confirmation");
            System.out.println("0 Back to main");

            numPick = readInt(scan);
            scan.nextLine();

            if (numPick == 0) {
                keepGoing = false;
                continue;
            }

            try {
                if (numPick == 1) {
                    List<Book> lst = repoBooks.findAllBooks();
                    int numI = 0;
                    numI = 0;
                    while (numI < lst.size()) {
                        System.out.println(lst.get(numI).toString());
                        numI = numI + 1;
                    }
                    System.out.println("Total rows: " + lst.size());
                }

                if (numPick == 2) {
                    System.out.print("Title part to search: ");
                    String strT = scan.nextLine();
                    List<Book> lst = repoBooks.searchBooksByTitle(strT);
                    printBooksList(lst);
                }

                if (numPick == 3) {
                    System.out.print("Author part to search: ");
                    String strA = scan.nextLine();
                    List<Book> lst = repoBooks.searchBooksByAuthor(strA);
                    printBooksList(lst);
                }

                if (numPick == 4) {
                    System.out.print("Book ID (numeric): ");
                    String strId = scan.nextLine();
                    if (!InputValidator.isBookIdNumeric(strId)) {
                        System.out.println("Error: Book ID must be numeric.");
                    } else {
                        int numId = Integer.parseInt(strId.trim());
                        Book b = repoBooks.findBookById(numId);
                        if (b == null) {
                            System.out.println("Book not found.");
                        } else {
                            System.out.println(b.toString());
                        }
                    }
                }

                if (numPick == 5) {
                    System.out.print("New Book ID (numeric): ");
                    String strId = scan.nextLine();
                    if (!InputValidator.isBookIdNumeric(strId)) {
                        System.out.println("Error: Book ID must be numeric.");
                    } else {
                        System.out.print("Title: ");
                        String strTitle = scan.nextLine();
                        System.out.print("Author: ");
                        String strAuthor = scan.nextLine();
                        System.out.print("Category: ");
                        String strCat = scan.nextLine();
                        System.out.print("Availability Status: ");
                        String strSt = scan.nextLine();
                        if (!InputValidator.isTitleNotEmpty(strTitle)) {
                            System.out.println("Error: Title must not be empty.");
                        } else if (!InputValidator.isTitleNotEmpty(strAuthor)) {
                            System.out.println("Error: Author must not be empty.");
                        } else if (!InputValidator.isTitleNotEmpty(strCat)) {
                            System.out.println("Error: Category must not be empty.");
                        } else if (!InputValidator.isTitleNotEmpty(strSt)) {
                            System.out.println("Error: Availability status must not be empty.");
                        } else {
                            int numId = Integer.parseInt(strId.trim());
                            repoBooks.insertBook(numId, strTitle.trim(), strAuthor.trim(), strCat.trim(), strSt.trim());
                            System.out.println("Book added successfully");
                        }
                    }
                }

                if (numPick == 6) {
                    System.out.print("Book ID to update (numeric): ");
                    String strId = scan.nextLine();
                    if (!InputValidator.isBookIdNumeric(strId)) {
                        System.out.println("Error: Book ID must be numeric.");
                    } else {
                        int numId = Integer.parseInt(strId.trim());
                        Book old = repoBooks.findBookById(numId);
                        if (old == null) {
                            System.out.println("Book not found.");
                        } else {
                            System.out.println("Current record:");
                            System.out.println(old.toString());
                            System.out.print("New title: ");
                            String strTitle = scan.nextLine();
                            System.out.print("New author: ");
                            String strAuthor = scan.nextLine();
                            System.out.print("New category: ");
                            String strCat = scan.nextLine();
                            System.out.print("New availability status: ");
                            String strSt = scan.nextLine();
                            if (!InputValidator.isTitleNotEmpty(strTitle)) {
                                System.out.println("Error: Title must not be empty.");
                            } else if (!InputValidator.isTitleNotEmpty(strAuthor)) {
                                System.out.println("Error: Author must not be empty.");
                            } else if (!InputValidator.isTitleNotEmpty(strCat)) {
                                System.out.println("Error: Category must not be empty.");
                            } else if (!InputValidator.isTitleNotEmpty(strSt)) {
                                System.out.println("Error: Availability status must not be empty.");
                            } else {
                                repoBooks.updateBook(numId, strTitle.trim(), strAuthor.trim(), strCat.trim(), strSt.trim());
                                System.out.println("Book updated successfully");
                            }
                        }
                    }
                }

                if (numPick == 7) {
                    System.out.print("Book ID to delete (numeric): ");
                    String strId = scan.nextLine();
                    if (!InputValidator.isBookIdNumeric(strId)) {
                        System.out.println("Error: Book ID must be numeric.");
                    } else {
                        int numId = Integer.parseInt(strId.trim());
                        Book old = repoBooks.findBookById(numId);
                        if (old == null) {
                            System.out.println("Book not found.");
                        } else {
                            System.out.println(old.toString());
                            System.out.print("Type YES to confirm delete: ");
                            String strYes = scan.nextLine();
                            if (strYes.trim().equalsIgnoreCase("YES")) {
                                repoBooks.deleteBook(numId);
                                System.out.println("Book deleted successfully");
                            } else {
                                System.out.println("Delete cancelled.");
                            }
                        }
                    }
                }

            } catch (Exception ex) {
                System.out.println("Database error or invalid input.");
                System.out.println(ex.getMessage());
            }
        }
    }

    private static void menuMembers(Scanner scan, MemberRepository repoMembers) {
        int numPick = 0;
        boolean keepGoing = true;
        keepGoing = true;
        while (keepGoing) {
            System.out.println("");
            System.out.println("--- Manage Members ---");
            System.out.println("1 Show all members");
            System.out.println("2 Search by name");
            System.out.println("3 Search by numeric ID");
            System.out.println("4 Add member");
            System.out.println("5 Update member");
            System.out.println("6 Delete member with confirmation");
            System.out.println("0 Back to main");

            numPick = readInt(scan);
            scan.nextLine();

            if (numPick == 0) {
                keepGoing = false;
                continue;
            }

            try {
                if (numPick == 1) {
                    List<Member> lst = repoMembers.findAllMembers();
                    int numI = 0;
                    numI = 0;
                    while (numI < lst.size()) {
                        System.out.println(lst.get(numI).toString());
                        numI = numI + 1;
                    }
                }

                if (numPick == 2) {
                    System.out.print("Name part to search: ");
                    String strN = scan.nextLine();
                    List<Member> lst = repoMembers.searchMembersByName(strN);
                    int numI = 0;
                    numI = 0;
                    while (numI < lst.size()) {
                        System.out.println(lst.get(numI).toString());
                        numI = numI + 1;
                    }
                }

                if (numPick == 3) {
                    System.out.print("Member ID (numeric): ");
                    String strId = scan.nextLine();
                    if (!InputValidator.isBookIdNumeric(strId)) {
                        System.out.println("Error: Member ID must be numeric.");
                    } else {
                        int numId = Integer.parseInt(strId.trim());
                        Member m = repoMembers.findMemberById(numId);
                        if (m == null) {
                            System.out.println("Member not found.");
                        } else {
                            System.out.println(m.toString());
                        }
                    }
                }

                if (numPick == 4) {
                    System.out.print("New Member ID (numeric): ");
                    String strId = scan.nextLine();
                    if (!InputValidator.isBookIdNumeric(strId)) {
                        System.out.println("Error: Member ID must be numeric.");
                    } else {
                        System.out.print("Name: ");
                        String strName = scan.nextLine();
                        System.out.print("Email: ");
                        String strEmail = scan.nextLine();
                        System.out.print("Member type: ");
                        String strType = scan.nextLine();
                        if (!InputValidator.isTitleNotEmpty(strName)) {
                            System.out.println("Error: Name must not be empty.");
                        } else if (!InputValidator.isEmailOk(strEmail)) {
                            System.out.println("Error: Email format is wrong.");
                        } else if (!InputValidator.isTitleNotEmpty(strType)) {
                            System.out.println("Error: Member type must not be empty.");
                        } else {
                            int numId = Integer.parseInt(strId.trim());
                            repoMembers.insertMember(numId, strName.trim(), strEmail.trim(), strType.trim());
                            System.out.println("Member registered successfully");
                        }
                    }
                }

                if (numPick == 5) {
                    System.out.print("Member ID (numeric): ");
                    String strId = scan.nextLine();
                    if (!InputValidator.isBookIdNumeric(strId)) {
                        System.out.println("Error: Member ID must be numeric.");
                    } else {
                        int numId = Integer.parseInt(strId.trim());
                        Member old = repoMembers.findMemberById(numId);
                        if (old == null) {
                            System.out.println("Member not found.");
                        } else {
                            System.out.print("New name: ");
                            String strName = scan.nextLine();
                            System.out.print("New email: ");
                            String strEmail = scan.nextLine();
                            System.out.print("New member type: ");
                            String strType = scan.nextLine();
                            if (!InputValidator.isTitleNotEmpty(strName)) {
                                System.out.println("Error: Name must not be empty.");
                            } else if (!InputValidator.isEmailOk(strEmail)) {
                                System.out.println("Error: Email format is wrong.");
                            } else if (!InputValidator.isTitleNotEmpty(strType)) {
                                System.out.println("Error: Member type must not be empty.");
                            } else {
                                repoMembers.updateMember(numId, strName.trim(), strEmail.trim(), strType.trim());
                                System.out.println("Member updated successfully");
                            }
                        }
                    }
                }

                if (numPick == 6) {
                    System.out.print("Member ID to delete (numeric): ");
                    String strId = scan.nextLine();
                    if (!InputValidator.isBookIdNumeric(strId)) {
                        System.out.println("Error: Member ID must be numeric.");
                    } else {
                        int numId = Integer.parseInt(strId.trim());
                        Member old = repoMembers.findMemberById(numId);
                        if (old == null) {
                            System.out.println("Member not found.");
                        } else {
                            System.out.println(old.toString());
                            System.out.print("Type YES to confirm delete: ");
                            String strYes = scan.nextLine();
                            if (strYes.trim().equalsIgnoreCase("YES")) {
                                repoMembers.deleteMember(numId);
                                System.out.println("Member removed successfully");
                            } else {
                                System.out.println("Delete cancelled.");
                            }
                        }
                    }
                }

            } catch (Exception ex) {
                System.out.println("Database error or invalid query.");
                System.out.println(ex.getMessage());
            }
        }
    }

    private static void menuBorrow(Scanner scan, BorrowRepository repoBorrow) {
        int numPick = 0;
        boolean keepGoing = true;
        keepGoing = true;
        while (keepGoing) {
            System.out.println("");
            System.out.println("--- Manage Borrowing Records ---");
            System.out.println("1 Show borrowing history");
            System.out.println("2 Search by member ID");
            System.out.println("3 Search by book ID");
            System.out.println("4 Add borrow record");
            System.out.println("5 Update borrowing status");
            System.out.println("6 Delete borrow record");
            System.out.println("7 Show overdue borrowed items");
            System.out.println("0 Back to main");

            numPick = readInt(scan);
            scan.nextLine();

            if (numPick == 0) {
                keepGoing = false;
                continue;
            }

            try {
                if (numPick == 1) {
                    List<BorrowRecord> lst = repoBorrow.findAllBorrows();
                    printBorrowList(lst);
                }

                if (numPick == 2) {
                    System.out.print("Member ID (numeric): ");
                    String strId = scan.nextLine();
                    if (!InputValidator.isBookIdNumeric(strId)) {
                        System.out.println("Error: Must be numeric.");
                    } else {
                        int numId = Integer.parseInt(strId.trim());
                        List<BorrowRecord> lst = repoBorrow.searchByMemberId(numId);
                        printBorrowList(lst);
                    }
                }

                if (numPick == 3) {
                    System.out.print("Book ID (numeric): ");
                    String strId = scan.nextLine();
                    if (!InputValidator.isBookIdNumeric(strId)) {
                        System.out.println("Error: Must be numeric.");
                    } else {
                        int numId = Integer.parseInt(strId.trim());
                        List<BorrowRecord> lst = repoBorrow.searchByBookId(numId);
                        printBorrowList(lst);
                    }
                }

                if (numPick == 4) {
                    System.out.print("Record ID (numeric): ");
                    String strRid = scan.nextLine();
                    System.out.print("Book ID (numeric): ");
                    String strBid = scan.nextLine();
                    System.out.print("Member ID (numeric): ");
                    String strMid = scan.nextLine();
                    System.out.print("Borrow date YYYY-MM-DD: ");
                    String strBdate = scan.nextLine();
                    System.out.print("Due date YYYY-MM-DD: ");
                    String strDdate = scan.nextLine();
                    System.out.print("Status (Borrowed/Returned/Overdue): ");
                    String strStatus = scan.nextLine();
                    LocalDate bd = null;
                    LocalDate dd = null;
                    bd = InputValidator.parseBorrowDate(strBdate);
                    dd = InputValidator.parseBorrowDate(strDdate);
                    boolean okDates = InputValidator.isDueAfterBorrow(bd, dd);
                    if (!InputValidator.isBookIdNumeric(strRid) || !InputValidator.isBookIdNumeric(strBid)
                        || !InputValidator.isBookIdNumeric(strMid)) {
                        System.out.println("Error: IDs must be numeric.");
                    } else if (!okDates) {
                        System.out.println("Error: Due date must be after borrow date.");
                    } else if (!InputValidator.isStatusOk(strStatus)) {
                        System.out.println("Error: Wrong status.");
                    } else {
                        int numR = Integer.parseInt(strRid.trim());
                        int numB = Integer.parseInt(strBid.trim());
                        int numM = Integer.parseInt(strMid.trim());
                        repoBorrow.insertBorrow(numR, numB, numM, bd.toString(), dd.toString(), strStatus.trim());
                        System.out.println("Borrow transaction saved.");
                    }
                }

                if (numPick == 5) {
                    System.out.print("Record ID (numeric): ");
                    String strRid = scan.nextLine();
                    System.out.print("New status (Borrowed/Returned/Overdue): ");
                    String strStatus = scan.nextLine();
                    if (!InputValidator.isBookIdNumeric(strRid)) {
                        System.out.println("Error: Record ID must be numeric.");
                    } else if (!InputValidator.isStatusOk(strStatus)) {
                        System.out.println("Error: Wrong status.");
                    } else {
                        int numR = Integer.parseInt(strRid.trim());
                        repoBorrow.updateBorrowStatus(numR, strStatus.trim());
                        System.out.println("Borrow record updated successfully");
                    }
                }

                if (numPick == 6) {
                    System.out.print("Record ID to delete (numeric): ");
                    String strRid = scan.nextLine();
                    System.out.print("Type YES to confirm delete: ");
                    String strYes = scan.nextLine();
                    if (!InputValidator.isBookIdNumeric(strRid)) {
                        System.out.println("Error: Record ID must be numeric.");
                    } else if (strYes.trim().equalsIgnoreCase("YES")) {
                        int numR = Integer.parseInt(strRid.trim());
                        repoBorrow.deleteBorrow(numR);
                        System.out.println("Borrow row removed.");
                    } else {
                        System.out.println("Delete cancelled.");
                    }
                }

                if (numPick == 7) {
                    List<BorrowRecord> lst = repoBorrow.findOverdueRecords();
                    System.out.println("Overdue list (borrowed past due date):");
                    printBorrowList(lst);
                }

            } catch (java.time.format.DateTimeParseException exDate) {
                System.out.println("Error: Borrow date must be valid YYYY-MM-DD.");
            } catch (Exception ex) {
                System.out.println("Database error.");
                System.out.println(ex.getMessage());
            }
        }
    }

    private static void menuSearch(Scanner scan, BookRepository repoBooks, MemberRepository repoMembers,
        BorrowRepository repoBorrow) {
        int numPick = 0;
        System.out.println("");
        System.out.println("--- Search Records ---");
        System.out.println("1 Search books (optional: title / author / category—leave any line empty to skip)");
        System.out.println("2 Borrow records by borrow date range");
        System.out.println("3 Overdue borrowing rows");
        System.out.println("0 Back");

        numPick = readInt(scan);
        scan.nextLine();

        try {
            if (numPick == 1) {
                System.out.print("Title keyword (leave empty to skip): ");
                String strT = scan.nextLine();
                System.out.print("Author keyword (leave empty to skip): ");
                String strA = scan.nextLine();
                System.out.print("Category keyword (leave empty to skip): ");
                String strC = scan.nextLine();
                System.out.print("Sort ascending by title? 1=yes 0=no: ");
                int numAsc = readInt(scan);
                scan.nextLine();
                boolean sortAsc = numAsc != 0;
                List<Book> lst = repoBooks.searchBooksAdvanced(strT, strA, strC, sortAsc);
                printBooksList(lst);
            }

            if (numPick == 2) {
                System.out.print("From date YYYY-MM-DD: ");
                String strFrom = scan.nextLine();
                System.out.print("To date YYYY-MM-DD: ");
                String strTo = scan.nextLine();
                LocalDate d1 = InputValidator.parseBorrowDate(strFrom);
                LocalDate d2 = InputValidator.parseBorrowDate(strTo);
                System.out.print("Sort borrow date ascending? 1=yes 0=no: ");
                int numAsc = readInt(scan);
                scan.nextLine();
                boolean sortAsc = numAsc != 0;
                List<BorrowRecord> lst = repoBorrow.filterByBorrowDateRange(d1, d2, sortAsc);
                printBorrowList(lst);
            }

            if (numPick == 3) {
                List<BorrowRecord> lst = repoBorrow.findOverdueRecords();
                printBorrowList(lst);
            }

        } catch (Exception ex) {
            System.out.println("Search failed.");
            System.out.println(ex.getMessage());
        }
    }

    private static void printBooksList(List<Book> lst) {
        int numI = 0;
        numI = 0;
        while (numI < lst.size()) {
            System.out.println(lst.get(numI).toString());
            numI = numI + 1;
        }
        System.out.println("Matches: " + lst.size());
    }

    private static void printBorrowList(List<BorrowRecord> lst) {
        int numI = 0;
        numI = 0;
        while (numI < lst.size()) {
            System.out.println(lst.get(numI).toString());
            numI = numI + 1;
        }
        System.out.println("Matches: " + lst.size());
    }

    private static int readInt(Scanner scan) {
        int num = 0;
        System.out.print("Enter choice number: ");
        if (scan.hasNextInt()) {
            num = scan.nextInt();
        } else {
            scan.nextLine();
            num = -1;
        }
        return num;
    }
}
