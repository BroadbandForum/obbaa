/*
 * Copyright 2020 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.ipfix.collector.pmdatahandler;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class IPFIXGUI extends JFrame implements ActionListener, WindowListener {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(IPFIXGUI.class);

    private JTextArea m_textArea;
    private JTextField m_textField;
    private long m_startTime;
    private int m_records;

    public IPFIXGUI() {
        LOG.info("TestPMDataHandler started.");

        _createView();
    }

    private void _createView() {
        JPanel panel = new JPanel(new BorderLayout());
        m_textArea = new JTextArea(8, 18);
        m_textField = new JTextField();
        JPanel msgPanel = new JPanel(new BorderLayout());
        msgPanel.add(new JScrollPane(m_textArea,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        JPanel recordsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        recordsPanel.add(new JLabel("Records: "), c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridx++;
        recordsPanel.add(m_textField, c);
        msgPanel.add(recordsPanel, BorderLayout.SOUTH);
        panel.add(msgPanel, BorderLayout.CENTER);

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        JButton startButton = new JButton("Start");
        startButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        addWindowListener(this);
        add(panel);
        pack();
        setLocation(800, 0);
        setSize(300, getPreferredSize().height);
        setTitle("IPFIX message generator");
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        switch (cmd) {
            case "Start":
                if (PMDataHandlerTest.start()) {
                    m_textArea.append("Started.\n");
                    m_startTime = System.currentTimeMillis() / 1000;
                }
                else {
                    if (m_startTime == 0) {
                        m_textArea.append("Failed to start.\n");
                    } else {
                        m_textArea.append("Test already running.\n");
                    }
                }
                break;
            case "Stop":
                if (m_startTime == 0) {
                    m_textArea.append("Test not running. Stopping anyway.\n");
                }
                else {
                    m_textArea.append("Stopped.\n");
                }
                if (m_startTime != 0) {
                    PMDataHandlerTest.stop();
                    long runTime = System.currentTimeMillis() / 1000 - m_startTime;
                    if (runTime == 0) {
                        runTime++;
                    }
                    m_textArea.append("Time [s]: " + runTime + "\n");
                    m_textArea.append("Nbr. of records: " + m_records + "\n");
                    m_textArea.append("Records [1/s]: " + m_records / runTime + "\n");
                }
                m_records = 0;
                m_startTime = 0;
                break;
        }
    }

    public void addMessage(int records) {
        m_records = records;
        SwingUtilities.invokeLater(() -> {
            m_textField.setText(((Integer) m_records).toString());
        });
    }

    public void addMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            m_textField.setText(message);
        });
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (m_startTime != 0) {
            PMDataHandlerTest.pmDataHandler.stop();
        }
        PMDataHandlerTest.destroy();
        LOG.info("TestPMDataHandler stopped.");
        dispose();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}
