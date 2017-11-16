package platform.model;

import java.lang.reflect.Field;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public final class Activator
        implements BundleActivator {
    
    private static final String  CLASS       = "class";                      //$NON-NLS-1$
    private static final String  DESCRIPTORS = "platform.model.descriptors"; //$NON-NLS-1$
    
    private static BundleContext BUNDLE_CONTEXT;
    
    public static Bundle getBundleBySymbolicName(final String symbolicName) {
        for (final Bundle bundle : Activator.BUNDLE_CONTEXT.getBundles()) {
            if (bundle.getSymbolicName().equals(symbolicName)) {
                return bundle;
            }
        }
        return null;
    }
    
    @Override
    public void start(final BundleContext bundleContext) {
        Activator.BUNDLE_CONTEXT = bundleContext;
        for (final IConfigurationElement configurationElement : Platform.getExtensionRegistry().getConfigurationElementsFor(Activator.DESCRIPTORS)) {
            final Bundle bundle = Activator.getBundleBySymbolicName(configurationElement.getNamespaceIdentifier());
            if (bundle != null) {
                final String className = configurationElement.getAttribute(Activator.CLASS); // $NON-NLS-1$
                try {
                    final Class<?> clazz = bundle.loadClass(className);
                    for (final Field field : clazz.getDeclaredFields()) {
                        final boolean accessible = field.isAccessible();
                        if (!accessible) {
                            field.setAccessible(true);
                        }
                        try {
                            field.get(null);
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        if (!accessible) {
                            field.setAccessible(false);
                        }
                    }
                } catch (final ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Override
    public void stop(final BundleContext bundleContext) {
        // do nothing
    }
    
}
