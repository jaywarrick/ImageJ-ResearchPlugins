/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2016 Board of Regents of the University of
 * Wisconsin-Madison, University of Konstanz and Brian Northan.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package function.ops.zernike;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.Op;
import net.imagej.ops.features.zernike.helper.Polynom;
import net.imagej.ops.features.zernike.helper.ZernikeMoment;
import net.imagej.ops.geom.geom2d.Circle;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.types.BigComplex;
import net.imglib2.IterableInterval;
import net.imglib2.RealCursor;
import net.imglib2.RealLocalizable;
import net.imglib2.type.numeric.RealType;

/**
 * 
 * Computes a specific zernike moment
 * 
 * @author Andreas Graumann, University of Konstanz
 * @author jaywarrick (added ability to define and calculate enclosing circle)
 */
@Plugin(type = Op.class)
public class DoubleNormalizedZernikeComputer<T extends RealType<T>> extends
AbstractUnaryFunctionOp<IterableInterval<T>, ZernikeMoment>
{	
	@Parameter
	private int order;

	@Parameter
	private int repetition;

	@Parameter(label = "The inner circle inside which radius will be normalized between 0 and the user-defined perimeter value (< 1)")
	private Circle innerCircle;
	
	@Parameter(label = "The outer circle. Between the inner and outer circle radius will be normalized between the inner circle perimeter value and 1")
	private Circle outerCircle;
	
	@Parameter(required = false, label = "The desired ratio of the inner circle radius to outer circle radius upon normalization")
	private double innerRadiusToOuterRadiusRatio = 1.0/3.0;

	private double x1, y1, c;
	
	/*
	 * Notes:
	 * 
	 * r1 is radius of inner circle
	 * r2 is radius of outer circle
	 * r is the distance from the center of circle 1 to the point to be calculated
	 * theta is the angle from the center of the inner circle through the point be calculated
	 * R is the distance from the center of the inner circle to the perimeter of the outer circle at an angle theta
	 * x1 is the horizontal component of the vector from the center of the inner circle to the outer circle
	 * y1 is the vertical component of the vector from the center of the inner circle to the outer circle
	 * x2 is the horizontal component of the vector from the center of the inner circle to the point to be calculated
	 * y2 is the vertical component of the vector from the center of the inner circle to the point to be calculated
	 * 
	 * The equation for an offset circle in polar coordinates is r2^2 = (R*cos(theta) - x1)^2 + (R*sin(theta) - y1)
	 * This is solved for using the quadratic formula. The angle theta is atan2(y2, x2)
	 * 
	 * Inside the inner circle, the radius is normalized to between 0 and this.intermediateNormalizedValue.
	 * Outside the inner circle (but inside the outer circle), the radius is normalized to between this.intermediateNormalizedValue and 1.
	 * Outside the outer circle, pixels are ignored.
	 * 
	 */

	@Override
	public void initialize() {
		super.initialize();
	}

	@Override
	public ZernikeMoment calculate(IterableInterval<T> ii) {

		// Compute pascal's triangle for binomal coefficients: d[x][y] equals (x
		// over y)
		double[][] d = computePascalsTriangle(order);

		// initialize zernike moment
		ZernikeMoment moment = initZernikeMoment(order, repetition, d);

		// get the cursor of the iterable interval
		final RealCursor<? extends RealType<?>> cur = ii.localizingCursor();

		// count number of pixel inside the unit circle
		int count = 0;
		
		// To produces expected results, we will treat the outer circle provided as the true
		// outer circle where the norm radius will be 1. Thus, when we typically use the 
		// equivalent radius of the whole cell, we should actually pass an outercircle with
		// a radius slightly larger (e.g., 1.5X) the radius of the whole cell so that the cell
		// does not extend past the bounds of the unit circle (that often). Thus, more typically,
		// The norm radius at the boundary of the cell will be about 2/3 (1/1.5). So, we set
		// the inner radius norm value to be ~1/3 (half the expected radius of the whole cell.
		double outer = 1;
		
		double inner = outer*this.innerRadiusToOuterRadiusRatio;

		// run over iterable interval
		while (cur.hasNext()) {
			cur.fwd();

			double[] radii = this.getRadii(cur);

			final double r1 = innerCircle.getRadius();

			double normRad = -1;
			if(radii[0] <= r1)
			{
				// then we are within the inner circle
				normRad = (inner)*(radii[0]/r1);
			}
			else if(radii[0] <= radii[1])
			{
				// we are outside the inner and within the bounds of the outer circle
				normRad = (outer-inner)*((radii[0]-r1)/(radii[1]-r1)) + (inner);
			}
			else
			{
				// we are outside the bounds of the outer circle
				// Skip the pixel and exclude it from the calculation
				continue;
			}

			// get current pixel value
			double pixel = cur.get().getRealDouble();

			if (pixel >= 0.0) {
				// increment number of pixel inside the unit circle
				count++;

				// calculate the desired moment
				// evaluate radial polynom at the normalized radial position normRad
				final double rad = moment.getP().evaluate(normRad);

				// p * rad * exp(-1i * m * theta);
				final BigComplex product = multiplyExp(pixel, rad, radii[2], moment.getM());

				// add together
				moment.getZm().add(product);
			}

		}

		// normalization
		normalize(moment.getZm(), moment.getN(), count);

		return moment;
	}

	/**
	 * 
	 * Multiplication of pixel * rad * exp(-m*theta) using eulers formula
	 * (pixel*rad) * (cos(m*theta) - i*sin(m*theta))
	 * 
	 * @param _pixel
	 *            Current pixel
	 * @param _rad
	 *            Computed value of radial polynom,
	 * @param _theta
	 *            Angle of current position
	 * @param _m
	 *            Repitition m
	 * @return Computed term
	 */
	private BigComplex multiplyExp(final double _pixel, final double _rad, final double _theta, final int _m) {
		BigComplex c = new BigComplex();
		c.setReal(_pixel * _rad * Math.cos(_m * _theta));
		c.setImag(-(_pixel * _rad * Math.sin(_m * _theta)));
		return c;
	}

	/**
	 * 
	 * Normalization of all calculated zernike moments in complex representation
	 * 
	 * @param _complex
	 *            Complex representation of zernike moment
	 * @param _n
	 *            Order n
	 * @param _count
	 *            Number of pixel within unit circle
	 */
	private void normalize(BigComplex _complex, int _n, int _count) {
		_complex.setReal(_complex.getRealDouble() * (_n + 1) / _count);
		_complex.setImag(_complex.getImaginaryDouble() * (_n + 1) / _count);
	}

	/**
	 * 
	 * Initialize a zernike moment with a given order and repition
	 * 
	 * @param _order
	 *            Order n
	 * @param _repitition
	 *            Repitition m
	 * @param _d
	 *            Pascal matrix
	 * @return Empty Zernike moment of order n and repitition m
	 */
	private ZernikeMoment initZernikeMoment(final int _order, final int _repitition, final double[][] _d) {

		if (_order - Math.abs(_repitition) % 2 != 0) {
			// throw new IllegalArgumentException("This combination of order an
			// repitition is not valid!");
		}

		return createZernikeMoment(_d, _order, _repitition);
	}

	/**
	 * 
	 * Create one zernike moment of order n and repitition m with suitable
	 * radial polynom
	 * 
	 * @param _d
	 *            Pascal matrix
	 * @param _n
	 *            Order n
	 * @param _m
	 *            Repition m
	 * @return Empty Zernike moment of order n and repition m
	 */
	private ZernikeMoment createZernikeMoment(double[][] _d, int _n, int _m) {
		ZernikeMoment p = new ZernikeMoment();
		p.setM(_m);
		p.setN(_n);
		p.setP(createRadialPolynom(_n, _m, _d));
		BigComplex complexNumber = new BigComplex();
		p.setZm(complexNumber);
		return p;
	}

	/**
	 * Efficient calculation of pascal's triangle up to order max
	 * 
	 * @param _max
	 *            maximal order of pascal's triangle
	 * @return pascal's triangle
	 */
	private double[][] computePascalsTriangle(int _max) {
		double[][] d = new double[_max + 1][_max + 1];
		for (int n = 0; n <= _max; n++) {
			for (int k = 0; k <= n; k++) {
				if ((n == 0 && k == 0) || (n == k) || (k == 0)) {
					d[n][k] = 1.0;
					continue;
				}
				d[n][k] = (((double) n / (n - k))) * d[n - 1][k];
			}
		}
		return d;
	}

	/**
	 * 
	 * @param _n
	 *            Order n
	 * @param _m
	 *            Repitition m
	 * @param _k
	 *            Radius k
	 * @param _d
	 *            Pascal matrix
	 * @return computed term
	 */
	public static int computeBinomialFactorial(final int _n, final int _m, final int _k, double[][] _d) {
		int fac1 = (int) _d[_n - _k][_k];
		int fac2 = (int) _d[_n - 2 * _k][((_n - _m) / 2) - _k];
		int sign = (int) Math.pow(-1, _k);

		return sign * fac1 * fac2;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public void setRepetition(int repetition) {
		this.repetition = repetition;
	}

	public void setEnclosingCircles(Circle inner, Circle outer) {
		this.innerCircle = inner;
		this.outerCircle = outer;
		//this.a = 1;
		this.x1 = outer.getCenter().getDoublePosition(0)-inner.getCenter().getDoublePosition(0);
		this.y1 = outer.getCenter().getDoublePosition(1)-inner.getCenter().getDoublePosition(1);
		this.c = x1*x1 + y1*y1 - outer.getRadius()*outer.getRadius();
	}

	/**
	 * 
	 * Creates a radial polynom for zernike moment with order n and repitition m
	 * 
	 * @param _n
	 *            Order n
	 * @param _m
	 *            Repitition m
	 * @param _d
	 *            Pascal matrix
	 * @return Radial polnom for moment of order n and repition m
	 */
	public static Polynom createRadialPolynom(final int _n, final int _m, final double[][] _d) {
		final Polynom result = new Polynom(_n);
		for (int s = 0; s <= ((_n - Math.abs(_m)) / 2); ++s) {
			final int pos = _n - (2 * s);
			result.setCoefficient(pos, computeBinomialFactorial(_n, _m, s, _d));
		}
		return result;
	}

	public double[] getRadii(RealLocalizable p)
	{
		final double dx2 = p.getDoublePosition(0) - this.innerCircle.getCenter().getDoublePosition(0);
		final double dy2 = p.getDoublePosition(1) - this.innerCircle.getCenter().getDoublePosition(1);
		final double r = Math.sqrt(dx2*dx2+dy2*dy2);
		final double theta = Math.atan2(dy2, dx2);
		double R = 0;
		if(r > this.innerCircle.getRadius())
		{
			// Then calculate the distance to the outer circle, R, from the center of the inner circle as we'll need it later
			final double b = -2*Math.cos(theta)*this.x1 -2*Math.sin(theta)*this.y1;
			// Calculate quandratic formula
			if(b*b > 4*this.c)
			{
				R = (-1*b + Math.sqrt(b*b-4.0*this.c))/(2.0);
			}
			else
			{
				R = (-1*b - Math.sqrt(b*b-4.0*this.c))/(2.0);
			}
			
			// In case the inner circle extends past the outer circle in some spots
			// use the max of the two radii to define the outer radius of the outer circle.
			// Any points outside R then will be ignored, and in the case where the
			// inner extends past the outer, points outside the inner will be ignored
			// as well.
			R = Math.max(this.innerCircle.getRadius(), R);
		}
		// Otherwise, R is never needed subsequently so no need to calculate/change (see calculate)
		
		// Return results
		final double[] ret = new double[3];
		ret[0] = r;
		ret[1] = R;
		ret[2] = theta;
		return ret;

	}

}
