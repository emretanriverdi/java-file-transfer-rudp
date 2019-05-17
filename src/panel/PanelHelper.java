package panel;

import javax.swing.*;

public class PanelHelper extends JFrame {
    private Panel panel;

    public PanelHelper() {
        super("UDP");
        this.replaceScreen(new MainPanel(this));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setSize(Panel.WIDTH_HEIGHT);
        this.setLocationRelativeTo(null);
    }

    void replaceScreen(final Panel s) {
        if (this.panel != null)
            this.remove(this.panel.panel());
        this.panel = s;
        this.getContentPane().add(this.panel.panel(), "Center");
        this.revalidate();
        this.repaint();
    }
}