package platform.ui.workbench.about;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import platform.ui.workbench.Activator;

/**
 * The UIJob is a Job that runs within the UI Thread via an asyncExec.
 *
 * @since 3.0
 */
public abstract class UIJob
        extends Job {
    
    private Display cachedDisplay;
    
    /**
     * Create a new instance of the receiver with the supplied Display.
     *
     * @param jobDisplay
     *            the display
     * @param name
     *            the job name
     * @see Job
     */
    public UIJob(final Display jobDisplay, final String name) {
        this(name);
        this.setDisplay(jobDisplay);
    }
    
    /**
     * Create a new instance of the receiver with the supplied name. The display
     * used will be the one from the workbench if this is available. UIJobs with
     * this constructor will determine their display at runtime.
     *
     * @param name
     *            the job name
     */
    private UIJob(final String name) {
        super(name);
    }
    
    /**
     * Returns the display for use by the receiver when running in an
     * asyncExec. If it is not set then the display set in the workbench
     * is used.
     * If the display is null the job will not be run.
     *
     * @return Display or <code>null</code>.
     */
    public Display getDisplay() {
        // If it was not set get it from the workbench
        if (this.cachedDisplay == null) {
            return Display.getCurrent();
        }
        return this.cachedDisplay;
    }
    
    /**
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     *      Note: this message is marked final. Implementors should use
     *      runInUIThread() instead.
     */
    @Override
    public final IStatus run(final IProgressMonitor monitor) {
        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        final Display asyncDisplay = this.getDisplay();
        if (asyncDisplay == null || asyncDisplay.isDisposed()) {
            return Status.CANCEL_STATUS;
        }
        asyncDisplay.asyncExec(() -> {
            IStatus result = null;
            Throwable throwable = null;
            try {
                // As we are in the UI Thread we can
                // always know what to tell the job.
                UIJob.this.setThread(Thread.currentThread());
                if (monitor.isCanceled()) {
                    result = Status.CANCEL_STATUS;
                } else {
                    result = UIJob.this.runInUIThread(monitor);
                }
                
            } catch (final Exception t) {
                throwable = t;
            } finally {
                if (result == null) {
                    result = new Status(IStatus.ERROR, Activator.getContext().getBundle().getSymbolicName(), IStatus.ERROR, "Internal error", throwable); //$NON-NLS-1$
                }
                UIJob.this.done(result);
            }
        });
        return Job.ASYNC_FINISH;
    }
    
    /**
     * Run the job in the UI Thread.
     *
     * @param monitor
     * @return IStatus
     */
    public abstract IStatus runInUIThread(IProgressMonitor monitor);
    
    /**
     * Sets the display to execute the asyncExec in. Generally this is not'
     * used if there is a valid display available via PlatformUI.isWorkbenchRunning().
     *
     * @param runDisplay
     *            Display
     * @see UIJob#getDisplay()
     * @see PlatformUI#isWorkbenchRunning()
     */
    public void setDisplay(final Display runDisplay) {
        Assert.isNotNull(runDisplay);
        this.cachedDisplay = runDisplay;
    }
}
