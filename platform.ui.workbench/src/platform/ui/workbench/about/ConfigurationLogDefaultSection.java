package platform.ui.workbench.about;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;

import platform.ui.workbench.Activator;

/**
 * This class puts basic platform information into the system summary log. This
 * includes sections for the java properties, the ids of all installed features
 * and plugins, as well as a the current contents of the preferences service.
 *
 * @since 3.0
 */
public class ConfigurationLogDefaultSection
        implements ISystemSummarySection {
    
    private static final String ECLIPSE_PROPERTY_PREFIX = "eclipse."; //$NON-NLS-1$
    
    /**
     * Appends the <code>System</code> properties.
     */
    private static void appendProperties(final PrintWriter writer) {
        writer.println();
        writer.println(WorkbenchMessages.SystemSummary_systemProperties);
        final Properties properties = System.getProperties();
        final SortedSet<String> set = new TreeSet<>((o1, o2) -> {
            final String s1 = o1;
            final String s2 = o2;
            return s1.compareTo(s2);
        });
        set.addAll(properties.stringPropertyNames());
        final Iterator<?> i = set.iterator();
        while (i.hasNext()) {
            final String key = (String) i.next();
            final String value = properties.getProperty(key);
            
            writer.print(key);
            writer.print('=');
            
            // some types of properties have special characters embedded
            if (key.startsWith(ConfigurationLogDefaultSection.ECLIPSE_PROPERTY_PREFIX)) {
                ConfigurationLogDefaultSection.printEclipseProperty(writer, value);
            } else if (key.toUpperCase().indexOf("PASSWORD") != -1) { //$NON-NLS-1$
                // We should obscure any property that may be a password
                for (int j = 0; j < value.length(); j++) {
                    writer.print('*');
                }
                writer.println();
            } else {
                writer.println(value);
            }
        }
    }
    
    /**
     * Appends the contents of the Plugin Registry.
     */
    private static void appendRegistry(final PrintWriter writer) {
        writer.println();
        writer.println(WorkbenchMessages.SystemSummary_pluginRegistry);
        
        final Bundle[] bundles = Activator.getContext().getBundles();
        final AboutBundleData[] bundleInfos = new AboutBundleData[bundles.length];
        for (int i = 0; i < bundles.length; ++i) {
            bundleInfos[i] = new AboutBundleData(bundles[i]);
        }
        
        AboutData.sortById(false, bundleInfos);
        
        for (final AboutBundleData info : bundleInfos) {
            final String[] args = new String[] { info.getId(), info.getVersion(),
                    info.getName(), info.getStateName() };
            writer.println(NLS.bind(WorkbenchMessages.SystemSummary_descriptorIdVersionState, args));
        }
    }
    
    private static String[] getArrayFromList(final String prop, final String separator) {
        if (prop == null || "".equals(prop.trim())) { //$NON-NLS-1$
            return new String[0];
        }
        final List<String> list = new ArrayList<>();
        final StringTokenizer tokens = new StringTokenizer(prop, separator);
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();
            if (!"".equals(token)) { //$NON-NLS-1$
                list.add(token);
            }
        }
        return list.isEmpty() ? new String[0] : (String[]) list.toArray(new String[list.size()]);
    }
    
    private static void printEclipseProperty(final PrintWriter writer, final String value) {
        final String[] lines = ConfigurationLogDefaultSection.getArrayFromList(value, "\n"); //$NON-NLS-1$
        for (final String line : lines) {
            writer.println(line);
        }
    }
    
    @Override
    public void write(final PrintWriter writer) {
        ConfigurationLogDefaultSection.appendProperties(writer);
        ConfigurationLogDefaultSection.appendRegistry(writer);
    }
}
