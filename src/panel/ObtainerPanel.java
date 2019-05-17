package panel;

import obtainer.ObtainerManager;
import obtainer.ObtainerInterface;

import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.net.SocketException;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JLabel;

class ObtainerPanel extends Panel {
    private JLabel fileLabel;
    private JButton backButton, declineButton;
    private ObtainerManager obtainerManager;

    ObtainerPanel(final PanelHelper frame) {
        super(frame);
        try {
            (this.obtainerManager = new ObtainerManager()).setFileReceiverListener(new ObtainerInterface() {
                @Override
                public boolean infoReceived(final String fileName, final int fileSize) {
                    ObtainerPanel.this.fileLabel.setText(fileName);
                    int response = JOptionPane.showConfirmDialog(ObtainerPanel.this.frame(), "Is this file " + fileName + " true?", "Confirm File", JOptionPane.YES_NO_OPTION);
                    if (response != 0) {
                        ObtainerPanel.this.changeScreen(new MainPanel(ObtainerPanel.this.frame()));
                        return false;
                    }
                    final JFileChooser jfc = new JFileChooser();
                    jfc.setSelectedFile(new File(fileName));
                    response = jfc.showSaveDialog(ObtainerPanel.this.frame());
                    if (response == 0) {
                        ObtainerPanel.this.obtainerManager.setSource(jfc.getSelectedFile());
                        ObtainerPanel.this.fileLabel.setText(jfc.getSelectedFile().getName());
                        System.out.println(jfc.getSelectedFile());
                        ObtainerPanel.this.backButton.setEnabled(false);
                        ObtainerPanel.this.declineButton.setEnabled(true);
                        return true;
                    }
                    ObtainerPanel.this.changeScreen(new MainPanel(ObtainerPanel.this.frame()));
                    return false;
                }

                @Override
                public void fileReceived(final int percent) {
                    if (percent == 100) {
                        ObtainerPanel.this.declineButton.setEnabled(false);
                        ObtainerPanel.this.backButton.setEnabled(true);
                        JOptionPane.showMessageDialog(ObtainerPanel.this.frame(), "File transfer is finished!");
                    }
                }

                @Override
                public void errorHappened(final int errorCode) {
                    if (errorCode == 2) {
                        ObtainerPanel.this.obtainerManager.finish();
                        ObtainerPanel.this.changeScreen(new MainPanel(ObtainerPanel.this.frame()));
                        JOptionPane.showMessageDialog(ObtainerPanel.this.frame(), "File transfer is declined by the source.", "Aborted", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        } catch (SocketException e) {
            e.printStackTrace();
        }
        final JPanel mainPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0;
        c.anchor = 17;
        c.insets = new Insets(2, 2, 8, 2);
        mainPanel.add(new JLabel("File:"), c);
        this.fileLabel = new JLabel("Waiting for the file...");
        c.gridx = 1; c.gridy = 0;
        mainPanel.add(this.fileLabel, c);
        c.gridx = 0; c.gridy = 1;
        c.insets = new Insets(10, 2, 2, 2);
        final JPanel bottomPanel = new JPanel(new FlowLayout(1));
        (this.backButton = new JButton("Back")).addActionListener(e -> {
            ObtainerPanel.this.obtainerManager.finish();
            ObtainerPanel.this.changeScreen(new MainPanel(ObtainerPanel.this.frame()));
        });
        (this.declineButton = new JButton("Decline")).setEnabled(false);
        this.declineButton.addActionListener(e -> {
            ObtainerPanel.this.obtainerManager.decline();
            ObtainerPanel.this.backButton.setEnabled(true);
            ObtainerPanel.this.declineButton.setEnabled(false);
            ObtainerPanel.this.changeScreen(new MainPanel(ObtainerPanel.this.frame()));
        });
        bottomPanel.add(this.backButton);
        bottomPanel.add(this.declineButton);
        c.gridx = 0; c.gridy = 3; c.gridwidth = 3; c.fill = 2;
        mainPanel.add(bottomPanel, c);
        this.add(mainPanel);
        this.obtainerManager.listen();
    }
}
