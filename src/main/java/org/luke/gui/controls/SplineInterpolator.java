package org.luke.gui.controls;

import javafx.animation.Interpolator;

/**
 * Custom cubic Bezier spline interpolator for animations.
 * 
 * @author SDIDSA
 */
public class SplineInterpolator extends Interpolator {

	/**
	 * An interpolator that initiates by moving away from the target, then
	 * anticipates with an overshooting motion before returning, creating a dynamic
	 * and exaggerated effect. Ideal for animations requiring a playful and bouncy
	 * motion.
	 */
	public static final SplineInterpolator ANTICIPATEOVERSHOOT = new SplineInterpolator(0.68, -0.6, 0.32, 1.6);

	/**
	 * An interpolator that starts by moving away from the target, creating
	 * anticipation, and then returns to the target value, producing a subtle yet
	 * dynamic effect. Suitable for animations that benefit from a gentle start
	 * followed by a lively motion.
	 */
	public static final SplineInterpolator ANTICIPATE = new SplineInterpolator(0.36, 0, 0.66, -0.56);

	/**
	 * An interpolator that starts by moving towards the target value and then
	 * overshoots, creating a dynamic and slightly exaggerated motion. Useful for
	 * animations that need a lively and impactful transition.
	 */
	public static final SplineInterpolator OVERSHOOT = new SplineInterpolator(0.34, 1.56, 0.64, 1);

	/**
	 * An interpolator that starts quickly and gradually slows down towards the
	 * target value, resulting in a smooth deceleration effect. Commonly used for
	 * animations where a graceful and smooth finish is desired.
	 */
	public static final SplineInterpolator EASE_OUT = new SplineInterpolator(0, 0, 0.16, 0.99);

	/**
	 * An interpolator that starts slowly and then accelerates towards the target
	 * value, creating a gentle acceleration effect. Suitable for animations that
	 * require a smooth and gradual build-up.
	 */
	public static final SplineInterpolator EASE_IN = new SplineInterpolator(0.82, 0.01, 1, 1);

	/**
	 * An interpolator that combines ease-in and ease-out effects for a balanced and
	 * natural transition between values. Useful for animations that demand a
	 * harmonious and well-balanced motion.
	 */
	public static final SplineInterpolator EASE_BOTH = new SplineInterpolator(0.5, 0, 0.5, 1);

	private final double x1;
	private final double y1;
	private final double x2;
	private final double y2;

	private final boolean isCurveLinear;

	private static final int SAMPLE_SIZE = 16;
	private static final double SAMPLE_INCREMENT = 1.0 / SAMPLE_SIZE;
	private final double[] xSamples = new double[SAMPLE_SIZE + 1];

	private SplineInterpolator(double px1, double py1, double px2, double py2) {

		this.x1 = px1;
		this.y1 = py1;
		this.x2 = px2;
		this.y2 = py2;

		isCurveLinear = ((x1 == y1) && (x2 == y2));

		if (!isCurveLinear) {
			for (int i = 0; i < SAMPLE_SIZE + 1; ++i) {
				xSamples[i] = eval(i * SAMPLE_INCREMENT, x1, x2);
			}
		}
	}

	@Override
	public double curve(double x) {
		if (x < 0 || x > 1) {
			throw new IllegalArgumentException("x must be in range [0,1]");
		}

		if (isCurveLinear || x == 0 || x == 1) {
			return x;
		}

		return eval(findTForX(x), y1, y2);
	}

	private double eval(double t, double p1, double p2) {
		double compT = 1 - t;
		return t * (3 * compT * (compT * p1 + t * p2) + (t * t));
	}

	private double evalDerivative(double t, double p1, double p2) {
		double compT = 1 - t;
		return 3 * (compT * (compT * p1 + 2 * t * (p2 - p1)) + t * t * (1 - p2));
	}

	private double getInitialGuessForT(double x) {
		for (int i = 1; i < SAMPLE_SIZE + 1; ++i) {
			if (xSamples[i] >= x) {
				double xRange = xSamples[i] - xSamples[i - 1];
				if (xRange == 0) {
					return (i - 1) * SAMPLE_INCREMENT;
				} else {
					return ((i - 1) + ((x - xSamples[i - 1]) / xRange)) * SAMPLE_INCREMENT;
				}
			}
		}
		return 1;
	}

	private double findTForX(double x) {
		double t = getInitialGuessForT(x);
		final int numIterations = 4;
		for (int i = 0; i < numIterations; ++i) {
			double xT = (eval(t, x1, x2) - x);
			double dXdT = evalDerivative(t, x1, x2);
			if (xT == 0 || dXdT == 0) {
				break;
			}
			t -= xT / dXdT;
		}
		return t;
	}

	@Override
	public String toString() {
		return "SplineInterpolator [x1=" + x1 + ", y1=" + y1 + ", x2=" + x2 + ", y2=" + y2 + "]";
	}

}
