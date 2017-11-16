package platform.ui.swt;

import java.util.Collection;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;

import platform.ui.actions.IActionSet;

public final class ActionSetListener
        implements KeyListener, MouseListener {
    
    private static void run(final int accelerator, final Collection<IAction> actions) {
        for (final IAction action : actions) {
            if (action.getAccelerator() == accelerator) {
                action.run();
            }
        }
    }
    
    private final IActionSet actionSet;
    
    public ActionSetListener(final IActionSet actionSet) {
        this.actionSet = actionSet;
    }
    
    @Override
    public void keyPressed(final KeyEvent e) {
        final int accelerator = e.stateMask | Character.toUpperCase(e.keyCode);
        ActionSetListener.run(accelerator, this.actionSet.getActions());
        
    }
    
    @Override
    public void keyReleased(final KeyEvent e) {
        // do nothing
    }
    
    @Override
    public void mouseDoubleClick(final MouseEvent e) {
        final IAction defaultAction = this.actionSet.getDefaultAction();
        if (defaultAction != null) {
            defaultAction.run();
        }
    }
    
    @Override
    public void mouseDown(final MouseEvent e) {
        // do nothing
    }
    
    @Override
    public void mouseUp(final MouseEvent e) {
        // do nothing
    }
    
}
