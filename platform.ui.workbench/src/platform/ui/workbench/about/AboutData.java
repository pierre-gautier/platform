package platform.ui.workbench.about;

import java.io.IOException;
import java.net.URL;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * An abstract parent that describes data that can be displayed in a table in one of
 * the about dialogs.
 *
 * @since 3.0
 */
public abstract class AboutData {
    
    /**
     * Modify the argument array to be sorted by id. If the reverse
     * boolean is true, the array is assumed to already be sorted and the
     * direction of sort (ascending vs. descending) is reversed. Entries
     * with the same name are sorted by name.
     *
     * @param reverse
     *            if true then the order of the argument is reversed without
     *            examining the value of the fields
     * @param infos
     *            the data to be sorted
     */
    public static void sortById(final boolean reverse, final AboutData[] infos) {
        if (reverse) {
            AboutData.reverse(infos);
            return;
        }
        
        Arrays.sort(infos, new Comparator<AboutData>() {
            
            Collator collator = Collator.getInstance(Locale.getDefault());
            
            @Override
            public int compare(final AboutData info1, final AboutData info2) {
                
                final String id1 = info1.getId();
                final String id2 = info2.getId();
                
                if (!id1.equals(id2)) {
                    return this.collator.compare(id1, id2);
                }
                
                return this.collator.compare(info1.getName(), info2.getName());
            }
        });
    }
    
    protected static ImageDescriptor getImage(final URL url) {
        return url == null ? null : ImageDescriptor.createFromURL(url);
    }
    
    protected static URL getURL(final String value) {
        try {
            if (value != null) {
                return new URL(value);
            }
        } catch (final IOException e) {
            // do nothing
        }
        
        return null;
    }
    
    /**
     * Modify the argument array to reverse the sort order.
     *
     * @param infos
     */
    private static void reverse(final AboutData[] infos) {
        final List<AboutData> infoList = Arrays.asList(infos);
        Collections.reverse(infoList);
        for (int i = 0; i < infos.length; ++i) {
            infos[i] = infoList.get(i);
        }
    }
    
    private final String providerName;
    private final String name;
    private final String version;
    private final String id;
    
    private String       versionedId;
    
    protected AboutData(final String providerName, final String name, final String version,
            final String id) {
        this.providerName = providerName == null ? "" : providerName; //$NON-NLS-1$
        this.name = name == null ? "" : name; //$NON-NLS-1$
        this.version = version == null ? "" : version; //$NON-NLS-1$
        this.id = id == null ? "" : id; //$NON-NLS-1$
    }
    
    public String getId() {
        return this.id;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getProviderName() {
        return this.providerName;
    }
    
    public String getVersion() {
        return this.version;
    }
    
    public String getVersionedId() {
        if (this.versionedId == null) {
            this.versionedId = this.getId() + "_" + this.getVersion(); //$NON-NLS-1$
        }
        return this.versionedId;
    }
}
