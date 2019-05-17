package panel;

import java.awt.Component;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.Dimension;

class Panel {
    static final Dimension WIDTH_HEIGHT = new Dimension(500, 500);
    static final Font DEFAULT_BIG_FONT = new Font("Calibri", Font.BOLD, 42);
    private PanelHelper transFrame;
    private JPanel panel;

    Panel(final PanelHelper transFrame) {
        this.transFrame = transFrame;
        (this.panel = new JPanel(new GridBagLayout())).setPreferredSize(Panel.WIDTH_HEIGHT);
    }

    JPanel panel() {
        return this.panel;
    }

    PanelHelper frame() {
        return this.transFrame;
    }

    void add(final Component component) {
        this.panel.add(component);
    }

    void changeScreen(final Panel panel) {
        this.transFrame.replaceScreen(panel);
        panel.transFrame = this.transFrame;
    }
}
