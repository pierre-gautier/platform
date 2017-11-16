package platform.sql;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public final class Activator
        implements BundleActivator {
    
    @Override
    @SuppressWarnings("unchecked")
    public void start(final BundleContext bundleContext) {
        final IExtensionRegistry registry = Platform.getExtensionRegistry();
        for (final IConfigurationElement configurationElement : registry.getConfigurationElementsFor("platform.sql.descriptor")) { //$NON-NLS-1$
            try {
                final Class<IDatabaseDescriptorFactory> c = (Class<IDatabaseDescriptorFactory>) Class.forName(configurationElement.getAttribute("class")); //$NON-NLS-1$
                final IDatabaseDescriptorFactory f = c.newInstance();
                DatabaseDescriptorFactories.INSTANCE.register(f);
            } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void stop(final BundleContext bundleContext) {
        // do nothing
    }
    
}
