package assignment;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

// Database Utility Class
class DBUtil {
    private static Connection con = null;
    private static Statement st = null;

    private DBUtil() {}

    public static Connection getConnection() {
        if (con == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://localhost/assignment", "root", "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return con;
    }

    public static Statement getStatement() {
        try {
            if (st == null) {
                st = getConnection().createStatement();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return st;
    }

    public static void closeConnection() {
        try {
            if (st != null) st.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

// Login Frame
class Login extends JFrame implements ActionListener {
    JLabel l1, l2, messageLabel;
    JTextField t1;
    JPasswordField p1;
    JButton reg, login;
    JRadioButton userRadio, adminRadio;
    ButtonGroup group;
    Container c;

    public Login() {
        c = getContentPane();
        c.setLayout(new GridBagLayout());  // Use GridBagLayout for fine control over components
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);  // Add padding around components

        // Create the components
        l1 = new JLabel("Username:");
        l2 = new JLabel("Password:");
        t1 = new JTextField(15);
        p1 = new JPasswordField(15);
        reg = new JButton("Register");
        login = new JButton("Login");
        messageLabel = new JLabel("");
        messageLabel.setForeground(Color.RED);  // Set the message label color to red for error messages

        userRadio = new JRadioButton("User", false);
        adminRadio = new JRadioButton("Admin", false);

        group = new ButtonGroup();
        group.add(userRadio);
        group.add(adminRadio);

        // Style the components
        styleButton(reg);
        styleButton(login);
        styleTextField(t1);
        styleTextField(p1);

        // Add components to the frame using GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(l1, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        add(t1, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(l2, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        add(p1, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(userRadio, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        add(adminRadio, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(login, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        add(reg, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;  // Span across 2 columns
        add(messageLabel, gbc);

        // Add action listeners
        login.addActionListener(this);
        reg.addActionListener(this);

        // Set frame properties
        setSize(400, 350);
        setLocationRelativeTo(null);  // Center the frame on the screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(52, 152, 219));  // Blue background
        button.setForeground(Color.WHITE);  // White text
        button.setFocusPainted(false);  // Remove focus outline
        button.setPreferredSize(new Dimension(150, 40));  // Consistent button size

        // Add hover effect to buttons
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(41, 128, 185));  // Darker blue on hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(52, 152, 219));  // Original blue
            }
        });
    }

    private void styleTextField(JTextField textField) {
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBackground(new Color(240, 240, 240));  // Light gray background
        textField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));  // Border around text field
        textField.setPreferredSize(new Dimension(200, 30));
    }

    private void styleTextField(JPasswordField textField) {
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBackground(new Color(240, 240, 240));  // Light gray background
        textField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));  // Border around text field
        textField.setPreferredSize(new Dimension(200, 30));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == login) {
            String username = t1.getText();
            char[] passwordChars = p1.getPassword();
            String password = new String(passwordChars);
            String role = userRadio.isSelected() ? "User" : "Admin";

            String query = "SELECT Password, Role FROM Login WHERE Username=? AND Role=?";
            try (PreparedStatement pst = DBUtil.getConnection().prepareStatement(query)) {
                pst.setString(1, username);
                pst.setString(2, role);
                ResultSet rst = pst.executeQuery();
                if (rst.next()) {
                    String storedPassword = rst.getString("Password");
                    if (storedPassword.equals(password)) {
                        messageLabel.setText(role + " Login Successful");
                        if ("User".equals(role)) {
                            new UserFrame(this);
                        } else {
                            new AdminFrame(this);
                        }
                        setVisible(false);
                    } else {
                        messageLabel.setText("Invalid Password");
                    }
                } else {
                    messageLabel.setText("User not found or wrong role");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                t1.setText("");
                p1.setText("");
            }
        }

        if (ae.getSource() == reg) {
            String username = t1.getText();
            char[] passwordChars = p1.getPassword();
            String password = new String(passwordChars);
            String role = userRadio.isSelected() ? "User" : "Admin";

            String insertQuery = "INSERT INTO Login (Username, Password, Role) VALUES (?, ?, ?)";
            try (PreparedStatement pst = DBUtil.getConnection().prepareStatement(insertQuery)) {
                pst.setString(1, username);
                pst.setString(2, password);
                pst.setString(3, role);
                int rowsAffected = pst.executeUpdate();
                if (rowsAffected > 0) {
                    messageLabel.setText("Successfully Registered as " + role);
                } else {
                    messageLabel.setText("Registration Failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                messageLabel.setText("Error during registration");
            } finally {
                t1.setText("");
                p1.setText("");
            }
        }
    }
}


// User Frame
class UserFrame extends JFrame {
    public UserFrame(JFrame parentFrame) {
        setTitle("User Options");
        setLayout(new BorderLayout(10, 10));  // Using BorderLayout for better control over button placement

        // Create a panel to hold the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 10, 10));  // GridLayout for button organization
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));  // Add padding around the buttons

        // Define the buttons
        JButton facultyButton = new JButton("Faculty");
        JButton subjectButton = new JButton("Subject");
        JButton backButton = new JButton("Back");

        // Add actions to buttons
        facultyButton.addActionListener(e -> new FacultyDisplayFrame(this));
        subjectButton.addActionListener(e -> new SubjectDisplayFrame(this));

        backButton.addActionListener(e -> {
            parentFrame.setVisible(true);
            dispose();
        });

        // Style the buttons with a consistent look
        styleButton(facultyButton);
        styleButton(subjectButton);
        styleButton(backButton);

        // Add the buttons to the panel
        buttonPanel.add(facultyButton);
        buttonPanel.add(subjectButton);
        buttonPanel.add(backButton);

        // Add the panel to the center of the frame
        add(buttonPanel, BorderLayout.CENTER);

        // Set frame properties
        setSize(350, 250);
        setLocationRelativeTo(null);  // Center the window on the screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void styleButton(JButton button) {
        // Set font and background color for the buttons
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(52, 152, 219));  // Blue background
        button.setForeground(Color.WHITE);  // White text
        button.setFocusPainted(false);  // Remove focus outline
        button.setPreferredSize(new Dimension(250, 50));  // Set a consistent size for all buttons

        // Add hover effect to buttons
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(41, 128, 185));  // Darker blue when hovered
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(52, 152, 219));  // Original blue background
            }
        });
    }
}



