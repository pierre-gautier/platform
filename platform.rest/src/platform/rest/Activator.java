package platform.rest;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public final class Activator
        implements BundleActivator {
    
    private static BundleContext context;
    
    public static BundleContext getBundleContext() {
        return Activator.context;
    }
    
    @Override
    public void start(final BundleContext bundleContext) {
        Activator.context = bundleContext;
    }
    
    @Override
    public void stop(final BundleContext bundleContext) {
        // do nothing
    }
    
}
