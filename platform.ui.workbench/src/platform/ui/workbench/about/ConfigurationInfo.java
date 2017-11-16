package platform.ui.workbench.about;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Collator;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;

/**
 * This class contains utility methods that clients may use to obtain
 * information about the system configuration.
 *
 * @since 3.4
 */
public final class ConfigurationInfo {
    
    private static final String SECTION_TITLE = "sectionTitle"; //$NON-NLS-1$
    
    /**
     * Return a multi-line String that describes the current configuration. This
     * may include but is not limited to system properties, installed bundles,
     * and installed features. The specific format of this message is undefined
     * and may change at any time.
     * <p>
     * The contents of this String are in part constructed via
     * {@link ISystemSummarySection} that are registered with this running
     * instance of the workbench.
     * </p>
     *
     * @return the configuration info
     */
    public static String getSystemSummary() {
        try (final StringWriter out = new StringWriter(); final PrintWriter writer = new PrintWriter(out);) {
            writer.println(NLS.bind(WorkbenchMessages.SystemSummary_timeStamp, DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format(new Date())));
            ConfigurationInfo.appendExtensions(writer);
            writer.close();
            return out.toString();
        } catch (final IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
    
    /*
     * Appends the contents of all extensions to the configurationLogSections
     * extension point.
     */
    private static void appendExtensions(final PrintWriter writer) {
        final IConfigurationElement[] configElements = Platform.getExtensionRegistry().getConfigurationElementsFor("platform.ui.workbench.systemSummarySections"); //$NON-NLS-1$
        for (final IConfigurationElement element : ConfigurationInfo.getSortedExtensions(configElements)) {
            Object obj = null;
            try {
                final Class<?> clazz2 = Class.forName(element.getAttribute("class")); //$NON-NLS-1$
                obj = clazz2.newInstance();
            } catch (final Exception e) {
                e.printStackTrace();
                continue;
            }
            writer.println();
            writer.println(NLS.bind(WorkbenchMessages.SystemSummary_sectionTitle, element.getAttribute(ConfigurationInfo.SECTION_TITLE)));
            if (obj instanceof ISystemSummarySection) {
                final ISystemSummarySection logSection = (ISystemSummarySection) obj;
                logSection.write(writer);
            }
        }
    }
    
    private static IConfigurationElement[] getSortedExtensions(final IConfigurationElement[] configElements) {
        Arrays.sort(configElements, new Comparator<IConfigurationElement>() {
            
            Collator collator = Collator.getInstance(Locale.getDefault());
            
            @Override
            public int compare(final IConfigurationElement a, final IConfigurationElement b) {
                final String id1 = a.getAttribute("id"); //$NON-NLS-1$
                final String id2 = b.getAttribute("id"); //$NON-NLS-1$
                if (id1 != null && id2 != null && !id1.equals(id2)) {
                    return this.collator.compare(id1, id2);
                }
                String title1 = a.getAttribute(ConfigurationInfo.SECTION_TITLE);
                String title2 = b.getAttribute(ConfigurationInfo.SECTION_TITLE);
                if (title1 == null) {
                    title1 = ""; //$NON-NLS-1$
                }
                if (title2 == null) {
                    title2 = ""; //$NON-NLS-1$
                }
                return this.collator.compare(title1, title2);
            }
        });
        return configElements;
    }
    
    private ConfigurationInfo() {
        // hide constructor
    }
    
}
