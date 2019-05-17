package panel;

import sender.SenderManager;
import sender.SenderInterface;

import java.awt.FlowLayout;
import java.io.IOException;
import javax.swing.JOptionPane;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import javax.swing.JFileChooser;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

class SenderPanel extends Panel {
    private JTextField ipText, fileText;
    private JButton backButton, sendButton;
    private File selectedFile;
    private SenderManager senderManager;

    SenderPanel(final PanelHelper frame) {
        super(frame);
        final JPanel mainPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        (this.fileText = new JTextField(14)).setEditable(false);
        c.gridx = 0; c.gridy = 0;
        c.anchor = 17; c.fill = 0;
        c.insets = new Insets(2, 2, 2, 2);
        final JLabel fileLabel = new JLabel("Path:");
        mainPanel.add(fileLabel, c);
        c.gridx = 1; c.gridy = 0; c.fill = 2;
        mainPanel.add(this.fileText, c);
        JButton chooseButton;
        (chooseButton = new JButton("Browse")).addActionListener(e -> {
            final JFileChooser jfc = new JFileChooser();
            final int response = jfc.showOpenDialog(SenderPanel.this.frame());
            if (response == 0) {
                SenderPanel.this.selectedFile = jfc.getSelectedFile();
                SenderPanel.this.fileText.setText(SenderPanel.this.selectedFile.getAbsolutePath());
                SenderPanel.this.sendButton.setEnabled(!SenderPanel.this.fileText.getText().isEmpty() && !SenderPanel.this.ipText.getText().trim().isEmpty());
            }
        });
        c.gridx = 2; c.gridy = 0; c.fill = 0;
        mainPanel.add(chooseButton, c);
        (this.ipText = new JTextField(14)).addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(final KeyEvent e) {
                if (SenderPanel.this.ipText.getText().trim().isEmpty() || SenderPanel.this.fileText.getText().trim().isEmpty()) {
                    SenderPanel.this.sendButton.setEnabled(false);
                } else SenderPanel.this.sendButton.setEnabled(true);
            }

            @Override
            public void keyTyped(final KeyEvent e) {
                if ((!Character.isDigit(e.getKeyChar()) && e.getKeyChar() != '.') || SenderPanel.this.ipText.getText().length() == 15) e.consume();
            }
        });
        c.gridx = 0; c.gridy = 1;
        final JLabel ipLabel = new JLabel("Obtainer's IP:");
        mainPanel.add(ipLabel, c);
        c.gridx = 1; c.gridy = 1; c.fill = 2;
        mainPanel.add(this.ipText, c);
        (this.sendButton = new JButton("Send")).addActionListener(e -> {
            try {
                if (SenderPanel.this.senderManager != null && SenderPanel.this.senderManager.isContinuation()) {
                    SenderPanel.this.sendButton.setEnabled(false);
                    SenderPanel.this.senderManager.decline();
                    SenderPanel.this.changeScreen(new SenderPanel(SenderPanel.this.frame()));
                } else {
                    SenderPanel.this.senderManager = new SenderManager(SenderPanel.this.ipText.getText(), SenderPanel.this.selectedFile);
                    SenderPanel.this.senderManager.setFileSenderListener(new SenderInterface() {
                        @Override
                        public void fileSent(final int percent) {
                            SenderPanel.this.sendButton.setText("Decline");
                            SenderPanel.this.sendButton.setEnabled(true);
                            if (percent == 100) {
                                SenderPanel.this.sendButton.setText("Send");
                                SenderPanel.this.sendButton.setEnabled(false);
                                SenderPanel.this.backButton.setEnabled(true);
                                JOptionPane.showMessageDialog(SenderPanel.this.frame(), "File transfer is finished!");
                            }
                        }

                        @Override
                        public void errorHappened(final int errorCode) {
                            if (errorCode == 2) {
                                SenderPanel.this.senderManager.finish();
                                JOptionPane.showMessageDialog(SenderPanel.this.frame(), "File transfer is declined by the receiver.", "Declined", JOptionPane.ERROR_MESSAGE);
                                SenderPanel.this.changeScreen(new SenderPanel(SenderPanel.this.frame()));
                            } else if (errorCode == 1) {
                                SenderPanel.this.senderManager.finish();
                                JOptionPane.showMessageDialog(SenderPanel.this.frame(), "File transfer is refused by the receiver.", "Refused", JOptionPane.ERROR_MESSAGE);
                                SenderPanel.this.changeScreen(new SenderPanel(SenderPanel.this.frame()));
                            }
                        }
                    });
                    SenderPanel.this.senderManager.send();
                    SenderPanel.this.sendButton.setEnabled(false);
                    SenderPanel.this.backButton.setEnabled(false);
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        });
        this.sendButton.setEnabled(false);
        c.gridx = 2; c.gridy = 1; c.fill = 0;
        mainPanel.add(this.sendButton, c);
        c.insets = new Insets(10, 2, 2, 2);
        c.gridx = 1; c.gridy = 2;
        final JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        (this.backButton = new JButton("Back")).addActionListener(e -> {
            if (SenderPanel.this.senderManager != null)
                SenderPanel.this.senderManager.finish();
            SenderPanel.this.changeScreen(new MainPanel(SenderPanel.this.frame()));
        });
        bottomPanel.add(this.backButton);
        c.gridx = 0; c.gridy = 3; c.gridwidth = 3; c.fill = 2;
        mainPanel.add(bottomPanel, c);
        this.add(mainPanel);
    }
}
