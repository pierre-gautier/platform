package platform.ui.workbench.about;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * A small class to manage the information related to IBundleGroup's.
 *
 * @since 3.0
 */
public class AboutBundleGroupData
        extends AboutData {
    
    private static final String LICENSE_HREF = "licenseHref"; //$NON-NLS-1$
    
    private final IBundleGroup  bundleGroup;
    
    private URL                 licenseUrl;
    private URL                 featureImageUrl;
    private Long                featureImageCrc;
    private ImageDescriptor     featureImage;
    
    public AboutBundleGroupData(final IBundleGroup bundleGroup) {
        super(bundleGroup.getProviderName(), bundleGroup.getName(), bundleGroup.getVersion(), bundleGroup.getIdentifier());
        this.bundleGroup = bundleGroup;
    }
    
    public String getAboutText() {
        return this.bundleGroup.getProperty(AboutConstants.ABOUT_TEXT);
    }
    
    public IBundleGroup getBundleGroup() {
        return this.bundleGroup;
    }
    
    public ImageDescriptor getFeatureImage() {
        if (this.featureImage == null) {
            this.featureImage = AboutData.getImage(this.getFeatureImageUrl());
        }
        return this.featureImage;
    }
    
    public Long getFeatureImageCrc() {
        if (this.featureImageCrc != null) {
            return this.featureImageCrc;
        }
        
        final URL url = this.getFeatureImageUrl();
        if (url == null) {
            return null;
        }
        
        // Get the image bytes
        InputStream in = null;
        try {
            final CRC32 checksum = new CRC32();
            in = new CheckedInputStream(url.openStream(), checksum);
            
            // the contents don't matter, the read just needs a place to go
            final byte[] sink = new byte[1024];
            while (true) {
                if (in.read(sink) <= 0) {
                    break;
                }
            }
            
            this.featureImageCrc = Long.valueOf(checksum.getValue());
            return this.featureImageCrc;
            
        } catch (final IOException e) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {
                    // do nothing
                }
            }
        }
    }
    
    public URL getFeatureImageUrl() {
        if (this.featureImageUrl == null) {
            this.featureImageUrl = AboutData.getURL(this.bundleGroup.getProperty(AboutConstants.FEATURE_IMAGE));
        }
        return this.featureImageUrl;
    }
    
    public URL getLicenseUrl() {
        if (this.licenseUrl == null) {
            this.licenseUrl = AboutData.getURL(this.bundleGroup.getProperty(AboutBundleGroupData.LICENSE_HREF));
        }
        
        return this.licenseUrl;
    }
}
