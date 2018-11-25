/*
 * Copyright (C) 2016 Netherlands Forensic Institute
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
package nl.minvenj.nfi.smartrank.gui.tabs.batchmode;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

public class BatchModeDetailPanel extends JPanel {
    private final JTextField _criteriaFileName;
    private final JTextField _requestorName;
    private final JTextField _outputFolderName;
    private final JTextField _status;
    private final JTextField _remarks;
    private final JButton _showReportButton;
    private final JTextField _logFileName;
    private final JTextField _reportFileName;
    private final JButton _showLogButton;
    private final JButton _openFolderButton;
    private final JButton _showLogFolderButton;
    private final JButton _showReportFolderButton;
    private final JButton _openCriteriaFileButton;
    private final JLabel _requestTimestampLabel;
    private final JTextField _requestTimestamp;
    private final JLabel _statusTimestampLabel;
    private final JTextField _statusTimestamp;

    public BatchModeDetailPanel() {
        setBorder(new TitledBorder(null, "Details of selected search criteria file", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new MigLayout("", "[][grow][51.00][][][]", "[][][][][][][]"));

        final JLabel searchCriteriaFileLabel = new JLabel("Search Criteria File");
        add(searchCriteriaFileLabel, "cell 0 0,alignx trailing");

        _criteriaFileName = new JTextField();
        _criteriaFileName.setEditable(false);
        _criteriaFileName.setColumns(10);
        add(_criteriaFileName, "flowx,cell 1 0 4 1,growx");

        _openCriteriaFileButton = new JButton("Open File", new ImageIcon(getClass().getResource("/images/16x16/tag.png")));
        _openCriteriaFileButton.setEnabled(false);
        _openCriteriaFileButton.addActionListener(new OpenInDesktopActionListener(getRootPane(), _criteriaFileName, "opening search criteria file"));
        add(_openCriteriaFileButton, "cell 5 0,growx");

        final JLabel statusLabel = new JLabel("Status");
        add(statusLabel, "cell 0 1,alignx trailing");

        _status = new JTextField();
        _status.setEditable(false);
        _status.setColumns(10);
        add(_status, "cell 1 1,growx");

        _statusTimestampLabel = new JLabel("Date and Time");
        add(_statusTimestampLabel, "cell 2 1");

        _statusTimestamp = new JTextField();
        _statusTimestamp.setEditable(false);
        add(_statusTimestamp, "cell 4 1,growx");
        _statusTimestamp.setColumns(10);

        final JLabel requestorLabel = new JLabel("Requested by");
        add(requestorLabel, "cell 0 2,alignx trailing");

        _requestorName = new JTextField();
        _requestorName.setEditable(false);
        _requestorName.setColumns(10);
        add(_requestorName, "cell 1 2,growx");

        _requestTimestampLabel = new JLabel("Date and Time");
        add(_requestTimestampLabel, "cell 2 2,alignx trailing");

        _requestTimestamp = new JTextField();
        _requestTimestamp.setEditable(false);
        add(_requestTimestamp, "cell 4 2,growx");
        _requestTimestamp.setColumns(10);

        final JLabel outputFolderLabel = new JLabel("Output folder");
        add(outputFolderLabel, "cell 0 3,alignx trailing");

        _outputFolderName = new JTextField();
        _outputFolderName.setEditable(false);
        _outputFolderName.setColumns(10);
        add(_outputFolderName, "flowx,cell 1 3 4 1,growx");

        _openFolderButton = new JButton("Open Folder", new ImageIcon(getClass().getResource("/images/16x16/folder.png")));
        _openFolderButton.setEnabled(false);
        add(_openFolderButton, "cell 5 3,growx");
        _openFolderButton.addActionListener(new OpenInDesktopActionListener(getRootPane(), _outputFolderName, "browsing to folder"));

        final JLabel logFileLabel = new JLabel("Log file");
        add(logFileLabel, "cell 0 4,alignx trailing");

        _logFileName = new JTextField();
        _logFileName.setEditable(false);
        _logFileName.setColumns(10);
        add(_logFileName, "cell 1 4 3 1,growx");

        _showLogButton = new JButton("Show Log", new ImageIcon(getClass().getResource("/images/16x16/text_align_justify.png")));
        _showLogButton.setEnabled(false);
        _showLogButton.addActionListener(new OpenInDesktopActionListener(getRootPane(), _logFileName, "opening log file"));
        add(_showLogButton, "cell 4 4,growx");

        _showLogFolderButton = new JButton("Open Folder", new ImageIcon(getClass().getResource("/images/16x16/folder.png")));
        _showLogFolderButton.setEnabled(false);
        _showLogFolderButton.addActionListener(new OpenParentInDesktopActionListener(getRootPane(), _logFileName, "opening log folder"));
        add(_showLogFolderButton, "cell 5 4,growx");

        final JLabel reportLabel = new JLabel("Report");
        add(reportLabel, "cell 0 5,alignx trailing");

        _reportFileName = new JTextField();
        _reportFileName.setEditable(false);
        _reportFileName.setColumns(10);
        add(_reportFileName, "cell 1 5 3 1,growx");

        _showReportButton = new JButton("Show Report", new ImageIcon(getClass().getResource("/images/16x16/report_go.png")));
        _showReportButton.setEnabled(false);
        _showReportButton.addActionListener(new OpenInDesktopActionListener(getRootPane(), _reportFileName, "opening report"));
        add(_showReportButton, "cell 4 5,growx");

        _showReportFolderButton = new JButton("Open Folder", new ImageIcon(getClass().getResource("/images/16x16/folder.png")));
        _showReportFolderButton.setEnabled(false);
        _showReportFolderButton.addActionListener(new OpenParentInDesktopActionListener(getRootPane(), _reportFileName, "opening report folder"));
        add(_showReportFolderButton, "cell 5 5,growx");

        final JLabel remarksLabel = new JLabel("Remarks");
        add(remarksLabel, "cell 0 6,alignx trailing");

        _remarks = new JTextField();
        _remarks.setEditable(false);
        _remarks.setColumns(10);
        add(_remarks, "cell 1 6 5 1,growx");
    }

    public void setCriteriaFileName(final String criteriaFileName) {
        _criteriaFileName.setText(criteriaFileName);
        _openCriteriaFileButton.setEnabled(!criteriaFileName.isEmpty());
    }

    public void setRequestorName(final String requestorName) {
        _requestorName.setText(requestorName);
    }

    public void setOutputFolderName(final String name) {
        _outputFolderName.setText(name);
        _openFolderButton.setEnabled(!name.isEmpty());
    }

    public void setLogFileName(final String name) {
        _logFileName.setText(name);
        _showLogButton.setEnabled(!name.isEmpty());
        _showLogFolderButton.setEnabled(!name.isEmpty());
    }

    public void setReportFileName(final String name) {
        _reportFileName.setText(name);
        _showReportButton.setEnabled(!name.isEmpty());
        _showReportFolderButton.setEnabled(!name.isEmpty());
    }

    public void setStatus(final String status) {
        _status.setText(status);
    }

    public void setRemarks(final String remarks) {
        _remarks.setText(remarks);
    }

    public void setRequestTimestamp(final String timestamp) {
        _requestTimestamp.setText(timestamp);
    }

    public void setStatusTimestamp(final String timeStamp) {
        _statusTimestamp.setText(timeStamp);
    }

    public void clear() {
        setCriteriaFileName("");
        setRequestorName("");
        setRequestTimestamp("");
        setStatus("");
        setStatusTimestamp("");
        setOutputFolderName("");
        setLogFileName("");
        setReportFileName("");
        setRemarks("");
    }
}