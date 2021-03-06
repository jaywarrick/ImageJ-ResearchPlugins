/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2015 Board of Regents of the University of
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

package function.ops.featuresets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import function.ops.geometry.Circle;
import function.ops.zernike.DoubleNormalizedZernikeComputer;
import net.imagej.ops.features.zernike.helper.ZernikeMoment;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * {@link FeatureSet} to calculate {@link StatOp}s.
 * 
 * @author Daniel Seebacher, University of Konstanz
 * @author Tim-Oliver Buchholz, University of Konstanz
 * @param <I>
 * @param <O>
 */
@Plugin(type = FeatureSet.class, label = "Double Normalized Zernike Features", description = "Calculates the Zernike Features using nested circles for normalizing radius to the perimeter and an intermediate interior cricle that doesn't have to be concentric")
public class DoubleNormalizedZernikeFeatureSet<I extends RealType<I>>
		extends AbstractFeatureSet<IterableInterval<I>, DoubleType> {

	@Parameter(type = ItemIO.INPUT, label = "Minimum Order of Zernike Moment", description = "The minimum order of the zernike moment to be calculated.", min = "1", max = "2147483647", stepSize = "1")
	private int orderMin = 2;

	@Parameter(type = ItemIO.INPUT, label = "Maximum Order of Zernike Moment", description = "The maximum order of the zernike moment to be calculated.", min = "1", max = "2147483647", stepSize = "1")
	private int orderMax = 4;
	
	@Parameter(label = "The inner circle inside which radius will be normalized between 0 and the user-defined perimeter value (< 1)")
	private Circle innerCircle;
	
	@Parameter(label = "The outer circle. Between the inner and outer circle radius will be normalized between the inner circle perimeter value and 1")
	private Circle outerCircle;
	
	@Parameter(required = false, label = "The value of a normalized radius at the perimeter of the inner circle.")
	private double intermediateNormalizedValue = 0.5;

	private DoubleNormalizedZernikeComputer<I> zernikeComputer;

	@Override
	public void initialize() {
		super.initialize();

		zernikeComputer = new DoubleNormalizedZernikeComputer<I>();
	}

	@Override
	public List<NamedFeature> getFeatures() {
		final List<NamedFeature> features = new ArrayList<NamedFeature>();

		for (int order = orderMin; order <= orderMax; order++) {
			for (int repetition = 0; repetition <= order; repetition++) {
				if (Math.abs(order - repetition) % 2 == 0) {
					features.add(new NamedFeature("DNZernikeMag_Order_" + order + "_Rep_" + repetition));
					features.add(new NamedFeature("DNZernikePhase_Order_" + order + "_Rep_" + repetition));
				}
			}
		}

		return features;
	}

	@Override
	public Map<NamedFeature, DoubleType> calculate(IterableInterval<I> input) {
		HashMap<NamedFeature, DoubleType> map = new HashMap<NamedFeature, DoubleType>();

		for (int order = orderMin; order <= orderMax; order++) {
			for (int repetition = 0; repetition <= order; repetition++) {
				if (Math.abs(order - repetition) % 2 == 0) {
					zernikeComputer.setOrder(order);
					zernikeComputer.setRepetition(repetition);
					zernikeComputer.setEnclosingCircles(this.innerCircle, this.outerCircle);

					ZernikeMoment results = zernikeComputer.calculate(input);

					map.put(new NamedFeature("DNZernikeMag_Order_" + order + "_Rep_" + repetition),
							new DoubleType(results.getMagnitude()));
					map.put(new NamedFeature("DNZernikePhase_Order_" + order + "_Rep_" + repetition),
							new DoubleType(results.getPhase()));
				}
			}
		}

		return map;
	}
	
	public void setEnclosingCircles(Circle inner, Circle outer)
	{
		this.innerCircle = inner;
		this.outerCircle = outer;
	}
}
