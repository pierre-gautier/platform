/*******************************************************************************
 * Copyright (c) 2009, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Tristan Hume - <trishume@gmail.com> -
 * Fix for Bug 2369 [Workbench] Would like to be able to save workspace without exiting
 * Implemented workbench auto-save to correctly restore state in case of crash.
 * Lars Vogel <Lars.Vogel@vogella.com> - Bug 366364, 445724, 446088, 458033, 393171
 * Terry Parker <tparker@google.com> - Bug 416673
 * Christian Georgi (SAP) - Bug 432480
 * Simon Scholz <simon.scholz@vogella.com> - Bug 478896
 ******************************************************************************/

package license.server.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.internal.services.EclipseAdapter;
import org.eclipse.e4.core.services.adapter.Adapter;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.log.ILoggerProvider;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.translation.TranslationProviderFactory;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.eclipse.e4.ui.internal.workbench.DefaultLoggerProvider;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.ModelServiceImpl;
import org.eclipse.e4.ui.internal.workbench.PlaceholderResolver;
import org.eclipse.e4.ui.internal.workbench.ReflectionContributionFactory;
import org.eclipse.e4.ui.internal.workbench.ResourceHandler;
import org.eclipse.e4.ui.internal.workbench.SelectionAggregator;
import org.eclipse.e4.ui.internal.workbench.SelectionServiceImpl;
import org.eclipse.e4.ui.internal.workbench.URIHelper;
import org.eclipse.e4.ui.internal.workbench.WorkbenchLogger;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IExceptionHandler;
import org.eclipse.e4.ui.workbench.IModelResourceHandler;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPlaceholderResolver;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.Bundle;

