package platform.ui.workbench.about;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.signedcontent.SignedContent;
import org.eclipse.osgi.signedcontent.SignedContentFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import platform.ui.workbench.Activator;

/**
 * A small class to manage the about dialog information for a single bundle.
 *
 * @since 3.0
 */
public class AboutBundleData
        extends AboutData {
    
    /**
     * A function to translate the resource tags that may be embedded in a
     * string associated with some bundle.
     *
     * @param headerName
     *            the used to retrieve the correct string
     * @return the string or null if the string cannot be found
     */
    private static String getResourceString(final Bundle bundle, final String headerName) {
        final String value = bundle.getHeaders().get(headerName);
        return value == null ? null : Platform.getResourceString(bundle, value);
    }
    
    private final Bundle bundle;
    
    private boolean      isSignedDetermined;
    private boolean      isSigned;
    
    public AboutBundleData(final Bundle bundle) {
        super(AboutBundleData.getResourceString(bundle, Constants.BUNDLE_VENDOR),
                AboutBundleData.getResourceString(bundle, Constants.BUNDLE_NAME),
                AboutBundleData.getResourceString(bundle, Constants.BUNDLE_VERSION),
                bundle.getSymbolicName());
        this.bundle = bundle;
    }
    
    /**
     * @return current bundle
     */
    public Bundle getBundle() {
        return this.bundle;
    }
    
    public int getState() {
        return this.bundle.getState();
    }
    
    /**
     * @return a string representation of the argument state.
     *         Does not return null.
     */
    public String getStateName() {
        switch (this.getState()) {
            case Bundle.INSTALLED:
                return WorkbenchMessages.AboutPluginsDialog_state_installed;
            case Bundle.RESOLVED:
                return WorkbenchMessages.AboutPluginsDialog_state_resolved;
            case Bundle.STARTING:
                return WorkbenchMessages.AboutPluginsDialog_state_starting;
            case Bundle.STOPPING:
                return WorkbenchMessages.AboutPluginsDialog_state_stopping;
            case Bundle.UNINSTALLED:
                return WorkbenchMessages.AboutPluginsDialog_state_uninstalled;
            case Bundle.ACTIVE:
                return WorkbenchMessages.AboutPluginsDialog_state_active;
            default:
                return WorkbenchMessages.AboutPluginsDialog_state_unknown;
        }
    }
    
    public boolean isSigned() {
        
        if (this.isSignedDetermined) {
            return this.isSigned;
        }
        
        final BundleContext bundleContext = Activator.getContext();
        final ServiceReference<?> factoryRef = bundleContext.getServiceReference(SignedContentFactory.class.getName());
        if (factoryRef == null) {
            throw new IllegalStateException();
        }
        final SignedContentFactory contentFactory = (SignedContentFactory) bundleContext.getService(factoryRef);
        try {
            final SignedContent signedContent = contentFactory.getSignedContent(this.bundle);
            this.isSigned = signedContent != null && signedContent.isSigned();
            this.isSignedDetermined = true;
            return this.isSigned;
        } catch (final IOException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        } catch (final GeneralSecurityException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        } finally {
            bundleContext.ungetService(factoryRef);
        }
    }
    
    public boolean isSignedDetermined() {
        return this.isSignedDetermined;
    }
    
}
