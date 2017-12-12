package platform.ws;

public class DelayerThread
        extends Thread {
    
    protected static final int DEFAULT_DELAY = 250;
    
    protected final Runnable   runnable;
    protected boolean          loop;
    
    private final int          delay;
    
    public DelayerThread(final String name, final Runnable runnable, final boolean loop, final int delay) {
        super(name);
        this.runnable = runnable;
        this.loop = loop;
        this.delay = delay;
    }
    
    public final void cancel() {
        this.loop = false;
        this.interrupt();
    }
    
    @Override
    public void run() {
        do {
            try {
                Thread.sleep(this.delay);
                this.exec();
            } catch (final InterruptedException e) {
                // if loop, loop, else do not loop ... (you'd better think about it !)
            }
        } while (this.loop);
    }
    
    protected void exec() {
        this.runnable.run();
        this.loop = false;
    }
}
