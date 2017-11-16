package platform.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import platform.ui.fields.Form;
import platform.ui.fields.IForm;
import platform.ui.messages.Messages;
import platform.ui.swt.SWTUtils;

public abstract class APreferencePage
        extends FieldEditorPreferencePage {
    
    protected final IForm form;
    protected boolean     saving;
    protected Text        filter;
    
    protected APreferencePage(final String title) {
        super(title, FieldEditorPreferencePage.GRID);
        this.noDefaultAndApplyButton();
        this.form = new Form();
    }
    
    @Override
    public final Control createContents(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(SWTUtils.createGridLayout(1, false, 0, 0));
        this.createHeadContents(composite);
        super.createContents(composite);
        return composite;
    }
    
    @Override
    public final void createControl(final Composite parent) {
        super.createControl(parent);
        this.form.registerChangeTrackingControl(this.getApplyButton());
        this.form.registerValidationTrackingControl(this.getApplyButton());
        this.form.registerChangeTrackingControl(this.getDefaultsButton());
        if (this.getDefaultsButton() != null) {
            this.getDefaultsButton().setText(Messages.keywordReset);
        }
        if (this.getApplyButton() != null) {
            this.getApplyButton().setText(Messages.keywordSave);
        }
    }
    
    @Override
    public boolean performOk() {
        this.saving = true;
        final boolean result = super.performOk();
        this.saving = false;
        return result;
    }
    
    @Override
    protected void createFieldEditors() {
        // do nothing
    }
    
    protected void createFilter(final Composite composite, final int columuns, final String message) {
        final Composite filterComposite = SWTUtils.createComposite(composite, 2, false, 0, 0, new GridData(SWT.FILL, SWT.BEGINNING, true, false, columuns, 1));
        this.filter = SWTUtils.createText(filterComposite);
        this.filter.setMessage(message);
        this.filter.addListener(SWT.Traverse, e -> {
            if (e.detail == SWT.TRAVERSE_RETURN) {
                this.doFilter();
                e.doit = false;
            }
        });
        final Image imageSearch = SWTUtils.getImageFromUrl("platform:/plugin/com.centreon.studio.client.model.ui/icons/search.gif", 16, 16); //$NON-NLS-1$
        final Button okButton = SWTUtils.createButtonPush(filterComposite, SWT.END, SWT.CENTER, imageSearch, new SelectionAdapter() {
            
            @Override
            public void widgetSelected(final SelectionEvent e) {
                APreferencePage.this.doFilter();
            }
        });
        okButton.setBackground(this.filter.getBackground());
    }
    
    @SuppressWarnings("unused")
    protected void createHeadContents(final Composite parent) {
        // do nothing
    }
    
    protected void doFilter() {
        // do nothing
    }
    
    @Override
    protected void initialize() {
        if (this.saving) {
            return;
        }
        super.initialize();
    }
    
    @Override
    protected void updateApplyButton() {
        if (this.getApplyButton() != null) {
            this.getApplyButton().setEnabled(this.form.hasChanged() && this.form.isValid());
        }
        if (this.getDefaultsButton() != null) {
            this.getDefaultsButton().setEnabled(this.form.hasChanged());
        }
    }
    
}
