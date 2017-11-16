package platform.ui.workbench.about;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * @since 3.5
 */
public class InstallationDialog
        extends TrayDialog
        implements IInstallationPageContainer {
    
    private static class ButtonManager {
        
        private final Composite           composite;
        private Map<String, List<Button>> buttonMap = new HashMap<>(); // page id->Collection of page
        
        // buttons
        
        private ButtonManager(final Composite composite) {
            this.composite = composite;
        }
        
        public Composite getParent() {
            return this.composite;
        }
        
        protected void setButtonLayoutData(final FontMetrics metrics, final Control button, final boolean visible) {
            final GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
            final int widthHint = Dialog.convertHorizontalDLUsToPixels(metrics, IDialogConstants.BUTTON_WIDTH);
            final Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
            data.widthHint = Math.max(widthHint, minSize.x);
            data.exclude = !visible;
            button.setLayoutData(data);
        }
        
        private void addPageButton(final String id, final Button button) {
            List<Button> list = this.buttonMap.get(id);
            if (list == null) {
                list = new ArrayList<>(1);
                this.buttonMap.put(id, list);
            }
            list.add(button);
        }
        
        private void clear() {
            this.buttonMap = new HashMap<>();
        }
        
        private void update(final String currentPageId) {
            if (this.composite == null || this.composite.isDisposed()) {
                return;
            }
            final GC metricsGC = new GC(this.composite);
            final FontMetrics metrics = metricsGC.getFontMetrics();
            metricsGC.dispose();
            final List<?> buttons = this.buttonMap.get(currentPageId);
            final Control[] children = this.composite.getChildren();
            
            int visibleChildren = 0;
            for (final Control control : children) {
                control.setVisible(false);
                this.setButtonLayoutData(metrics, control, false);
            }
            if (buttons != null) {
                for (int i = 0; i < buttons.size(); i++) {
                    final Button button = (Button) buttons.get(i);
                    button.setVisible(true);
                    this.setButtonLayoutData(metrics, button, true);
                    final GridData data = (GridData) button.getLayoutData();
                    data.exclude = false;
                    visibleChildren++;
                }
            }
            
            final GridLayout compositeLayout = (GridLayout) this.composite.getLayout();
            compositeLayout.numColumns = visibleChildren;
            this.composite.layout(true);
        }
    }
    
    private static final String ATT_ID                  = "id";                         //$NON-NLS-1$
    private static final String ATT_NAME                = "name";                       //$NON-NLS-1$
    private static final String ATT_CLASS               = "class";                      //$NON-NLS-1$
    private static final String ID                      = "ID";                         //$NON-NLS-1$
    
    private static final String DIALOG_SETTINGS_SECTION = "InstallationDialogSettings"; //$NON-NLS-1$
    
    private static final int    TAB_WIDTH_IN_DLUS       = 440;
    private static final int    TAB_HEIGHT_IN_DLUS      = 320;
    
    private static String       lastSelectedTabId;
    
    private static void rememberSelectedTab(final String pageId) {
        InstallationDialog.lastSelectedTabId = pageId;
    }
    
    private final Map<InstallationPage, String> pageToId = new HashMap<>();
    
    private TabFolder                           folder;
    private ButtonManager                       buttonManager;
    
    public InstallationDialog(final Shell parentShell) {
        super(parentShell);
    }
    
    @Override
    public void registerPageButton(final InstallationPage page, final Button button) {
        this.buttonManager.addPageButton(this.pageToId(page), button);
    }
    
    @Override
    protected void buttonPressed(final int buttonId) {
        if (IDialogConstants.CLOSE_ID == buttonId) {
            this.okPressed();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets
     * .Shell)
     */
    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        String productName = ""; //$NON-NLS-1$
        final IProduct product = Platform.getProduct();
        if (product != null && product.getName() != null) {
            productName = product.getName();
        }
        newShell.setText(NLS.bind(WorkbenchMessages.InstallationDialog_ShellTitle, productName));
    }
    
    protected void createButtons(final InstallationPage page) {
        page.createPageButtons(this.buttonManager.getParent());
        Dialog.applyDialogFont(this.buttonManager.getParent());
    }
    
    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
     * .swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        // The button manager will handle the correct sizing of the buttons.
        // We do not want columns equal width because we are going to add some
        // padding in the final column (close button).
        final GridLayout layout = (GridLayout) parent.getLayout();
        layout.makeColumnsEqualWidth = false;
        this.buttonManager = new ButtonManager(parent);
    }
    
    @Override
    protected Control createContents(final Composite parent) {
        final Control control = super.createContents(parent);
        boolean selected = false;
        if (this.folder.getItemCount() > 0) {
            if (InstallationDialog.lastSelectedTabId != null) {
                final TabItem[] items = this.folder.getItems();
                for (int i = 0; i < items.length; i++) {
                    if (items[i].getData(InstallationDialog.ID).equals(InstallationDialog.lastSelectedTabId)) {
                        this.folder.setSelection(i);
                        this.tabSelected(items[i]);
                        selected = true;
                        break;
                    }
                }
            }
            if (!selected) {
                this.tabSelected(this.folder.getItem(0));
            }
        }
        // need to reapply the dialog font now that we've created new
        // tab items
        Dialog.applyDialogFont(this.folder);
        return control;
    }
    
    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
     * .Composite)
     */
    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        
        this.folder = new TabFolder(composite, SWT.NONE);
        this.createFolderItems(this.folder);
        
        final GridData folderData = new GridData(SWT.FILL, SWT.FILL, true, true);
        folderData.widthHint = this.convertHorizontalDLUsToPixels(InstallationDialog.TAB_WIDTH_IN_DLUS);
        folderData.heightHint = this.convertVerticalDLUsToPixels(InstallationDialog.TAB_HEIGHT_IN_DLUS);
        this.folder.setLayoutData(folderData);
        this.folder.addSelectionListener(this.createFolderSelectionListener());
        this.folder.addDisposeListener(e -> InstallationDialog.this.releaseContributions());
        return composite;
    }
    
    protected void createFolderItems(final TabFolder tabFolder) {
        for (final IConfigurationElement element : Platform.getExtensionRegistry().getConfigurationElementsFor("platform.ui.workbench.installationPages")) { //$NON-NLS-1$
            try {
                Class.forName(element.getAttribute(InstallationDialog.ATT_CLASS));
                final TabItem item = new TabItem(tabFolder, SWT.NONE);
                item.setData(InstallationDialog.ID, element.getAttribute(InstallationDialog.ATT_ID));
                item.setText(element.getAttribute(InstallationDialog.ATT_NAME));
                item.setData(element);
                final Composite control = new Composite(tabFolder, SWT.NONE);
                control.setLayout(new GridLayout());
                item.setControl(control);
            } catch (final ClassNotFoundException e) {
                // ignore
            }
        }
    }
    
    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        final IDialogSettings settings = new DialogSettings("Workbench"); //$NON-NLS-1$
        IDialogSettings section = settings.getSection(InstallationDialog.DIALOG_SETTINGS_SECTION);
        if (section == null) {
            section = settings.addNewSection(InstallationDialog.DIALOG_SETTINGS_SECTION);
        }
        return section;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    @Override
    protected boolean isResizable() {
        return true;
    }
    
    protected String pageToId(final InstallationPage page) {
        final String pageId = this.pageToId.get(page);
        Assert.isLegal(pageId != null);
        return pageId;
    }
    
    private SelectionAdapter createFolderSelectionListener() {
        return new SelectionAdapter() {
            
            @Override
            public void widgetSelected(final SelectionEvent e) {
                InstallationDialog.this.tabSelected((TabItem) e.item);
            }
        };
    }
    
    private void releaseContributions() {
        this.buttonManager.clear();
    }
    
    /*
     * Must be called after contributions and button manager are created.
     */
    private void tabSelected(final TabItem item) {
        if (item.getData() instanceof IConfigurationElement) {
            final IConfigurationElement element = (IConfigurationElement) item.getData();
            
            final Composite pageComposite = (Composite) item.getControl();
            try {
                final String className = element.getAttribute(InstallationDialog.ATT_CLASS);
                final String simpleClassName = className.substring(className.lastIndexOf('.'));
                final String newClassName = this.getClass().getPackage().getName() + simpleClassName;
                final Class<?> clazz = Class.forName(newClassName);
                final InstallationPage page = (InstallationPage) clazz.newInstance();
                page.createControl(pageComposite);
                // new controls created since the dialog font was applied, so apply again.
                Dialog.applyDialogFont(pageComposite);
                page.setPageContainer(this);
                // Must be done before creating the buttons because the control button creation methods use this map.
                this.pageToId.put(page, element.getAttribute(InstallationDialog.ATT_ID));
                this.createButtons(page);
                item.setData(page);
                item.addDisposeListener(e -> page.dispose());
                pageComposite.layout(true, true);
            } catch (final Exception e1) {
                e1.printStackTrace();
            }
        }
        final String id = (String) item.getData(InstallationDialog.ID);
        InstallationDialog.rememberSelectedTab(id);
        this.buttonManager.update(id);
        final Button button = this.createButton(this.buttonManager.getParent(), IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
        final GridData gd = (GridData) button.getLayoutData();
        gd.horizontalAlignment = SWT.BEGINNING;
        gd.horizontalIndent = this.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH) / 2;
        // Layout the button bar's parent and all of its children. We must
        // cascade through all children because the buttons have changed and
        // because tray dialog inserts an extra composite in the button bar
        // hierarchy.
        this.getButtonBar().getParent().layout(true, true);
        
    }
}
