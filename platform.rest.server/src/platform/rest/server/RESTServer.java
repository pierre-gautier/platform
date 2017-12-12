package platform.rest.server;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.DispatcherType;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.AcceptHeaderApiListingResource;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

@SuppressWarnings("nls")
public class RESTServer {
    
    private static final String ROOT_PATH_SPEC = "/*";
    
    private static ServletContextHandler buildService(final Set<Class<?>> classes, final Set<Object> instances) {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .register(MultiPartFeature.class)
                .registerInstances(instances)
                .registerClasses(classes);
        final ServletHolder holder = new ServletHolder(new ServletContainer(resourceConfig));
        holder.setAsyncSupported(true);
        final ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath(RESTServer.getOrDefault(System.getProperty("baseUrl"), "/"));
        handler.addServlet(holder, RESTServer.ROOT_PATH_SPEC);
        return handler;
    }
    
    private static <T> T getOrDefault(final T property, final T defaultVal) {
        return property != null ? property : defaultVal;
    }
    
    private final Server                            server;
    private final List<Tuple<FilterHolder, String>> filterList;
    private final Set<Class<?>>                     serviceClasses;
    private final Set<Object>                       serviceInstances;
    private final List<ContextHandler>              staticResources;
    
    public RESTServer(final int port, final String baseUrl, final Object... instances) {
        System.setProperty("baseUrl", baseUrl);
        this.serviceInstances = new HashSet<>();
        this.staticResources = new ArrayList<>();
        this.filterList = new ArrayList<>();
        this.serviceClasses = new HashSet<>();
        this.server = new Server(port);
        // this.enableCors();
        try {
            for (final Object instance : instances) {
                this.addServiceInstance(instance, "service : " + instance.getClass().getSimpleName(), "description : " + instance.getClass().getSimpleName());
            }
            this.registerSwaggerUi(baseUrl);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    
    public void addFilter(final FilterHolder filter, final String pathSpec) {
        this.filterList.add(new Tuple<>(filter, pathSpec));
    }
    
    public void addStaticResource(final String staticResourceLocation, final String staticResourceEndpoint) {
        final ResourceHandler staticResourceHandler = new ResourceHandler();
        staticResourceHandler.setResourceBase(staticResourceLocation);
        final ContextHandler staticContext = new ContextHandler();
        staticContext.setContextPath(staticResourceEndpoint);
        staticContext.setHandler(staticResourceHandler);
        this.staticResources.add(staticContext);
    }
    
    /**
     * Enables CORS support on all path specs
     *
     * @return this
     */
    public void enableCors() {
        this.enableCors(RESTServer.ROOT_PATH_SPEC);
    }
    
    /**
     * Enables CORS support at the given path spec
     *
     * @param pathSpec
     *            Path Spec to add the CORS support to
     * @return this
     */
    public void enableCors(final String pathSpec) {
        final FilterHolder corsFilter = new FilterHolder(CrossOriginFilter.class);
        corsFilter.setAsyncSupported(true);
        corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*"); //$NON-NLS-1$
        corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "*"); //$NON-NLS-1$
        corsFilter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*"); //$NON-NLS-1$
        corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD,DELETE,PUT,OPTIONS"); //$NON-NLS-1$
        this.addFilter(corsFilter, pathSpec);
    }
    
    public void start()
            throws Exception {
        try {
            this.startNonBlocking();
            this.server.join();
        } finally {
            this.server.stop();
        }
    }
    
    public void startNonBlocking()
            throws Exception {
        // Workaround for resources from JAR files
        Resource.setDefaultUseCaches(false);
        // Apply all the filters to the service resources
        final ServletContextHandler servletContextHandler = RESTServer.buildService(this.serviceClasses, this.serviceInstances);
        this.applyFilters(servletContextHandler);
        // Add all the handlers to a list
        final HandlerCollection handlers = new HandlerCollection();
        for (final ContextHandler handler : this.staticResources) {
            handlers.addHandler(handler);
        }
        handlers.addHandler(servletContextHandler);
        // Start server
        this.server.setHandler(handlers);
        this.server.start();
    }
    
    public void stop()
            throws Exception {
        this.server.stop();
    }
    
    private void addServiceInstance(final Object instance, final String title, final String description) {
        this.serviceInstances.add(instance);
        // Build Swagger for this class
        this.buildSwagger(instance.getClass(), title, description);
        
    }
    
    private void applyFilters(final ServletContextHandler servletContextHandler) {
        for (final Tuple<FilterHolder, String> filter : this.filterList) {
            servletContextHandler.addFilter(filter._1(), filter._2(), EnumSet.allOf(DispatcherType.class));
        }
    }
    
    private void buildSwagger(final Class<?> clazz, final String title, final String description) {
        // Add the Swagger endpoint
        this.serviceClasses.add(ApiListingResource.class);
        this.serviceClasses.add(SwaggerSerializers.class);
        this.serviceClasses.add(AcceptHeaderApiListingResource.class);
        // This configures Swagger bean
        final BeanConfig beanConfig = new CustomBeanConfig(clazz.getClassLoader());
        beanConfig.setBasePath(RESTServer.getOrDefault(System.getProperty("baseUrl"), "/")); //$NON-NLS-1$ //$NON-NLS-2$
        beanConfig.setResourcePackage(clazz.getPackage().getName());
        beanConfig.setDescription(description);
        beanConfig.setVersion("1.0.0"); //$NON-NLS-1$
        beanConfig.setTitle(title);
        beanConfig.setScan(true);
    }
    
    private void registerSwaggerUi(final String baseUrl)
            throws IOException, URISyntaxException {
        final String resourcePath;
        if (Activator.getBundleContext() != null) {
            final URL url = Activator.getBundleContext().getBundle().getResource("src/platform/rest/server/ui"); //$NON-NLS-1$
            resourcePath = new File(FileLocator.toFileURL(url).getPath()).toURI().normalize().toString();
        } else {
            resourcePath = Activator.class.getResource("ui").toURI().toString(); //$NON-NLS-1$
        }
        this.addStaticResource(resourcePath, baseUrl + "/ui"); //$NON-NLS-1$
    }
    
}
