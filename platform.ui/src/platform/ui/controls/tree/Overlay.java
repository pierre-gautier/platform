package platform.ui.controls.tree;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

@SuppressWarnings("deprecation")
public class Overlay
        extends CompositeImageDescriptor {
    
    public enum Position {
        
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;
        
    }
    
    private final Point           fSize;
    private final ImageDescriptor fBase;
    
    private ImageDescriptor       topLeftOverlay;
    private ImageDescriptor       topRightOverlay;
    private ImageDescriptor       bottomLeftOverlay;
    private ImageDescriptor       bottomRightOverlay;
    
    public Overlay(final ImageDescriptor base, final Point size) {
        this.fBase = base;
        this.fSize = size;
    }
    
    public void addOverlayIcon(final Position pos, final ImageDescriptor overlay) {
        switch (pos) {
            case TOP_LEFT:
                this.topLeftOverlay = overlay;
                break;
            case TOP_RIGHT:
                this.topRightOverlay = overlay;
                break;
            case BOTTOM_LEFT:
                this.bottomLeftOverlay = overlay;
                break;
            default:
                this.bottomRightOverlay = overlay;
                break;
        }
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Overlay other = (Overlay) obj;
        if (this.bottomLeftOverlay == null) {
            if (other.bottomLeftOverlay != null) {
                return false;
            }
        } else if (!this.bottomLeftOverlay.equals(other.bottomLeftOverlay)) {
            return false;
        }
        if (this.bottomRightOverlay == null) {
            if (other.bottomRightOverlay != null) {
                return false;
            }
        } else if (!this.bottomRightOverlay.equals(other.bottomRightOverlay)) {
            return false;
        }
        if (this.fBase == null) {
            if (other.fBase != null) {
                return false;
            }
        } else if (!this.fBase.equals(other.fBase)) {
            return false;
        }
        if (this.fSize == null) {
            if (other.fSize != null) {
                return false;
            }
        } else if (!this.fSize.equals(other.fSize)) {
            return false;
        }
        if (this.topLeftOverlay == null) {
            if (other.topLeftOverlay != null) {
                return false;
            }
        } else if (!this.topLeftOverlay.equals(other.topLeftOverlay)) {
            return false;
        }
        if (this.topRightOverlay == null) {
            if (other.topRightOverlay != null) {
                return false;
            }
        } else if (!this.topRightOverlay.equals(other.topRightOverlay)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.bottomLeftOverlay == null ? 0 : this.bottomLeftOverlay.hashCode());
        result = prime * result + (this.bottomRightOverlay == null ? 0 : this.bottomRightOverlay.hashCode());
        result = prime * result + (this.fBase == null ? 0 : this.fBase.hashCode());
        result = prime * result + (this.fSize == null ? 0 : this.fSize.hashCode());
        result = prime * result + (this.topLeftOverlay == null ? 0 : this.topLeftOverlay.hashCode());
        result = prime * result + (this.topRightOverlay == null ? 0 : this.topRightOverlay.hashCode());
        return result;
    }
    
    @Override
    protected void drawCompositeImage(final int width, final int height) {
        ImageData bg;
        if (this.fBase == null || (bg = this.fBase.getImageData()) == null) {
            bg = ImageDescriptor.DEFAULT_IMAGE_DATA;
        }
        this.drawImage(bg, 0, 0);
        this.drawOverlays();
    }
    
    protected void drawOverlays() {
        if (this.topLeftOverlay != null) {
            this.drawImage(this.topLeftOverlay.getImageData(), 0, 0);
        }
        if (this.topRightOverlay != null) {
            int x = this.getSize().x;
            final ImageData id = this.topRightOverlay.getImageData();
            x -= id.width;
            this.drawImage(id, x, 0);
        }
        if (this.bottomLeftOverlay != null) {
            final ImageData id = this.bottomLeftOverlay.getImageData();
            int y = this.getSize().y;
            y -= id.height;
            this.drawImage(this.bottomLeftOverlay.getImageData(), 0, y);
        }
        if (this.bottomRightOverlay != null) {
            final ImageData id = this.bottomRightOverlay.getImageData();
            int x = this.getSize().x;
            int y = this.getSize().y;
            x -= id.width;
            y -= id.height;
            this.drawImage(this.bottomRightOverlay.getImageData(), x, y);
        }
    }
    
    @Override
    protected Point getSize() {
        return this.fSize;
    }
}
