import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LibrarySystem {

    // --- CONFIGURATION ---
    // Added '?createDatabaseIfNotExist=true' to guarantee the database is created!
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db?createDatabaseIfNotExist=true";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "your_mysql_password"; // <-- CHANGE THIS TO YOUR MYSQL PASSWORD

    // --- GLOBAL STATE ---
    private static JFrame mainFrame;
    private static String currentUserRole = "";

    public static void main(String[] args) {
        setupModernUI();
        setupDatabaseTables();

        // --- EMERGENCY PASSWORD RESET ---
        // This forces the database to use Java's exact hash for "user123"
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE username = 'user'")) {
            pstmt.setString(1, hashPassword("user123"));
            pstmt.executeUpdate();
            System.out.println("✅ SUCCESS: User password forcefully reset to 'user123'!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // --------------------------------

        SwingUtilities.invokeLater(LibrarySystem::showLoginScreen);
    }

    // ================= AUTO-SETUP DATABASE =================
    // This entirely fixes the "Table doesn't exist" error
    private static void setupDatabaseTables() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {

            // 1. Create Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password_hash VARCHAR(64) NOT NULL, " +
                    "role ENUM('Admin', 'User') NOT NULL)");

            // 2. Create Books Table
            stmt.execute("CREATE TABLE IF NOT EXISTS books (" +
                    "book_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "title VARCHAR(100) NOT NULL, " +
                    "author VARCHAR(100), " +
                    "category VARCHAR(50), " +
                    "status ENUM('Available', 'Issued') DEFAULT 'Available')");

            // 3. Create Transactions Table
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "trans_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "book_id INT, " +
                    "member_id INT, " +
                    "issue_date DATE, " +
                    "due_date DATE, " +
                    "return_date DATE, " +
                    "fine_amount DECIMAL(10,2) DEFAULT 0.00, " +
                    "FOREIGN KEY (book_id) REFERENCES books(book_id))");

            // 4. Inject Default Data if the table is empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Initializing fresh database with default accounts...");

                // Admin pass: admin123 | User pass: user123
                stmt.execute("INSERT INTO users (username, password_hash, role) VALUES " +
                        "('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Admin'), " +
                        "('user', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', 'User')");

                stmt.execute("INSERT INTO books (title, author, category) VALUES " +
                        "('Effective Java', 'Joshua Bloch', 'Tech'), " +
                        "('Clean Code', 'Robert C. Martin', 'Tech'), " +
                        "('The Alchemist', 'Paulo Coelho', 'Fiction')");
            }
        } catch (SQLException e) {
            System.err.println("Failed to auto-setup tables. Check MySQL connection.");
            e.printStackTrace();
        }
    }

    // ================= UI UPGRADES =================
    private static void setupModernUI() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            Font modernFont = new Font("Segoe UI", Font.PLAIN, 14);
            UIManager.put("Label.font", modernFont);
            UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
            UIManager.put("TextField.font", modernFont);
            UIManager.put("PasswordField.font", modernFont);
            UIManager.put("Table.font", modernFont);
            UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 14));
            UIManager.put("OptionPane.messageFont", modernFont);
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 13));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SECURITY UTILS =================
    private static String hashPassword(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error hashing password", ex);
        }
    }

    // ================= DATABASE CONNECTION =================
    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // ================= GUI: LOGIN SCREEN =================
    private static void showLoginScreen() {
        if (mainFrame != null) mainFrame.dispose();
        mainFrame = new JFrame("Library System - Secure Login");
        mainFrame.setSize(450, 350);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(new Color(245, 247, 250));

        JLabel titleLabel = new JLabel("Welcome Back", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        formPanel.setOpaque(false);

        JLabel lblUser = new JLabel("Username");
        JTextField txtUser = new JTextField();
        JLabel lblPass = new JLabel("Password");
        JPasswordField txtPass = new JPasswordField();

        formPanel.add(lblUser);
        formPanel.add(txtUser);
        formPanel.add(lblPass);
        formPanel.add(txtPass);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton btnLogin = new JButton("Secure Login");
        btnLogin.setPreferredSize(new Dimension(150, 40));
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);

        buttonPanel.add(btnLogin);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        btnLogin.addActionListener(e -> {
            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword());
            authenticateUser(user, pass);
        });

        mainFrame.add(mainPanel);
        mainFrame.setVisible(true);
    }

    private static void authenticateUser(String username, String password) {
        String hashedPassword = hashPassword(password);
        String query = "SELECT role FROM users WHERE username=? AND password_hash=?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                currentUserRole = rs.getString("role");
                showDashboard();
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Invalid Credentials", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Database Error: " + e.getMessage());
        }
    }

    // ================= GUI: DASHBOARD =================
    private static void showDashboard() {
        mainFrame.dispose();
        mainFrame = new JFrame("Library Dashboard - " + currentUserRole);
        mainFrame.setSize(850, 600);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPane.setBackground(new Color(245, 247, 250));

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        navPanel.setBackground(Color.WHITE);
        navPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));

        JButton btnIssue = createNavButton("Issue Book", new Color(46, 204, 113));
        JButton btnReturn = createNavButton("Return Book", new Color(243, 156, 18));
        JButton btnLogout = createNavButton("Logout", new Color(231, 76, 60));

        navPanel.add(btnIssue);
        navPanel.add(btnReturn);

        if (currentUserRole.equals("Admin")) {
            JButton btnAddBook = createNavButton("Add New Book", new Color(52, 152, 219));
            navPanel.add(btnAddBook);
            btnAddBook.addActionListener(e -> showAddBookDialog());
        }

        navPanel.add(btnLogout);
        contentPane.add(navPanel, BorderLayout.NORTH);

        JTable bookTable = new JTable();
        bookTable.setRowHeight(30);
        bookTable.setShowGrid(false);
        bookTable.setIntercellSpacing(new Dimension(0, 0));
        refreshBookTable(bookTable);

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        contentPane.add(scrollPane, BorderLayout.CENTER);

        btnIssue.addActionListener(e -> showIssueBookDialog());
        btnReturn.addActionListener(e -> showReturnBookDialog());
        btnLogout.addActionListener(e -> showLoginScreen());

        mainFrame.add(contentPane);
        mainFrame.setVisible(true);
    }

    private static JButton createNavButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return btn;
    }

    // ================= LOGIC: DATA LOADING =================
    private static void refreshBookTable(JTable table) {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Book ID");
        model.addColumn("Title");
        model.addColumn("Author");
        model.addColumn("Status");

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("status")
                });
            }
            table.setModel(model);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= LOGIC: ISSUE BOOK =================
    private static void showIssueBookDialog() {
        JTextField txtBookID = new JTextField();
        JTextField txtMemberID = new JTextField();
        Object[] message = {
                "Book ID:", txtBookID,
                "Member ID:", txtMemberID
        };

        int option = JOptionPane.showConfirmDialog(mainFrame, message, "Issue a Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            try {
                int bookId = Integer.parseInt(txtBookID.getText());
                int memberId = Integer.parseInt(txtMemberID.getText());
                performIssue(bookId, memberId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Please enter valid numeric IDs.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void performIssue(int bookId, int memberId) {
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(15);

        String checkQuery = "SELECT status FROM books WHERE book_id = ?";
        String updateBook = "UPDATE books SET status = 'Issued' WHERE book_id = ?";
        String insertTrans = "INSERT INTO transactions (book_id, member_id, issue_date, due_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = connect()) {
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, bookId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                if ("Issued".equals(rs.getString("status"))) {
                    JOptionPane.showMessageDialog(mainFrame, "Book is already issued!", "Notice", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Book ID not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            conn.setAutoCommit(false);

            PreparedStatement transStmt = conn.prepareStatement(insertTrans);
            transStmt.setInt(1, bookId);
            transStmt.setInt(2, memberId);
            transStmt.setDate(3, java.sql.Date.valueOf(issueDate));
            transStmt.setDate(4, java.sql.Date.valueOf(dueDate));
            transStmt.executeUpdate();

            PreparedStatement updateStmt = conn.prepareStatement(updateBook);
            updateStmt.setInt(1, bookId);
            updateStmt.executeUpdate();

            conn.commit();
            JOptionPane.showMessageDialog(mainFrame, "Book Issued Successfully!\nReturn Due Date: " + dueDate, "Success", JOptionPane.INFORMATION_MESSAGE);
            showDashboard();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Database Error", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= LOGIC: RETURN BOOK =================
    private static void showReturnBookDialog() {
        String input = JOptionPane.showInputDialog(mainFrame, "Enter Book ID to Return:");
        if (input == null || input.trim().isEmpty()) return;

        try {
            int bookId = Integer.parseInt(input);
            String findTrans = "SELECT * FROM transactions WHERE book_id = ? AND return_date IS NULL";
            String updateTrans = "UPDATE transactions SET return_date = ?, fine_amount = ? WHERE trans_id = ?";
            String updateBook = "UPDATE books SET status = 'Available' WHERE book_id = ?";

            try (Connection conn = connect()) {
                PreparedStatement findStmt = conn.prepareStatement(findTrans);
                findStmt.setInt(1, bookId);
                ResultSet rs = findStmt.executeQuery();

                if (rs.next()) {
                    int transId = rs.getInt("trans_id");
                    LocalDate dueDate = rs.getDate("due_date").toLocalDate();
                    LocalDate returnDate = LocalDate.now();

                    long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
                    double fine = (daysOverdue > 0) ? daysOverdue * 10.0 : 0.0;

                    conn.setAutoCommit(false);

                    PreparedStatement upTransStmt = conn.prepareStatement(updateTrans);
                    upTransStmt.setDate(1, java.sql.Date.valueOf(returnDate));
                    upTransStmt.setDouble(2, fine);
                    upTransStmt.setInt(3, transId);
                    upTransStmt.executeUpdate();

                    PreparedStatement upBookStmt = conn.prepareStatement(updateBook);
                    upBookStmt.setInt(1, bookId);
                    upBookStmt.executeUpdate();

                    conn.commit();

                    String msg = "Book Returned Successfully.";
                    int msgType = JOptionPane.INFORMATION_MESSAGE;

                    if (fine > 0) {
                        msg += "\n\nATTENTION: Book is overdue!\nFine Payable: $" + fine;
                        msgType = JOptionPane.WARNING_MESSAGE;
                    }

                    JOptionPane.showMessageDialog(mainFrame, msg, "Return Receipt", msgType);
                    showDashboard();

                } else {
                    JOptionPane.showMessageDialog(mainFrame, "No active issue found for this book ID.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(mainFrame, "Invalid ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= LOGIC: ADD BOOK =================

    // ================= LOGIC: ADD BOOK (Admin Only) =================
    private static void showAddBookDialog() {
        JTextField txtTitle = new JTextField();
        JTextField txtAuthor = new JTextField();
        JTextField txtQuantity = new JTextField("1"); // As per instructions: "by default - 1"

        Object[] message = {
                "Book Title:", txtTitle,
                "Author:", txtAuthor,
                "Quantity/Copies:", txtQuantity
        };

        int option = JOptionPane.showConfirmDialog(mainFrame, message, "Add New Book(s) to Master List", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            if (txtTitle.getText().trim().isEmpty() || txtAuthor.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Title and Author fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int quantity = Integer.parseInt(txtQuantity.getText().trim());
                if (quantity < 1) throw new NumberFormatException();

                try (Connection conn = connect();
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO books (title, author, category) VALUES (?, ?, 'General')")) {

                    // Loop to insert the exact number of copies requested
                    for (int i = 0; i < quantity; i++) {
                        pstmt.setString(1, txtTitle.getText().trim());
                        pstmt.setString(2, txtAuthor.getText().trim());
                        pstmt.executeUpdate();
                    }

                    JOptionPane.showMessageDialog(mainFrame, quantity + " copy/copies of the book added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    showDashboard(); // Refresh the table

                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(mainFrame, "Database error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Please enter a valid numeric quantity (1 or more).", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
