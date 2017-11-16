package platform.ui.workbench.about;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Abstract superclass of about dialog installation pages. The ProductInfoPage
 * is set up so that the page can be hosted as one of many pages in the
 * InstallationDialog, or as the only page in a ProductInfoDialog.
 */

public abstract class ProductInfoPage
        extends InstallationPage
        implements IShellProvider {
    
    private IProduct product;
    private String   productName;
    
    public String getProductName() {
        if (this.productName == null) {
            if (this.getProduct() != null) {
                this.productName = this.getProduct().getName();
            }
            if (this.productName == null) {
                this.productName = WorkbenchMessages.AboutDialog_defaultProductName;
            }
        }
        return this.productName;
    }
    
    public void setProductName(final String name) {
        this.productName = name;
    }
    
    protected Composite createOuterComposite(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(gd);
        final GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        return composite;
    }
    
    abstract String getId();
    
    private IProduct getProduct() {
        if (this.product == null) {
            this.product = Platform.getProduct();
        }
        return this.product;
    }
}
