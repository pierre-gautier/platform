package platform.jetter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;

import com.caffinc.jetter.Api;

public class JetterService {
    
    private final Api api;
    
    public JetterService(final int port, final String baseUrl, final Object... instances) {
        this.api = new Api(port).setBaseUrl(baseUrl);
        try {
            for (final Object instance : instances) {
                this.api.addServiceInstance(instance, "service : " + instance.getClass().getSimpleName(), "description : " + instance.getClass().getSimpleName()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            this.registerSwaggerUi(baseUrl);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    
    public void start()
            throws Exception {
        this.api.start();
    }
    
    public void startNonBlocking()
            throws Exception {
        this.api.startNonBlocking();
    }
    
    public void stop()
            throws Exception {
        this.api.stop();
    }
    
    private void registerSwaggerUi(final String baseUrl)
            throws IOException, URISyntaxException {
        String resourcePath;
        if (Activator.getBundleContext() != null) {
            final URL url = Activator.getBundleContext().getBundle().getResource("src/platform/jetter/ui"); //$NON-NLS-1$
            resourcePath = new File(FileLocator.toFileURL(url).getPath()).toURI().normalize().toString();
        } else {
            resourcePath = Activator.class.getResource("ui").toURI().toString(); //$NON-NLS-1$
        }
        this.api.addStaticResource(resourcePath, baseUrl + "/ui"); //$NON-NLS-1$
    }
    
}
