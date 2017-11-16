package platform.ui.workbench.about;

import org.eclipse.swt.widgets.Button;

/**
 * Interface for a container that hosts one or more installation pages (
 * {@link InstallationPage}).
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 3.5
 */
interface IInstallationPageContainer {
    
    /**
     * Register a button as belonging to a particular page in the container. The
     * container will manage the placement and visibility of the page's buttons.
     *
     * @param page
     *            the page that created the button
     * @param button
     *            the button to be managed
     */
    void registerPageButton(InstallationPage page, Button button);
    
}
