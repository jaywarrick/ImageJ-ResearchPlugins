package function.ops.stats;

import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import function.ops.JEXOps;
import net.imagej.ops.special.function.AbstractBinaryFunctionOp;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.TwinCursor;
import net.imglib2.algorithm.math.ImageStatistics;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Pair;
import utilities.FeatureUtils;

@Plugin(type = JEXOps.PearsonsCorrelationCoefficient.class, priority = Priority.NORMAL)
public class DefaultPearsonsCorrelationCoefficient<I1 extends RealType<I1>> extends AbstractBinaryFunctionOp<Pair<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I1>>, Cursor<Void>, DoubleType>
implements JEXOps.PearsonsCorrelationCoefficient {
	
	@Parameter(required = false)
	Double imMean1 = null;

	@Parameter(required = false)
	Double imMean2 = null;

	@Override
	public DoubleType calculate(Pair<RandomAccessibleInterval<I1>, RandomAccessibleInterval<I1>> input1,
			Cursor<Void> input2)
	{
		try
		{
			sc.fiji.coloc.algorithms.PearsonsCorrelation<I1> pc = new sc.fiji.coloc.algorithms.PearsonsCorrelation<>(sc.fiji.coloc.algorithms.PearsonsCorrelation.Implementation.Classic);
			if(imMean1 == null)
			{
				imMean1 = ImageStatistics.getImageMean(input1.getA());
			}
			if(imMean2 == null)
			{
				imMean2 = ImageStatistics.getImageMean(input1.getB());
			}
			FeatureUtils utils = new FeatureUtils();
			Cursor<BitType> c = utils.convertVoidTypeToBitTypeCursor(input2);
			TwinCursor<I1> cursor = new TwinCursor<>(
					input1.getA().randomAccess(),
					input1.getB().randomAccess(),
					c);
			double r = pc.calculatePearsons(cursor, imMean1, imMean2, null, null, sc.fiji.coloc.gadgets.ThresholdMode.None);
			return new DoubleType(r);
		} 
		catch (sc.fiji.coloc.algorithms.MissingPreconditionException e)
		{
			e.printStackTrace();
			return new DoubleType(Double.NaN);
		}
	}
	
	public void setMeans(Double mean1, Double mean2)
	{
		this.imMean1 = mean1;
		this.imMean2 = mean2;
	}
}