//Admin Frame
class AdminFrame extends JFrame {
    public AdminFrame(JFrame parentFrame) {
        setTitle("Admin Options");
        setLayout(new BorderLayout(10, 10));  // Use BorderLayout for better overall structure

        // Create a panel to hold the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(7, 1, 10, 10));  // GridLayout for button organization
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));  // Add padding around the buttons

        // Define the buttons
        JButton facultyButton = new JButton("Faculty");
        JButton modifyFacultyButton = new JButton("Modify Faculty");
        JButton subjectButton = new JButton("Subject");
        JButton modifySubjectButton = new JButton("Modify Subject");
        JButton studentButton = new JButton("Student");
        JButton modifyStudentButton = new JButton("Modify Student");
        JButton backButton = new JButton("Back");

        // Add actions to buttons
        facultyButton.addActionListener(e -> new FacultyDisplayFrame(this));
        modifyFacultyButton.addActionListener(e -> new ModifyFacultyFrame(this));
        subjectButton.addActionListener(e -> new SubjectDisplayFrame(this));
        modifySubjectButton.addActionListener(e -> new ModifySubjectFrame(this));
        studentButton.addActionListener(e -> new StudentDisplayFrame(this));
        modifyStudentButton.addActionListener(e -> new ModifyStudentFrame(this));

        backButton.addActionListener(e -> {
            parentFrame.setVisible(true);
            dispose();
        });

        // Style the buttons with a consistent look
        styleButton(facultyButton);
        styleButton(modifyFacultyButton);
        styleButton(subjectButton);
        styleButton(modifySubjectButton);
        styleButton(studentButton);
        styleButton(modifyStudentButton);
        styleButton(backButton);

        // Add the buttons to the panel
        buttonPanel.add(facultyButton);
        buttonPanel.add(modifyFacultyButton);
        buttonPanel.add(subjectButton);
        buttonPanel.add(modifySubjectButton);
        buttonPanel.add(studentButton);
        buttonPanel.add(modifyStudentButton);
        buttonPanel.add(backButton);

        // Add the panel to the center of the frame
        add(buttonPanel, BorderLayout.CENTER);

        // Set frame properties
        setSize(400, 600);
        setLocationRelativeTo(null);  // Center the window on the screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void styleButton(JButton button) {
        // Set font and background color for the buttons
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(52, 152, 219));  // Blue background
        button.setForeground(Color.WHITE);  // White text
        button.setFocusPainted(false);  // Remove focus outline
        button.setPreferredSize(new Dimension(300, 50));  // Set a consistent size for all buttons

        // Add hover effect to buttons
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(41, 128, 185));  // Darker blue when hovered
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(52, 152, 219));  // Original blue background
            }
        });
    }
}


// Student Display Frame
class StudentDisplayFrame extends JFrame {
    JLabel rollNoLabel;
    JTextField rollNoField;
    JButton displayButton;
    JTextArea resultArea;

