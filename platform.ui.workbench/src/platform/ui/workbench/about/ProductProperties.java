package platform.ui.workbench.about;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import platform.utils.Strings;

/**
 * A class that converts the strings returned by
 * <code>org.eclipse.core.runtime.IProduct.getProperty</code> to the
 * appropriate class. This implementation is tightly bound to the properties
 * provided in IProductConstants. Clients adding their own properties could
 * choose to subclass this.
 *
 * @see org.eclipse.ui.branding.IProductConstants
 * @since 3.0
 */
public class ProductProperties
        extends BrandingProperties {
    
    private static Map<Bundle, String[]> mappingsMap = new HashMap<>(4);
    
    public static String getAboutImagePath(final IProduct product) {
        final String aboutImage = product.getProperty(AboutConstants.ABOUT_IMAGE);
        if (Strings.isNullEmptyOrBlank(aboutImage)) {
            return null;
        }
        return "platform:/plugin/" + product.getDefiningBundle().getSymbolicName() + aboutImage; //$NON-NLS-1$
    }
    
    /**
     * The text to show in an "about" dialog for this product.
     * Products designed to run "headless" typically would not
     * have such text.
     * <p>
     * The returned value will have {n} values substituted based on the
     * current product's mappings regardless of the given product argument.
     * </p>
     */
    public static String getAboutText(final IProduct product) {
        
        final String property = product.getProperty(AboutConstants.ABOUT_TEXT);
        if (property == null) {
            return ""; //$NON-NLS-1$
        }
        if (property.indexOf('{') == -1) {
            return property;
        }
        final String[] tempMappings = ProductProperties.getMappings(product.getDefiningBundle());
        /*
         * Check if the mapping value is a system property, specified
         * by '$' at the beginning and end of the string. If so, update
         * the mappings array with the system property value.
         */
        for (int i = 0; i < tempMappings.length; i++) {
            final String nextString = tempMappings[i];
            final int length = nextString.length();
            
            if (length > 2 && nextString.charAt(0) == '$' && nextString.charAt(length - 1) == '$') {
                final String systemPropertyKey = nextString.substring(1, length - 1);
                tempMappings[i] = System.getProperty(systemPropertyKey, ""); //$NON-NLS-1$ ;
            }
        }
        
        return MessageFormat.format(property, (Object[]) tempMappings);
    }
    
    private static String[] getMappings(final Bundle definingBundle) {
        String[] mappings = ProductProperties.mappingsMap.get(definingBundle);
        if (mappings == null) {
            mappings = ProductProperties.loadMappings(definingBundle);
        }
        if (mappings == null) {
            mappings = new String[0];
        }
        return mappings;
    }
    
    @SuppressWarnings("deprecation")
    private static String[] loadMappings(final Bundle definingBundle) {
        final URL location = Platform.find(definingBundle, new Path(AboutConstants.ABOUT_MAPPINGS));
        PropertyResourceBundle bundle = null;
        InputStream is;
        if (location != null) {
            is = null;
            try {
                is = location.openStream();
                bundle = new PropertyResourceBundle(is);
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (final IOException e) {
                    // do nothing if we fail to close
                }
            }
        }
        
        final List<String> mappingsList = new ArrayList<>();
        if (bundle != null) {
            boolean found = true;
            int i = 0;
            while (found) {
                try {
                    mappingsList.add(bundle.getString(Integer.toString(i)));
                } catch (final MissingResourceException e) {
                    found = false;
                }
                i++;
            }
        }
        final String[] mappings = mappingsList.toArray(new String[mappingsList.size()]);
        ProductProperties.mappingsMap.put(definingBundle, mappings);
        return mappings;
    }
    
}
