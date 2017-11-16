package platform.ui.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import platform.ui.swt.SWTUtils;

public abstract class APlatformAction
        extends Action {
    
    private static final Map<String, ImageDescriptor> PATH_IMAGEDESCRIPTORS = new HashMap<>(32);
    
    public APlatformAction(final String text) {
        super(text);
    }
    
    @Override
    public final ImageDescriptor getImageDescriptor() {
        final String imagePath = this.getImageDescriptorPath();
        if (imagePath == null) {
            return null;
        }
        ImageDescriptor desc = APlatformAction.PATH_IMAGEDESCRIPTORS.get(imagePath);
        if (desc == null) {
            final Image image = SWTUtils.getImageFromUrl(imagePath, 16, 16);
            if (image == null) {
                return null;
            }
            desc = ImageDescriptor.createFromImage(image);
        }
        return desc;
    }
    
    @Override
    public final void run() {
        if (!this.isEnabled()) {
            return;
        }
        ActionHistory.historize(this.internalRun());
    }
    
    public boolean showInMenu() {
        return true;
    }
    
    protected String getImageDescriptorPath() {
        return null;
    }
    
    protected abstract IUndoHandler internalRun();
    
}
