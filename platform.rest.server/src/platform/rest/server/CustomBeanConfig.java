package platform.rest.server;

import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.config.BeanConfig;

public class CustomBeanConfig
        extends BeanConfig {
    
    private final ClassLoader loader;
    
    public CustomBeanConfig(final ClassLoader loader) {
        this.loader = loader;
    }
    
    @Override
    public Set<Class<?>> classes() {
        final ConfigurationBuilder config = new ConfigurationBuilder();
        final Set<String> acceptablePackages = new HashSet<>();
        
        boolean allowAllPackages = false;
        
        final String resourcePackage = this.getResourcePackage();
        if (resourcePackage != null && !"".equals(resourcePackage)) { //$NON-NLS-1$
            final String[] parts = resourcePackage.split(","); //$NON-NLS-1$
            for (final String pkg : parts) {
                if (!"".equals(pkg)) { //$NON-NLS-1$
                    acceptablePackages.add(pkg);
                    config.addUrls(ClasspathHelper.forPackage(pkg, this.loader));
                }
            }
        } else {
            allowAllPackages = true;
        }
        
        config.addClassLoader(this.loader);
        config.setScanners(new ResourcesScanner(), new TypeAnnotationsScanner(), new SubTypesScanner());
        
        final Reflections reflections = new Reflections(config);
        final Set<Class<?>> classes = reflections.getTypesAnnotatedWith(javax.ws.rs.Path.class);
        classes.addAll(reflections.getTypesAnnotatedWith(SwaggerDefinition.class));
        
        final Set<Class<?>> output = new HashSet<>();
        for (final Class<?> cls : classes) {
            if (allowAllPackages) {
                output.add(cls);
            } else {
                for (final String pkg : acceptablePackages) {
                    if (cls.getPackage().getName().startsWith(pkg)) {
                        output.add(cls);
                    }
                }
            }
        }
        return output;
    }
    
}
