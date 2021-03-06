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
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
	/ notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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

import org.scijava.plugin.Plugin;

import function.ops.lbp.Default8BitUniformLBP2D;
import net.imagej.ops.image.histogram.HistogramCreate;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.LongType;

/**
 * {@link FeatureSet} representing each bin of an LBP histogram as a feature
 * 
 * @author Christian Dietz (University of Konstanz)
 * @param <T>
 */
@Plugin(type = FeatureSet.class, label = "8bit Uniform 8 neighbor LBP Histogram Features", description = "Calculates the Uniform (8,1) LBP2D Histogram of 59 Features")
public class LBPHistogramFeatureSet<I extends RealType<I>> extends AbstractIteratingFeatureSet<RandomAccessibleInterval<I>, LongType>
		implements FeatureSet<RandomAccessibleInterval<I>, LongType> {

	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<RandomAccessibleInterval<I>, ArrayList> lbpFunc;
	
	@SuppressWarnings("rawtypes")
	private UnaryFunctionOp<ArrayList, Histogram1d> histFunc;

	private Histogram1d<I> histogram;

	@Override
	public void initialize() {
		super.initialize();
		histFunc = Functions.unary(ops(), HistogramCreate.class, Histogram1d.class, ArrayList.class, 59);
		lbpFunc = Functions.unary(ops(), Default8BitUniformLBP2D.class, ArrayList.class, in());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void preCompute(final RandomAccessibleInterval<I> input) {
		ArrayList<LongType> codes = lbpFunc.calculate(input);
		histogram = histFunc.calculate(codes);
	}

	@Override
	protected String getNamePrefix() {
		return "LBPCode:";
	}

	@Override
	protected LongType getResultAtIndex(int i) {
		return new LongType(histogram.frequency(i));
	}

	@Override
	protected int getNumEntries() {
		return 59;
	}
}
