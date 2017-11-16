package license.server.application;

import java.util.Hashtable;
import java.util.UUID;

import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.UIEventPublisher;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.emf.common.notify.Notifier;
import org.osgi.framework.ServiceRegistration;

@SuppressWarnings("restriction")
public class E4HeadlessWorkbench
        implements IWorkbench {
    
    private static void init(final MApplication appElement) {
        final IEclipseContext context = appElement.getContext();
        if (context != null) {
            context.set(ExpressionContext.ALLOW_ACTIVATION, Boolean.TRUE);
        }
    }
    
    private final String           id;
    private final IEclipseContext  appContext;
    
    private ServiceRegistration<?> osgiRegistration;
    private IPresentationEngine    renderer;
    private MApplication           appModel;
    private UIEventPublisher       uiEventPublisher;
    private boolean                restart;
    
    /**
     * Constructor
     *
     * @param uiRoot
     *            the root UI element
     * @param applicationContext
     *            the root context
     */
    public E4HeadlessWorkbench(final MApplicationElement uiRoot, final IEclipseContext applicationContext) {
        this.id = UUID.randomUUID().toString();
        this.appContext = applicationContext;
        this.appContext.set(IWorkbench.class.getName(), this);
        if (uiRoot instanceof MApplication) {
            this.appModel = (MApplication) uiRoot;
        }
        
        if (uiRoot instanceof MApplication) {
            E4HeadlessWorkbench.init((MApplication) uiRoot);
        }
        
        this.uiEventPublisher = new UIEventPublisher(this.appContext);
        this.appContext.set(UIEventPublisher.class, this.uiEventPublisher);
        ((Notifier) uiRoot).eAdapters().add(this.uiEventPublisher);
        final Hashtable<String, Object> properties = new Hashtable<>();
        properties.put("id", this.getId()); //$NON-NLS-1$
        
        this.osgiRegistration = Activator.getContext().registerService(IWorkbench.class.getName(), this, properties);
        
        // ContextInjectionFactory.make(PartOnTopManager.class, this.appContext);
    }
    
    @Override
    public boolean close() {
        // Fire an E4 lifecycle notification
        UIEvents.publishEvent(UIEvents.UILifeCycle.APP_SHUTDOWN_STARTED, this.appModel);
        
        if (this.renderer != null) {
            this.renderer.stop();
        }
        if (this.uiEventPublisher != null && this.appModel != null) {
            ((Notifier) this.appModel).eAdapters().remove(this.uiEventPublisher);
            this.uiEventPublisher = null;
        }
        if (this.osgiRegistration != null) {
            this.osgiRegistration.unregister();
            this.osgiRegistration = null;
        }
        return true;
    }
    
    /**
     * @param uiRoot
     */
    public void createAndRunUI(final MApplicationElement uiRoot) {
        // Has someone already created one ?
        this.instantiateRenderer();
        
        if (this.renderer != null) {
            this.renderer.run(uiRoot, this.appContext);
        }
    }
    
    @Override
    public MApplication getApplication() {
        return this.appModel;
    }
    
    /**
     * @return the {@link IEclipseContext} for the main application
     */
    public IEclipseContext getContext() {
        return this.appContext;
    }
    
    @Override
    public final String getId() {
        return this.id;
    }
    
    /**
     * @return <code>true</code> when the workbench should be restarted
     */
    public boolean isRestart() {
        return this.restart;
    }
    
    @Override
    public boolean restart() {
        this.restart = true;
        return this.close();
    }
    
    private void instantiateRenderer() {
        this.renderer = this.appContext.get(IPresentationEngine.class);
        if (this.renderer == null) {
            final String presentationURI = (String) this.appContext.get(IWorkbench.PRESENTATION_URI_ARG);
            if (presentationURI != null) {
                final IContributionFactory factory = this.appContext.get(IContributionFactory.class);
                this.renderer = (IPresentationEngine) factory.create(presentationURI, this.appContext);
                this.appContext.set(IPresentationEngine.class, this.renderer);
            }
            if (this.renderer == null) {
                final Logger logger = this.appContext.get(Logger.class);
                logger.error("Failed to create the presentation engine for URI: " + presentationURI); //$NON-NLS-1$
            }
        }
    }
    
}