@SuppressWarnings({ "restriction", "nls" })
public class E4HeadlessApplication
        implements IApplication {
    
    private static final String PLUGIN_ID                      = "org.eclipse.e4.ui.workbench.swt";  //$NON-NLS-1$
    private static final String METADATA_FOLDER                = ".metadata";                        //$NON-NLS-1$
    private static final String VERSION_FILENAME               = "version.ini";                      //$NON-NLS-1$
    private static final String WORKSPACE_VERSION_KEY          = "org.eclipse.core.runtime";         //$NON-NLS-1$
    private static final String WORKSPACE_VERSION_VALUE        = "2";                                //$NON-NLS-1$
    private static final String APPLICATION_MODEL_PATH_DEFAULT = "Application.e4xmi";
    private static final String PERSPECTIVE_ARG_NAME           = "perspective";
    private static final String SHOWLOCATION_ARG_NAME          = "showLocation";
    private static final String CONTEXT_INITIALIZED            = "org.eclipse.ui.contextInitialized";
    
    /**
     * Simplified copy of IDEAplication processing that does not offer to choose a workspace location.
     */
    private static boolean checkInstanceLocation(final Location instanceLocation, final IEclipseContext context) {
        
        // Eclipse has been run with -data @none or -data @noDefault options so we don't need to validate the location
        if (instanceLocation == null && Boolean.FALSE.equals(context.get(IWorkbench.PERSIST_STATE))) {
            return true;
        }
        
        if (instanceLocation == null) {
            // MessageDialog.openError(shell, WorkbenchSWTMessages.IDEApplication_workspaceMandatoryTitle, WorkbenchSWTMessages.IDEApplication_workspaceMandatoryMessage);
            return false;
        }
        
        // -data "/valid/path", workspace already set
        if (instanceLocation.isSet()) {
            // make sure the meta data version is compatible (or the user
            // has
            // chosen to overwrite it).
            if (!E4HeadlessApplication.checkValidWorkspace(instanceLocation.getURL())) {
                return false;
            }
            
            // at this point its valid, so try to lock it and update the
            // metadata version information if successful
            try {
                if (instanceLocation.lock()) {
                    E4HeadlessApplication.writeWorkspaceVersion();
                    return true;
                }
                
                // we failed to create the directory.
                // Two possibilities:
                // 1. directory is already in use
                // 2. directory could not be created
                // final File workspaceDirectory = new File(instanceLocation.getURL().getFile());
                // if (workspaceDirectory.exists()) {
                // MessageDialog.openError(shell, WorkbenchSWTMessages.IDEApplication_workspaceCannotLockTitle, WorkbenchSWTMessages.IDEApplication_workspaceCannotLockMessage);
                // } else {
                // MessageDialog.openError(shell, WorkbenchSWTMessages.IDEApplication_workspaceCannotBeSetTitle, WorkbenchSWTMessages.IDEApplication_workspaceCannotBeSetMessage);
                // }
            } catch (final IOException e) {
                final Logger logger = new WorkbenchLogger(E4HeadlessApplication.PLUGIN_ID);
                logger.error(e);
                // MessageDialog.openError(shell, WorkbenchSWTMessages.InternalError, e.getMessage());
            }
            return false;
        }
        return false;
    }
    
    /**
     * Return true if the argument directory is ok to use as a workspace and
     * false otherwise. A version check will be performed, and a confirmation
     * box may be displayed on the argument shell if an older version is
     * detected.
     *
     * @return true if the argument URL is ok to use as a workspace and false
     *         otherwise.
     */
    private static boolean checkValidWorkspace(final URL url) {
        // a null url is not a valid workspace
        if (url == null) {
            return false;
        }
        
        final String version = E4HeadlessApplication.readWorkspaceVersion(url);
        
        // if the version could not be read, then there is not any existing
        // workspace data to trample, e.g., perhaps its a new directory that
        // is just starting to be used as a workspace
        if (version == null) {
            return true;
        }
        
        final int ide_version = Integer.parseInt(E4HeadlessApplication.WORKSPACE_VERSION_VALUE);
        final int workspace_version = Integer.parseInt(version);
        
        // equality test is required since any version difference (newer
        // or older) may result in data being trampled
        if (workspace_version == ide_version) {
            return true;
        }
        
        // At this point workspace has been detected to be from a version
        // other than the current ide version -- find out if the user wants
        // to use it anyhow.
        // final String title = WorkbenchSWTMessages.IDEApplication_versionTitle;
        // final String message = NLS.bind(WorkbenchSWTMessages.IDEApplication_versionMessage, url.getFile());
        //
        // final MessageBox mbox = new MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_WARNING | SWT.APPLICATION_MODAL);
        // mbox.setText(title);
        // mbox.setMessage(message);
        // return mbox.open() == SWT.OK;
        return true;
    }
    
    // TODO This should go into a different bundle
    private static IEclipseContext createDefaultContext() {
        
        final IEclipseContext serviceContext = E4HeadlessApplication.createDefaultHeadlessContext();
        final IEclipseContext appContext = serviceContext.createChild("WorkbenchContext"); //$NON-NLS-1$
        // make application context available for dependency injection under the E4Application.APPLICATION_CONTEXT_KEY key
        appContext.set(IWorkbench.APPLICATION_CONTEXT_KEY, appContext);
        
        appContext.set(Logger.class, ContextInjectionFactory.make(WorkbenchLogger.class, appContext));
        appContext.set(EModelService.class, new ModelServiceImpl(appContext));
        appContext.set(EPlaceholderResolver.class, new PlaceholderResolver());
        
        // setup for commands and handlers
        // appContext.set(IServiceConstants.ACTIVE_PART, new ActivePartLookupFunction());
        // appContext.set(IServiceConstants.ACTIVE_SHELL, new ActiveChildLookupFunction(IServiceConstants.ACTIVE_SHELL, E4Workbench.LOCAL_ACTIVE_SHELL));
        
        // translation
        E4HeadlessApplication.initializeLocalization(appContext);
        
        return appContext;
    }
    
    // TODO This should go into a different bundle
    private static IEclipseContext createDefaultHeadlessContext() {
        final IEclipseContext serviceContext = E4Workbench.getServiceContext();
        
        final IExtensionRegistry registry = RegistryFactory.getRegistry();
        final ReflectionContributionFactory contributionFactory = new ReflectionContributionFactory(registry);
        serviceContext.set(IContributionFactory.class, contributionFactory);
        serviceContext.set(IExceptionHandler.class, e -> e.printStackTrace());
        serviceContext.set(IExtensionRegistry.class, registry);
        
        serviceContext.set(Adapter.class, ContextInjectionFactory.make(EclipseAdapter.class, serviceContext));
        
        // No default log provider available
        if (serviceContext.get(ILoggerProvider.class) == null) {
            serviceContext.set(ILoggerProvider.class, ContextInjectionFactory.make(DefaultLoggerProvider.class, serviceContext));
        }
        
        return serviceContext;
    }
    
    /**
     * The version file is stored in the metadata area of the workspace. This
     * method returns an URL to the file or null if the directory or file does
     * not exist (and the create parameter is false).
     *
     * @param create
     *            If the directory and file does not exist this parameter
     *            controls whether it will be created.
     * @return An url to the file or null if the version file does not exist or
     *         could not be created.
     */
    private static File getVersionFile(final URL workspaceUrl, final boolean create) {
        if (workspaceUrl == null) {
            return null;
        }
        
        try {
            // make sure the directory exists
            final File metaDir = new File(workspaceUrl.getPath(), E4HeadlessApplication.METADATA_FOLDER);
            if (!metaDir.exists() && (!create || !metaDir.mkdir())) {
                return null;
            }
            
            // make sure the file exists
            final File versionFile = new File(metaDir, E4HeadlessApplication.VERSION_FILENAME);
            if (!versionFile.exists() && (!create || !versionFile.createNewFile())) {
                return null;
            }
            
            return versionFile;
        } catch (final IOException e) {
            // cannot log because instance area has not been set
            return null;
        }
    }
    
    static private void initializeApplicationServices(final IEclipseContext appContext) {
        final IEclipseContext theContext = appContext;
        // we add a special tracker to bring up current selection from
        // the active window to the application level
        appContext.runAndTrack(new RunAndTrack() {
            
            @Override
            public boolean changed(final IEclipseContext context) {
                final IEclipseContext activeChildContext = context.getActiveChild();
                if (activeChildContext != null) {
                    final Object selection = activeChildContext.get(IServiceConstants.ACTIVE_SELECTION);
                    theContext.set(IServiceConstants.ACTIVE_SELECTION, selection);
                }
                return true;
            }
        });
        
        // we create a selection service handle on every node that we are asked
        // about as handle needs to know its context
        appContext.set(ESelectionService.class.getName(), new ContextFunction() {
            
            @Override
            public Object compute(final IEclipseContext context, final String contextKey) {
                return ContextInjectionFactory.make(SelectionServiceImpl.class, context);
            }
        });
    }
    
    /**
     * Initializes the given context with the locale and the TranslationService
     * to use.
     *
     * @param appContext
     *            The application context to which the locale and the
     *            TranslationService should be set.
     */
    private static void initializeLocalization(final IEclipseContext appContext) {
        appContext.set(TranslationService.LOCALE, Locale.getDefault());
        appContext.set(TranslationService.class, TranslationProviderFactory.bundleTranslationService(appContext));
    }
    
    static private void initializeServices(final MApplication appModel) {
        final IEclipseContext appContext = appModel.getContext();
        // make sure we only add trackers once
        if (appContext.containsKey(E4HeadlessApplication.CONTEXT_INITIALIZED)) {
            return;
        }
        appContext.set(E4HeadlessApplication.CONTEXT_INITIALIZED, "true");
        E4HeadlessApplication.initializeApplicationServices(appContext);
        final List<MWindow> windows = appModel.getChildren();
        for (final MWindow childWindow : windows) {
            E4HeadlessApplication.initializeWindowServices(childWindow);
        }
        ((EObject) appModel).eAdapters().add(new AdapterImpl() {
            
            @Override
            public void notifyChanged(final Notification notification) {
                if (notification.getFeatureID(MApplication.class) != UiPackageImpl.ELEMENT_CONTAINER__CHILDREN) {
                    return;
                }
                if (notification.getEventType() != Notification.ADD) {
                    return;
                }
                final MWindow childWindow = (MWindow) notification.getNewValue();
                E4HeadlessApplication.initializeWindowServices(childWindow);
            }
        });
    }
    
    static private void initializeWindowServices(final MWindow childWindow) {
        final IEclipseContext windowContext = childWindow.getContext();
        E4HeadlessApplication.initWindowContext(windowContext);
        // Mostly MWindow contexts are lazily created by renderers and is not
        // set at this point.
        ((EObject) childWindow).eAdapters().add(new AdapterImpl() {
            
            @Override
            public void notifyChanged(final Notification notification) {
                if (notification.getFeatureID(MWindow.class) != BasicPackageImpl.WINDOW__CONTEXT) {
                    return;
                }
                final IEclipseContext notificationContext = (IEclipseContext) notification.getNewValue();
                E4HeadlessApplication.initWindowContext(notificationContext);
            }
        });
    }
    
    static private void initWindowContext(final IEclipseContext windowContext) {
        if (windowContext == null) {
            return;
        }
        final SelectionAggregator selectionAggregator = ContextInjectionFactory.make(SelectionAggregator.class,
                windowContext);
        windowContext.set(SelectionAggregator.class, selectionAggregator);
    }
    
    /**
     * Look at the argument URL for the workspace's version information. Return
     * that version if found and null otherwise.
     */
    private static String readWorkspaceVersion(final URL workspace) {
        final File versionFile = E4HeadlessApplication.getVersionFile(workspace, false);
        if (versionFile == null || !versionFile.exists()) {
            return null;
        }
        
        try (final FileInputStream is = new FileInputStream(versionFile)) {
            // Although the version file is not spec'ed to be a Java properties
            // file, it happens to follow the same format currently, so using
            // Properties to read it is convenient.
            final Properties props = new Properties();
            try {
                props.load(is);
            } finally {
                is.close();
            }
            
            return props.getProperty(E4HeadlessApplication.WORKSPACE_VERSION_KEY);
        } catch (final IOException e) {
            final Logger logger = new WorkbenchLogger(E4HeadlessApplication.PLUGIN_ID);
            logger.error(e);
            return null;
        }
    }
    
    /**
     * Write the version of the metadata into a known file overwriting any
     * existing file contents. Writing the version file isn't really crucial, so
     * the function is silent about failure
     */
    private static void writeWorkspaceVersion() {
        final Location instanceLoc = Platform.getInstanceLocation();
        if (instanceLoc == null || instanceLoc.isReadOnly()) {
            return;
        }
        
        final File versionFile = E4HeadlessApplication.getVersionFile(instanceLoc.getURL(), true);
        if (versionFile == null) {
            return;
        }
        
        try (OutputStream output = new FileOutputStream(versionFile)) {
            final String versionLine = E4HeadlessApplication.WORKSPACE_VERSION_KEY + '=' + E4HeadlessApplication.WORKSPACE_VERSION_VALUE;
            output.write(versionLine.getBytes(StandardCharsets.UTF_8));
        } catch (final IOException e) {
            final Logger logger = new WorkbenchLogger(E4HeadlessApplication.PLUGIN_ID);
            logger.error(e);
        }
    }
    
    private String[]              args;
    private IModelResourceHandler handler;
    private E4HeadlessWorkbench   workbench;
    private Object                lifeCylceManager;
    
    @Override
    public Object start(final IApplicationContext applicationContext) throws Exception {
        // set the display name before the Display is
        // created to ensure the app name is used in any
        // platform menus, etc. See
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=329456#c14
        // final IProduct product = Platform.getProduct();
        // if (product != null && product.getName() != null) {
        // Display.setAppName(product.getName());
        // }
        // final Display display = getApplicationDisplay();
        Location instanceLocation = null;
        try {
            final E4HeadlessWorkbench e4workbench = this.createE4Workbench(applicationContext);
            
            instanceLocation = (Location) e4workbench.getContext().get(E4Workbench.INSTANCE_LOCATION);
            // Shell shell = display.getActiveShell();
            // if (shell == null) {
            // shell = new Shell();
            // // place it off so it's not visible
            // shell.setLocation(0, 10000);
            // }
            if (!E4HeadlessApplication.checkInstanceLocation(instanceLocation, e4workbench.getContext())) {
                return IApplication.EXIT_OK;
            }
            
            // Create and run the UI (if any)
            e4workbench.createAndRunUI(e4workbench.getApplication());
            
            this.saveModel();
            e4workbench.close();
            
            if (e4workbench.isRestart()) {
                return IApplication.EXIT_RESTART;
            }
            
            return IApplication.EXIT_OK;
        } finally {
            if (instanceLocation != null) {
                instanceLocation.release();
            }
        }
    }
    
    @Override
    public void stop() {
        if (this.workbench != null) {
            this.workbench.close();
        }
    }
    
    private E4HeadlessWorkbench createE4Workbench(final IApplicationContext applicationContext) {
        this.args = (String[]) applicationContext.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        
        final IEclipseContext appContext = E4HeadlessApplication.createDefaultContext();
        // appContext.set(Display.class, display);
        // appContext.set(Realm.class, DisplayRealm.getRealm(display));
        // appContext.set(UISynchronize.class, new UISynchronize() {
        //
        // @Override
        // public void asyncExec(final Runnable runnable) {
        // if (display != null && !display.isDisposed()) {
        // display.asyncExec(runnable);
        // }
        // }
        //
        // @Override
        // public void syncExec(final Runnable runnable) {
        // if (display != null && !display.isDisposed()) {
        // display.syncExec(runnable);
        // }
        // }
        // });
        appContext.set(IApplicationContext.class, applicationContext);
        
        // This context will be used by the injector for its
        // extended data suppliers
        ContextInjectionFactory.setDefault(appContext);
        
        // Get the factory to create DI instances with
        final IContributionFactory factory = appContext.get(IContributionFactory.class);
        
        // Install the life-cycle manager for this session if there's one defined
        final Optional<String> lifeCycleURI = this.getArgValue(IWorkbench.LIFE_CYCLE_URI_ARG, applicationContext, false);
        lifeCycleURI.ifPresent(lifeCycleURIValue -> {
            this.lifeCylceManager = factory.create(lifeCycleURIValue, appContext);
            if (this.lifeCylceManager != null) {
                // Let the manager manipulate the appContext if desired
                ContextInjectionFactory.invoke(this.lifeCylceManager, PostContextCreate.class, appContext, null);
            }
        });
        
        final Optional<String> forcedPerspectiveId = this.getArgValue(E4HeadlessApplication.PERSPECTIVE_ARG_NAME, applicationContext, false);
        forcedPerspectiveId.ifPresent(forcedPerspectiveIdValue -> appContext.set(E4Workbench.FORCED_PERSPECTIVE_ID, forcedPerspectiveIdValue));
        
        final String showLocation = this.getLocationFromCommandLine();
        if (showLocation != null) {
            appContext.set(E4Workbench.FORCED_SHOW_LOCATION, showLocation);
        }
        
        // Create the app model and its context
        final MApplication appModel = this.loadApplicationModel(applicationContext, appContext);
        appModel.setContext(appContext);
        
        // final boolean isRtl = (Window.getDefaultOrientation() & SWT.RIGHT_TO_LEFT) != 0;
        appModel.getTransientData().put(E4Workbench.RTL_MODE, false);
        
        // for compatibility layer: set the application in the OSGi service context (see Workbench#getInstance())
        if (!E4Workbench.getServiceContext().containsKey(MApplication.class)) {
            // first one wins.
            E4Workbench.getServiceContext().set(MApplication.class, appModel);
        }
        
        // Set the app's context after adding itself
        appContext.set(MApplication.class, appModel);
        
        // adds basic services to the contexts
        E4HeadlessApplication.initializeServices(appModel);
        
        // let the life cycle manager add to the model
        if (this.lifeCylceManager != null) {
            ContextInjectionFactory.invoke(this.lifeCylceManager, ProcessAdditions.class, appContext, null);
            ContextInjectionFactory.invoke(this.lifeCylceManager, ProcessRemovals.class, appContext, null);
        }
        
        // Create the addons
        final IEclipseContext addonStaticContext = EclipseContextFactory.create();
        for (final MAddon addon : appModel.getAddons()) {
            addonStaticContext.set(MAddon.class, addon);
            final Object obj = factory.create(addon.getContributionURI(), appContext, addonStaticContext);
            addon.setObject(obj);
        }
        
        // Parse out parameters from both the command line and/or the product
        // definition (if any) and put them in the context
        final Optional<String> xmiURI = this.getArgValue(IWorkbench.XMI_URI_ARG, applicationContext, false);
        xmiURI.ifPresent(xmiURIValue -> {
            appContext.set(IWorkbench.XMI_URI_ARG, xmiURIValue);
        });
        
        // this.setCSSContextVariables(applicationContext, appContext);
        
        final Optional<String> rendererFactoryURI = this.getArgValue(E4Workbench.RENDERER_FACTORY_URI, applicationContext, false);
        rendererFactoryURI.ifPresent(rendererFactoryURIValue -> {
            appContext.set(E4Workbench.RENDERER_FACTORY_URI, rendererFactoryURIValue);
        });
        
        // This is a default arg, if missing we use the default rendering engine
        // final Optional<String> presentationURI = this.getArgValue(IWorkbench.PRESENTATION_URI_ARG, applicationContext, false);
        // appContext.set(IWorkbench.PRESENTATION_URI_ARG, presentationURI.orElse("bundleclass://org.eclipse.e4.ui.workbench.swt/org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine"));
        
        // Instantiate the Workbench (which is responsible for 'running' the UI (if any)...
        return this.workbench = new E4HeadlessWorkbench(appModel, appContext);
    }
    
    /**
     * @param appContext
     * @return
     */
    private URI determineApplicationModelURI(final IApplicationContext appContext) {
        final Optional<String> appModelPath = this.getArgValue(IWorkbench.XMI_URI_ARG, appContext, false);
        
        final String appModelPathValue = appModelPath.filter(path -> !path.isEmpty()).orElseGet(() -> {
            final Bundle brandingBundle = appContext.getBrandingBundle();
            if (brandingBundle != null) {
                return brandingBundle.getSymbolicName() + "/" + E4HeadlessApplication.APPLICATION_MODEL_PATH_DEFAULT;
            }
            final Logger logger = new WorkbenchLogger(E4HeadlessApplication.PLUGIN_ID);
            logger.error(new Exception(), "applicationXMI parameter not set and no branding plugin defined. "); //$NON-NLS-1$
            return null;
        });
        
        URI applicationModelURI = null;
        
        // check if the appModelPath is already a platform-URI and if so use it
        if (URIHelper.isPlatformURI(appModelPathValue)) {
            applicationModelURI = URI.createURI(appModelPathValue, true);
        } else {
            applicationModelURI = URI.createPlatformPluginURI(appModelPathValue, true);
        }
        return applicationModelURI;
        
    }
    
    /**
     * Finds an argument's value in the app's command line arguments, branding,
     * and system properties
     *
     * @param argName
     *            the argument name
     * @param appContext
     *            the application context
     * @param singledCmdArgValue
     *            whether it's a single-valued argument
     * @return an {@link Optional} containing the value or an empty
     *         {@link Optional}, if no value could be found
     */
    private Optional<String> getArgValue(final String argName, final IApplicationContext appContext, final boolean singledCmdArgValue) {
        // Is it in the arg list ?
        if (argName == null || argName.length() == 0) {
            return Optional.empty();
        }
        
        if (singledCmdArgValue) {
            for (final String arg : this.args) {
                if (("-" + argName).equals(arg)) {
                    return Optional.of("true");
                }
            }
        } else {
            for (int i = 0; i < this.args.length; i++) {
                if (("-" + argName).equals(this.args[i]) && i + 1 < this.args.length) {
                    return Optional.of(this.args[i + 1]);
                }
            }
        }
        
        final String brandingProperty = appContext.getBrandingProperty(argName);
        
        return Optional.ofNullable(brandingProperty).map(brandingPropertyValue -> Optional.of(brandingPropertyValue)).orElse(Optional.ofNullable(System.getProperty(argName)));
    }
    
    /**
     * @return the value of the {@link E4HeadlessApplication#SHOWLOCATION_ARG_NAME
     *         showlocation} command line argument, or <code>null</code> if it
     *         is not set
     */
    private String getLocationFromCommandLine() {
        final String fullArgName = "-" + E4HeadlessApplication.SHOWLOCATION_ARG_NAME;
        for (int i = 0; i < this.args.length; i++) {
            // ignore case for compatibility reasons
            if (fullArgName.equalsIgnoreCase(this.args[i])) { // $NON-NLS-1$
                String name = null;
                if (this.args.length > i + 1) {
                    name = this.args[i + 1];
                }
                if (name != null && name.indexOf("-") == -1) { //$NON-NLS-1$
                    return name;
                }
                return Platform.getLocation().toOSString();
            }
        }
        return null;
    }
    
    private MApplication loadApplicationModel(final IApplicationContext appContext, final IEclipseContext eclipseContext) {
        MApplication theApp = null;
        
        final Location instanceLocation = Activator.getInstanceLocation();
        
        final URI applicationModelURI = this.determineApplicationModelURI(appContext);
        eclipseContext.set(E4Workbench.INITIAL_WORKBENCH_MODEL_URI, applicationModelURI);
        
        // Save and restore
        final Boolean saveAndRestore = this.getArgValue(IWorkbench.PERSIST_STATE, appContext, false)
                .map(value -> Boolean.parseBoolean(value))
                .orElse(Boolean.TRUE);
        
        eclipseContext.set(IWorkbench.PERSIST_STATE, saveAndRestore);
        
        // when -data @none or -data @noDefault options
        if (instanceLocation != null && instanceLocation.getURL() != null) {
            eclipseContext.set(E4Workbench.INSTANCE_LOCATION, instanceLocation);
        } else {
            eclipseContext.set(IWorkbench.PERSIST_STATE, false);
        }
        
        // Persisted state
        final Boolean clearPersistedState = this.getArgValue(IWorkbench.CLEAR_PERSISTED_STATE, appContext, true)
                .map(value -> Boolean.parseBoolean(value))
                .orElse(Boolean.FALSE);
        eclipseContext.set(IWorkbench.CLEAR_PERSISTED_STATE, clearPersistedState);
        
        final String resourceHandler = this.getArgValue(IWorkbench.MODEL_RESOURCE_HANDLER, appContext, false)
                .orElse("bundleclass://org.eclipse.e4.ui.workbench/" + ResourceHandler.class.getName());
        
        final IContributionFactory factory = eclipseContext.get(IContributionFactory.class);
        
        this.handler = (IModelResourceHandler) factory.create(resourceHandler, eclipseContext);
        eclipseContext.set(IModelResourceHandler.class, this.handler);
        
        final Resource resource = this.handler.loadMostRecentModel();
        theApp = (MApplication) resource.getContents().get(0);
        
        return theApp;
    }
    
    private void saveModel() {
        // Save the model into the targetURI
        if (this.lifeCylceManager != null && this.workbench != null) {
            ContextInjectionFactory.invoke(this.lifeCylceManager, PreSave.class, this.workbench.getContext(), null);
        }
    }
    
}
