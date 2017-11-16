package platform.model.license;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import platform.model.factory.NodeFactories;

public final class Activator
        implements BundleActivator {
    
    @Override
    public void start(final BundleContext bundleContext) {
        NodeFactories.INSTANCE.register(LicenseFactory.INSTANCE);
    }
    
    @Override
    public void stop(final BundleContext bundleContext) {
        // do nothing
    }
    
}
