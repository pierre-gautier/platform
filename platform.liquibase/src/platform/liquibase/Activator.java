package platform.liquibase;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public final class Activator
        implements BundleActivator {
    
    @Override
    @SuppressWarnings("nls")
    public void start(final BundleContext bundleContext) {
        final IExtensionRegistry registry = Platform.getExtensionRegistry();
        for (final IConfigurationElement configurationElement : registry.getConfigurationElementsFor("platform.sql.driver")) {
            try {
                Class.forName(configurationElement.getAttribute("class"));
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
