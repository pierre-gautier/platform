package platform.whatsup;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

import platform.model.commons.Root;

@Creatable
@Singleton
public class WhatsupRoot
        extends Root {
    
    public WhatsupRoot() {
        super("whatsup-root"); //$NON-NLS-1$
    }
    
}
