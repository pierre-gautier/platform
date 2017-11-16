package platform.model.license;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

import platform.model.commons.Root;

@Creatable
@Singleton
public class LicenseRoot
        extends Root {
    
    public LicenseRoot() {
        super("licenses"); //$NON-NLS-1$
    }
    
}
