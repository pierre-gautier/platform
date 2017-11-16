package platform.acl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

import platform.model.AStrategy;
import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IObject;
import platform.model.IRoot;
import platform.model.utils.NodeUtils;
import platform.model.utils.TraversalContext;

@Creatable
@Singleton
public class ACLStrategy
        extends AStrategy {
    
    private final IRoot root;
    
    @Inject
    public ACLStrategy(final ACLRoot root) {
        this.root = root;
    }
    
    @Override
    public void addAttributes(final IObject object, final Collection<Attribute> values) {
        if (this.getAttribute(object, ACLDescriptors.UPDATE, null) != Boolean.TRUE) {
            values.clear();
        }
    }
    
    @Override
    public <T> T getAttribute(final IObject object, final Descriptor<T> descriptor, final Object value) {
        if (descriptor != ACLDescriptors.CREATE
                && descriptor != ACLDescriptors.UPDATE
                && descriptor != ACLDescriptors.DELETE
                && descriptor != ACLDescriptors.RETRIEVE) {
            return null;
        }
        final INode aclNode = NodeUtils.find(this.root, new TraversalContext(), object.getId());
        if (aclNode == null) {
            return null;
        }
        return aclNode.getAttribute(descriptor);
    }
    
}
