package platform.preferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

import platform.model.AStrategy;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IObject;
import platform.model.IRoot;
import platform.model.utils.NodeUtils;
import platform.model.utils.TraversalContext;

@Creatable
@Singleton
public class PreferencesStrategy
        extends AStrategy {
    
    private final IRoot root;
    
    @Inject
    public PreferencesStrategy(final PreferencesRoot root) {
        this.root = root;
    }
    
    @Override
    public <T> T getAttribute(final IObject object, final Descriptor<T> descriptor, final Object value) {
        final INode preferenceNode = NodeUtils.find(this.root, new TraversalContext(), object.getId());
        if (preferenceNode == null) {
            return null;
        }
        return preferenceNode.getAttribute(descriptor);
    }
    
}