    public StudentDisplayFrame(JFrame parentFrame) {
        setTitle("Display Student Details");
        setLayout(new BorderLayout());

        // Top Panel for Inputs
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Spacing

        rollNoLabel = new JLabel("Roll No:");
        rollNoField = new JTextField(15);
        displayButton = new JButton("Display");
        
        displayButton.addActionListener(e -> {
            String rollNo = rollNoField.getText();
            String query = "SELECT * FROM StudentDetails WHERE Roll_No=?";
            try (PreparedStatement pst = DBUtil.getConnection().prepareStatement(query)) {
                pst.setString(1, rollNo);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    resultArea.setText("Roll No: " + rs.getString("Roll_No") + "\n" +
                            "Name: " + rs.getString("Student_Name") + "\n" +
                            "Address: " + rs.getString("Address") + "\n" +
                            "Contact: " + rs.getString("Contact"));
                } else {
                    resultArea.setText("No student found with Roll No " + rollNo);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        topPanel.add(rollNoLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(rollNoField, gbc);

        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        topPanel.add(displayButton, gbc);

        // Center Panel for Result Area
        resultArea = new JTextArea(10, 30);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Bottom Panel for Back Button
        JPanel bottomPanel = new JPanel();
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            parentFrame.setVisible(true);
            dispose();
        });
        bottomPanel.add(backButton);

        // Add Panels to Frame
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setSize(500, 400);
        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }
}

//Modify Student
class ModifyStudentFrame extends JFrame {
    public ModifyStudentFrame(JFrame parentFrame) {
        setTitle("Modify Student Details");
        setLayout(new BorderLayout(10, 10));  // Use BorderLayout for overall structure

        // Panel to hold form fields (Roll No, Name, Address, Contact)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);  // Padding for better spacing

        JLabel rollNoLabel = new JLabel("Roll No:");
        JTextField rollNoField = new JTextField(15);
        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField(15);
        JLabel addressLabel = new JLabel("Address:");
        JTextField addressField = new JTextField(15);
        JLabel contactLabel = new JLabel("Contact:");
        JTextField contactField = new JTextField(15);

        // Adding form fields to the form panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(rollNoLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(rollNoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(addressLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(addressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(contactLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(contactField, gbc);

        // Panel for buttons (Insert, Update, Back)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JButton insertButton = new JButton("Insert");
        JButton updateButton = new JButton("Update");
        JButton backButton = new JButton("Back");

        // Action listeners for the buttons
        insertButton.addActionListener(e -> {
            String query = "INSERT INTO StudentDetails (Roll_No, Student_Name, Address, Contact) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pst = DBUtil.getConnection().prepareStatement(query)) {
                pst.setString(1, rollNoField.getText());
                pst.setString(2, nameField.getText());
                pst.setString(3, addressField.getText());
                pst.setString(4, contactField.getText());
                int rows = pst.executeUpdate();
                JOptionPane.showMessageDialog(this, rows + " Record Inserted Successfully");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        updateButton.addActionListener(e -> {
            String query = "UPDATE StudentDetails SET Student_Name=?, Address=?, Contact=? WHERE Roll_No=?";
            try (PreparedStatement pst = DBUtil.getConnection().prepareStatement(query)) {
                pst.setString(1, nameField.getText());
                pst.setString(2, addressField.getText());
                pst.setString(3, contactField.getText());
                pst.setString(4, rollNoField.getText());
                int rows = pst.executeUpdate();
                JOptionPane.showMessageDialog(this, rows + " Record Updated Successfully");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        backButton.addActionListener(e -> {
            parentFrame.setVisible(true);
            dispose();
        });

        // Adding buttons to the button panel
        buttonPanel.add(insertButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(backButton);

        // Adding the panels to the frame
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Final Frame Settings
        setSize(400, 300);
        setLocationRelativeTo(null);  // Center the window on the screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}

//Display Faculty
class FacultyDisplayFrame extends JFrame implements ActionListener{
    JLabel F_ID;
    JTextField FID_TF;
    JButton display;
    JTextArea resultArea;
    JScrollPane resultScrollPane;

    public FacultyDisplayFrame(JFrame parentFrame){
        setTitle("Display Faculty Details");
        setLayout(new BorderLayout(10, 10));  // Use BorderLayout for overall structure
        
        JPanel formPanel = new JPanel();  // Panel for form fields (ID input)
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);  // Padding for better spacing

        F_ID = new JLabel("Faculty ID:");
        FID_TF = new JTextField(15);
        display = new JButton("Display");

        // Adding components to formPanel with GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(F_ID, gbc);
        gbc.gridx = 1;
        formPanel.add(FID_TF, gbc);

        display.addActionListener(this);

        // Result area to display fetched data
        resultArea = new JTextArea(5, 20);
        resultArea.setEditable(false);
        resultScrollPane = new JScrollPane(resultArea);

        // Button Panel for display and back buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            parentFrame.setVisible(true);
            dispose();
        });

        buttonPanel.add(display);
        buttonPanel.add(backButton);

        // Add the panels to the frame
        add(formPanel, BorderLayout.CENTER);
        add(resultScrollPane, BorderLayout.SOUTH);  // Position result area at the bottom
        add(buttonPanel, BorderLayout.NORTH);

        // Final Frame Settings
        setVisible(true);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent ae){
        if(ae.getSource() == display){
            String facultyid = FID_TF.getText();
            String query = "SELECT * FROM faculty WHERE FID=?";
            
            try{
                PreparedStatement pst = DBUtil.getConnection().prepareStatement(query);
                pst.setString(1, facultyid);
                ResultSet rs = pst.executeQuery();
                if(rs.next()){
                    resultArea.setText("Faculty ID : " + rs.getString("FID") + "\n" +
                                       "Faculty Name : " + rs.getString("FName") + "\n" +
                                       "Department : " + rs.getString("Dept") + "\n" +
                                       "Contact : " + rs.getString("contact"));
                } else {
                    resultArea.setText("No Faculty found with the Faculty ID " + facultyid);
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}

//Modify Faculty
class ModifyFacultyFrame extends JFrame implements ActionListener{
    JLabel idLabel, nameLabel, deptLabel, contactLabel;
    JTextField idTF, nameTF, deptTF, contactTF;
    JButton insertButton, updateButton;
    JTextArea outcome;
    JScrollPane outcomeScrollPane;

    public ModifyFacultyFrame(JFrame parentFrame){
        setTitle("Modify Faculty Data");
        setLayout(new BorderLayout(10, 10));  // Use BorderLayout for overall structure

        JPanel formPanel = new JPanel();  // To hold labels and textfields
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);  // Padding for better spacing

        idLabel = new JLabel("Faculty ID: ");
        nameLabel = new JLabel("Faculty Name: ");
        deptLabel = new JLabel("Department: ");
        contactLabel = new JLabel("Contact: ");
        idTF = new JTextField(15);
        nameTF = new JTextField(15);
        deptTF = new JTextField(15);
        contactTF = new JTextField(15);

        // Adding components to formPanel with GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(idLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(idTF, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(nameTF, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(deptLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(deptTF, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(contactLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(contactTF, gbc);

        // Adding outcome JTextArea inside a JScrollPane
        outcome = new JTextArea(5, 20);
        outcome.setEditable(false);
        outcomeScrollPane = new JScrollPane(outcome);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(outcomeScrollPane, gbc);

        // Button Panel for Insert, Update and Back buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        insertButton = new JButton("Insert");
        updateButton  = new JButton("Update");
        JButton backButton = new JButton("Back");

        insertButton.addActionListener(this);
        updateButton.addActionListener(this);
        backButton.addActionListener(e -> {
            parentFrame.setVisible(true);
            dispose();
        });

        buttonPanel.add(insertButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(backButton);

        // Add the panels to the frame
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Final Frame Settings
        setVisible(true);
        setSize(400, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void actionPerformed(ActionEvent ae){
        if(ae.getSource() == insertButton){
            String query = "INSERT INTO Faculty(FID,FName,Dept,Contact) VALUES(?,?,?,?)";
            try{
                PreparedStatement pst = DBUtil.getConnection().prepareStatement(query);
                pst.setString(1, idTF.getText());
                pst.setString(2, nameTF.getText());
                pst.setString(3, deptTF.getText());
                pst.setString(4, contactTF.getText());

                int rows = pst.executeUpdate();
                outcome.setText(rows + " Row(s) inserted Successfully");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        if(ae.getSource() == updateButton){
            String query = "UPDATE Faculty SET FName=?, Dept=?, Contact=? WHERE FID=?";
            try{
                PreparedStatement pst = DBUtil.getConnection().prepareStatement(query);
                pst.setString(1, nameTF.getText());
                pst.setString(2, deptTF.getText());
                pst.setString(3, contactTF.getText());
                pst.setString(4, idTF.getText());

                int rows = pst.executeUpdate();
                outcome.setText(rows + " Row(s) updated Successfully");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}

//Display Subject
class SubjectDisplayFrame extends JFrame implements ActionListener {
    JLabel SCodeLabel;
    JTextField SCode_TF;
    JButton display;
    JTextArea resultArea;

    public SubjectDisplayFrame(JFrame parentFrame) {
        setTitle("Display Subject Details");
        setLayout(new BorderLayout());

        // Top Panel for Inputs
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Spacing

        SCodeLabel = new JLabel("Subject Code:");
        SCode_TF = new JTextField(15);
        display = new JButton("Display");
        display.addActionListener(this);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        topPanel.add(SCodeLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(SCode_TF, gbc);

        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        topPanel.add(display, gbc);

        // Center Panel for Result Area
        resultArea = new JTextArea(10, 30);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Bottom Panel for Back Button
        JPanel bottomPanel = new JPanel();
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            parentFrame.setVisible(true);
            dispose();
        });
        bottomPanel.add(backButton);

        // Add Panels to Frame
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setSize(500, 400);
        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == display) {
            String facultyid = SCode_TF.getText();
            String query = "SELECT * FROM Subject WHERE FID=?";
            try {
                PreparedStatement pst = DBUtil.getConnection().prepareStatement(query);
                pst.setString(1, facultyid);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    resultArea.setText("Subject Code: " + rs.getString("SCode") + "\n" +
                                       "Subject Name: " + rs.getString("SName") + "\n" +
                                       "Department: " + rs.getString("Dept") + "\n" +
                                       "Faculty: " + rs.getString("Facu"));
                } else {
                    resultArea.setText("No Faculty found with the Faculty ID " + facultyid);
                }
            } catch (Exception e) {
                e.printStackTrace();
                resultArea.setText("Error occurred while fetching data.");
            }
        }
    }
}

//Modify Subject
class ModifySubjectFrame extends JFrame implements ActionListener {

    JLabel codeLabel, nameLabel, deptLabel, facultyLabel;
    JTextField codeTF, nameTF, deptTF, facultyTF;
    JTextArea outcome;
    JButton insertButton, updateButton;

    public ModifySubjectFrame(JFrame parentFrame) {
        setTitle("Modify Subject Data");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Create a main panel with GridBagLayout for better alignment
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        codeLabel = new JLabel("Subject Code:");
        nameLabel = new JLabel("Subject Name:");
        deptLabel = new JLabel("Department:");
        facultyLabel = new JLabel("Faculty:");

        codeTF = new JTextField(15);
        nameTF = new JTextField(15);
        deptTF = new JTextField(15);
        facultyTF = new JTextField(15);

        insertButton = new JButton("Insert");
        updateButton = new JButton("Update");
        insertButton.addActionListener(this);
        updateButton.addActionListener(this);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            parentFrame.setVisible(true);
            dispose();
        });

        outcome = new JTextArea(3, 20);
        outcome.setEditable(false);
        outcome.setLineWrap(true);
        outcome.setWrapStyleWord(true);
        JScrollPane outcomeScroll = new JScrollPane(outcome);

        // Adding components to the panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(codeLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(codeTF, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(nameTF, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(deptLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(deptTF, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(facultyLabel, gbc);

        gbc.gridx = 1;
        mainPanel.add(facultyTF, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(insertButton, gbc);

        gbc.gridx = 1;
        mainPanel.add(updateButton, gbc);

        // Adding panels and buttons to frame
        add(mainPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(outcomeScroll, BorderLayout.CENTER);
        bottomPanel.add(backButton, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == insertButton) {
            String query = "INSERT INTO Subject(SCode,SName,Dept,Facu) VALUES(?,?,?,?)";
            try {
                PreparedStatement pst = DBUtil.getConnection().prepareStatement(query);
                pst.setString(1, codeTF.getText());
                pst.setString(2, nameTF.getText());
                pst.setString(3, deptTF.getText());
                pst.setString(4, facultyTF.getText());

                int rows = pst.executeUpdate();
                outcome.setText(rows + " Row(s) inserted Successfully");
            } catch (Exception e) {
                e.printStackTrace();
                outcome.setText("Error: " + e.getMessage());
            }
        }
        if (ae.getSource() == updateButton) {
            String query = "UPDATE Subject SET SName=?,Dept=?,Facu=? WHERE SCode=?";
            try {
                PreparedStatement pst = DBUtil.getConnection().prepareStatement(query);
                pst.setString(1, nameTF.getText());
                pst.setString(2, deptTF.getText());
                pst.setString(3, facultyTF.getText());
                pst.setString(4, codeTF.getText());

                int rows = pst.executeUpdate();
                outcome.setText(rows + " Row(s) updated Successfully");
            } catch (Exception e) {
                e.printStackTrace();
                outcome.setText("Error: " + e.getMessage());
            }
        }
    }
}


// Main Class
public class Assignment {
    public static void main(String[] args) {
        new Login();
    }
}
