package platform.ui.workbench.about;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobFunction;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Displays system information about the eclipse application. The content of
 * what is displayed is selectable through the
 * <code>org.eclipse.ui.systemSummaryExtensions</code> extension point.
 */
public final class AboutSystemPage
        extends ProductInfoPage {
    
    // This id is used when the system page is opened in its own dialog
    private static final String ID                       = "productInfo.system";          //$NON-NLS-1$
    
    private static final int    BROWSE_ERROR_LOG_BUTTON  = IDialogConstants.CLIENT_ID;
    private static final int    COPY_TO_CLIPBOARD_BUTTON = IDialogConstants.CLIENT_ID + 1;
    
    private static void fetchConfigurationInfo(final Text text) {
        text.setText(WorkbenchMessages.AboutSystemPage_RetrievingSystemInfo);
        final Job job = Job.create(WorkbenchMessages.AboutSystemPage_FetchJobTitle, (IJobFunction) monitor -> {
            final String info = ConfigurationInfo.getSystemSummary();
            if (!text.isDisposed()) {
                text.getDisplay().asyncExec(() -> {
                    if (!text.isDisposed()) {
                        text.setText(info);
                    }
                });
            }
            return Status.OK_STATUS;
        });
        job.schedule();
    }
    
    private Text text;
    
    public void copyToClipboard() {
        if (this.text == null) {
            return;
        }
        
        Clipboard clipboard = null;
        try {
            clipboard = new Clipboard(this.text.getShell().getDisplay());
            String contents = this.text.getSelectionText();
            if (contents.length() == 0) {
                contents = this.text.getText();
            }
            clipboard.setContents(new Object[] { contents },
                    new Transfer[] { TextTransfer.getInstance() });
        } finally {
            if (clipboard != null) {
                clipboard.dispose();
            }
        }
    }
    
    @Override
    public void createControl(final Composite parent) {
        
        final Composite outer = this.createOuterComposite(parent);
        
        this.text = new Text(outer, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.NO_FOCUS | SWT.H_SCROLL);
        this.text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        final GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
        gridData.grabExcessVerticalSpace = true;
        gridData.grabExcessHorizontalSpace = true;
        gridData.heightHint = this.convertVerticalDLUsToPixels(300);
        gridData.widthHint = this.convertHorizontalDLUsToPixels(400);
        this.text.setLayoutData(gridData);
        this.text.setFont(JFaceResources.getTextFont());
        AboutSystemPage.fetchConfigurationInfo(this.text);
        this.setControl(outer);
    }
    
    @Override
    public void createPageButtons(final Composite parent) {
        final Button button = this.createButton(parent, AboutSystemPage.BROWSE_ERROR_LOG_BUTTON, WorkbenchMessages.AboutSystemDialog_browseErrorLogName);
        final String filename = Platform.getLogFileLocation().toOSString();
        button.setEnabled(new File(filename).exists());
        this.createButton(parent, AboutSystemPage.COPY_TO_CLIPBOARD_BUTTON, WorkbenchMessages.AboutSystemDialog_copyToClipboardName);
    }
    
    @Override
    public String getId() {
        return AboutSystemPage.ID;
    }
    
    @Override
    protected void buttonPressed(final int buttonId) {
        switch (buttonId) {
            case BROWSE_ERROR_LOG_BUTTON:
                AboutUtils.openErrorLogBrowser(this.getShell());
                break;
            case COPY_TO_CLIPBOARD_BUTTON:
                if (this.text != null) {
                    Clipboard clipboard = null;
                    try {
                        clipboard = new Clipboard(this.getShell().getDisplay());
                        clipboard.setContents(new Object[] { this.text.getText() }, new Transfer[] { TextTransfer.getInstance() });
                    } finally {
                        if (clipboard != null) {
                            clipboard.dispose();
                        }
                    }
                }
                break;
            default:
                break;
        }
        super.buttonPressed(buttonId);
    }
    
}
