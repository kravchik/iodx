package yk.lang.iodx;

import java.io.Serializable;

public final class Vec2f implements Serializable {
    public float x, y;

    public static final Vec2f ZERO = new Vec2f();
    public static final Vec2f AXIS_X = new Vec2f(1, 0);
    public static final Vec2f AXIS_Y = new Vec2f(0, 1);

    public Vec2f() {}

    public static Vec2f v2(final float x, final float y) {
        return new Vec2f(x, y);
    }

    public Vec2f(final float x, final float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2f(final Vec2f v) {
        this.x = v.x;
        this.y = v.y;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public float lengthSqr() {
        return x * x + y * y;
    }

    @Override
    public String toString() {
        return "x: " + x + " y: " + y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vec2f vec2f = (Vec2f) o;

        if (Float.compare(vec2f.x, x) != 0) return false;
        if (Float.compare(vec2f.y, y) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        return result;
    }


}