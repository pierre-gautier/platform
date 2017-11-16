package platform.ui.workbench.about;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.signedcontent.SignedContent;
import org.eclipse.osgi.signedcontent.SignedContentFactory;
import org.eclipse.osgi.signedcontent.SignerInfo;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import platform.ui.workbench.Activator;

/**
 * @since 3.3
 */
public class BundleSigningInfo {
    
    /**
     * @param certString
     * @return
     */
    private static Properties parseCert(final String certString) {
        final StringTokenizer toker = new StringTokenizer(certString, ","); //$NON-NLS-1$
        final Properties cert = new Properties();
        while (toker.hasMoreTokens()) {
            final String pair = toker.nextToken();
            final int idx = pair.indexOf('=');
            if (idx > 0 && idx < pair.length() - 2) {
                final String key = pair.substring(0, idx).trim();
                String value = pair.substring(idx + 1).trim();
                if (value.length() > 2) {
                    if (value.charAt(0) == '\"') {
                        value = value.substring(1);
                    }
                    
                    if (value.charAt(value.length() - 1) == '\"') {
                        value = value.substring(0, value.length() - 1);
                    }
                }
                cert.setProperty(key, value);
            }
        }
        return cert;
    }
    
    private static Properties[] parseCerts(final Certificate[] chain) {
        final List<Map<?, ?>> certs = new ArrayList<>(chain.length);
        for (int i = 0; i < chain.length; i++) {
            if (!(chain[i] instanceof X509Certificate)) {
                continue;
            }
            final Map<?, ?> cert = BundleSigningInfo.parseCert(((X509Certificate) chain[i]).getSubjectDN().getName());
            if (cert != null) {
                certs.add(cert);
            }
        }
        return certs.toArray(new Properties[certs.size()]);
        
    }
    
    private Composite       composite;
    private StyledText      certificate;
    private Text            date;
    private AboutBundleData data;
    
    public BundleSigningInfo() {
    }
    
    public Control createContents(final Composite parent) {
        
        this.composite = new Composite(parent, SWT.BORDER);
        this.composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        final GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        this.composite.setLayout(layout);
        
        // date
        final Label dateLabel = new Label(this.composite, SWT.NONE);
        dateLabel.setText(WorkbenchMessages.BundleSigningTray_Signing_Date);
        final GridData dateGridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        this.date = new Text(this.composite, SWT.READ_ONLY);
        final GC gc = new GC(this.date);
        gc.setFont(JFaceResources.getDialogFont());
        final Point size = gc.stringExtent(DateFormat.getDateTimeInstance().format(new Date()));
        dateGridData.widthHint = size.x;
        gc.dispose();
        this.date.setText(WorkbenchMessages.BundleSigningTray_Working);
        this.date.setLayoutData(dateGridData);
        // signer
        final Label signerLabel = new Label(this.composite, SWT.NONE);
        signerLabel.setText(WorkbenchMessages.BundleSigningTray_Signing_Certificate);
        GridData signerGridData = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false);
        signerGridData.horizontalSpan = 2;
        signerGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        signerGridData.horizontalSpan = 2;
        this.certificate = new StyledText(this.composite, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
        this.certificate.setText(WorkbenchMessages.BundleSigningTray_Working);
        this.certificate.setLayoutData(signerGridData);
        Dialog.applyDialogFont(this.composite);
        
        this.startJobs(); // start the jobs that will prime the content
        return this.composite;
    }
    
    public void dispose() {
        this.composite.dispose();
    }
    
    public void setData(final AboutBundleData data) {
        this.data = data;
        this.startJobs();
    }
    
    /**
     *
     */
    private boolean isOpen() {
        return this.certificate != null && !this.certificate.isDisposed();
    }
    
