package platform.ui.workbench.about;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

public class LinkMessageDialog
        extends MessageDialog {
    
    private Link messageLink;
    
    public LinkMessageDialog(final Shell parentShell, final String title, final String dialogMessage) {
        super(parentShell, title, null, dialogMessage, MessageDialog.INFORMATION, new String[] { IDialogConstants.OK_LABEL }, 0);
    }
    
    @Override
    protected Control createMessageArea(final Composite composite) {
        // create composite
        // create image
        final Image image = this.getImage();
        if (image != null) {
            this.imageLabel = new Label(composite, SWT.NULL);
            image.setBackground(this.imageLabel.getBackground());
            this.imageLabel.setImage(image);
            // addAccessibleListeners(imageLabel, image);
            GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING).applyTo(this.imageLabel);
        }
        // create message
        if (this.message != null) {
            this.messageLink = new Link(composite, this.getMessageLabelStyle());
            this.messageLink.setText(this.message);
            GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.BEGINNING)
                    .hint(this.convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
                    .applyTo(this.messageLink);
            this.messageLink.addSelectionListener(new SelectionAdapter() {
                
                @Override
                public void widgetSelected(final SelectionEvent e) {
                    Program.launch(e.text);
                }
            });
        }
        return composite;
    }
    
}
