package nl.minvenj.nfi.smartrank.gui.tabs.database;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.domain.DatabaseConfiguration;
import nl.minvenj.nfi.smartrank.gui.SmartRankGUISettings;
import nl.minvenj.nfi.smartrank.io.databases.jdbc.JDBCDriverWrapper;
import nl.minvenj.nfi.smartrank.io.databases.jdbc.drivers.SQLServerDriverWrapper;
import nl.minvenj.nfi.smartrank.io.databases.jdbc.drivers.SybaseDriverWrapper;

public class DBSettingsDialog extends JDialog {

    private static final Logger LOG = LoggerFactory.getLogger(DBSettingsDialog.class);

    private static final List<String> VALID_LOCI = Arrays.asList("CSF1PO", "D3S1358", "D5S818", "D7S820", "D8S1179", "D13S317", "D16S539", "D18S51", "D21S11", "FGA", "THO1", "TPOX", "VWA");

    private final JPanel _contentPanel = new JPanel();
    private boolean _ok;
    private final JTextField _hostPortTextField;
    private final JTextField _usernameTextField;
    private final JPasswordField _passwordField;
    private final JTextField _databaseName;
    private final JComboBox<Object> _dbTypeCombo;
    private final JCheckBox _savePasswordCheckbox;
    private final JTextArea _query;
    private final JLabel _errorLabel;
    private final JProgressBar _progressBar;

    private final JButton _connectButton;
    private final JButton _cancelButton;

    private DatabaseConfiguration _databaseConfig;
    private final SettingsValidator _validator;


    private class SettingsValidator extends Thread {
        private final AtomicBoolean _doUpdate = new AtomicBoolean(true);

