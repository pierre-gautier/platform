package platform.model.commons;

import org.eclipse.core.runtime.Assert;

public class Col {
    
    public static final Col  NULL = new Col(0, 0, 0, 0);
    
    private static final int MAX  = 255;
    
    private final int        red;
    private final int        green;
    private final int        blue;
    private final int        alpha;
    
    public Col(final int red, final int green, final int blue) {
        this(red, green, blue, Col.MAX);
    }
    
    public Col(final int red, final int green, final int blue, final int alpha) {
        Assert.isTrue(red >= 0 && green >= 0 && blue >= 0 && alpha >= 0
                && red <= Col.MAX && green <= Col.MAX && blue <= Col.MAX && alpha <= Col.MAX);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
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
        final Col other = (Col) obj;
        if (this.alpha != other.alpha) {
            return false;
        }
        if (this.blue != other.blue) {
            return false;
        }
        if (this.green != other.green) {
            return false;
        }
        if (this.red != other.red) {
            return false;
        }
        return true;
    }
    
    public int getAlpha() {
        return this.alpha;
    }
    
    public int getBlue() {
        return this.blue;
    }
    
    public int getGreen() {
        return this.green;
    }
    
    public int getRed() {
        return this.red;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.alpha;
        result = prime * result + this.blue;
        result = prime * result + this.green;
        result = prime * result + this.red;
        return result;
    }
    
}
