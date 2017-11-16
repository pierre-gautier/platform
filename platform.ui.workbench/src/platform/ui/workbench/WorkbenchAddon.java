package platform.ui.workbench;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.eclipse.swt.widgets.Shell;

import platform.ui.workbench.handlers.QuitHandler;

public class WorkbenchAddon {
    
    @Inject
    @Optional
    public void register(final IWorkbench workbench, final Shell shell) {
        workbench.getApplication().getContext().set(IWindowCloseHandler.class, windowClosed -> {
            QuitHandler.execute(workbench, shell);
            return true;
        });
    }
    
}
