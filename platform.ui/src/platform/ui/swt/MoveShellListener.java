package platform.ui.swt;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public final class MoveShellListener
        extends MouseAdapter
        implements MouseMoveListener {
    
    private boolean     isDraged;
    private int         x;
    private int         y;
    private final Shell shell;
    
    public MoveShellListener(final Shell shell) {
        super();
        this.shell = shell;
        this.addListener(shell);
    }
    
    @Override
    public void mouseDown(final MouseEvent e) {
        if (e.button == 1) {
            this.isDraged = true;
            this.x = e.x;
            this.y = e.y;
        }
    }
    
    @Override
    public void mouseMove(final MouseEvent e) {
        if (this.isDraged) {
            final int deltaX = this.x - e.x;
            final int deltaY = this.y - e.y;
            final Rectangle bounds = this.shell.getBounds();
            this.shell.setBounds(bounds.x - deltaX, bounds.y - deltaY, bounds.width, bounds.height);
        }
    }
    
    @Override
    public void mouseUp(final MouseEvent e) {
        if (e.button == 1) {
            this.isDraged = false;
        }
    }
    
    private void addListener(final Composite control) {
        control.addMouseListener(this);
        control.addMouseMoveListener(this);
        final Composite composite = control;
        for (final Control child : composite.getChildren()) {
            if (child instanceof Composite) {
                this.addListener((Composite) child);
            }
        }
    }
    
}
