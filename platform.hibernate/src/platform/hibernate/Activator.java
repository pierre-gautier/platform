package platform.hibernate;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public final class Activator
        implements BundleActivator {
    
    @Override
    public void start(final BundleContext bundleContext) {
        final IExtensionRegistry registry = Platform.getExtensionRegistry();
        final IConfigurationElement[] entities = registry.getConfigurationElementsFor("platform.hibernate.entity"); //$NON-NLS-1$
        for (final IConfigurationElement configurationElement : entities) {
            try {
                HibernateDao.register(Class.forName(configurationElement.getAttribute("class"))); //$NON-NLS-1$
            } catch (final ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void stop(final BundleContext bundleContext) {
        // do nothing
    }
    
}
