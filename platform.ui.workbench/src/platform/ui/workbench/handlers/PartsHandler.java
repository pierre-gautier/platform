package platform.ui.workbench.handlers;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import platform.ui.messages.Messages;
import platform.ui.swt.MoveShellListener;
import platform.ui.swt.SWTUtils;

public final class PartsHandler {
    
    @CanExecute
    public static boolean canExecute(final EPartService service) {
        return !service.getParts().isEmpty();
    }
    
    @Execute
    public static void execute(final EPartService service) {
        
        final Shell shell = SWTUtils.createShellTool(SWTUtils.getDisplay(), 1, true, false, true, 5, true);
        
        final Collection<MPart> parts = service.getParts();
        final Collection<Button> buttons = new ArrayList<>(parts.size());
        
        for (final MPart part : parts) {
            if (part.getTags().contains(EPartService.REMOVE_ON_HIDE_TAG)
                    || !part.isCloseable()) {
                continue;
            }
            final Button check = SWTUtils.createButtonCheck(shell, part.getLocalizedLabel());
            check.setSelection(part.isToBeRendered());
            check.setData(part);
            buttons.add(check);
        }
        
        final Composite composite = SWTUtils.createComposite(shell, 2, true, 0, 0);
        
        SWTUtils.createButtonDefault(composite, SWT.END, SWT.CENTER, Messages.keywordOk, new SelectionAdapter() {
            
            @Override
            public void widgetSelected(final SelectionEvent e) {
                for (final Button check : buttons) {
                    final MPart part = (MPart) check.getData();
                    if (check.getSelection()) {
                        service.activate(part);
                    } else {
                        service.hidePart(part);
                    }
                }
                shell.close();
            }
        });
        
        SWTUtils.createButtonPush(composite, SWT.END, SWT.CENTER, Messages.keywordCancel, new SelectionAdapter() {
            
            @Override
            public void widgetSelected(final SelectionEvent e) {
                shell.close();
            }
        });
        
        new MoveShellListener(shell);
        
        final Point size = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        final Point pt = SWTUtils.getDisplay().getCursorLocation();
        
        shell.setBounds(pt.x, pt.y, size.x, size.y);
        shell.open();
        
        return;
        
    }
}
