package panel;

import java.awt.*;
import javax.swing.JPanel;
import javax.swing.JButton;

class MainPanel extends Panel {

    MainPanel(final PanelHelper helperPanel) {
        super(helperPanel);
        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 2));
        JButton sendButton, obtainButton;
        (sendButton = new JButton("Send Data")).setFont(MainPanel.DEFAULT_BIG_FONT);
        sendButton.addActionListener(e -> MainPanel.this.changeScreen(new SenderPanel(MainPanel.this.frame())));
        sendButton.setBackground(Color.CYAN);
        mainPanel.add(sendButton);
        (obtainButton = new JButton("Obtain Data")).setFont(MainPanel.DEFAULT_BIG_FONT);
        obtainButton.addActionListener(e -> MainPanel.this.changeScreen(new ObtainerPanel(MainPanel.this.frame())));
        mainPanel.add(obtainButton);
        obtainButton.setBackground(Color.MAGENTA);
        mainPanel.setPreferredSize(new Dimension(500, 500));
        this.add(mainPanel);
    }
}