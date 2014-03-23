package org.marlin.pisces;

import sun.awt.geom.PathConsumer2D;

final class CollinearSimplifier implements PathConsumer2D {
    
    enum SimplifierState { Empty, PreviousPoint, PreviousLine };
    static final float EPS = 1e-3f; 

    PathConsumer2D delegate;
    SimplifierState state;
    float px1, py1, px2, py2;
    float pslope;
    
    CollinearSimplifier() {
    }
    
    public void init(PathConsumer2D delegate) {
        this.delegate = delegate;
        this.state = SimplifierState.Empty;
    }

    public void closePath() {
        emitStashedLine();
        delegate.closePath();
    }

    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {
        emitStashedLine();
        delegate.curveTo(x1, y1, x2, y2, x3, y3);
    }

    public long getNativeConsumer() {
        return delegate.getNativeConsumer();
    }

    public void lineTo(float x, float y) {
        if(state == SimplifierState.Empty) {
            setPreviousPoint(x, y);
            delegate.lineTo(x, y);
        } else if(state == SimplifierState.PreviousPoint) {
            px2 = x;
            py2 = y;
            pslope = getSlope(px1, py1, px2, py2);
            state = SimplifierState.PreviousLine;
        } else {
            final float slope = getSlope(px2, py2, x, y);
            // test for collinear
            if(slope == pslope || Math.abs(pslope - slope) < EPS) {
                px2 = x;
                py2 = y;
            } else {
                delegate.lineTo(px2, py2);
                px1 = px2;
                py1 = py2;
                px2 = x;
                py2 = y;
                pslope = slope;
            }
        }
    }
    
    private float getSlope(float x1, float y1, float x2, float y2) {
        if(y2 - y1 == 0) {
            return (x2 > x1) ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
        } else {
            return (x2 - x1) / (y2 - y1);
        }
    }

    private void setPreviousPoint(float x, float y) {
        px1 = x;
        py1 = y;
        state = SimplifierState.PreviousPoint;
    }

    public void moveTo(float x, float y) {
        emitStashedLine();
        delegate.moveTo(x, y);
        state = SimplifierState.PreviousPoint;
        px1 = x;
        py1 = y;
         
    }

    public void pathDone() {
        emitStashedLine();
        delegate.pathDone();
    }

    public void quadTo(float x1, float y1, float x2, float y2) {
        emitStashedLine();
        delegate.quadTo(x1, y1, x2, y2);
    }
    
    private void emitStashedLine() {
        if(state != SimplifierState.Empty) {
            if(state == SimplifierState.PreviousLine) {
                delegate.lineTo(px2, py2);
            }
            state = SimplifierState.Empty;
        }
    }

}
