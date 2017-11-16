package platform.ui.workbench.about;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Abstract base implementation for an installation dialog page.
 * <p>
 * Clients should extend this class and include the name of the subclass in an
 * extension contributed to the workbench's installation pages extension point
 * (named <code>"org.eclipse.ui.installationPages"</code>). For example, the
 * plug-in's XML markup might contain:
 *
 * <pre>
 * &LT;extension point="org.eclipse.ui.installationPages"&GT;
 *      &LT;page id="com.example.myplugin.installInfo"
 *         name="Example Details"
 *         class="com.example.myplugin.MyInstallationPage" /&GT;
 * &LT;/extension&GT;
 * </pre>
 * </p>
 *
 * @since 3.5
 */
public abstract class InstallationPage
        extends DialogPage {
    
    private IInstallationPageContainer container;
    
    /**
     * Create the buttons that belong to this page using the specified parent.
     *
     * @param parent
     *            the parent to use for the buttons.
     * @see #createButton(Composite, int, String)
     * @see #buttonPressed(int)
     */
    public void createPageButtons(final Composite parent) {
        // By default, there are no page-specific buttons
    }
    
    /**
     * Set the page container that is hosting this page. This method is
     * typically called by the container itself so that the pages have access to
     * the container when registering buttons using
     * {@link IInstallationPageContainer#registerPageButton(InstallationPage, Button)}
     * or performing other container-related tasks.
     *
     * @param container
     *            the container that is hosting the page.
     */
    public void setPageContainer(final IInstallationPageContainer container) {
        this.container = container;
    }
    
    /**
     * Notifies that this page's button with the given id has been pressed.
     * Subclasses should extend this method to handle the buttons created in
     * {@link #createButton(Composite, int, String)}
     *
     * @param buttonId
     *            the id of the button that was pressed (see
     *            <code>IDialogConstants.*_ID</code> constants)
     */
    protected void buttonPressed(final int buttonId) {
        // do nothing
    }
    
    /**
     * Creates a new button with the given id.
     * <p>
     * This method creates a standard push button, registers it for selection
     * events, and registers it as a button belonging to this page. Subclasses
     * should not make any assumptions about the visibility, layout, or
     * presentation of this button inside the dialog.
     * </p>
     *
     * @param parent
     *            the parent composite
     * @param id
     *            the id of the button (see <code>IDialogConstants.*_ID</code>
     *            constants for standard dialog button ids)
     * @param label
     *            the label from the button
     * @return the new button
     * @see #createPageButtons(Composite)
     * @see #buttonPressed(int)
     */
    protected Button createButton(final Composite parent, final int id, final String label) {
        final Button button = new Button(parent, SWT.PUSH);
        button.setText(label);
        button.setData(Integer.valueOf(id));
        button.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(final SelectionEvent event) {
                InstallationPage.this.buttonPressed(((Integer) event.widget.getData()).intValue());
            }
        });
        this.container.registerPageButton(this, button);
        return button;
    }
    
}
