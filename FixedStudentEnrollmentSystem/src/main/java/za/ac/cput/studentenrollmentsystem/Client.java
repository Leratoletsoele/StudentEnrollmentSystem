package za.ac.cput.studentenrollmentsystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class Client {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WelcomeFrame().setVisible(true));
    }
}

/* ============================================================
   THEME (completely different look)
   ============================================================ */
class Theme {
    static final Color BG        = new Color(17, 24, 39);   // charcoal
    static final Color PANEL     = new Color(31, 41, 55);   // slate
    static final Color PANEL_SOFT= new Color(55, 65, 81);   // softer slate
    static final Color TEXT      = new Color(236, 239, 244);
    static final Color MUTED     = new Color(156, 163, 175);
    static final Color ACCENT    = new Color(6, 182, 212);  // cyan
    static final Color DANGER    = new Color(239, 68, 68);  // red
    static final Font  TITLE     = new Font("Segoe UI", Font.BOLD, 22);
    static final Font  SUBTITLE  = new Font("Segoe UI", Font.BOLD, 16);
    static final Font  LABEL     = new Font("Segoe UI", Font.PLAIN, 14);

    static void paintFrame(JFrame f) { f.getContentPane().setBackground(BG); }
    static JPanel card(String title) {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255,255,255,25),1),
                new EmptyBorder(14,14,14,14)));
        if (title!=null && !title.isEmpty()) {
            JLabel t = new JLabel(title);
            t.setForeground(TEXT);
            t.setFont(SUBTITLE);
            p.add(t, BorderLayout.NORTH);
        }
        return p;
    }
    static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT);
        l.setFont(LABEL);
        return l;
    }
    static JTextField textField() {
        JTextField t = new JTextField();
        styleField(t);
        return t;
    }
    static JPasswordField passwordField() {
        JPasswordField t = new JPasswordField();
        styleField(t);
        return t;
    }
    static void styleField(JTextField f) {
        f.setOpaque(true);
        f.setBackground(PANEL_SOFT);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
        f.setFont(LABEL);
    }
    static JButton primary(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBorder(BorderFactory.createEmptyBorder(10,18,10,18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    static JButton ghost(String text) {
        JButton b = new JButton(text);
        b.setBackground(PANEL_SOFT);
        b.setForeground(TEXT);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBorder(BorderFactory.createEmptyBorder(10,18,10,18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}

/* ============================================================
   WELCOME / LOGIN (new flow)
   - Role dropdown
   - Student sign-up opens modal
   - Admin login opens a separate modal
   ============================================================ */
class WelcomeFrame extends JFrame {
    private final JComboBox<String> cboRole = new JComboBox<>(new String[]{"Student", "Admin"});
    private final JTextField txtUser = Theme.textField();         // Student Number or Admin Username
    private final JPasswordField txtPass = Theme.passwordField(); // Password

    WelcomeFrame() {
        super("Uni Application Portal — Sign In");
        setSize(960, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Theme.paintFrame(this);

        // Left panel (branding)
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(Theme.PANEL);
        left.setBorder(new EmptyBorder(24,24,24,24));

        JLabel brand = new JLabel("CampusConnect");
        brand.setForeground(Theme.TEXT);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JLabel tagline = new JLabel("<html>Apply, track, and manage <br/>your university journey.</html>");
        tagline.setForeground(Theme.MUTED);
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        left.add(brand, BorderLayout.NORTH);
        left.add(tagline, BorderLayout.CENTER);

        // Right panel (form)
        JPanel right = Theme.card("Sign In");
        right.setPreferredSize(new Dimension(500, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8,4,8,4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;

        JLabel lblRole = Theme.label("Role");
        form.add(lblRole, gc);
        gc.gridy++;
        form.add(cboRole, gc);

        gc.gridy++;
        JLabel lblUser = Theme.label("Username / Student Number");
        form.add(lblUser, gc);
        gc.gridy++;
        form.add(txtUser, gc);

        gc.gridy++;
        JLabel lblPass = Theme.label("Password");
        form.add(lblPass, gc);
        gc.gridy++;
        form.add(txtPass, gc);

        gc.gridy++;
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        JButton btnLogin = Theme.primary("Sign in");
        JButton btnSignup = Theme.ghost("Create student account");
        row.add(btnLogin);
        row.add(btnSignup);
        form.add(row, gc);

        right.add(form, BorderLayout.CENTER);

        // footer
        JLabel footer = new JLabel("© " + Calendar.getInstance().get(Calendar.YEAR) + " CampusConnect");
        footer.setForeground(Theme.MUTED);
        right.add(footer, BorderLayout.SOUTH);

        // Layout
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.45);
        split.setBorder(null);
        split.setDividerSize(2);

        setContentPane(split);

        // Events
        btnLogin.addActionListener(e -> doLogin());
        btnSignup.addActionListener(e -> openSignupDialog());
    }

    private void doLogin() {
        String role = String.valueOf(cboRole.getSelectedItem());
        String user = txtUser.getText().trim();
        String pw = new String(txtPass.getPassword()).trim();

        if (user.isEmpty() || pw.isEmpty()) {
            alert("Please enter your " + (role.equals("Admin") ? "admin username" : "student number") + " and password.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (role.equals("Admin")) {
            // hardcoded admin creds (change here if needed)
            if (!"admin".equals(user) || !"admin123".equals(pw)) {
                alert("Invalid admin credentials.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            SwingUtilities.invokeLater(() -> {
                new AdminConsole().setVisible(true);
                dispose();
            });
        } else {
            try {
                boolean ok = Database.authenticateStudent(user, pw);
                if (!ok) {
                    alert("Invalid student number or password.", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                final String studentNumber = user;
                SwingUtilities.invokeLater(() -> {
                    new StudentPortal(studentNumber).setVisible(true);
                    dispose();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                alert("Login error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

private void openSignupDialog() {
    final JDialog dialog = new JDialog(this, "Create Student Account", true);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    // Shell with title + padding
    JPanel shell = Theme.card("Student Registration");

    // --- Form ---
    JPanel form = new JPanel(new GridBagLayout());
    form.setOpaque(false);

    GridBagConstraints gc = new GridBagConstraints();
    gc.gridx = 0;
    gc.weightx = 1.0;
    gc.fill = GridBagConstraints.HORIZONTAL;
    gc.insets = new Insets(8, 6, 0, 6);

    final JTextField     txtName = Theme.textField();
    final JTextField     txtEmail = Theme.textField();
    final JTextField     txtSN = Theme.textField();
    final JPasswordField txtPW = Theme.passwordField();
    txtPW.setColumns(18);                 // <- ensure visible width

    int row = 0;

    // Full Name
    gc.gridy = row++; form.add(Theme.label("Full Name"), gc);
    gc.gridy = row++; form.add(txtName, gc);

    // Email
    gc.gridy = row++; form.add(Theme.label("Email"), gc);
    gc.gridy = row++; form.add(txtEmail, gc);

    // Student Number
    gc.gridy = row++; form.add(Theme.label("Student Number"), gc);
    gc.gridy = row++; form.add(txtSN, gc);

    // Password
    gc.gridy = row++; form.add(Theme.label("Password"), gc);
    gc.gridy = row++; form.add(txtPW, gc);

    // A little spacer so buttons aren't glued to the field
    gc.gridy = row++;
    gc.insets = new Insets(8, 6, 8, 6);
    form.add(Box.createVerticalStrut(6), gc);

    // --- Actions ---
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    actions.setOpaque(false);
    JButton btnCancel = Theme.ghost("Cancel");
    JButton btnCreate = Theme.primary("Create Account");
    actions.add(btnCancel);
    actions.add(btnCreate);

    shell.add(form, BorderLayout.CENTER);
    shell.add(actions, BorderLayout.SOUTH);

    dialog.setContentPane(shell);
    dialog.pack();                        // <- size AFTER adding components
    dialog.setMinimumSize(new Dimension(Math.max(520, dialog.getWidth()), dialog.getHeight()));
    dialog.setLocationRelativeTo(this);

    // Wire buttons
    btnCancel.addActionListener(e -> dialog.dispose());
    btnCreate.addActionListener(e -> {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String sn = txtSN.getText().trim();
        String pw = new String(txtPW.getPassword()).trim();

        if (name.isEmpty() || sn.isEmpty() || pw.isEmpty()) {
            alert("Name, Student Number and Password are required.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (sn.length() < 5) {
            alert("Student Number looks too short.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (Database.userExists(sn)) {
                alert("This student number is already registered.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Database.registerStudent(name, sn, email, pw);
            alert("Registration successful. You can sign in now.", JOptionPane.INFORMATION_MESSAGE);

            // Prefill login
            txtUser.setText(sn);
            txtPass.setText(pw);
            dialog.dispose();
            cboRole.setSelectedItem("Student");
        } catch (Exception ex) {
            ex.printStackTrace();
            alert("Registration error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    });

    dialog.setVisible(true);
}


    private void alert(String msg, int type) {
        UIManager.put("OptionPane.background", Theme.PANEL);
        UIManager.put("Panel.background", Theme.PANEL);
        UIManager.put("OptionPane.messageForeground", Theme.TEXT);
        JOptionPane.showMessageDialog(this, msg, "Notice", type);
    }
}

/* ============================================================
   STUDENT PORTAL (totally new layout)
   - Sidebar with big actions
   - CardLayout on the right: Apply / My Applications / Profile
   ============================================================ */
class StudentPortal extends JFrame {
    private final String studentNumber;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);

    // Apply form controls
    private final JTextField txtFullName = Theme.textField();
    private final JComboBox<String> cboProgram;
    private final JComboBox<String> cboSpec = new JComboBox<>();
    private final JList<String> lstCourses = new JList<>();

    // My Applications table
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID","Applicant","Student #","Program","Specialization","Courses","Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    StudentPortal(String studentNumber) {
        super("CampusConnect — Student");
        this.studentNumber = studentNumber;
        setSize(1160, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Theme.paintFrame(this);

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setBackground(Theme.PANEL);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setLayout(new GridLayout(0,1,10,10));
        sidebar.setBorder(new EmptyBorder(18,14,18,14));

        JLabel me = new JLabel("<html><b>Welcome</b><br/>Student #: " + studentNumber + "</html>");
        me.setForeground(Theme.TEXT);
        me.setFont(Theme.LABEL);

        JButton btnApply = Theme.primary("New Application");
        JButton btnMyApps = Theme.ghost("My Applications");
        JButton btnProfile = Theme.ghost("Profile");
        JButton btnSignOut = Theme.ghost("Sign Out");

        sidebar.add(me);
        sidebar.add(btnApply);
        sidebar.add(btnMyApps);
        sidebar.add(btnProfile);
        sidebar.add(new JLabel());
        sidebar.add(new JLabel());
        sidebar.add(btnSignOut);

        // Content cards
        content.setBackground(Theme.BG);
        content.add(buildApplyCard(), "apply");
        content.add(buildMyAppsCard(), "apps");
        content.add(buildProfileCard(), "profile");

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, content);
        split.setResizeWeight(0.0);
        split.setDividerSize(2);
        split.setBorder(null);

        setContentPane(split);

        // Program/spec data (different from previous UI)
        Map<String, String[]> programSpec = new LinkedHashMap<String, String[]>();
        programSpec.put("Sciences", new String[]{"Biology", "Chemistry", "Physics"});
        programSpec.put("Engineering", new String[]{"Electrical", "Mechanical", "Civil"});
        programSpec.put("Information Tech", new String[]{"Software", "Networks", "AI"});

        cboProgram = new JComboBox<String>(programSpec.keySet().toArray(new String[0]));
        cboProgram.addActionListener(e -> {
            String prog = (String) cboProgram.getSelectedItem();
            cboSpec.removeAllItems();
            if (prog != null) {
                String[] specs = programSpec.get(prog);
                if (specs != null) {
                    for (String s : specs) cboSpec.addItem(s);
                }
            }
            updateCourses();
        });
        // populate once now
        cboProgram.setSelectedIndex(0);

        // Wire sidebar buttons
        btnApply.addActionListener(e -> cards.show(content, "apply"));
        btnMyApps.addActionListener(e -> {
            loadMyApps();
            cards.show(content, "apps");
        });
        btnProfile.addActionListener(e -> cards.show(content, "profile"));
        btnSignOut.addActionListener(e -> {
            dispose();
            new WelcomeFrame().setVisible(true);
        });

        // After UI is ready, insert dynamic components (program/spec) into form panel
        // We placed placeholders; now locate and add them.
        attachProgramSpecComponents();
        loadMyApps();
    }

    private JPanel buildApplyCard() {
        JPanel card = Theme.card("Submit a New Application");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8,6,8,6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;

        form.add(Theme.label("Applicant (Full Name)"), gc);
        gc.gridy++;
        form.add(txtFullName, gc);

        gc.gridy++;
        form.add(Theme.label("Programme"), gc);
        gc.gridy++;
        // placeholder; actual combobox added in attachProgramSpecComponents()
        form.add(new JPanel(){ { setOpaque(false); setName("program_placeholder"); setPreferredSize(new Dimension(220,32)); } }, gc);

        gc.gridy++;
        form.add(Theme.label("Specialisation"), gc);
        gc.gridy++;
        form.add(new JPanel(){ { setOpaque(false); setName("spec_placeholder"); setPreferredSize(new Dimension(220,32)); } }, gc);

        gc.gridy++;
        form.add(Theme.label("Select Courses"), gc);
        gc.gridy++;
        lstCourses.setVisibleRowCount(6);
        lstCourses.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane sp = new JScrollPane(lstCourses);
        form.add(sp, gc);

        gc.gridy++;
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        row.setOpaque(false);
        JButton submit = Theme.primary("Submit Application");
        row.add(submit);
        form.add(row, gc);

        submit.addActionListener(e -> onSubmitApplication());

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildMyAppsCard() {
        JPanel card = Theme.card("My Applications");
        JTable table = new JTable(model);
        table.setRowHeight(26);
        JScrollPane scroll = new JScrollPane(table);
        card.add(scroll, BorderLayout.CENTER);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bar.setOpaque(false);
        JButton refresh = Theme.ghost("Refresh");
        bar.add(refresh);
        refresh.addActionListener(e -> loadMyApps());
        card.add(bar, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildProfileCard() {
        JPanel card = Theme.card("Profile");
        JLabel info = Theme.label("Tip: Your profile can be extended to show saved details, documents, etc.");
        card.add(info, BorderLayout.CENTER);
        return card;
    }

    private void attachProgramSpecComponents() {
        // walk the "apply" card and replace placeholders
        Component[] comps = content.getComponents();
        for (Component c : comps) {
            if (!(c instanceof JPanel)) continue;
            JPanel card = (JPanel) c;
            if (!"Submit a New Application".equals(
                    ((JLabel)((BorderLayout)card.getLayout()).getLayoutComponent(BorderLayout.NORTH)).getText())) continue;
            // search in center
            JPanel center = (JPanel) ((BorderLayout) card.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            for (Component x : center.getComponents()) {
                if (x instanceof JPanel && "program_placeholder".equals(x.getName())) {
                    JPanel ph = (JPanel) x;
                    ph.setLayout(new BorderLayout());
                    ph.add(cboProgram, BorderLayout.CENTER);
                } else if (x instanceof JPanel && "spec_placeholder".equals(x.getName())) {
                    JPanel ph = (JPanel) x;
                    ph.setLayout(new BorderLayout());
                    ph.add(cboSpec, BorderLayout.CENTER);
                }
            }
            break;
        }
    }

    private void updateCourses() {
        String spec = (String) cboSpec.getSelectedItem();
        Map<String, String[]> data = new HashMap<String, String[]>();
        data.put("Biology", new String[]{"Cell Biology","Genetics","Ecology"});
        data.put("Chemistry", new String[]{"Organic Chem","Analytical Chem","Physical Chem"});
        data.put("Physics", new String[]{"Mechanics","Electromagnetism","Quantum"});
        data.put("Electrical", new String[]{"Circuits","Signals","Power Systems"});
        data.put("Mechanical", new String[]{"Thermodynamics","Dynamics","Materials"});
        data.put("Civil", new String[]{"Structures","Geotech","Hydraulics"});
        data.put("Software", new String[]{"Java","Databases","Web"});
        data.put("Networks", new String[]{"Routing","Switching","Security"});
        data.put("AI", new String[]{"ML Basics","Deep Learning","Data Mining"});
        String[] list = data.getOrDefault(spec, new String[0]);
        lstCourses.setListData(list);
    }

    private void onSubmitApplication() {
        String name = txtFullName.getText().trim();
        String prog = (String) cboProgram.getSelectedItem();
        String spec = (String) cboSpec.getSelectedItem();
        List<String> selected = lstCourses.getSelectedValuesList();

        if (name.isEmpty() || prog == null || spec == null || selected.isEmpty()) {
            alert("Please complete all fields and select at least one course.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String coursesCsv = selected.stream().collect(Collectors.joining(", "));
        try {
            int id = Database.addApplication(name, studentNumber, prog, spec, coursesCsv);
            alert("Application submitted. Reference ID: " + id, JOptionPane.INFORMATION_MESSAGE);
            txtFullName.setText("");
            lstCourses.clearSelection();
            loadMyApps();
            // Switch to My Applications
            cards.show(content, "apps");
        } catch (Exception ex) {
            ex.printStackTrace();
            alert("Failed to submit: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMyApps() {
        try {
            List<String[]> rows = Database.findApplicationsByStudent(studentNumber);
            model.setRowCount(0);
            for (String[] r : rows) model.addRow(r);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void alert(String msg, int type) {
        UIManager.put("OptionPane.background", Theme.PANEL);
        UIManager.put("Panel.background", Theme.PANEL);
        UIManager.put("OptionPane.messageForeground", Theme.TEXT);
        JOptionPane.showMessageDialog(this, msg, "Notice", type);
    }
}

/* ============================================================
   ADMIN CONSOLE (new look)
   - Top toolbar (filter by status)
   - Action buttons on the right
   ============================================================ */
class AdminConsole extends JFrame {
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID","Applicant","Student #","Program","Specialisation","Courses","Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JComboBox<String> cboFilter = new JComboBox<>(new String[]{"All","Pending","Approved","Rejected"});

    AdminConsole() {
        super("CampusConnect — Admin Console");
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Theme.paintFrame(this);

        // Toolbar
        JPanel bar = new JPanel(new BorderLayout(8,8));
        bar.setBackground(Theme.PANEL);
        bar.setBorder(new EmptyBorder(10,10,10,10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JLabel t = new JLabel("Applications");
        t.setForeground(Theme.TEXT);
        t.setFont(Theme.SUBTITLE);
        left.add(t);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        center.setOpaque(false);
        center.add(Theme.label("Filter"));
        center.add(cboFilter);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton btnApprove = Theme.primary("Approve");
        JButton btnReject  = Theme.ghost("Reject");
        JButton btnDelete  = Theme.ghost("Delete");
        JButton btnRefresh = Theme.ghost("Refresh");
        right.add(btnApprove);
        right.add(btnReject);
        right.add(btnDelete);
        right.add(btnRefresh);

        bar.add(left, BorderLayout.WEST);
        bar.add(center, BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);

        // Table
        table.setRowHeight(26);
        JScrollPane scroll = new JScrollPane(table);

        setLayout(new BorderLayout());
        add(bar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // Events
        btnRefresh.addActionListener(e -> loadApps());
        btnApprove.addActionListener(e -> bulkStatus("Approved"));
        btnReject.addActionListener(e -> bulkStatus("Rejected"));
        btnDelete.addActionListener(e -> bulkDelete());
        cboFilter.addActionListener(e -> loadApps());

        loadApps();
    }

    private void loadApps() {
        try {
            String filter = String.valueOf(cboFilter.getSelectedItem());
            List<String[]> rows = Database.findAllApplications();
            model.setRowCount(0);
            for (String[] r : rows) {
                if (!"All".equals(filter)) {
                    if (!filter.equalsIgnoreCase(r[6])) continue;
                }
                model.addRow(r);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private List<Integer> selectedIds() {
        int[] rows = table.getSelectedRows();
        List<Integer> ids = new ArrayList<Integer>();
        for (int r : rows) {
            try { ids.add(Integer.parseInt(String.valueOf(model.getValueAt(r,0)))); }
            catch (Exception ignore) {}
        }
        return ids;
    }

    private void bulkStatus(String status) {
        List<Integer> ids = selectedIds();
        if (ids.isEmpty()) {
            alert("Select one or more rows first.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int updated = 0;
            for (Integer id : ids) updated += Database.updateApplicationStatus(id, status);
            alert("Updated " + updated + " row(s) to " + status + ".", JOptionPane.INFORMATION_MESSAGE);
            loadApps();
        } catch (Exception ex) {
            ex.printStackTrace();
            alert("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bulkDelete() {
        List<Integer> ids = selectedIds();
        if (ids.isEmpty()) {
            alert("Select one or more rows first.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Delete " + ids.size() + " application(s)?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            int deleted = 0;
            for (Integer id : ids) deleted += Database.deleteApplication(id);
            alert("Deleted " + deleted + " row(s).", JOptionPane.INFORMATION_MESSAGE);
            loadApps();
        } catch (Exception ex) {
            ex.printStackTrace();
            alert("Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alert(String msg, int type) {
        UIManager.put("OptionPane.background", Theme.PANEL);
        UIManager.put("Panel.background", Theme.PANEL);
        UIManager.put("OptionPane.messageForeground", Theme.TEXT);
        JOptionPane.showMessageDialog(this, msg, "Notice", type);
    }
}
