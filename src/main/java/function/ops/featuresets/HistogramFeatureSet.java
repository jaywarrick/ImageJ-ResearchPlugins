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

import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import function.ops.histogram.BoundedHistogramCreate;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * {@link FeatureSet} representing each bin of a histogram as a feature
 * 
 * @author Christian Dietz (University of Konstanz)
 * @param <T>
 */
@Plugin(type = FeatureSet.class, label = "Histogram Features", description = "Calculates the Histogram Features")
public class HistogramFeatureSet<I extends RealType<I>> extends AbstractIteratingFeatureSet<Iterable<I>, DoubleType>
		implements FeatureSet<Iterable<I>, DoubleType> {

	@Parameter(type = ItemIO.INPUT, label = "Number of Bins", description = "The number of bins of the histogram", min = "1", max = "2147483647", stepSize = "1")
	private int numBins = 256;

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<Iterable<I>, Histogram1d> histogramFunc;

	private Histogram1d<I> histogram;

	@Override
	public void initialize() {
		super.initialize();
		histogramFunc = Functions.unary(ops(), BoundedHistogramCreate.class, Histogram1d.class, in(), numBins, BoundedHistogramCreate.SIGMA, -2.0, 2.0, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void preCompute(final Iterable<I> input) {
		histogram = histogramFunc.calculate(input);
	}

	@Override
	protected String getNamePrefix() {
		return "Histogram Bin:";
	}

	@Override
	protected DoubleType getResultAtIndex(int i) {
		return new DoubleType(((double) histogram.frequency(i))/((double) histogram.totalCount()));
	}

	@Override
	protected int getNumEntries() {
		return numBins;
	}
}
