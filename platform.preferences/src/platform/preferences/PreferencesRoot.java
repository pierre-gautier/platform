package platform.preferences;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

import platform.model.commons.Root;

@Creatable
@Singleton
public class PreferencesRoot
        extends Root {
    
    protected PreferencesRoot() {
        super("preferences-root"); //$NON-NLS-1$
    }
    
}
