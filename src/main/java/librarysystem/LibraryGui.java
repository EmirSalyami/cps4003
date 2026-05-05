package librarysystem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class LibraryGui {

    private LibraryGui() {
    }

    public static void launchGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        try {
            DatabaseHelper.makeDatabaseReady();
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                "Database connection failed.\n" + ex.getMessage(), "Digital Library", JOptionPane.ERROR_MESSAGE));
            return;
        }
        SwingUtilities.invokeLater(() -> buildAndShowFrame());
    }

    private static void buildAndShowFrame() {
        final JFrame frame = new JFrame("St Mary Digital Library Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 650);

        BookRepository repoBooks = new BookRepository();
        MemberRepository repoMembers = new MemberRepository();
        BorrowRepository repoBorrow = new BorrowRepository();

        JPanel panelTop = new JPanel(new BorderLayout());
        JPanel panelDash = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblCounts = new JLabel("Loading dashboard...");
        JLabel lblThreadNote = new JLabel(" | Background loaders keep UI responsive");
        panelDash.add(lblCounts);
        panelDash.add(lblThreadNote);

        JProgressBar barMain = new JProgressBar();
        barMain.setStringPainted(true);
        hideDashboardProgress(barMain);
        panelTop.add(panelDash, BorderLayout.NORTH);
        panelTop.add(barMain, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();

        JPanel tabBooks = makeBooksTab(frame, repoBooks, barMain, lblCounts);
        JPanel tabMembers = makeMembersTab(frame, repoMembers, barMain, lblCounts);
        JPanel tabBorrow = makeBorrowTab(frame, repoBorrow, barMain, lblCounts);
        JPanel tabBatch = makeBatchConcurrencyTab(repoBooks, barMain);

        tabs.addTab("Books", tabBooks);
        tabs.addTab("Members", tabMembers);
        tabs.addTab("Borrowing Records", tabBorrow);
        tabs.addTab("Background batch demo", tabBatch);

        JMenuBar bar = new JMenuBar();
        JMenu menuNav = new JMenu("Navigation");
        menuNav.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuSelected(javax.swing.event.MenuEvent e) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent e) {
            }

            public void menuCanceled(javax.swing.event.MenuEvent e) {
            }
        });
        menuNav.add(new JMenuItem("Use tabs Books / Members / Borrowing"));
        bar.add(menuNav);
        frame.setJMenuBar(bar);

        frame.getContentPane().add(panelTop, BorderLayout.NORTH);
        frame.getContentPane().add(tabs, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        refreshDashboardCounts(repoBooks, repoMembers, repoBorrow, lblCounts);
    }

    private static JPanel makeBooksTab(JFrame owner, BookRepository repoBooks, JProgressBar barMain, JLabel lblDash) {
        JPanel panelOuter = new JPanel(new BorderLayout(5, 5));
        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnSearch = new JButton("Search");
        JButton btnLoadBg = new JButton("Load books in background");

        JTextField fldBookIdFilter = new JTextField(8);
        JComboBox<String> comboAsc = new JComboBox<>(new String[] {"Sort title A-Z", "Sort title Z-A"});

        String[] cols = new String[] {"Book ID", "Title", "Author", "Category", "Availability"};
        DefaultTableModel tblModelBooks = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(tblModelBooks);
        TableRowSorter<TableModel> sorterBooks = new TableRowSorter<>(tblModelBooks);
        table.setRowSorter(sorterBooks);
        styleTableHeaderBottomLine(table);

        panelBtns.add(btnRefresh);
        panelBtns.add(btnAdd);
        panelBtns.add(btnUpdate);
        panelBtns.add(btnDelete);
        panelBtns.add(btnSearch);
        panelBtns.add(btnLoadBg);
        panelBtns.add(new JLabel("Search book by id:"));
        panelBtns.add(fldBookIdFilter);
        panelBtns.add(comboAsc);

        panelOuter.add(panelBtns, BorderLayout.NORTH);
        panelOuter.add(new JScrollPane(table), BorderLayout.CENTER);

        btnRefresh.addActionListener(
            ev -> reloadBooksSimple(table, tblModelBooks, fldBookIdFilter, comboAsc, repoBooks));
        btnLoadBg.addActionListener(ev -> runSwingWorkerBooksLoad(table, repoBooks, tblModelBooks, fldBookIdFilter,
            comboAsc, barMain, lblDash));

        btnAdd.addActionListener(ev -> dialogAddBook(owner, repoBooks, () -> reloadBooksSimple(
            table, tblModelBooks, fldBookIdFilter, comboAsc, repoBooks)));

        btnUpdate.addActionListener(ev -> {
            int numRowView = table.getSelectedRow();
            if (numRowView < 0) {
                JOptionPane.showMessageDialog(owner, "Select a row.");
                return;
            }
            int numReal = table.convertRowIndexToModel(numRowView);
            int numId = Integer.parseInt(tblModelBooks.getValueAt(numReal, 0).toString());
            dialogEditBook(owner, repoBooks, numId, () -> reloadBooksSimple(
                table, tblModelBooks, fldBookIdFilter, comboAsc, repoBooks));
        });

        btnDelete.addActionListener(ev -> {
            int numRowView = table.getSelectedRow();
            if (numRowView < 0) {
                JOptionPane.showMessageDialog(owner, "Select a row.");
                return;
            }
            int numReal = table.convertRowIndexToModel(numRowView);
            int numId = Integer.parseInt(tblModelBooks.getValueAt(numReal, 0).toString());
            String strTitle = tblModelBooks.getValueAt(numReal, 1).toString();
            int numAns =
                JOptionPane.showConfirmDialog(owner, "Delete book ID " + numId + ": " + strTitle + " ?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (numAns != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                repoBooks.deleteBook(numId);
                JOptionPane.showMessageDialog(owner, "Book deleted successfully");
                reloadBooksSimple(table, tblModelBooks, fldBookIdFilter, comboAsc, repoBooks);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(owner, "Database error: " + ex.getMessage());
            }
        });

        btnSearch.addActionListener(ev -> {
            JTextField fldTitleKw = new JTextField(22);
            JTextField fldAuthorKw = new JTextField(22);
            JTextField fldCategoryKw = new JTextField(22);

            JPanel pnlGrid = new JPanel(new GridBagLayout());
            pnlGrid.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 0, 4, 8);
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.gridy = 0;
            gbc.gridx = 0;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            pnlGrid.add(new JLabel("Title:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pnlGrid.add(fldTitleKw, gbc);
            gbc.gridy = 1;
            gbc.gridx = 0;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            pnlGrid.add(new JLabel("Author:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pnlGrid.add(fldAuthorKw, gbc);
            gbc.gridy = 2;
            gbc.gridx = 0;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            pnlGrid.add(new JLabel("Category:"), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pnlGrid.add(fldCategoryKw, gbc);

            JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.TRAILING, 12, 8));
            pnlButtons.setBorder(BorderFactory.createEmptyBorder(0, 14, 10, 14));
            JButton btnOk = new JButton("OK");
            JButton btnCancel = new JButton("Cancel");
            pnlButtons.add(btnOk);
            pnlButtons.add(btnCancel);

            JDialog dlgSearch = new JDialog(owner, "Search books", true);
            dlgSearch.setLayout(new BorderLayout(0, 0));
            dlgSearch.add(pnlGrid, BorderLayout.CENTER);
            dlgSearch.add(pnlButtons, BorderLayout.SOUTH);

            final boolean[] arrOkClicked = new boolean[] {false};
            btnOk.addActionListener(evOk -> {
                arrOkClicked[0] = true;
                dlgSearch.dispose();
            });
            btnCancel.addActionListener(evCa -> dlgSearch.dispose());
            dlgSearch.getRootPane().setDefaultButton(btnOk);
            dlgSearch.pack();
            dlgSearch.setLocationRelativeTo(owner);
            dlgSearch.setVisible(true);

            if (!arrOkClicked[0]) {
                return;
            }
            boolean asc = comboAsc.getSelectedIndex() == 0;
            try {
                List<Book> lst =
                    repoBooks.searchBooksAdvanced(fldTitleKw.getText(), fldAuthorKw.getText(), fldCategoryKw.getText(),
                        asc);
                fillBooksModel(tblModelBooks, lst);
                applyBooksRowFilter(table, fldBookIdFilter);

                JOptionPane.showMessageDialog(owner, "Found " + lst.size() + " book(s).");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(owner, "Search failed: " + ex.getMessage());
            }
        });

        reloadBooksSimple(table, tblModelBooks, fldBookIdFilter, comboAsc, repoBooks);
        fldBookIdFilter.addActionListener(ev -> applyBooksRowFilter(table, fldBookIdFilter));

        comboAsc.addActionListener(ev -> {
            int numColTitle = 1;
            if (comboAsc.getSelectedIndex() == 0) {
                ArrayList<SortKey> lstKeys = new ArrayList<>();
                lstKeys.add(new SortKey(numColTitle, SortOrder.ASCENDING));
                sorterBooks.setSortKeys(lstKeys);
            } else {
                ArrayList<SortKey> lstKeys = new ArrayList<>();
                lstKeys.add(new SortKey(numColTitle, SortOrder.DESCENDING));
                sorterBooks.setSortKeys(lstKeys);
            }
            sorterBooks.sort();
        });

        return panelOuter;
    }

    private static void applyBooksRowFilter(JTable table, JTextField fldBookId) {

        TableRowSorter<?> sorter = null;
        sorter = (TableRowSorter<?>) table.getRowSorter();
        if (sorter == null) {
            return;
        }
        RowFilter<Object, Object> rf = null;
        rf = new RowFilter<Object, Object>() {
            @Override
            public boolean include(RowFilter.Entry<?, ?> entry) {
                String strWantId = fldBookId.getText().trim().toLowerCase();
                String strRowId = entry.getValue(0).toString().toLowerCase();
                boolean okId = true;
                if (strWantId.length() > 0) {
                    okId = strRowId.contains(strWantId);
                }
                return okId;
            }
        };
        sorter.setRowFilter(rf);
    }

    private static void runSwingWorkerBooksLoad(JTable table, BookRepository repoBooks, DefaultTableModel tblModelBooks,
        JTextField fldBookId, JComboBox<String> comboAsc,
        JProgressBar barMain, JLabel lblDash) {

        showDashboardProgress(barMain);
        barMain.setIndeterminate(false);
        barMain.setMaximum(100);
        barMain.setValue(0);

        SwingWorker<List<Book>, Integer> fixedWorker =
            new SwingWorker<List<Book>, Integer>() {
                @Override
                protected List<Book> doInBackground() throws Exception {
                    Thread.sleep(50);
                    publish(33);
                    List<Book> lst = repoBooks.findAllBooks();
                    publish(66);
                    Thread.sleep(50);
                    publish(100);
                    return lst;
                }

                @Override
                protected void process(java.util.List<Integer> chunks) {
                    int idx = chunks.size() - 1;
                    Integer num = chunks.get(idx);
                    barMain.setValue(num.intValue());
                    barMain.setString("Books async " + num + "%");
                }

                @Override
                protected void done() {
                    try {
                        List<Book> lst = get();
                        fillBooksModel(tblModelBooks, lst);
                        applyBooksRowFilter(table, fldBookId);
                        TableRowSorter<TableModel> sorterBooks =
                            (TableRowSorter<TableModel>) table.getRowSorter();
                        int numColTitle = 1;
                        if (comboAsc.getSelectedIndex() == 0) {
                            ArrayList<SortKey> lstKeys = new ArrayList<>();
                            lstKeys.add(new SortKey(numColTitle, SortOrder.ASCENDING));
                            sorterBooks.setSortKeys(lstKeys);
                        } else {
                            ArrayList<SortKey> lstKeys = new ArrayList<>();
                            lstKeys.add(new SortKey(numColTitle, SortOrder.DESCENDING));
                            sorterBooks.setSortKeys(lstKeys);
                        }
                        sorterBooks.sort();
                        JOptionPane.showMessageDialog(table, "Books loaded from database successfully");
                        lblDash.setText("Books async load OK");
                        hideDashboardProgress(barMain);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(table, "Background load failed: " + ex.getMessage());
                        hideDashboardProgress(barMain);
                    }
                }
            };

        fixedWorker.execute();
    }

    private static void reloadBooksSimple(JTable table, DefaultTableModel m, JTextField fldBookId,
        JComboBox<String> comboAsc, BookRepository repoBooks) {

        List<Book> lst = null;
        try {
            lst = repoBooks.findAllBooks();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(table, "Database connection failed.");
            JOptionPane.showMessageDialog(table, ex.getMessage());
            return;
        }
        fillBooksModel(m, lst);
        applyBooksRowFilter(table, fldBookId);

        TableRowSorter<TableModel> sorterBooks = null;
        sorterBooks = (TableRowSorter<TableModel>) table.getRowSorter();
        int numColTitle = 1;
        if (comboAsc.getSelectedIndex() == 0) {
            ArrayList<SortKey> lstKeys = new ArrayList<>();
            lstKeys.add(new SortKey(numColTitle, SortOrder.ASCENDING));
            sorterBooks.setSortKeys(lstKeys);
        } else {
            ArrayList<SortKey> lstKeys = new ArrayList<>();
            lstKeys.add(new SortKey(numColTitle, SortOrder.DESCENDING));
            sorterBooks.setSortKeys(lstKeys);
        }
        sorterBooks.sort();
    }

    private static void fillBooksModel(DefaultTableModel m, List<Book> lst) {
        m.setRowCount(0);
        int num = 0;
        num = 0;
        while (num < lst.size()) {
            Book b = lst.get(num);
            Object[] arr = new Object[5];
            arr[0] = Integer.valueOf(b.getNumBookId());
            arr[1] = b.getStrTitle();
            arr[2] = b.getStrAuthor();
            arr[3] = b.getStrCategory();
            arr[4] = b.getStrAvailabilityStatus();
            m.addRow(arr);
            num = num + 1;
        }
    }

    private static void dialogAddBook(JFrame owner, BookRepository repoBooks, Runnable onOk) {
        JTextField fid = new JTextField();
        JTextField ftitle = new JTextField();
        JTextField fauth = new JTextField();
        JTextField fcat = new JTextField();
        JTextField fstat = new JTextField();
        JPanel p = new JPanel(new GridLayout(5, 2, 8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(new JLabel("Book ID numeric"));
        p.add(fid);
        p.add(new JLabel("Title"));
        p.add(ftitle);
        p.add(new JLabel("Author"));
        p.add(fauth);
        p.add(new JLabel("Category"));
        p.add(fcat);
        p.add(new JLabel("Availability"));
        p.add(fstat);
        int ans = JOptionPane.showConfirmDialog(owner, p, "Add book", JOptionPane.OK_CANCEL_OPTION);
        if (ans != JOptionPane.OK_OPTION) {
            return;
        }
        if (!InputValidator.isBookIdNumeric(fid.getText())) {
            JOptionPane.showMessageDialog(owner, "Book ID must be numeric.");
            return;
        }
        if (!InputValidator.isTitleNotEmpty(ftitle.getText())) {
            JOptionPane.showMessageDialog(owner, "Title must not be empty.");
            return;
        }
        if (!InputValidator.isTitleNotEmpty(fauth.getText())) {
            JOptionPane.showMessageDialog(owner, "Author must not be empty.");
            return;
        }
        if (!InputValidator.isTitleNotEmpty(fcat.getText())) {
            JOptionPane.showMessageDialog(owner, "Category must not be empty.");
            return;
        }
        if (!InputValidator.isTitleNotEmpty(fstat.getText())) {
            JOptionPane.showMessageDialog(owner, "Availability must not be empty.");
            return;
        }
        try {
            repoBooks.insertBook(Integer.parseInt(fid.getText().trim()), ftitle.getText().trim(), fauth.getText().trim(),
                fcat.getText().trim(), fstat.getText().trim());
            JOptionPane.showMessageDialog(owner, "Book added successfully");
            if (onOk != null) {
                onOk.run();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Cannot save row: " + ex.getMessage());
        }
    }

    private static void dialogEditBook(JFrame owner, BookRepository repoBooks, int numId, Runnable onOk) {
        Book b = null;
        try {
            b = repoBooks.findBookById(numId);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage());
            return;
        }
        if (b == null) {
            JOptionPane.showMessageDialog(owner, "Missing book.");
            return;
        }
        JTextField ftitle = new JTextField(b.getStrTitle());
        JTextField fauth = new JTextField(b.getStrAuthor());
        JTextField fcat = new JTextField(b.getStrCategory());
        JTextField fstat = new JTextField(b.getStrAvailabilityStatus());
        JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
        p.add(new JLabel("Title"));
        p.add(ftitle);
        p.add(new JLabel("Author"));
        p.add(fauth);
        p.add(new JLabel("Category"));
        p.add(fcat);
        p.add(new JLabel("Availability"));
        p.add(fstat);
        int ans = JOptionPane.showConfirmDialog(owner, p, "Update book ID " + numId, JOptionPane.OK_CANCEL_OPTION);
        if (ans != JOptionPane.OK_OPTION) {
            return;
        }
        if (!InputValidator.isTitleNotEmpty(ftitle.getText())) {
            JOptionPane.showMessageDialog(owner, "Title must not be empty.");
            return;
        }
        if (!InputValidator.isTitleNotEmpty(fauth.getText())) {
            JOptionPane.showMessageDialog(owner, "Author must not be empty.");
            return;
        }
        if (!InputValidator.isTitleNotEmpty(fcat.getText())) {
            JOptionPane.showMessageDialog(owner, "Category must not be empty.");
            return;
        }
        if (!InputValidator.isTitleNotEmpty(fstat.getText())) {
            JOptionPane.showMessageDialog(owner, "Availability must not be empty.");
            return;
        }
        try {
            repoBooks.updateBook(numId, ftitle.getText().trim(), fauth.getText().trim(), fcat.getText().trim(),
                fstat.getText().trim());
            JOptionPane.showMessageDialog(owner, "Book updated successfully");
            if (onOk != null) {
                onOk.run();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Update failed: " + ex.getMessage());
        }
    }

    private static JPanel makeMembersTab(JFrame owner, MemberRepository repoMembers, JProgressBar barMain,
        JLabel lblDash) {
        JPanel panelOuter = new JPanel(new BorderLayout(5, 5));
        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnLoadBg = new JButton("Load members in background");
        JTextField fldMemberIdFilter = new JTextField(7);
        JTextField fldTypeFilter = new JTextField(10);

        String[] cols = new String[] {"Member ID", "Name", "Email", "Member type"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(m);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(m);
        table.setRowSorter(sorter);
        styleTableHeaderBottomLine(table);

        btnLoadBg.addActionListener(ev ->
            swingWorkerReloadMembers(table, repoMembers, m, fldMemberIdFilter, fldTypeFilter, barMain));

        btnRefresh.addActionListener(ev -> reloadMembersTable(table, m, fldMemberIdFilter, fldTypeFilter, repoMembers));
        btnAdd.addActionListener(ev ->
            dialogAddMember(owner, repoMembers,
                () -> reloadMembersTable(table, m, fldMemberIdFilter, fldTypeFilter, repoMembers)));
        btnUpdate.addActionListener(ev -> {
            int rv = table.getSelectedRow();
            if (rv < 0) {
                JOptionPane.showMessageDialog(owner, "Select row");
                return;
            }
            int mr = table.convertRowIndexToModel(rv);
            int id = Integer.parseInt(m.getValueAt(mr, 0).toString());
            dialogEditMember(owner, repoMembers, id,
                () -> reloadMembersTable(table, m, fldMemberIdFilter, fldTypeFilter, repoMembers));
        });
        btnDelete.addActionListener(ev -> {
            int rv = table.getSelectedRow();
            if (rv < 0) {
                return;
            }
            int mr = table.convertRowIndexToModel(rv);
            int id = Integer.parseInt(m.getValueAt(mr, 0).toString());
            int ans = JOptionPane.showConfirmDialog(owner, "Delete member " + id + "?", "Confirm",
                JOptionPane.YES_NO_OPTION);
            if (ans != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                repoMembers.deleteMember(id);
                JOptionPane.showMessageDialog(owner, "Member removed successfully");
                reloadMembersTable(table, m, fldMemberIdFilter, fldTypeFilter, repoMembers);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(owner, ex.getMessage());
            }
        });

        panelBtns.add(btnRefresh);
        panelBtns.add(btnAdd);
        panelBtns.add(btnUpdate);
        panelBtns.add(btnDelete);
        panelBtns.add(btnLoadBg);
        panelBtns.add(new JLabel("Search by ID:"));
        panelBtns.add(fldMemberIdFilter);
        panelBtns.add(new JLabel("Search by type:"));
        panelBtns.add(fldTypeFilter);

        fldMemberIdFilter.addActionListener(ev -> applyMemberFilters(table, fldMemberIdFilter, fldTypeFilter));
        fldTypeFilter.addActionListener(ev -> applyMemberFilters(table, fldMemberIdFilter, fldTypeFilter));

        panelOuter.add(panelBtns, BorderLayout.NORTH);
        panelOuter.add(new JScrollPane(table), BorderLayout.CENTER);
        reloadMembersTable(table, m, fldMemberIdFilter, fldTypeFilter, repoMembers);
        return panelOuter;
    }

    private static void swingWorkerReloadMembers(JTable table, MemberRepository repoMembers, DefaultTableModel m,
        JTextField fldId, JTextField fldType, JProgressBar bar) {

        showDashboardProgress(bar);
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish(20);
                List<Member> lst = repoMembers.findAllMembers();
                publish(60);
                Thread.sleep(120);
                publish(90);
                JTable tblRef = table;
                SwingUtilities.invokeLater(() -> {
                    fillMemberModel(m, lst);
                    applyMemberFilters(tblRef, fldId, fldType);
                    bar.setValue(100);
                    bar.setString("Members ready");
                    JOptionPane.showMessageDialog(null, "Members loaded asynchronously");
                    hideDashboardProgress(bar);
                });
                publish(100);
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> ch) {
                int v = ch.get(ch.size() - 1).intValue();
                bar.setValue(v);
                bar.setString("Member load " + v + "%");
            }
        };

        worker.execute();
    }

    private static void applyMemberFilters(JTable table, JTextField fldId, JTextField fldType) {
        TableRowSorter<?> sr = (TableRowSorter<?>) table.getRowSorter();
        sr.setRowFilter(new RowFilter<Object, Object>() {
            @Override
            public boolean include(RowFilter.Entry<?, ?> entry) {
                String wantId = fldId.getText().trim().toLowerCase();
                String want = fldType.getText().trim().toLowerCase();
                String cellId = entry.getValue(0).toString().toLowerCase();
                String cell = entry.getValue(3).toString().toLowerCase();
                boolean okId = true;
                boolean okTy = true;
                if (wantId.length() > 0) {
                    okId = cellId.contains(wantId);
                }
                if (want.length() > 0) {
                    okTy = cell.contains(want);
                }
                return okId && okTy;
            }
        });
    }

    private static void reloadMembersTable(JTable table, DefaultTableModel m, JTextField fldId, JTextField fldType,
        MemberRepository repoMembers) {

        try {
            List<Member> lst = repoMembers.findAllMembers();
            fillMemberModel(m, lst);
            applyMemberFilters(table, fldId, fldType);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(table, "Database error");
            JOptionPane.showMessageDialog(table, ex.getMessage());
        }
    }

    private static void fillMemberModel(DefaultTableModel m, List<Member> lst) {
        m.setRowCount(0);
        int num = 0;
        num = 0;
        while (num < lst.size()) {
            Member mb = lst.get(num);
            m.addRow(new Object[] {
                mb.getNumMemberId(),
                mb.getStrMemberName(),
                mb.getStrEmail(),
                mb.getStrMembershipType()
            });
            num = num + 1;
        }
    }

    private static void dialogAddMember(JFrame owner, MemberRepository repo, Runnable onOk) {
        JTextField fid = new JTextField();
        JTextField fn = new JTextField();
        JTextField fe = new JTextField();
        JTextField ft = new JTextField();
        JPanel p = new JPanel(new GridLayout(4, 2));
        p.add(new JLabel("Member ID numeric"));
        p.add(fid);
        p.add(new JLabel("Name"));
        p.add(fn);
        p.add(new JLabel("Email"));
        p.add(fe);
        p.add(new JLabel("Member type"));
        p.add(ft);
        if (JOptionPane.showConfirmDialog(owner, p, "Add member", JOptionPane.OK_CANCEL_OPTION)
            != JOptionPane.OK_OPTION) {
            return;
        }
        if (!InputValidator.isBookIdNumeric(fid.getText())) {
            JOptionPane.showMessageDialog(owner, "Numeric ID needed");
            return;
        }
        if (!InputValidator.isTitleNotEmpty(fn.getText())) {
            JOptionPane.showMessageDialog(owner, "Name must not be empty.");
            return;
        }
        if (!InputValidator.isEmailOk(fe.getText())) {
            JOptionPane.showMessageDialog(owner, "Email format incorrect");
            return;
        }
        if (!InputValidator.isTitleNotEmpty(ft.getText())) {
            JOptionPane.showMessageDialog(owner, "Member type must not be empty.");
            return;
        }
        try {
            repo.insertMember(Integer.parseInt(fid.getText().trim()), fn.getText().trim(), fe.getText().trim(),
                ft.getText().trim());
            JOptionPane.showMessageDialog(owner, "Member registered successfully");
            onOk.run();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage());
        }
    }

    private static void dialogEditMember(JFrame owner, MemberRepository repo, int id, Runnable onOk) {
        Member old = null;
        try {
            old = repo.findMemberById(id);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage());
            return;
        }
        if (old == null) {
            return;
        }
        JTextField fn = new JTextField(old.getStrMemberName());
        JTextField fe = new JTextField(old.getStrEmail());
        JTextField ft = new JTextField(old.getStrMembershipType());
        JPanel p = new JPanel(new GridLayout(3, 2));
        p.add(new JLabel("Name"));
        p.add(fn);
        p.add(new JLabel("Email"));
        p.add(fe);
        p.add(new JLabel("Member type"));
        p.add(ft);
        if (JOptionPane.showConfirmDialog(owner, p, "Update member", JOptionPane.OK_CANCEL_OPTION)
            != JOptionPane.OK_OPTION) {
            return;
        }
        if (!InputValidator.isTitleNotEmpty(fn.getText())) {
            JOptionPane.showMessageDialog(owner, "Name must not be empty.");
            return;
        }
        if (!InputValidator.isEmailOk(fe.getText())) {
            JOptionPane.showMessageDialog(owner, "Wrong email pattern");
            return;
        }
        if (!InputValidator.isTitleNotEmpty(ft.getText())) {
            JOptionPane.showMessageDialog(owner, "Member type must not be empty.");
            return;
        }
        try {
            repo.updateMember(id, fn.getText().trim(), fe.getText().trim(), ft.getText().trim());
            JOptionPane.showMessageDialog(owner, "Member updated successfully");
            onOk.run();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage());
        }
    }

    private static JPanel makeBorrowTab(JFrame owner, BorrowRepository repoBorrow,
        JProgressBar barMain, JLabel lblDash) {

        JPanel panelOuter = new JPanel(new BorderLayout(5, 5));
        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update status");
        JButton btnDelete = new JButton("Delete");
        JButton btnOverdue = new JButton("Show overdue only");
        JButton btnRange = new JButton("Filter borrow date range");
        JButton btnLoadBg = new JButton("Load borrows in background");
        JTextField fldMember = new JTextField(6);
        JTextField fldBook = new JTextField(6);
        fldMember.setToolTipText("Filter by member ID contains");
        fldBook.setToolTipText("Filter by book ID contains");

        String[] cols =
            new String[] {"Record ID", "Book ID", "Member ID", "Borrow date", "Due date", "Return status"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(m);
        table.setAutoCreateRowSorter(true);
        styleTableHeaderBottomLine(table);

        Runnable reloadAll = () -> reloadBorrowFullList(repoBorrow, m, table, fldMember, fldBook);

        btnRefresh.addActionListener(ev -> reloadAll.run());
        btnOverdue.addActionListener(ev -> {
            applyBorrowCustomList(table, m, fldMember, fldBook, () -> {
                try {
                    return repoBorrow.findOverdueRecords();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            JOptionPane.showMessageDialog(owner, "Showing overdue borrowing records only");
        });
        btnRange.addActionListener(ev -> {
            JTextField fFrom = new JTextField(10);
            JTextField fTo = new JTextField(10);
            JComboBox<String> ord =
                new JComboBox<>(new String[] {"Sort borrow dates ascending", "Sort borrow dates descending"});
            JPanel dlg = new JPanel(new GridLayout(3, 2));
            dlg.add(new JLabel("From YYYY-MM-DD"));
            dlg.add(fFrom);
            dlg.add(new JLabel("To"));
            dlg.add(fTo);
            dlg.add(new JLabel("Order"));
            dlg.add(ord);
            if (JOptionPane.showConfirmDialog(owner, dlg, "Date filter", JOptionPane.OK_CANCEL_OPTION)
                != JOptionPane.OK_OPTION) {
                return;
            }
            LocalDate dtFromParsed = null;
            LocalDate dtToParsed = null;
            try {
                dtFromParsed = LocalDate.parse(fFrom.getText().trim());
                dtToParsed = LocalDate.parse(fTo.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(owner, "Invalid date");
                return;
            }
            final LocalDate dtFromFinal = dtFromParsed;
            final LocalDate dtToFinal = dtToParsed;
            final boolean sortAsc = ord.getSelectedIndex() == 0;
            applyBorrowCustomList(table, m, fldMember, fldBook, () -> {
                try {
                    return repoBorrow.filterByBorrowDateRange(dtFromFinal, dtToFinal, sortAsc);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
            JOptionPane.showMessageDialog(owner, "Borrow records filtered successfully");
        });
        btnLoadBg.addActionListener(ev -> {
            SwingWorker<List<BorrowRecord>, Integer> w = new SwingWorker<List<BorrowRecord>, Integer>() {
                protected List<BorrowRecord> doInBackground() throws Exception {
                    publish(25);
                    List<BorrowRecord> lstInner = repoBorrow.findAllBorrows();
                    publish(100);
                    return lstInner;
                }

                protected void done() {
                    try {
                        List<BorrowRecord> lst = get();
                        fillBorrowModel(m, lst);
                        applyBorrowIdRowFilter(table, fldMember, fldBook);
                        JOptionPane.showMessageDialog(owner,
                            "Borrow list loaded via background SwingWorker successfully");
                        lblDash.setText("Borrow history rows: " + lst.size());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(owner, "Async load failure " + ex.getMessage());
                    }
                }
            };
            w.execute();
        });

        btnAdd.addActionListener(ev ->
            dialogAddBorrow(owner, repoBorrow, reloadAll));

        btnUpdate.addActionListener(ev -> {
            int rv = table.getSelectedRow();
            if (rv < 0) {
                return;
            }
            int mr = table.convertRowIndexToModel(rv);
            int rid = Integer.parseInt(m.getValueAt(mr, 0).toString());
            String[] opts = {"Borrowed", "Returned", "Overdue"};
            String cho = (String) JOptionPane.showInputDialog(owner, "Pick new borrowing status:", "Edit status",
                JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
            if (cho == null) {
                return;
            }
            try {
                repoBorrow.updateBorrowStatus(rid, cho);
                JOptionPane.showMessageDialog(owner, "Borrow record updated successfully");
                reloadAll.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(owner, ex.getMessage());
            }
        });

        btnDelete.addActionListener(ev -> {
            int rv = table.getSelectedRow();
            if (rv < 0) {
                return;
            }
            int mr = table.convertRowIndexToModel(rv);
            int rid = Integer.parseInt(m.getValueAt(mr, 0).toString());
            int ans =
                JOptionPane.showConfirmDialog(owner, "Really delete borrowing record?", "Sure", JOptionPane.YES_NO_OPTION);
            if (ans != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                repoBorrow.deleteBorrow(rid);
                JOptionPane.showMessageDialog(owner, "Borrow row deleted");
                reloadAll.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(owner, ex.getMessage());
            }
        });

        panelBtns.add(btnRefresh);
        panelBtns.add(btnAdd);
        panelBtns.add(btnUpdate);
        panelBtns.add(btnDelete);
        panelBtns.add(btnOverdue);
        panelBtns.add(btnRange);
        panelBtns.add(btnLoadBg);
        panelBtns.add(new JLabel("Filter by member ID:"));
        panelBtns.add(fldMember);
        panelBtns.add(new JLabel("Filter by book ID:"));
        panelBtns.add(fldBook);

        fldMember.addActionListener(ev -> reloadAll.run());
        fldBook.addActionListener(ev -> reloadAll.run());

        panelOuter.add(panelBtns, BorderLayout.NORTH);
        panelOuter.add(new JScrollPane(table), BorderLayout.CENTER);

        reloadAll.run();
        return panelOuter;
    }

    private static void reloadBorrowFullList(BorrowRepository repoBorrow, DefaultTableModel m,
        JTable table, JTextField fldM, JTextField fldB) {
        try {
            List<BorrowRecord> lst = repoBorrow.findAllBorrows();
            fillBorrowModel(m, lst);
            applyBorrowIdRowFilter(table, fldM, fldB);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(table, "Database problem: " + ex.getMessage());
        }
    }

    private static void applyBorrowCustomList(JTable table, DefaultTableModel m, JTextField fldM, JTextField fldB,
        java.util.concurrent.Callable<List<BorrowRecord>> supplier) {

        try {
            List<BorrowRecord> lst = supplier.call();
            fillBorrowModel(m, lst);
            applyBorrowIdRowFilter(table, fldM, fldB);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(table, "Error");
            JOptionPane.showMessageDialog(table, ex.getMessage());
        }
    }

    private static void applyBorrowIdRowFilter(JTable table, JTextField fldM, JTextField fldB) {
        TableRowSorter<?> sr = (TableRowSorter<?>) table.getRowSorter();
        if (sr == null) {
            return;
        }
        sr.setRowFilter(new RowFilter<Object, Object>() {
            @Override
            public boolean include(RowFilter.Entry<?, ?> entry) {
                String wantM = fldM.getText().trim();
                String wantB = fldB.getText().trim();
                String cellM = entry.getValue(2).toString();
                String cellB = entry.getValue(1).toString();
                boolean ok = true;
                if (wantM.length() > 0) {
                    ok = cellM.contains(wantM);
                }
                if (wantB.length() > 0) {
                    ok = ok && cellB.contains(wantB);
                }
                return ok;
            }
        });
    }

    private static void fillBorrowModel(DefaultTableModel m, List<BorrowRecord> lst) {
        m.setRowCount(0);
        int n = 0;
        while (n < lst.size()) {
            BorrowRecord r = lst.get(n);
            m.addRow(new Object[] {
                r.getNumRecordId(),
                r.getNumBookId(),
                r.getNumMemberId(),
                r.getStrBorrowDate(),
                r.getStrDueDate(),
                r.getStrReturnStatus()
            });
            n = n + 1;
        }
    }

    private static void dialogAddBorrow(JFrame owner, BorrowRepository repoBorrow, Runnable onOk) {
        JTextField fRid = new JTextField();
        JTextField fBid = new JTextField();
        JTextField fMid = new JTextField();
        JTextField fB = new JTextField();
        JTextField fD = new JTextField();
        JTextField fS = new JTextField();
        JPanel p = new JPanel(new GridLayout(6, 2));
        p.add(new JLabel("Record ID"));
        p.add(fRid);
        p.add(new JLabel("Book ID"));
        p.add(fBid);
        p.add(new JLabel("Member ID"));
        p.add(fMid);
        p.add(new JLabel("Borrow yyyy-mm-dd"));
        p.add(fB);
        p.add(new JLabel("Due yyyy-mm-dd"));
        p.add(fD);
        p.add(new JLabel("Status Borrowed/Returned/Overdue"));
        p.add(fS);
        if (JOptionPane.showConfirmDialog(owner, p, "New borrow row", JOptionPane.OK_CANCEL_OPTION)
            != JOptionPane.OK_OPTION) {
            return;
        }
        LocalDate bd = null;
        LocalDate dd = null;
        try {
            bd = LocalDate.parse(fB.getText().trim());
            dd = LocalDate.parse(fD.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Borrow dates must be valid");
            return;
        }
        if (!InputValidator.isDueAfterBorrow(bd, dd)) {
            JOptionPane.showMessageDialog(owner, "Due must be after borrow");
            return;
        }
        if (!InputValidator.isStatusOk(fS.getText())) {
            JOptionPane.showMessageDialog(owner, "Wrong status phrase");
            return;
        }
        if (!InputValidator.isBookIdNumeric(fRid.getText())) {
            JOptionPane.showMessageDialog(owner, "Numeric IDs needed");
            return;
        }
        if (!InputValidator.isBookIdNumeric(fBid.getText())) {
            JOptionPane.showMessageDialog(owner, "Numeric IDs needed");
            return;
        }
        if (!InputValidator.isBookIdNumeric(fMid.getText())) {
            JOptionPane.showMessageDialog(owner, "Numeric IDs needed");
            return;
        }
        try {
            repoBorrow.insertBorrow(Integer.parseInt(fRid.getText().trim()), Integer.parseInt(fBid.getText().trim()),
                Integer.parseInt(fMid.getText().trim()), bd.toString(), dd.toString(), fS.getText().trim());
            JOptionPane.showMessageDialog(owner, "Borrow transaction recorded successfully");
            if (onOk != null) {
                onOk.run();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage());
        }
    }

    private static JPanel makeBatchConcurrencyTab(BookRepository repoBooks, JProgressBar barMain) {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        JLabel lbl = new JLabel("Comma-separated book IDs, then the new status (e.g. Available).");
        JTextField fldIds =
            new JTextField("comma separated book IDs e.g. 1,2,3 status change (Available/Borrowed test)");
        JTextField fldStat = new JTextField("Available");
        JButton btnRun = new JButton("Run batch with thread pool");

        fldIds.setColumns(35);
        fldStat.setColumns(14);

        btnRun.addActionListener(ev -> {
            String txt = fldIds.getText();
            List<Integer> ids = new ArrayList<>();
            String[] parts = txt.split(",");
            int x = 0;
            while (x < parts.length) {
                String part = parts[x].trim();
                if (part.length() > 0) {
                    if (InputValidator.isBookIdNumeric(part)) {
                        ids.add(Integer.parseInt(part));
                    }
                }
                x = x + 1;
            }
            if (ids.isEmpty()) {
                JOptionPane.showMessageDialog(p, "Type some numeric IDs");
                return;
            }
            AtomicInteger done = new AtomicInteger(0);

            ExecutorService pool = Executors.newFixedThreadPool(2);

            showDashboardProgress(barMain);
            barMain.setIndeterminate(true);
            barMain.setString("Batch threads updating…");

            int mid = ids.size() / 2;

            BookRepository rb = repoBooks;
            List<Integer> lstA = new ArrayList<>(ids.subList(0, mid));
            List<Integer> lstB = new ArrayList<>(ids.subList(mid, ids.size()));
            Runnable job1 = new BatchAvailabilityJob(rb, lstA, fldStat.getText().trim(), done);
            Runnable job2 = new BatchAvailabilityJob(rb, lstB, fldStat.getText().trim(), done);

            pool.execute(job1);
            pool.execute(job2);

            SwingWorker<Void, Void> waiter = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    pool.shutdown();
                    boolean finished = pool.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS);
                    if (!finished) {
                        JOptionPane.showMessageDialog(p, "Threads still running slowly");
                    }
                    return null;
                }

                @Override
                protected void done() {
                    barMain.setIndeterminate(false);
                    hideDashboardProgress(barMain);
                    JOptionPane.showMessageDialog(p, "Concurrent batch touched " + ids.size() + " book rows safely");
                    done.set(0);
                }
            };
            waiter.execute();
        });

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(btnRun);

        JPanel center = new JPanel(new GridLayout(3, 1, 8, 8));
        center.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        center.add(lbl);
        center.add(fldIds);
        center.add(fldStat);

        p.add(north, BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private static void styleTableHeaderBottomLine(JTable table) {
        JTableHeader header = table.getTableHeader();
        if (header == null) {
            return;
        }
        Color line = table.getGridColor();
        Color borderColor;
        if (line != null) {
            borderColor = line;
        } else {
            borderColor = new Color(0xAA, 0xAA, 0xAA);
        }
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, borderColor));
    }

    private static void hideDashboardProgress(JProgressBar bar) {
        bar.setIndeterminate(false);
        bar.setValue(0);
        bar.setString("");
        bar.setVisible(false);
    }

    private static void showDashboardProgress(JProgressBar bar) {
        bar.setVisible(true);
    }

    private static void refreshDashboardCounts(BookRepository b, MemberRepository m,
        BorrowRepository br, JLabel lblDash) {

        SwingWorker<Void, Void> ww = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                int nb = b.findAllBooks().size();
                int nm = m.findAllMembers().size();
                int nr = br.findAllBorrows().size();
                String str = "";
                str = "Dashboard | Books: " + nb + " | Members: " + nm + " | Borrow records: " + nr;
                String finalStr = str;
                SwingUtilities.invokeLater(() -> lblDash.setText(finalStr));
                return null;
            }
        };

        ww.execute();
    }
}
