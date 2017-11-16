package license.server.application;

import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

public class Activator
        implements BundleActivator {
    
    private static BundleContext               context;
    
    private static ServiceTracker<?, Location> locationTracker;
    
    /**
     * @return the instance Location service
     */
    public static Location getInstanceLocation() {
        if (Activator.locationTracker == null) {
            Filter filter = null;
            try {
                filter = Activator.context.createFilter(Location.INSTANCE_FILTER);
            } catch (final InvalidSyntaxException e) {
                // ignore It should never happen as we have tested the
                // above format.
            }
            Activator.locationTracker = new ServiceTracker<>(Activator.context, filter, null);
            Activator.locationTracker.open();
        }
        return Activator.locationTracker.getService();
    }
    
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
