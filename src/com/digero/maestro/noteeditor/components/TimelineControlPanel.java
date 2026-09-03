package com.digero.maestro.noteeditor.components;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.BorderLayout;

public class TimelineControlPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int BUTTON_WIDTH = 20;
    private static final int BUTTON_HEIGHT = 22;

    private final JButton zoomInButton;
    private final JButton zoomOutButton;
    private final JLabel zoomLabel;

    public TimelineControlPanel() {
        zoomOutButton = new JButton("-");
        zoomLabel = new JLabel("100%", SwingConstants.CENTER);
        zoomInButton = new JButton("+");

        configureButton(zoomInButton);
        configureButton(zoomOutButton);

        JPanel controls = new JPanel(new BorderLayout());

        controls.add(zoomOutButton, BorderLayout.WEST);
        controls.add(zoomLabel, BorderLayout.CENTER);
        controls.add(zoomInButton, BorderLayout.EAST);

        setLayout(new GridBagLayout());
        add(controls);
    }

    private void configureButton(JButton button) {
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setFocusable(false);
    }

    public void addZoomInListener(ActionListener listener) {
        zoomInButton.addActionListener(listener);
    }

    public void addZoomOutListener(ActionListener listener) {
        zoomOutButton.addActionListener(listener);
    }

    public void setZoomLabel(int zoomPercent) {
        zoomLabel.setText(zoomPercent + "%");
    }

}
