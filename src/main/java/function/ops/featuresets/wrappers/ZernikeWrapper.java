package function.ops.featuresets.wrappers;

import java.util.Map;
import java.util.Map.Entry;

import IJ2.IJ2PluginUtility;
import function.ops.featuresets.NamedFeature;
import function.ops.featuresets.ZernikeFeatureSet;
import function.ops.geometry.Circle;
import helper.objects.Canceler;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import tables.DimensionMap;

public class ZernikeWrapper<T extends RealType<T>> {
	


	public ZernikeFeatureSet<T> opZernike = null;
	private int zernikeMomentMin, zernikeMomentMax;
	
	public void setMinMoment(int min)
	{
		this.zernikeMomentMin = min;
	}
	
	public void setMaxMoment(int max)
	{
		this.zernikeMomentMax = max;
	}

	@SuppressWarnings("unchecked")
	public boolean putZernike(WriterWrapper writer, DimensionMap mapM, int id, int label, IterableInterval<T> vals, Circle circle, Canceler canceler) {
		if (canceler.isCanceled()) {
			return false;
		}
		if (this.opZernike == null) {
			opZernike = IJ2PluginUtility.ij().op().op(ZernikeFeatureSet.class, vals, zernikeMomentMin, zernikeMomentMax);
		}

		// Set the enclosing circle for this cell
		opZernike.setEnclosingCircle(circle);

		Map<NamedFeature, DoubleType> results = opZernike.calculate(vals);
		for (Entry<NamedFeature, DoubleType> result : results.entrySet()) {
			DimensionMap newMap = mapM.copyAndSet("Measurement=" + result.getKey().getName());
			newMap.put("Id", "" + id);
			newMap.put("Label", "" + label);
			WriterWrapper.write(writer, newMap, result.getValue().get());
		}

		// Set the enclosing circle for this cell
		opZernike.setEnclosingCircle(circle);

		results = opZernike.calculate(vals);
		for (Entry<NamedFeature, DoubleType> result : results.entrySet()) {
			DimensionMap newMap = mapM.copyAndSet("Measurement=" + result.getKey().getName());
			newMap.put("Id", "" + id);
			newMap.put("Label", "" + label);
			WriterWrapper.write(writer, newMap, result.getValue().get());
		}

		// Set the enclosing circle for this cell
		opZernike.setEnclosingCircle(circle);

		results = opZernike.calculate(vals);
		for (Entry<NamedFeature, DoubleType> result : results.entrySet()) {
			DimensionMap newMap = mapM.copyAndSet("Measurement=" + result.getKey().getName());
			newMap.put("Id", "" + id);
			newMap.put("Label", "" + label);
			WriterWrapper.write(writer, newMap, result.getValue().get());
		}
		return true;
	}
}
