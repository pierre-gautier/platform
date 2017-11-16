package platform.ui.workbench.about;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.ConfigureColumns;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;

import platform.ui.swt.SWTUtils;
import platform.ui.workbench.Activator;

/**
 * Displays information about the product plugins.
 * PRIVATE this class is internal to the IDE
 */
public class AboutPluginsPage
        extends ProductInfoPage {
    
    private class BundleTableLabelProvider
            extends LabelProvider
            implements ITableLabelProvider {
        
        /**
         * Queue containing bundle signing info to be resolved.
         */
        private final Deque<AboutBundleData> resolveQueue = new LinkedList<>();
        
        /**
         * Queue containing bundle data that's been resolve and needs updating.
         */
        private final List<AboutBundleData>  updateQueue  = new ArrayList<>();
        
        /*
         * this job will attempt to discover the signing state of a given bundle
         * and then send it along to the update job
         */
        private final Job                    resolveJob   = new Job(AboutPluginsPage.class.getName()) {
                                                              
                                                              {
                                                                  this.setSystem(true);
                                                                  this.setPriority(Job.SHORT);
                                                              }
                                                              
                                                              @Override
                                                              protected IStatus run(final IProgressMonitor monitor) {
                                                                  while (true) {
                                                                      // If the UI has not been created, nothing to do.
                                                                      if (AboutPluginsPage.this.vendorInfo == null) {
                                                                          return Status.OK_STATUS;
                                                                      }
                                                                      // If the UI has been disposed since we were asked to
                                                                      // render, nothing to do.
                                                                      final Table table = AboutPluginsPage.this.vendorInfo.getTable();
                                                                      // the table has been disposed since we were asked to render
                                                                      if (table == null || table.isDisposed()) {
                                                                          return Status.OK_STATUS;
                                                                      }
                                                                      AboutBundleData data = null;
                                                                      synchronized (BundleTableLabelProvider.this.resolveQueue) {
                                                                          if (BundleTableLabelProvider.this.resolveQueue.isEmpty()) {
                                                                              return Status.OK_STATUS;
                                                                          }
                                                                          data = BundleTableLabelProvider.this.resolveQueue.removeFirst();
                                                                      }
                                                                      try {
                                                                          // following is an expensive call
                                                                          data.isSigned();
                                                                          synchronized (BundleTableLabelProvider.this.updateQueue) {
                                                                              BundleTableLabelProvider.this.updateQueue.add(data);
                                                                          }
                                                                          // start the update job
                                                                          BundleTableLabelProvider.this.updateJob.schedule();
                                                                      } catch (final IllegalStateException e) {
                                                                          // the bundle we're testing has been unloaded. Do
                                                                          // nothing.
                                                                      }
                                                                  }
                                                              }
                                                          };
        
        /*
         * this job is responsible for feeding label change events into the
         * viewer as they become available from the resolve job
         */
        private final Job                    updateJob    = new WorkbenchJob(Display.getCurrent(), AboutPluginsPage.class.getName()) {
                                                              
                                                              {
                                                                  this.setSystem(true);
                                                                  this.setPriority(Job.DECORATE);
                                                              }
                                                              
                                                              @Override
                                                              public IStatus runInUIThread(final IProgressMonitor monitor) {
                                                                  while (true) {
                                                                      final Control page = AboutPluginsPage.this.getControl();
                                                                      // the page has gone down since we were asked to render
                                                                      if (page == null || page.isDisposed()) {
                                                                          return Status.OK_STATUS;
                                                                      }
                                                                      AboutBundleData[] data = null;
                                                                      synchronized (BundleTableLabelProvider.this.updateQueue) {
                                                                          if (BundleTableLabelProvider.this.updateQueue.isEmpty()) {
                                                                              return Status.OK_STATUS;
                                                                          }
                                                                          data = BundleTableLabelProvider.this.updateQueue
                                                                                  .toArray(new AboutBundleData[BundleTableLabelProvider.this.updateQueue.size()]);
                                                                          BundleTableLabelProvider.this.updateQueue.clear();
                                                                      }
                                                                      BundleTableLabelProvider.this.fireLabelProviderChanged(new LabelProviderChangedEvent(BundleTableLabelProvider.this, data));
                                                                  }
                                                              }
                                                          };
        
        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java
         * .lang.Object, int)
         */
        @Override
        public Image getColumnImage(final Object element, final int columnIndex) {
            if (columnIndex == 0 && element instanceof AboutBundleData) {
                final AboutBundleData data = (AboutBundleData) element;
                if (data.isSignedDetermined()) {
                    if (data.isSigned()) {
                        return SWTUtils.getImageFromUrl("platform:/plugin/" + Activator.getContext().getBundle().getSymbolicName() + "/icons/signed_yes_tbl.png"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    return SWTUtils.getImageFromUrl("platform:/plugin/" + Activator.getContext().getBundle().getSymbolicName() + "/icons/signed_no_tbl.png"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                synchronized (this.resolveQueue) {
                    this.resolveQueue.add(data);
                }
                this.resolveJob.schedule();
                return SWTUtils.getImageFromUrl("platform:/plugin/" + Activator.getContext().getBundle().getSymbolicName() + "/icons/signed_unkn_tbl.png"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return null;
        }
        
        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.
         * lang.Object, int)
         */
        @Override
        public String getColumnText(final Object element, final int columnIndex) {
            if (element instanceof AboutBundleData) {
                final AboutBundleData data = (AboutBundleData) element;
                switch (columnIndex) {
                    case 1:
                        return data.getProviderName();
                    case 2:
                        return data.getName();
                    case 3:
                        return data.getVersion();
                    case 4:
                        return data.getId();
                    default:
                        return ""; //$NON-NLS-1$
                }
            }
            return ""; //$NON-NLS-1$
        }
    }
    
    // This id is used when the page is created inside its own dialog
    private static final String   ID                       = "productInfo.plugins";         //$NON-NLS-1$
    
    /**
     * Table height in dialog units (value 200).
     */
    private static final int      TABLE_HEIGHT             = 200;
    
    private static final int      MORE_ID                  = IDialogConstants.CLIENT_ID + 1;
    private static final int      SIGNING_ID               = AboutPluginsPage.MORE_ID + 1;
    private static final int      COLUMNS_ID               = AboutPluginsPage.MORE_ID + 2;
    
    private static final IPath    BASE_NL_PATH             = new Path("$nl$");              //$NON-NLS-1$
    
    private static final String   PLUGININFO               = "about.html";                  //$NON-NLS-1$
    
    private static final int      PLUGIN_NAME_COLUMN_INDEX = 2;
    
    private static final int      SIGNING_AREA_PERCENTAGE  = 30;
    
    private static final String[] COLUMN_TITLES            = {
            WorkbenchMessages.AboutPluginsDialog_signed,
            WorkbenchMessages.AboutPluginsDialog_provider,
            WorkbenchMessages.AboutPluginsDialog_pluginName,
            WorkbenchMessages.AboutPluginsDialog_version,
            WorkbenchMessages.AboutPluginsDialog_pluginId, };
    
    /**
     * Return an URL to the plugin's about.html file (what is shown when
     * "More info" is pressed) or null if no such file exists. The method does
     * nl lookup to allow for i18n.
     *
     * @param bundleInfo
     *            the bundle info
     * @param makeLocal
     *            whether to make the about content local
     * @return the URL or <code>null</code>
     */
    @SuppressWarnings("deprecation")
    private static URL getMoreInfoURL(final AboutBundleData bundleInfo, final boolean makeLocal) {
        final Bundle bundle = Platform.getBundle(bundleInfo.getId());
        if (bundle == null) {
            return null;
        }
        
        final URL aboutUrl = Platform.find(bundle, AboutPluginsPage.BASE_NL_PATH.append(AboutPluginsPage.PLUGININFO), null);
        if (!makeLocal) {
            return aboutUrl;
        }
        if (aboutUrl != null) {
            try {
                final URL result = Platform.asLocalURL(aboutUrl);
                try {
                    // Make local all content in the "about" directory.
                    // This is needed to handle jar'ed plug-ins.
                    // See Bug 88240 [About] About dialog needs to extract subdirs.
                    new URL(aboutUrl, "about_files"); //$NON-NLS-1$
                } catch (final IOException e) {
                    // skip the about dir if its not found or there are other problems.
                }
                return result;
            } catch (final IOException e) {
                // do nothing
            }
        }
        return null;
    }
    
    private static boolean isReady(final int bundleState) {
        return (bundleState & (Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING)) != 0;
    }
    
    /**
     * Check if the currently selected plugin has additional information to
     * show.
     *
     * @param bundleInfo
     * @return true if the selected plugin has additional info available to
     *         display
     */
    private static boolean selectionHasInfo(final AboutBundleData bundleInfo) {
        final URL infoURL = AboutPluginsPage.getMoreInfoURL(bundleInfo, false);
        // only report ini problems if the -debug command line argument is used
        if (infoURL == null) {
            System.err.println("Problem reading plugin info for: " + bundleInfo.getName()); //$NON-NLS-1$
        }
        return infoURL != null;
    }
    
    // private Bundle[] bundles = Activator.getContext().getBundles();
    private TableViewer       vendorInfo;
    private AboutBundleData[] bundleInfos;
    private SashForm          sashForm;
    private BundleSigningInfo signingArea;
    private Button            moreInfo, signingInfo;
    private String            message;
    
    @Override
    public void createControl(final Composite parent) {
        this.initializeDialogUnits(parent);
        
        // create a data object for each bundle, remove duplicates, and include
        // only resolved bundles (bug 65548)
        final Map<String, AboutBundleData> map = new HashMap<>();
        for (final Bundle bundle : Activator.getContext().getBundles()) {
            final AboutBundleData data = new AboutBundleData(bundle);
            
            if (AboutPluginsPage.isReady(data.getState()) && !map.containsKey(data.getVersionedId())) {
                map.put(data.getVersionedId(), data);
            }
        }
        this.bundleInfos = map.values().toArray(new AboutBundleData[0]);
        Activator.class.getSigners();
        
        this.sashForm = new SashForm(parent, SWT.HORIZONTAL | SWT.SMOOTH);
        final FillLayout layout = new FillLayout();
        this.sashForm.setLayout(layout);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        final GridData data = new GridData(GridData.FILL_BOTH);
        this.sashForm.setLayoutData(data);
        
        final Composite outer = this.createOuterComposite(this.sashForm);
        
        if (this.message != null) {
            final Label label = new Label(outer, SWT.NONE);
            label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            label.setFont(parent.getFont());
            label.setText(this.message);
        }
        
        this.createTable(outer);
        this.setControl(outer);
    }
    
    @Override
    public void createPageButtons(final Composite parent) {
        this.moreInfo = this.createButton(parent, AboutPluginsPage.MORE_ID, WorkbenchMessages.AboutPluginsDialog_moreInfo);
        this.moreInfo.setEnabled(false);
        this.signingInfo = this.createButton(parent, AboutPluginsPage.SIGNING_ID, WorkbenchMessages.AboutPluginsDialog_signingInfo_show);
        this.signingInfo.setEnabled(false);
        this.createButton(parent, AboutPluginsPage.COLUMNS_ID, WorkbenchMessages.AboutPluginsDialog_columns);
    }
    
    // public void setBundles(final Bundle[] bundles) {
    // this.bundles = Arrays.copyOf(bundles, bundles.length);
    //
    // }
    
    @Override
    public void setMessage(final String message) {
        this.message = message;
    }
    
    @Override
    protected void buttonPressed(final int buttonId) {
        switch (buttonId) {
            case MORE_ID:
                this.handleMoreInfoPressed();
                break;
            case SIGNING_ID:
                this.handleSigningInfoPressed();
                break;
            case COLUMNS_ID:
                this.handleColumnsPressed();
                break;
            default:
                super.buttonPressed(buttonId);
                break;
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.internal.about.ProductInfoPage#getId()
     */
    @Override
    String getId() {
        return AboutPluginsPage.ID;
    }
    
    private void checkEnablement() {
        // enable if there is an item selected and that
        // item has additional info
        final IStructuredSelection selection = (IStructuredSelection) this.vendorInfo
                .getSelection();
        if (selection.getFirstElement() instanceof AboutBundleData) {
            final AboutBundleData selected = (AboutBundleData) selection
                    .getFirstElement();
            this.moreInfo.setEnabled(AboutPluginsPage.selectionHasInfo(selected));
            this.signingInfo.setEnabled(true);
            if (this.signingArea != null) {
                this.signingArea.setData(selected);
            }
        } else {
            this.signingInfo.setEnabled(false);
            this.moreInfo.setEnabled(false);
        }
    }
    
    /**
     * Create the table part of the dialog.
     *
     * @param parent
     *            the parent composite to contain the dialog area
     */
    private void createTable(final Composite parent) {
        final Text filterText = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
        filterText.setLayoutData(GridDataFactory.fillDefaults().create());
        filterText.setMessage(WorkbenchMessages.AboutPluginsDialog_filterTextMessage);
        filterText.setFocus();
        
        this.vendorInfo = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
        this.vendorInfo.setUseHashlookup(true);
        this.vendorInfo.getTable().setHeaderVisible(true);
        this.vendorInfo.getTable().setLinesVisible(true);
        this.vendorInfo.getTable().setFont(parent.getFont());
        this.vendorInfo.addSelectionChangedListener(event -> AboutPluginsPage.this.checkEnablement());
        
        final TableComparator comparator = new TableComparator();
        this.vendorInfo.setComparator(comparator);
        final int[] columnWidths = {
                this.convertHorizontalDLUsToPixels(30), // signature
                this.convertHorizontalDLUsToPixels(120),
                this.convertHorizontalDLUsToPixels(120),
                this.convertHorizontalDLUsToPixels(70),
                this.convertHorizontalDLUsToPixels(130), };
        
        // create table headers
        for (int i = 0; i < AboutPluginsPage.COLUMN_TITLES.length; i++) {
            final TableColumn column = new TableColumn(this.vendorInfo.getTable(),
                    SWT.NULL);
            if (i == AboutPluginsPage.PLUGIN_NAME_COLUMN_INDEX) { // prime initial sorting
                this.updateTableSorting(i);
            }
            column.setWidth(columnWidths[i]);
            column.setText(AboutPluginsPage.COLUMN_TITLES[i]);
            final int columnIndex = i;
            column.addSelectionListener(new SelectionAdapter() {
                
                @Override
                public void widgetSelected(final SelectionEvent e) {
                    AboutPluginsPage.this.updateTableSorting(columnIndex);
                }
            });
        }
        
        this.vendorInfo.setContentProvider(new ArrayContentProvider());
        this.vendorInfo.setLabelProvider(new BundleTableLabelProvider());
        
        final BundlePatternFilter searchFilter = new BundlePatternFilter();
        filterText.addModifyListener(e -> {
            searchFilter.setPattern(filterText.getText());
            AboutPluginsPage.this.vendorInfo.refresh();
        });
        this.vendorInfo.addFilter(searchFilter);
        
        final GridData gridData = new GridData(GridData.FILL, GridData.FILL, true,
                true);
        gridData.heightHint = this.convertVerticalDLUsToPixels(AboutPluginsPage.TABLE_HEIGHT);
        this.vendorInfo.getTable().setLayoutData(gridData);
        
        this.vendorInfo.setInput(this.bundleInfos);
    }
    
    /**
     *
     */
    private void handleColumnsPressed() {
        ConfigureColumns.forTable(this.vendorInfo.getTable(), this);
    }
    
    /**
     * The More Info button was pressed. Open a browser showing the license
     * information for the selected bundle or an error dialog if the browser
     * cannot be opened.
     */
    private void handleMoreInfoPressed() {
        if (this.vendorInfo == null) {
            return;
        }
        
        if (this.vendorInfo.getSelection().isEmpty()) {
            return;
        }
        
        final AboutBundleData bundleInfo = (AboutBundleData) ((IStructuredSelection) this.vendorInfo.getSelection()).getFirstElement();
        
        if (!Program.launch(AboutPluginsPage.getMoreInfoURL(bundleInfo, true).toString())) {
            final String errorMessage = NLS.bind(WorkbenchMessages.AboutPluginsDialog_unableToOpenFile, AboutPluginsPage.PLUGININFO, bundleInfo.getId());
            System.err.println(WorkbenchMessages.AboutPluginsDialog_errorTitle + ": " + errorMessage); //$NON-NLS-1$
        }
    }
    
    /**
     */
    private void handleSigningInfoPressed() {
        if (this.signingArea == null) {
            this.signingArea = new BundleSigningInfo();
            final AboutBundleData bundleInfo = (AboutBundleData) ((IStructuredSelection) this.vendorInfo.getSelection()).getFirstElement();
            this.signingArea.setData(bundleInfo);
            this.signingArea.createContents(this.sashForm);
            this.sashForm.setWeights(new int[] { 100 - AboutPluginsPage.SIGNING_AREA_PERCENTAGE, AboutPluginsPage.SIGNING_AREA_PERCENTAGE });
            this.signingInfo.setText(WorkbenchMessages.AboutPluginsDialog_signingInfo_hide);
        } else {
            // hide
            this.signingInfo.setText(WorkbenchMessages.AboutPluginsDialog_signingInfo_show);
            this.signingArea.dispose();
            this.signingArea = null;
            this.sashForm.setWeights(new int[] { 100 });
        }
    }
    
    /**
     * Update the sort information on both the comparator and the table.
     *
     * @param columnIndex
     *            the index to sort by
     * @since 3.4
     */
    private void updateTableSorting(final int columnIndex) {
        final TableComparator comparator = (TableComparator) this.vendorInfo.getComparator();
        // toggle direction if it's the same column
        if (columnIndex == comparator.getSortColumn()) {
            comparator.setAscending(!comparator.isAscending());
        }
        comparator.setSortColumn(columnIndex);
        this.vendorInfo.getTable().setSortColumn(this.vendorInfo.getTable().getColumn(columnIndex));
        this.vendorInfo.getTable().setSortDirection(comparator.isAscending() ? SWT.UP : SWT.DOWN);
        this.vendorInfo.refresh(false);
    }
}

class BundlePatternFilter
        extends ViewerFilter {
    
    private StringMatcher matcher;
    
    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        if (this.matcher == null) {
            return true;
        }
        
        if (element instanceof AboutBundleData) {
            final AboutBundleData data = (AboutBundleData) element;
            return this.matcher.match(data.getName()) || this.matcher.match(data.getProviderName())
                    || this.matcher.match(data.getId());
        }
        return true;
    }
    
    public void setPattern(final String searchPattern) {
        if (searchPattern == null || searchPattern.length() == 0) {
            this.matcher = null;
        } else {
            final String pattern = "*" + searchPattern + "*"; //$NON-NLS-1$//$NON-NLS-2$
            this.matcher = new StringMatcher(pattern, true, false);
        }
    }
}

class TableComparator
        extends ViewerComparator {
    
    /**
     * @param data
     * @return a sort value depending on the signed state
     */
    private static int getSignedSortValue(final AboutBundleData data) {
        if (!data.isSignedDetermined()) {
            return 0;
        } else if (data.isSigned()) {
            return 1;
        } else {
            return -1;
        }
    }
    
    private int     sortColumn     = 0;
    private int     lastSortColumn = 0;
    private boolean ascending      = true;
    
    private boolean lastAscending  = true;
    
    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.
     * viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(final Viewer viewer, final Object e1, final Object e2) {
        if (this.sortColumn == 0 && e1 instanceof AboutBundleData
                && e2 instanceof AboutBundleData) {
            final AboutBundleData d1 = (AboutBundleData) e1;
            final AboutBundleData d2 = (AboutBundleData) e2;
            final int diff = TableComparator.getSignedSortValue(d1) - TableComparator.getSignedSortValue(d2);
            // If values are different, or there is no secondary column defined,
            // we are done
            if (diff != 0 || this.lastSortColumn == 0) {
                return this.ascending ? diff : -diff;
            }
            // try a secondary sort
            if (viewer instanceof TableViewer) {
                final TableViewer tableViewer = (TableViewer) viewer;
                final IBaseLabelProvider baseLabel = tableViewer.getLabelProvider();
                if (baseLabel instanceof ITableLabelProvider) {
                    final ITableLabelProvider tableProvider = (ITableLabelProvider) baseLabel;
                    final String e1p = tableProvider.getColumnText(e1, this.lastSortColumn);
                    final String e2p = tableProvider.getColumnText(e2, this.lastSortColumn);
                    final int result = this.getComparator().compare(e1p, e2p);
                    return this.lastAscending ? result : -1 * result;
                }
            }
            // we couldn't determine a secondary sort, call it equal
            return 0;
        }
        if (viewer instanceof TableViewer) {
            final TableViewer tableViewer = (TableViewer) viewer;
            final IBaseLabelProvider baseLabel = tableViewer.getLabelProvider();
            if (baseLabel instanceof ITableLabelProvider) {
                final ITableLabelProvider tableProvider = (ITableLabelProvider) baseLabel;
                String e1p = tableProvider.getColumnText(e1, this.sortColumn);
                String e2p = tableProvider.getColumnText(e2, this.sortColumn);
                int result = this.getComparator().compare(e1p, e2p);
                // Secondary column sort
                if (result == 0) {
                    if (this.lastSortColumn != 0) {
                        e1p = tableProvider.getColumnText(e1, this.lastSortColumn);
                        e2p = tableProvider.getColumnText(e2, this.lastSortColumn);
                        result = this.getComparator().compare(e1p, e2p);
                        return this.lastAscending ? result : -1 * result;
                    } // secondary sort is by column 0
                    if (e1 instanceof AboutBundleData
                            && e2 instanceof AboutBundleData) {
                        final AboutBundleData d1 = (AboutBundleData) e1;
                        final AboutBundleData d2 = (AboutBundleData) e2;
                        final int diff = TableComparator.getSignedSortValue(d1)
                                - TableComparator.getSignedSortValue(d2);
                        return this.lastAscending ? diff : -diff;
                    }
                }
                // primary column sort
                return this.ascending ? result : -1 * result;
            }
        }
        
        return super.compare(viewer, e1, e2);
    }
    
    /**
     * @return Returns the sortColumn.
     */
    public int getSortColumn() {
        return this.sortColumn;
    }
    
    /**
     * @return Returns the ascending.
     */
    public boolean isAscending() {
        return this.ascending;
    }
    
    /**
     * @param ascending
     *            The ascending to set.
     */
    public void setAscending(final boolean ascending) {
        this.ascending = ascending;
    }
    
    /**
     * @param sortColumn
     *            The sortColumn to set.
     */
    public void setSortColumn(final int sortColumn) {
        if (this.sortColumn != sortColumn) {
            this.lastSortColumn = this.sortColumn;
            this.lastAscending = this.ascending;
            this.sortColumn = sortColumn;
        }
    }
}
