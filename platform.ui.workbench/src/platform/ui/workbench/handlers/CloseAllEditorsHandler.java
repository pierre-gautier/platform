package platform.ui.workbench.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public final class CloseAllEditorsHandler {
    
    @CanExecute
    public static boolean canExecute(final EPartService service) {
        for (final MPart part : service.getParts()) {
            if (part.getTags().contains(EPartService.REMOVE_ON_HIDE_TAG) && part.isCloseable()) {
                return true;
            }
        }
        return false;
    }
    
    @Execute
    public static void execute(final EPartService service) {
        try {
            for (final MPart part : service.getParts()) {
                if (part.getTags().contains(EPartService.REMOVE_ON_HIDE_TAG) && part.isCloseable()) {
                    service.hidePart(part);
                }
            }
        } catch (final IllegalStateException e) {
            // ignore
        }
    }
}
