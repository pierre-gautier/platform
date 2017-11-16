package platform.model.commons;

import org.eclipse.core.runtime.Assert;

import platform.utils.Strings;

public class Pos {
    
    public static final Pos NULL = new Pos(0d, 0d);
    
    public static Pos create(final double x, final double y) {
        if (x == 0d && y == 0d) {
            return Pos.NULL;
        }
        return new Pos(x, y);
    }
    
    public static final Pos toRatio(final Pos original, final Pos max) {
        final double r1 = original.x / original.y;
        final double r2 = max.x / max.y;
        if (r1 > r2) {
            return Pos.create(max.x, max.x / r1);
        }
        return Pos.create(max.y * r1, max.y);
    }
    
    public final Double x;
    
    public final Double y;
    
    protected Pos(final Double x, final Double y) {
        Assert.isNotNull(x);
        Assert.isNotNull(y);
        this.x = x;
        this.y = y;
    }
    
    public final Pos div(final double scale) {
        if (scale == 1) {
            return this;
        }
        return Pos.create(this.x / scale, this.y / scale);
    }
    
    public final Pos div(final Pos other) {
        if (other == null) {
            return this;
        }
        return Pos.create(this.x / other.x, this.y / other.y);
    }
    
    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Pos other = (Pos) obj;
        if (Math.abs(this.x - other.x) > this.getEqualsDelta()) {
            return false;
        }
        if (Math.abs(this.y - other.y) > this.getEqualsDelta()) {
            return false;
        }
        return true;
    }
    
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.x.hashCode();
        result = prime * result + this.y.hashCode();
        return result;
    }
    
    public boolean isEmpty() {
        return this.x <= 0 || this.y <= 0;
    }
    
    public final Pos minus(final double minusX, final double minusY) {
        return Pos.create(this.x - minusX, this.y - minusY);
    }
    
    public final Pos minus(final Pos other) {
        if (other == null) {
            return this;
        }
        return this.minus(other.x, other.y);
    }
    
    public final Pos mul(final double scale) {
        if (scale == 1) {
            return this;
        }
        return Pos.create(this.x * scale, this.y * scale);
    }
    
    public final Pos mul(final Pos other) {
        if (other == null) {
            return this;
        }
        return Pos.create(this.x * other.x, this.y * other.y);
    }
    
    public final Pos plus(final double plusX, final double plusY) {
        return Pos.create(this.x + plusX, this.y + plusY);
    }
    
    public final Pos plus(final Pos other) {
        if (other == null) {
            return this;
        }
        return this.plus(other.x, other.y);
    }
    
    @Override
    public String toString() {
        return this.x + Strings.COMMA + this.y;
    }
    
    protected double getEqualsDelta() {
        return 0.1;
    }
    
}
