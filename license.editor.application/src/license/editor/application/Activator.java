package license.editor.application;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator
        implements BundleActivator {
    
    private static BundleContext context;
    
    static BundleContext getContext() {
        return Activator.context;
    }
    
    @Override
    public void start(final BundleContext bundleContext)
            throws Exception {
        Activator.context = bundleContext;
    }
    
    @Override
    public void stop(final BundleContext bundleContext)
            throws Exception {
        Activator.context = null;
    }
    
}
