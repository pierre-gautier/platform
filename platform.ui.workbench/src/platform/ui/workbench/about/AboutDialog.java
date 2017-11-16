package platform.ui.workbench.about;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import platform.ui.swt.SWTUtils;
import platform.utils.Strings;

/**
 * Displays information about the product.
 */
public class AboutDialog
        extends TrayDialog {
    
    private static final int  MAX_IMAGE_WIDTH_FOR_TEXT = 250;
    private static final int  WHATSNEW_ID              = 2049;
    private static final int  DETAILS_ID               = IDialogConstants.CLIENT_ID + 1;
    
    private final List<Image> images                   = new ArrayList<>();
    private final IProduct    product;
    
    private AboutTextManager  aboutTextManager;
    private String            productName;
    private StyledText        text;
    
    /**
     * Create an instance of the AboutDialog for the given window.
     *
     * @param parentShell
     *            The parent of the dialog.
     */
    public AboutDialog(final Shell parentShell) {
        super(parentShell);
        this.product = Platform.getProduct();
        if (this.product != null) {
            this.productName = this.product.getName();
        }
        if (this.productName == null) {
            this.productName = WorkbenchMessages.AboutDialog_defaultProductName;
        }
        
        // create a descriptive object for each BundleGroup
        final IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();
        final List<AboutBundleGroupData> groups = new LinkedList<>();
        if (providers != null) {
            for (final IBundleGroupProvider provider : providers) {
                final IBundleGroup[] bundleGroups = provider.getBundleGroups();
                for (final IBundleGroup bundleGroup : bundleGroups) {
                    groups.add(new AboutBundleGroupData(bundleGroup));
                }
            }
        }
    }
    
    @Override
    public boolean close() {
        // dispose all images
        for (int i = 0; i < this.images.size(); ++i) {
            final Image image = this.images.get(i);
            image.dispose();
        }
        return super.close();
    }
    
    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    @Override
    protected void buttonPressed(final int buttonId) {
        switch (buttonId) {
            case WHATSNEW_ID:
                final String updated = System.getProperty("updated"); //$NON-NLS-1$
                if (!Strings.isNullEmptyOrBlank(updated)) {
                    new LinkMessageDialog(Display.getCurrent().getActiveShell(), "Info", updated).open(); //$NON-NLS-1$
                }
                break;
            case DETAILS_ID:
                BusyIndicator.showWhile(this.getShell().getDisplay(), () -> {
                    new InstallationDialog(AboutDialog.this.getShell()).open();
                });
                break;
            default:
                super.buttonPressed(buttonId);
                break;
        }
    }
    
    /*
     * (non-Javadoc) Method declared on Window.
     */
    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        newShell.setMinimumSize(400, 400);
        newShell.setText(NLS.bind(WorkbenchMessages.AboutDialog_shellTitle, this.productName));
    }
    
    /**
     * Add buttons to the dialog's button bar.
     * Subclasses should override.
     *
     * @param parent
     *            the button bar composite
     */
    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        final Button w = this.createButton(parent, AboutDialog.WHATSNEW_ID, "What's new", false); //$NON-NLS-1$
        w.setEnabled(!Strings.isNullEmptyOrBlank(System.getProperty("updated"))); //$NON-NLS-1$
        
        this.createButton(parent, AboutDialog.DETAILS_ID, WorkbenchMessages.AboutDialog_DetailsButton, false);
        
        final Label l = new Label(parent, SWT.NONE);
        l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        final GridLayout layout = (GridLayout) parent.getLayout();
        layout.numColumns++;
        layout.makeColumnsEqualWidth = false;
        
        final Button b = this.createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        b.setFocus();
    }
    
    /**
     * Creates and returns the contents of the upper part
     * of the dialog (above the button bar).
     * Subclasses should overide.
     *
     * @param parent
     *            the parent composite to contain the dialog area
     * @return the dialog area control
     */
    @Override
    protected Control createDialogArea(final Composite parent) {
        // brand the about box if there is product info
        Image aboutImage = null;
        AboutItem item = null;
        if (this.product != null) {
            final String imagePath = ProductProperties.getAboutImagePath(this.product);
            if (imagePath != null) {
                aboutImage = SWTUtils.getImageFromUrl(imagePath);
            }
            
            // if the about image is small enough, then show the text
            if (aboutImage == null || aboutImage.getBounds().width <= AboutDialog.MAX_IMAGE_WIDTH_FOR_TEXT) {
                final String aboutText = ProductProperties.getAboutText(this.product);
                if (aboutText != null) {
                    item = AboutTextManager.scan(aboutText);
                }
            }
            
            if (aboutImage != null) {
                this.images.add(aboutImage);
            }
        }
        
        // create a composite which is the parent of the top area and the bottom
        // button bar, this allows there to be a second child of this composite with
        // a banner background on top but not have on the bottom
        final Composite workArea = new Composite(parent, SWT.NONE);
        final GridLayout workLayout = new GridLayout();
        workLayout.marginHeight = 0;
        workLayout.marginWidth = 0;
        workLayout.verticalSpacing = 0;
        workLayout.horizontalSpacing = 0;
        workArea.setLayout(workLayout);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        // page group
        final Color background = JFaceColors.getBannerBackground(parent.getDisplay());
        final Color foreground = JFaceColors.getBannerForeground(parent.getDisplay());
        final Composite top = (Composite) super.createDialogArea(workArea);
        
        // override any layout inherited from createDialogArea
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        top.setLayout(layout);
        top.setLayoutData(new GridData(GridData.FILL_BOTH));
        top.setBackground(background);
        top.setForeground(foreground);
        
        // the image & text
        final Composite topContainer = new Composite(top, SWT.NONE);
        topContainer.setBackground(background);
        topContainer.setForeground(foreground);
        
        layout = new GridLayout();
        layout.numColumns = aboutImage == null || item == null ? 1 : 2;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        topContainer.setLayout(layout);
        
        final GC gc = new GC(parent);
        // arbitrary default
        int topContainerHeightHint = 100;
        try {
            // default height enough for 6 lines of text
            topContainerHeightHint = Math.max(topContainerHeightHint, gc.getFontMetrics().getHeight() * 6);
        } finally {
            gc.dispose();
        }
        
        // image on left side of dialog
        if (aboutImage != null) {
            final Label imageLabel = new Label(topContainer, SWT.NONE);
            imageLabel.setBackground(background);
            imageLabel.setForeground(foreground);
            
            final GridData data = new GridData();
            data.horizontalAlignment = GridData.FILL;
            data.verticalAlignment = GridData.CENTER;
            data.grabExcessHorizontalSpace = false;
            imageLabel.setLayoutData(data);
            imageLabel.setImage(aboutImage);
            topContainerHeightHint = Math.max(topContainerHeightHint, aboutImage.getBounds().height);
        }
        
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.heightHint = topContainerHeightHint;
        topContainer.setLayoutData(data);
        
        if (item != null) {
            final int minWidth = 400; // This value should really be calculated
            final int minHeight = 400; // This value should really be calculated
            // from the computeSize(SWT.DEFAULT,
            // SWT.DEFAULT) of all the
            // children in infoArea excluding the
            // wrapped styled text
            // There is no easy way to do this.
            final ScrolledComposite scroller = new ScrolledComposite(topContainer, SWT.V_SCROLL | SWT.H_SCROLL);
            data = new GridData(GridData.FILL_BOTH);
            data.widthHint = minWidth;
            data.heightHint = minHeight;
            scroller.setLayoutData(data);
            
            final Composite textComposite = new Composite(scroller, SWT.NONE);
            textComposite.setBackground(background);
            
            layout = new GridLayout();
            textComposite.setLayout(layout);
            
            this.text = new StyledText(textComposite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
            
            // Don't set caret to 'null' as this causes https://bugs.eclipse.org/293263.
            this.text.setFont(parent.getFont());
            this.text.setText(item.getText());
            this.text.setCursor(null);
            this.text.setBackground(background);
            this.text.setForeground(foreground);
            
            this.aboutTextManager = new AboutTextManager(this.text);
            this.aboutTextManager.setItem(item);
            
            this.createTextMenu();
            
            final GridData gd = new GridData();
            gd.verticalAlignment = GridData.CENTER;
            gd.horizontalAlignment = GridData.FILL;
            gd.grabExcessHorizontalSpace = true;
            this.text.setLayoutData(gd);
            
            // Adjust the scrollbar increments
            scroller.getHorizontalBar().setIncrement(20);
            scroller.getVerticalBar().setIncrement(20);
            
            final boolean[] inresize = new boolean[1]; // flag to stop unneccesary
            // recursion
            textComposite.addControlListener(new ControlAdapter() {
                
                @Override
                public void controlResized(final ControlEvent e) {
                    if (inresize[0]) {
                        return;
                    }
                    inresize[0] = true;
                    // required because of bugzilla report 4579
                    textComposite.layout(true);
                    // required because you want to change the height that the
                    // scrollbar will scroll over when the width changes.
                    final int width = textComposite.getClientArea().width;
                    final Point p = textComposite.computeSize(width, SWT.DEFAULT);
                    scroller.setMinSize(minWidth, p.y);
                    inresize[0] = false;
                }
            });
            
            scroller.setExpandHorizontal(true);
            scroller.setExpandVertical(true);
            final Point p = textComposite.computeSize(minWidth, SWT.DEFAULT);
            textComposite.setSize(p.x, p.y);
            scroller.setMinWidth(minWidth);
            scroller.setMinHeight(p.y);
            
            scroller.setContent(textComposite);
        }
        
        // horizontal bar
        Label bar = new Label(workArea, SWT.HORIZONTAL | SWT.SEPARATOR);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        bar.setLayoutData(data);
        
        // add image buttons for bundle groups that have them
        final Composite bottom = (Composite) super.createDialogArea(workArea);
        // override any layout inherited from createDialogArea
        layout = new GridLayout();
        bottom.setLayout(layout);
        data = new GridData();
        data.verticalAlignment = SWT.FILL;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        
        bottom.setLayoutData(data);
        
        // spacer
        bar = new Label(bottom, SWT.NONE);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        bar.setLayoutData(data);
        
        return workArea;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    @Override
    protected boolean isResizable() {
        return true;
    }
    
    /**
     * Create the context menu for the text widget.
     *
     * @since 3.4
     */
    private void createTextMenu() {
        final MenuManager textManager = new MenuManager();
        this.text.setMenu(textManager.createContextMenu(this.text));
        this.text.addDisposeListener(e -> textManager.dispose());
        
    }
}
