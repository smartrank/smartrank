/*
 * Copyright (C) 2017 Netherlands Forensic Institute
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.minvenj.nfi.smartrank.gui.tabs.database.config;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.domain.DatabaseConfiguration;
import nl.minvenj.nfi.smartrank.gui.SmartRankGUISettings;
import nl.minvenj.nfi.smartrank.gui.utils.UndoDecorator;
import nl.minvenj.nfi.smartrank.io.databases.jdbc.JDBCDriverWrapper;
import nl.minvenj.nfi.smartrank.io.databases.jdbc.drivers.H2DriverWrapper;
import nl.minvenj.nfi.smartrank.io.databases.jdbc.drivers.SQLServerDriverWrapper;
import nl.minvenj.nfi.smartrank.io.databases.jdbc.drivers.SybaseDriverWrapper;

public class DBSettingsDialog extends JDialog implements DocumentListener {

    private static final Logger LOG = LoggerFactory.getLogger(DBSettingsDialog.class);

    private final JSpinner _batchSizeSpinner;
    private final JButton _cancelButton;
    private final JButton _connectButton;
    private final JTextField _databaseName;
    private final JTextArea _databaseRevisionQuery;
    private final JComboBox<JDBCDriverWrapper> _databaseTypeCombo;
    private final JLabel _errorLabel;
    private final JTextField _hostPortTextField;
    private final JPasswordField _passwordField;
    private final JProgressBar _progressBar;
    private final JTextArea _sampleKeysQuery;
    private final JLabel _sampleKeysQueryTabTitle;
    private final JTextArea _sampleQuery;
    private final JLabel _sampleQueryTabTitle;
    private final JCheckBox _savePasswordCheckbox;
    private final JTextField _usernameTextField;
    private final JLabel _hostPortLabel;

    private DatabaseConfiguration _databaseConfig;
    private boolean _ok;

    public DBSettingsDialog(final JFrame owner, final boolean autoConnect) {
        super(owner, ModalityType.APPLICATION_MODAL);
        setTitle("SmartRank Database Settings");
        setBounds(100, 100, 550, 491);
        getContentPane().setLayout(new MigLayout("", "[183.00px,grow][183.00px,grow]", "[172.00px,grow][33px]"));

        final JPanel contentPanel = new JPanel(new MigLayout("", "[][122.00,grow,fill]", "[][][][][][][15.00][104.00,grow][25.00]"));
        contentPanel.setName("contentPanel");
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, "cell 0 0 2 1,grow");

        _progressBar = new JProgressBar();
        _progressBar.setVisible(false);
        _progressBar.setStringPainted(true);
        _progressBar.setName("progressBar");
        getContentPane().add(_progressBar, "cell 0 1,growx");

        final JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPane.setName("buttonPane");
        getContentPane().add(buttonPane, "cell 1 1,alignx right");

        _connectButton = new JButton("Connect");
        _connectButton.setName("connectButton");
        _connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                onConnect();
            }
        });
        buttonPane.add(_connectButton);
        getRootPane().setDefaultButton(_connectButton);

        _cancelButton = new JButton("Cancel");
        _cancelButton.setName("cancelButton");
        _cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                _ok = false;
                dispose();
            }
        });
        buttonPane.add(_cancelButton);

        final JTabbedPane queriesTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        queriesTabbedPane.setName("queriesTabbedPane");
        contentPanel.add(queriesTabbedPane, "cell 0 7 2 1,grow");

        final JPanel sampleKeysQueryPanel = new JPanel(new MigLayout("", "[grow][20%]", "[][grow][]"));
        sampleKeysQueryPanel.setName("sampleKeysQueryPanel");
        sampleKeysQueryPanel.add(new JLabel("This query retrieves the keys of all valid specimens in the database."), "cell 0 0 2 1");

        _batchSizeSpinner = new JSpinner(new SpinnerNumberModel(SmartRankGUISettings.getDatabaseSpecimenBatchSize(), new Integer(0), null, new Integer(100)));
        _batchSizeSpinner.setEditor(new JSpinner.NumberEditor(_batchSizeSpinner, "#"));
        _batchSizeSpinner.setName("batchSize");
        final JLabel batchSizeLabel = new JLabel("Batch size for specimen retrieval (0=all at once)");
        batchSizeLabel.setLabelFor(_batchSizeSpinner);
        sampleKeysQueryPanel.add(batchSizeLabel, "flowx,cell 0 2,alignx trailing");
        sampleKeysQueryPanel.add(_batchSizeSpinner, "cell 1 2,growx");

        _sampleKeysQuery = new JTextArea(SmartRankGUISettings.getDatabaseSpecimenKeysQuery());
        UndoDecorator.register(_sampleKeysQuery);
        _sampleKeysQuery.setName("sampleKeysQuery");
        _sampleKeysQuery.getDocument().addDocumentListener(new QueryInvalidatingDocumentListener());
        _sampleKeysQuery.getDocument().addDocumentListener(this);
        sampleKeysQueryPanel.add(new JScrollPane(_sampleKeysQuery), "cell 0 1 2 1,grow");
        queriesTabbedPane.addTab("Specimen Keys Query", null, sampleKeysQueryPanel, "This query is used to obtain the keys for specimens that qualify for a SmartRank search");
        _sampleKeysQueryTabTitle = new JLabel("Specimen Keys Query");
        queriesTabbedPane.setTabComponentAt(0, _sampleKeysQueryTabTitle);

        _sampleQuery = new JTextArea(SmartRankGUISettings.getDatabaseSpecimenQuery());
        _sampleQuery.setName("sampleQuery");
        UndoDecorator.register(_sampleQuery);
        _sampleQuery.setWrapStyleWord(true);
        _sampleQuery.getDocument().addDocumentListener(new QueryInvalidatingDocumentListener());
        _sampleQuery.getDocument().addDocumentListener(this);
        final JPanel sampleQueryPanel = new JPanel(new MigLayout("", "[2px,grow]", "[][2px,grow]"));
        sampleQueryPanel.add(new JLabel("This query is used to retrieve specimen data from the database."), "cell 0 0");
        sampleQueryPanel.add(new JScrollPane(_sampleQuery), "cell 0 1,grow");
        queriesTabbedPane.addTab("Specimen Retrieval Query", null, sampleQueryPanel, "This query is used to obtain the data for specimens that qualify for a SmartRank search");
        _sampleQueryTabTitle = new JLabel("Specimen Retrieval Query");
        queriesTabbedPane.setTabComponentAt(1, _sampleQueryTabTitle);

        _databaseRevisionQuery = new JTextArea(SmartRankGUISettings.getDatabaseRevisionQuery());
        _databaseRevisionQuery.getDocument().addDocumentListener(new QueryInvalidatingDocumentListener());
        _databaseRevisionQuery.setName("databaseRevisionQuery");
        UndoDecorator.register(_databaseRevisionQuery);
        final JPanel databaseRevisionQueryPanel = new JPanel(new MigLayout("", "[grow]", "[][grow]"));
        databaseRevisionQueryPanel.add(new JLabel("This query is used to get a unique value that identifies the current state of the database."), "cell 0 0");
        databaseRevisionQueryPanel.add(new JScrollPane(_databaseRevisionQuery), "cell 0 1,grow");
        queriesTabbedPane.addTab("Database Revision Query", null, databaseRevisionQueryPanel, "This query is used to obtain a value identifying the current state of the database");

        _errorLabel = new JLabel("");
        _errorLabel.setName("errorLabel");
        _errorLabel.setVisible(false);
        _errorLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
        _errorLabel.setOpaque(true);
        _errorLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        _errorLabel.setForeground(Color.WHITE);
        _errorLabel.setBackground(Color.RED);
        contentPanel.add(_errorLabel, "cell 0 8 2 1,growx");

        final JLabel dbTypeLabel = new JLabel("Database Type");
        contentPanel.add(dbTypeLabel, "cell 0 0,alignx trailing");

        _databaseTypeCombo = new JComboBox<>(new JDBCDriverWrapper[]{new SQLServerDriverWrapper(), new SybaseDriverWrapper(), new H2DriverWrapper()});
        _databaseTypeCombo.setName("databaseType");
        for (int idx = 0; idx < _databaseTypeCombo.getModel().getSize(); idx++) {
            if (_databaseTypeCombo.getModel().getElementAt(idx).toString().equalsIgnoreCase(SmartRankGUISettings.getDatabaseType())) {
                _databaseTypeCombo.setSelectedIndex(idx);
            }
        }
        contentPanel.add(_databaseTypeCombo, "cell 1 0,growx");

        _hostPortLabel = new JLabel("Host:Port");
        _hostPortLabel.setName("hostPortLabel");
        contentPanel.add(_hostPortLabel, "cell 0 1,alignx trailing");

        _hostPortTextField = new JTextField(SmartRankGUISettings.getDatabaseHostPort());
        _hostPortTextField.setName("hostPort");
        _hostPortTextField.setColumns(10);
        _hostPortTextField.getDocument().addDocumentListener(this);
        UndoDecorator.register(_hostPortTextField);
        contentPanel.add(_hostPortTextField, "cell 1 1,growx");

        contentPanel.add(new JLabel("Database Name"), "cell 0 2,alignx trailing");

        _databaseName = new JTextField(SmartRankGUISettings.getDatabaseSchemaName());
        _databaseName.setName("databaseName");
        _databaseName.setColumns(10);
        UndoDecorator.register(_databaseName);
        contentPanel.add(_databaseName, "cell 1 2,growx");

        contentPanel.add(new JLabel("Username"), "cell 0 3,alignx trailing");
        _usernameTextField = new JTextField(SmartRankGUISettings.getDatabaseUsername());
        _usernameTextField.setName("userName");
        _usernameTextField.setColumns(10);
        UndoDecorator.register(_usernameTextField);
        contentPanel.add(_usernameTextField, "cell 1 3,growx");

        contentPanel.add(new JLabel("Password"), "cell 0 4,alignx trailing");
        _passwordField = new JPasswordField(SmartRankGUISettings.getDatabasePassword());
        _passwordField.setName("password");
        contentPanel.add(_passwordField, "cell 1 4,growx");
        _savePasswordCheckbox = new JCheckBox("Save password (insecure!)");
        _savePasswordCheckbox.setName("savePassword");
        contentPanel.add(_savePasswordCheckbox, "cell 1 5");

        contentPanel.add(new JSeparator(), "flowx,cell 0 6 2 1");
        contentPanel.add(new JSeparator(), "cell 1 6");

        // Trigger update of the error string and connect button based on the initial settings
        insertUpdate(null);

        if (autoConnect) {
            Executors.newSingleThreadScheduledExecutor().submit(() -> _connectButton.doClick());
        }
    }

    private void onConnect() {
        // Compose a database config object
        final DatabaseConfiguration databaseConfiguration = getDBConfig();
        databaseConfiguration.setSingleRowQuery(SmartRankGUISettings.isSingleRowSpecimenQuery());
        databaseConfiguration.setSpecimenIdColumnIndex(SmartRankGUISettings.getDatabaseQuerySpecimenIdColumnIndex());
        databaseConfiguration.setLocusColumnIndex(SmartRankGUISettings.getDatabaseQueryLocusColumnIndex());
        databaseConfiguration.setAlleleColumnIndex(SmartRankGUISettings.getDatabaseQueryAlleleColumnIndex());

        // Let the selected wrapper validate the settings
        try {
            databaseConfiguration.validate();
        }
        catch (final Throwable t) {
            LOG.error("Validation error for '{}':", databaseConfiguration.getConnectString(), t);
            setErrorMessage("Validation error: " + t.getLocalizedMessage());
            return;
        }

        new QueryValidatorThread(this, databaseConfiguration).start();
    }

    private DatabaseConfiguration getDBConfig() {
        return new DatabaseConfiguration(_databaseTypeCombo.getItemAt(_databaseTypeCombo.getSelectedIndex()),
                                         _hostPortTextField.getText(),
                                         _databaseName.getText(),
                                         _usernameTextField.getText(),
                                         new String(_passwordField.getPassword()),
                                         _sampleKeysQuery.getText(),
                                         _sampleQuery.getText(),
                                         _databaseRevisionQuery.getText());
    }

    void setIdle() {
        setBusy(false);
    }

    void setBusy() {
        setBusy(true);
    }

    private void setBusy(final boolean busy) {
        _hostPortTextField.setEnabled(!busy);
        _usernameTextField.setEnabled(!busy);
        _passwordField.setEnabled(!busy);
        _databaseName.setEnabled(!busy);
        _databaseTypeCombo.setEnabled(!busy);
        _savePasswordCheckbox.setEnabled(!busy);
        _sampleQuery.setEnabled(!busy);
        _sampleKeysQuery.setEnabled(!busy);
        _databaseRevisionQuery.setEnabled(!busy);
        _connectButton.setEnabled(!busy);
        _cancelButton.setEnabled(!busy);
    }

    void clearErrorMessage() {
        _errorLabel.setVisible(false);
    }

    void setErrorMessage(final String message) {
        _errorLabel.setText("<html>" + message.replaceAll("\n", "<br>"));
        _errorLabel.setVisible(true);
    }

    /**
     * @return true if the user dismissed the dialog with the Connect button
     */
    public boolean isOk() {
        return _ok;
    }

    void setOk() {
        _ok = true;
    }

    public DatabaseConfiguration getDBSettings() {
        return _databaseConfig;
    }

    void setDBSettings(final DatabaseConfiguration databaseConfiguration) {
        _databaseConfig = databaseConfiguration;
    }

    /**
     * @return the batchSizeSpinner
     */
    Integer getBatchSize() {
        return (Integer) _batchSizeSpinner.getValue();
    }

    /**
     * @return the databaseTypeCombo
     */
    JDBCDriverWrapper getDatabaseType() {
        return (JDBCDriverWrapper) _databaseTypeCombo.getSelectedItem();
    }

    /**
     * @return the hostPortTextField
     */
    public String getHostAndPort() {
        return _hostPortTextField.getText();
    }

    public String getDatabaseName() {
        return _databaseName.getText();
    }

    public String getUsername() {
        return _usernameTextField.getText();
    }

    public String getSampleQuery() {
        return _sampleQuery.getText();
    }

    public boolean isSavePassword() {
        return _savePasswordCheckbox.isSelected();
    }

    public String getSampleKeysQuery() {
        return _sampleKeysQuery.getText();
    }

    public String getDatabaseRevisionQuery() {
        return _databaseRevisionQuery.getText();
    }

    public String getPassword() {
        return new String(_passwordField.getPassword());
    }

    public void startProgress(final int max, final String message) {
        _progressBar.setValue(0);
        _progressBar.setMaximum(max);
        _progressBar.setString(message);
        _progressBar.setVisible(true);
    }

    void stopProgress() {
        _progressBar.setVisible(false);
    }

    public void stepProgress(final String message) {
        _progressBar.setValue(_progressBar.getValue() + 1);
        _progressBar.setString(message);
    }

    @Override
    public void insertUpdate(final DocumentEvent e) {
        clearErrorMessage();
        String msg = "";
        msg += checkNotEmpty(_hostPortTextField, _hostPortLabel, "<li>Database Host and Port</li>");
        msg += checkNotEmpty(_sampleKeysQuery, _sampleKeysQueryTabTitle, "<li>Specimen Keys Query</li>");
        msg += checkNotEmpty(_sampleQuery, _sampleQueryTabTitle, "<li>Specimen Retrieval Query</li>");

        if (!msg.isEmpty()) {
            setErrorMessage("Settings incomplete. Please enter value for <UL>" + msg);
        }
        _connectButton.setEnabled(msg.isEmpty());
    }

    private String checkNotEmpty(final JTextComponent field, final JLabel label, final String msg) {
        if (field.getText().trim().isEmpty()) {
            label.setForeground(Color.RED);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            return msg;
        }
        label.setForeground(null);
        label.setFont(label.getFont().deriveFont(Font.PLAIN));
        return "";
    }

    @Override
    public void removeUpdate(final DocumentEvent e) {
        insertUpdate(e);
    }

    @Override
    public void changedUpdate(final DocumentEvent e) {
        insertUpdate(e);
    }
}
