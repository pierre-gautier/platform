/*******************************************************************************
 * Copyright (c) 2003, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package platform.ui.workbench.about;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;

/**
 * WorkbenchJob is a type of job that implements a done listener
 * and does the shutdown checks before scheduling. This is used if
 * a job is not meant to run when the Workbench is shutdown.
 *
 * @since 3.0
 */
public abstract class WorkbenchJob
        extends UIJob {
    
    /**
     * Create a new instance of the receiver with the
     * supplied display and name.
     * Normally this constructor would not be used as
     * it is best to let the job find the display from
     * the workbench
     *
     * @param jobDisplay
     *            Display. The display to run the
     *            job with.
     * @param name
     *            String
     */
    public WorkbenchJob(final Display jobDisplay, final String name) {
        super(jobDisplay, name);
        this.addDefaultJobChangeListener();
    }
    
    /**
     * Add a job change listeners that handles a done
     * event if the result was IStatus.OK.
     */
    private void addDefaultJobChangeListener() {
        this.addJobChangeListener(new JobChangeAdapter() {
            
            @Override
            public void done(final IJobChangeEvent event) {
                
                if (event.getResult().getCode() == IStatus.OK) {
                    WorkbenchJob.this.performDone(event);
                }
            }
        });
    }
    
    /**
     * Perform done with the supplied event. This will
     * only occur if the returned status was OK.
     * This is called only if the job is finished with an IStatus.OK
     * result and the workbench is still running.
     *
     * @param event
     *            IJobChangeEvent
     */
    private void performDone(final IJobChangeEvent event) {
        // Do nothing by default.
    }
    
}
