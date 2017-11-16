package license.server.application;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals;

@SuppressWarnings("restriction")
public class E4LifeCycle {
    
    @PostContextCreate
    void postContextCreate(final IEclipseContext workbenchContext) {
        System.out.println("E4LifeCycle.postContextCreate() " + workbenchContext); //$NON-NLS-1$
    }
    
    @PreSave
    void preSave(final IEclipseContext workbenchContext) {
        System.out.println("E4LifeCycle.preSave()" + workbenchContext); //$NON-NLS-1$
    }
    
    @ProcessAdditions
    void processAdditions(final IEclipseContext workbenchContext) {
        System.out.println("E4LifeCycle.processAdditions()" + workbenchContext); //$NON-NLS-1$
    }
    
    @ProcessRemovals
    void processRemovals(final IEclipseContext workbenchContext) {
        System.out.println("E4LifeCycle.processRemovals()" + workbenchContext); //$NON-NLS-1$
    }
}
