package platform.ui.workbench.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public final class CloseActiveEditorHandler {
    
    @CanExecute
    public static boolean canExecute(final EPartService service) {
        return CloseActiveEditorHandler.getActivePart(service) != null;
    }
    
    @Execute
    public static void execute(final EPartService service) {
        final MPart activePart = CloseActiveEditorHandler.getActivePart(service);
        if (activePart == null) {
            return;
        }
        final MUIElement parent = activePart.getParent();
        service.hidePart(activePart);
        if (parent instanceof MPartStack) {
            final MPartStack stack = (MPartStack) parent;
            if (!stack.getChildren().isEmpty()) {
                final MStackElement element = stack.getChildren().get(stack.getChildren().size() - 1);
                if (element instanceof MPart) {
                    final MPart part = (MPart) element;
                    service.activate(part);
                }
            }
        }
    }
    
    private static MPart getActivePart(final EPartService service) {
        final MPart activePart = service.getActivePart();
        if (activePart == null || !activePart.isCloseable() || !activePart.getTags().contains(EPartService.REMOVE_ON_HIDE_TAG)) {
            return null;
        }
        return activePart;
    }
    
}
