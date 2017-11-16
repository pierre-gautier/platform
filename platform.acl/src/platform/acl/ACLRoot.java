package platform.acl;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

import platform.model.commons.Root;

@Creatable
@Singleton
public class ACLRoot
        extends Root {
    
    public ACLRoot() {
        super("acl-root"); //$NON-NLS-1$
    }
    
}