        public void triggerUpdate() {
            _doUpdate.set(true);
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    if (_doUpdate.getAndSet(false)) {
                        // Compose a database config object
                        final DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration((JDBCDriverWrapper) _dbTypeCombo.getSelectedItem(), _hostPortTextField.getText(), _databaseName.getText(), _usernameTextField.getText(), new String(_passwordField.getPassword()), _query.getText());
                        // Let the selected wrapper validate the settings
                        try {
                            databaseConfiguration.validate();

                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    _connectButton.setEnabled(true);
                                }
                            });
                        }
                        catch (final Throwable t) {
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    _connectButton.setEnabled(false);
                                    _errorLabel.setText("<html>Error in settings: " + t.getLocalizedMessage());
                                    _errorLabel.setVisible(true);
                                }
                            });
                        }
                    }
                    sleep(100);
                }
            }
            catch (final InterruptedException e) {
            }
        }
    }

    private class ValidationListener implements ItemListener, DocumentListener {
        @Override
        public void itemStateChanged(final ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                validateSettings();
            }
        }

        @Override
        public void insertUpdate(final DocumentEvent e) {
            validateSettings();
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            validateSettings();
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
            validateSettings();
        }

        private void validateSettings() {
            _connectButton.setEnabled(false);
            _errorLabel.setVisible(false);
            _validator.triggerUpdate();
        }
    }

    public DBSettingsDialog(final JFrame owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        setTitle("SmartRank Database Settings");
        setBounds(100, 100, 550, 491);

        _validator = new SettingsValidator();

        getContentPane().setLayout(new MigLayout("", "[183.00px,grow][183.00px,grow]", "[172.00px,grow][33px]"));
        _contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(_contentPanel, "cell 0 0 2 1,grow");

        _contentPanel.setLayout(new MigLayout("", "[][122.00,grow,fill]", "[][][][][][][15.00][108.00,grow][25.00]"));

        _progressBar = new JProgressBar();
        _progressBar.setVisible(false);
        _progressBar.setStringPainted(true);
        getContentPane().add(_progressBar, "cell 0 1,growx");

        final JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, "cell 1 1,alignx right");

        _connectButton = new JButton("Connect");
        _connectButton.setEnabled(false);
        _connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                onConnect();
            }
        });
        buttonPane.add(_connectButton);
        getRootPane().setDefaultButton(_connectButton);

        _cancelButton = new JButton("Cancel");
        _cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                _ok = false;
                _validator.interrupt();
                dispose();
            }
        });
        buttonPane.add(_cancelButton);

        _errorLabel = new JLabel("");
        _errorLabel.setVisible(false);
        _errorLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
        _errorLabel.setOpaque(true);
        _errorLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        _errorLabel.setForeground(Color.WHITE);
        _errorLabel.setBackground(Color.RED);
        _contentPanel.add(_errorLabel, "cell 0 8 2 1,growx");

        final JLabel dbTypeLabel = new JLabel("Database Type");
        _contentPanel.add(dbTypeLabel, "cell 0 0,alignx trailing");

        _dbTypeCombo = new JComboBox<>(new Object[]{new SQLServerDriverWrapper(), new SybaseDriverWrapper()});
        for (int idx = 0; idx < _dbTypeCombo.getModel().getSize(); idx++) {
            if (_dbTypeCombo.getModel().getElementAt(idx).toString().equalsIgnoreCase(SmartRankGUISettings.getDatabaseType())) {
                _dbTypeCombo.setSelectedIndex(idx);
            }
        }
        final ValidationListener validationListener = new ValidationListener();
        _dbTypeCombo.addItemListener(validationListener);
        _contentPanel.add(_dbTypeCombo, "cell 1 0,growx");

        final JLabel hostPortLabel = new JLabel("Host:Port");
        _contentPanel.add(hostPortLabel, "cell 0 1,alignx trailing");

        _hostPortTextField = new JTextField(SmartRankGUISettings.getDatabaseHostPort());
        _hostPortTextField.getDocument().addDocumentListener(validationListener);
        _contentPanel.add(_hostPortTextField, "cell 1 1,growx");
        _hostPortTextField.setColumns(10);

        _contentPanel.add(new JLabel("Database Name"), "cell 0 2,alignx trailing");

        _databaseName = new JTextField(SmartRankGUISettings.getDatabaseSchemaName());
        _databaseName.getDocument().addDocumentListener(validationListener);
        _contentPanel.add(_databaseName, "cell 1 2,growx");
        _databaseName.setColumns(10);

        _contentPanel.add(new JLabel("Username"), "cell 0 3,alignx trailing");
        _usernameTextField = new JTextField(SmartRankGUISettings.getDatabaseUsername());
        _usernameTextField.getDocument().addDocumentListener(validationListener);
        _contentPanel.add(_usernameTextField, "cell 1 3,growx");
        _usernameTextField.setColumns(10);

        _contentPanel.add(new JLabel("Password"), "cell 0 4,alignx trailing");
        _passwordField = new JPasswordField(SmartRankGUISettings.getDatabasePassword());
        _passwordField.getDocument().addDocumentListener(validationListener);
        _contentPanel.add(_passwordField, "cell 1 4,growx");
        _savePasswordCheckbox = new JCheckBox("Save password (insecure!)");
        _contentPanel.add(_savePasswordCheckbox, "cell 1 5");

        _contentPanel.add(new JSeparator(), "flowx,cell 0 6 2 1");
        _contentPanel.add(new JLabel("Sample Query"), "cell 0 7,alignx trailing,aligny top");
        _query = new JTextArea(SmartRankGUISettings.getDatabaseQuery());
        _query.setWrapStyleWord(true);
        _query.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(final DocumentEvent e) {
                onChanged();
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                onChanged();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                onChanged();
            }

            private void onChanged() {
                SmartRankGUISettings.setDatabaseQueryValidated(false);
            }
        });
        _contentPanel.add(new JScrollPane(_query), "cell 1 7,grow");
        _contentPanel.add(new JSeparator(), "cell 1 6");

        _validator.start();
    }

    private void onConnect() {
        // Compose a database config object
        final DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration((JDBCDriverWrapper) _dbTypeCombo.getSelectedItem(), _hostPortTextField.getText(), _databaseName.getText(), _usernameTextField.getText(), new String(_passwordField.getPassword()), _query.getText());
        databaseConfiguration.setSingleRowQuery(SmartRankGUISettings.isSingleRowQuery());
        databaseConfiguration.setSpecimenIdColumnIndex(SmartRankGUISettings.getDatabaseQuerySpecimenIdColumnIndex());
        databaseConfiguration.setLocusColumnIndex(SmartRankGUISettings.getDatabaseQueryLocusColumnIndex());
        databaseConfiguration.setAlleleColumnIndex(SmartRankGUISettings.getDatabaseQueryAlleleColumnIndex());

        // Let the selected wrapper validate the settings
        try {
            databaseConfiguration.validate();
        }
        catch (final Throwable t) {
            LOG.error("Validation error for '{}':", databaseConfiguration.getConnectString(), t);
            JOptionPane.showMessageDialog(this, "<html>" + t.getLocalizedMessage() + "<br>Please review the settings...", "SmartRank database error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final Thread t = new Thread() {
            @Override
            public void run() {

                _errorLabel.setVisible(false);
                try {
                    setBusy();

                    checkQuery(databaseConfiguration, _query.getText());
                    SmartRankGUISettings.setDatabaseType(_dbTypeCombo.getSelectedItem().toString());
                    SmartRankGUISettings.setDatabaseHostPort(_hostPortTextField.getText());
                    SmartRankGUISettings.setDatabaseName(_databaseName.getText());
                    SmartRankGUISettings.setDatabaseUsername(_usernameTextField.getText());
                    SmartRankGUISettings.setDatabaseQuery(_query.getText());
                    SmartRankGUISettings.setDatabaseQuerySpecimenIdColumnIndex(databaseConfiguration.getSpecimenIdColumnIndex());
                    SmartRankGUISettings.setDatabaseQueryLocusColumnIndex(databaseConfiguration.getLocusColumnIndex());
                    SmartRankGUISettings.setDatabaseQueryAlleleColumnIndex(databaseConfiguration.getAlleleColumnIndex());

                    if (_savePasswordCheckbox.isSelected())
                        SmartRankGUISettings.setDatabasePassword(new String(_passwordField.getPassword()));

                    _databaseConfig = databaseConfiguration;

                    // Dismiss the dialog
                    _ok = true;
                    _validator.interrupt();
                    dispose();
                }
                catch (final Throwable e) {
                    LOG.error("Error validating query {}", _query.getText(), e);
                    _errorLabel.setText("<html>Error in query: " + e.getLocalizedMessage().replaceAll("\n", "<br>"));
                    _errorLabel.setVisible(true);
                    setIdle();
                }
            };
        };
        t.start();
    }

    protected void setIdle() {
        setBusy(false);
    }

    protected void setBusy() {
        setBusy(true);
    }

    private void setBusy(final boolean busy) {
        _hostPortTextField.setEnabled(!busy);
        _usernameTextField.setEnabled(!busy);
        _passwordField.setEnabled(!busy);
        _databaseName.setEnabled(!busy);
        _dbTypeCombo.setEnabled(!busy);
        _savePasswordCheckbox.setEnabled(!busy);
        _query.setEnabled(!busy);
        _connectButton.setEnabled(!busy);
        _cancelButton.setEnabled(!busy);
    }

    private void checkQuery(final DatabaseConfiguration config, final String query) throws SQLException {
        if (!SmartRankGUISettings.getDatabaseQueryValidated()) {
            _progressBar.setValue(0);
            _progressBar.setMaximum(4);
            _progressBar.setString("Checking database connection");
            _progressBar.setVisible(true);
            try (final Connection con = DriverManager.getConnection(config.getConnectString(), config.getUserName(), config.getPassword())) {
                con.setReadOnly(true);
                con.setAutoCommit(false);
                final Statement statement = con.createStatement();

                _progressBar.setValue(1);
                _progressBar.setString("Checking query");
                final ResultSet resultSetQuery = statement.executeQuery(query);
                if (!resultSetQuery.next()) {
                    throw new IllegalArgumentException("Query returned no data!");
                }
                final ResultSetMetaData metaData = resultSetQuery.getMetaData();

                _progressBar.setValue(2);
                _progressBar.setString("Checking results");

                boolean singleRowResult = false;

                // If (some of) the column names contain locus names, we are dealing with a query that returns an entire specimen in one row
                for (int col = 1; col <= metaData.getColumnCount(); col++) {
                    final String colName = metaData.getColumnName(col).toUpperCase().replaceAll(" ", "");
                    singleRowResult |= VALID_LOCI.contains(colName);

                    // But we might also be dealing with a query that returns multiple columns for a locus (VWA_1, VWA_2 etc)
                    for (final String valid : VALID_LOCI) {
                        singleRowResult |= colName.startsWith(valid);
                    }
                }

                int idColumn = -1;
                int locusColumn = -1;
                int alleleColumn = -1;
                if (!singleRowResult) {
                    if (metaData.getColumnCount() != 3) {
                        throw new IllegalArgumentException("Expected query that returns 3 columns: specimenId, locus and allele. See the manual for more information.");
                    }

                    for (int colIdx = 1; colIdx <= metaData.getColumnCount(); colIdx++) {
                        final String colName = metaData.getColumnName(colIdx);
                        if (colName.equalsIgnoreCase("specimenId")) {
                            idColumn = colIdx;
                        }

                        if (colName.equalsIgnoreCase("locus")) {
                            locusColumn = colIdx;
                        }

                        if (colName.equalsIgnoreCase("allele")) {
                            alleleColumn = colIdx;
                        }
                    }

                    String errorMessage = "";
                    if (idColumn == -1) {
                        errorMessage += " specimenId";
                    }

                    if (locusColumn == -1) {
                        errorMessage += " locus";
                    }

                    if (alleleColumn == -1) {
                        errorMessage += " allele";
                    }

                    if (!errorMessage.isEmpty()) {
                        String msg = errorMessage.trim();
                        final int lastSpaceIndex = msg.lastIndexOf(" ");
                        if (lastSpaceIndex != -1) {
                            msg = "Columns " + msg.substring(0, lastSpaceIndex).replaceAll(" ", ", ") + " and " + msg.substring(lastSpaceIndex + 1) + " are";
                        }
                        else {
                            msg += "Column " + msg + " is";
                        }

                        throw new IllegalArgumentException("Expected a query that returns columns: 'specimenId', 'locus' and 'allele'.\n" + msg + " missing!\nPlease see the manual for more information.");
                    }
                }

                final String sizeQuery = config.getResultSizeQuery();
                if (sizeQuery != null) {
                    final ResultSet resultSetCount = statement.executeQuery(sizeQuery);
                    if (!resultSetCount.next()) {
                        throw new IllegalArgumentException("Size query returned no data!");
                    }
                }

                config.setSingleRowQuery(singleRowResult);
                config.setSpecimenIdColumnIndex(idColumn);
                config.setLocusColumnIndex(locusColumn);
                config.setAlleleColumnIndex(alleleColumn);
                SmartRankGUISettings.setDatabaseQueryValidated(true);
            }
            finally {
                _progressBar.setVisible(false);
            }
        }
    }

    /**
     * @return true if the user dismissed the dialog with the Connect button
     */
    public boolean isOk() {
        return _ok;
    }

    public DatabaseConfiguration getDBSettings() {
        return _databaseConfig;
    }
}