    /**
     *
     */
    private void startJobs() {
        if (!this.isOpen()) {
            return;
        }
        this.certificate.setText(WorkbenchMessages.BundleSigningTray_Working);
        this.date.setText(WorkbenchMessages.BundleSigningTray_Working);
        final BundleContext bundleContext = Activator.getContext();
        final ServiceReference<?> factoryRef = bundleContext.getServiceReference(SignedContentFactory.class.getName());
        if (factoryRef == null) {
            System.err.println(WorkbenchMessages.BundleSigningTray_Cant_Find_Service);
            return;
        }
        
        final SignedContentFactory contentFactory = (SignedContentFactory) bundleContext.getService(factoryRef);
        if (contentFactory == null) {
            System.err.println(WorkbenchMessages.BundleSigningTray_Cant_Find_Service);
            return;
        }
        
        final AboutBundleData myData = this.data;
        final Job signerJob = new Job(NLS.bind(WorkbenchMessages.BundleSigningTray_Determine_Signer_For, myData.getId())) {
            
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                try {
                    if (!myData.equals(BundleSigningInfo.this.data)) {
                        return Status.OK_STATUS;
                    }
                    final SignedContent signedContent = contentFactory.getSignedContent(myData.getBundle());
                    if (!myData.equals(BundleSigningInfo.this.data)) {
                        return Status.OK_STATUS;
                    }
                    final SignerInfo[] signers = signedContent.getSignerInfos();
                    final String signerText, dateText;
                    if (!BundleSigningInfo.this.isOpen() && myData.equals(BundleSigningInfo.this.data)) {
                        return Status.OK_STATUS;
                    }
                    
                    if (signers.length == 0) {
                        signerText = WorkbenchMessages.BundleSigningTray_Unsigned;
                        dateText = WorkbenchMessages.BundleSigningTray_Unsigned;
                    } else {
                        final Properties[] certs = BundleSigningInfo.parseCerts(signers[0].getCertificateChain());
                        if (certs.length == 0) {
                            signerText = WorkbenchMessages.BundleSigningTray_Unknown;
                        } else {
                            final StringBuffer buffer = new StringBuffer();
                            for (final Iterator<?> i = certs[0].entrySet().iterator(); i.hasNext();) {
                                final Entry<?, ?> entry = (Entry<?, ?>) i.next();
                                buffer.append(entry.getKey());
                                buffer.append('=');
                                buffer.append(entry.getValue());
                                if (i.hasNext()) {
                                    buffer.append('\n');
                                }
                            }
                            signerText = buffer.toString();
                        }
                        
                        final Date signDate = signedContent.getSigningTime(signers[0]);
                        if (signDate != null) {
                            dateText = DateFormat.getDateTimeInstance().format(signDate);
                        } else {
                            dateText = WorkbenchMessages.BundleSigningTray_Unknown;
                        }
                    }
                    
                    // PlatformUI.getWorkbench().getDisplay().asyncExec(
                    Display.getCurrent().asyncExec(
                            () -> {
                                // check to see if the tray is still visible
                                // and if
                                // we're still looking at the same item
                                if (!BundleSigningInfo.this.isOpen() && !myData.equals(BundleSigningInfo.this.data)) {
                                    return;
                                }
                                BundleSigningInfo.this.certificate.setText(signerText);
                                BundleSigningInfo.this.date.setText(dateText);
                            });
                            
                } catch (final IOException e) {
                    return new Status(IStatus.ERROR, Activator.getContext().getBundle().getSymbolicName(), e.getMessage(), e);
                } catch (final GeneralSecurityException e) {
                    return new Status(IStatus.ERROR, Activator.getContext().getBundle().getSymbolicName(), e.getMessage(), e);
                }
                return Status.OK_STATUS;
            }
        };
        signerJob.setSystem(true);
        signerJob.belongsTo(signerJob);
        signerJob.schedule();
        
        final Job cleanup = new Job(
                WorkbenchMessages.BundleSigningTray_Unget_Signing_Service) {
            
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                try {
                    Job.getJobManager().join(signerJob, monitor);
                } catch (final OperationCanceledException e) {
                    e.printStackTrace();
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                bundleContext.ungetService(factoryRef);
                return Status.OK_STATUS;
            }
        };
        cleanup.setSystem(true);
        cleanup.schedule();
        
    }
}
