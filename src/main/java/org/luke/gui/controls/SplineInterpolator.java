package org.luke.gui.controls;

import javafx.animation.Interpolator;

public class SplineInterpolator extends Interpolator {

	public static final SplineInterpolator ANTICIPATEOVERSHOOT = new SplineInterpolator(0.68, -0.6, 0.32, 1.6);
	public static final SplineInterpolator ANTICIPATE = new SplineInterpolator(0.36, 0, 0.66, -0.56);
	public static final SplineInterpolator OVERSHOOT = new SplineInterpolator(0.34, 1.56, 0.64, 1);

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
