package org.marlin.pisces;

/**
 * Applies a simple line simplification algorithm based on distance and angle:
 * <ul>
 * <li>Segments are simplified out only when connected to other segments</li>
 * <li>A segment is simplified out if its start and end elements are falling in the same sub-pixel</li>
 * <li>A segment is simplified out even if it spans more than one sub-pixel provided it is collinear
 * to the one that came before it</li>
 * <ul>
 */
final class LineSimplifier {

    static final float EPS = 1e-3f;

    private boolean hasPreviousLine;

    private float px1, py1, px2, py2;

    private float pslope;

    float sx1, sy1, sx2, sy2;

    LineSimplifier() {
        this.hasPreviousLine = false;
    }

    private float getSlope(final float x1, final float y1, final float x2, final float y2) {
        if (y2 - y1 == 0) {
            return (x2 > x1) ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
        } else {
            return (x2 - x1) / (y2 - y1);
        }
    }

    boolean addLine(final float x1, final float y1, final float x2, final float y2) {
        // check we have a previous line to compare to, and the new edge is a continuation of the
        // previous one
        final float slope;
        if (hasPreviousLine && px2 == x1 && py2 == y2) {
            // same pixel test
            if(x1 == x2 && y1 == y2) {
                return false;
            }
//            // this one is never entered into, so I've commented it out
//            else if (samePixel(x1, y1, x2, y2)) {
//                // so short it all falls in the same (sub)pixel,
//                // let's just extend the previous line
//                px2 = x2;
//                py2 = y2;
//                // recompute the slope, we're interested in the overall slope of
//                // the simplified line, not the slope of its first too short element
//                pslope = getSlope(px1, py1, x2, y2);
//                return false;
//            }

            // collinearity test
            slope = getSlope(x1, y1, x2, y2);
            if (slope == pslope || Math.abs(pslope - slope) < EPS) {
                // almost collinear lines, extend the previous line
                px2 = x2;
                py2 = y2;
                return false;
            }
        } else {
            // we'll need the slope later
            slope = getSlope(x1, y1, x2, y2);
        }

        // we could not simplify out the line, accumulate state
        if (hasPreviousLine) {
            previousToExposed();
        }
        px1 = x1;
        py1 = y1;
        px2 = x2;
        py2 = y2;
        pslope = slope;

        if (!hasPreviousLine) {
            hasPreviousLine = true;
            // no line to fetch from the simplifier, move on
            return false;
        } else {
            // fetch the previous line from the simplifier
            return true;
        }
    }

    /**
     * Makes the previous segment available for the taking in the renderer code
     */
    private void previousToExposed() {
        sx1 = px1;
        sy1 = py1;
        sx2 = px2;
        sy2 = py2;
    }

    private boolean samePixel(final float x1, final float y1, final float x2, final float y2) {
        return (int) x1 == (int) x2 && (int) y1 == (int) y2;
    }

    public boolean completed() {
        if (hasPreviousLine) {
            previousToExposed();
            hasPreviousLine = false;

            return true;
        } else {
            return false;
        }
    }

}
