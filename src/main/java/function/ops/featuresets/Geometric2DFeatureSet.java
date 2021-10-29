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

import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.OpService;
import net.imagej.ops.featuresets.AbstractOpRefFeatureSet;
import net.imagej.ops.featuresets.DimensionBoundFeatureSet;
import net.imagej.ops.featuresets.FeatureSet;
import net.imglib2.type.numeric.RealType;

/**
 * {@link FeatureSet} to calculate Geometric2DFeatureSet
 * 
 * @author Christian Dietz, University of Konstanz
 * @author jaywarrick
 *
 * @param <I>
 * @param <O>
 */
@Plugin(type = FeatureSet.class, label = "Geometric Features 2D", description = "Calculates Geometric Features on 2D LabelRegions")
public class Geometric2DFeatureSet
<I, O extends RealType<O>> extends AbstractOpRefFeatureSet<I, O>
		implements DimensionBoundFeatureSet<I, O> {
	
	private static final String PKG = "net.imagej.ops.Ops$Geometric$";
	
	@Parameter
	private OpService ops;

	@Parameter(required = false, label = "Size", attrs = { @Attr(name = ATTR_FEATURE),
			@Attr(name = ATTR_TYPE, value = PKG + "Size") })
	private boolean isSizeActive = false; // Inactive as pixel size is more robust, giving a slight area to "line-like" features.

	@Parameter(required = false, label = "Circularity", attrs = { @Attr(name = ATTR_FEATURE),
			@Attr(name = ATTR_TYPE, value = PKG + "Circularity") })
	private boolean isCircularityActive = true;

	@Parameter(required = false, label = "Convexity", attrs = { @Attr(name = ATTR_FEATURE),
			@Attr(name = ATTR_TYPE, value = PKG + "Convexity") })
	private boolean isConvexityActive = true;

	@Parameter(required = false, label = "Eccentricity", attrs = { @Attr(name = ATTR_FEATURE),
			@Attr(name = ATTR_TYPE, value = PKG + "Eccentricity") })
	private boolean isEccentricityActive = true;

	@Parameter(required = false, label = "MainElongation", attrs = { @Attr(name = ATTR_FEATURE),
			@Attr(name = ATTR_TYPE, value = PKG + "MainElongation") })
	private boolean isMainElongationActive = true;

	@Parameter(required = false, label = "MaximumFeretsAngle", attrs = { @Attr(name = ATTR_FEATURE),
			@Attr(name = ATTR_TYPE, value = PKG + "MaximumFeretsAngle") })
	private boolean isMaximumFeretsAngleActive = false; // Typically we care about being rotationally invariant
	
	@Parameter(required = false, label = "MaximumFeretsDiameter", attrs = { @Attr(name = ATTR_FEATURE),
			@Attr(name = ATTR_TYPE, value = PKG + "MaximumFeretsDiameter") })
	private boolean isMaximumFeretsDiameterActive = true;
	
	@Parameter(required = false, label = "MinimumFeretsAngle", attrs = { @Attr(name = ATTR_FEATURE),
			@Attr(name = ATTR_TYPE, value = PKG + "MinimumFeretsAngle") })
	private boolean isMinimumFeretsAngleActive = false; // Typically we care about being rotationally invariant
	
	@Parameter(required = false, label = "MinimumFeretsDiameter", attrs = { @Attr(name = ATTR_FEATURE),
			@Attr(name = ATTR_TYPE, value = PKG + "MinimumFeretsDiameter") })
	private boolean isMinimumFeretsDiameterActive = true;

	@Parameter(required = false, label = "MajorAxis", attrs = { @Attr(name = ATTR_FEATURE),
			@Attr(name = ATTR_TYPE, value = PKG + "MajorAxis") })
	private boolean isMajorAxisActive = true;

	@Parameter(required = false, label = "MinorAxis", attrs = { @Attr(name = ATTR_FEATURE),
			@Attr(name = ATTR_TYPE, value = PKG + "MinorAxis") })
	private boolean isMinorAxisActive = true;

	@Parameter(required = false, label = "BoundarySize", attrs = { @Attr(name = ATTR_FEATURE),
			@Attr(name = ATTR_TYPE, value = PKG + "BoundarySize") })
	private boolean isBoundarySizeActive = true;

	@Parameter(required = false, label = "Boxivity", attrs = { @Attr(name = ATTR_FEATURE),
			@Attr(name = ATTR_TYPE, value = PKG + "Boxivity") })
	private boolean isBoxivityActive = true;

	@Parameter(required = false, label = "Roundness", attrs = { @Attr(name = ATTR_FEATURE),
			@Attr(name = ATTR_TYPE, value = PKG + "Roundness") })
	private boolean isRoundnessActive = true;

	@Parameter(required = false, label = "Solidity", attrs = { @Attr(name = ATTR_FEATURE),
			@Attr(name = ATTR_TYPE, value = PKG + "Solidity") })
	private boolean isSolidityActive = true;

	@Override
	public int getMinDimensions() {
		return 2;
	}

	@Override
	public int getMaxDimensions() {
		return 2;
	}

	@Override
	public boolean conforms() {
		return true;
	}

}