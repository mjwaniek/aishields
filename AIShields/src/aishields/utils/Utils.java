package aishields.utils;

import java.io.File;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A set of useful methods
 * 
 * @author Marcin Waniek
 */
public class Utils {

	public static final Random RAND = new Random();
	public static final String WHITESPACE = "\\s+";
	public static final String LETTERS_ONLY = "[^A-Za-z]+";
	
	/**
	 * Current time in the yyMMdd-HHmmss format.
	 */
	public static String timestamp(){
		return new SimpleDateFormat("yyMMdd-HHmmss-SSS").format(new Date());
	}
	
	public static String capitalize(String s) {
		return s.substring(0,1).toUpperCase() + s.substring(1);
	}
	
	public static <T> T getRandom(List<T> l) {
		return l.size() > 0 ? l.get(RAND.nextInt(l.size())) : null;
	}
	
	public static <T> T getRandom(Stream<T> s) {
		return getRandom(s.collect(Collectors.toList()));
	}
	
	public static <T> T getRandom(Stream<T> s, int size) {
		return s.skip(RAND.nextInt(size)).findFirst().orElse(null);
	}
	
	public static <T> Map<T, Integer> count(Stream<T> s) {
		Map<T, Integer> res = new HashMap<>();
		s.forEach(t -> res.compute(t, (__, x) -> x == null ? 1 : x + 1));
		return res;
	}
	
	/**
	 * Finds the element of collection that maximizes given function.
	 */
	public static <T> T argmax(Iterable<T> it, Function<T,Number> f){
		ArgMaxCounter<T> counter = new ArgMaxCounter<>();
		it.forEach(t -> counter.update(t, f.apply(t).doubleValue()));
		return counter.res;
	}
	
	/**
	 * Finds the element of stream that maximizes given function.
	 */
	public static <T> T argmax(Stream<T> s, Function<T,Number> f){
		ArgMaxCounter<T> counter = new ArgMaxCounter<>();
		s.forEach(t -> counter.update(t, f.apply(t).doubleValue()));
		return counter.res;
	}
	
	private static class ArgMaxCounter<T> {
		private T res;
		private double resVal;
		private int equalCount;
		
		public ArgMaxCounter() {
			this.res = null;
			this.resVal = 0;
			this.equalCount = 0;
		}
		
		public void update(T elem, double val) {
			if (res == null || val > resVal){
				equalCount = 0;
				res = elem;
				resVal = val;
			} else if (val == resVal){
				++equalCount;
				if (RAND.nextDouble() >= (double)equalCount/(equalCount + 1)){
					res = elem;
					resVal = val;
				}
			}
		}
	}
	
	/**
	 * Finds the element of collection that maximizes given function.
	 */
	public static <T> T argmin(Iterable<T> it, final Function<T,Number> f){
		return argmax(it, t -> -f.apply(t).doubleValue());
	}
	
	/**
	 * Finds the element of stream that maximizes given function.
	 */
	public static <T> T argmin(Stream<T> s, final Function<T,Number> f){
		return argmax(s, t -> -f.apply(t).doubleValue());
	}
	
	public static <T> T last(T[] a) {
		return a[a.length - 1];
	}
	
	public static <T> T last(List<T> l) {
		return l.get(l.size() - 1);
	}

	/**
	 * Concatenate two arrays.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] concat(T[] a, T[] b){
		T[] res = (T[]) Array.newInstance(a.getClass().getComponentType(), a.length + b.length);
		int i = 0;
		for (T t : a)
			res[i++] = t;
		for (T t : b)
			res[i++] = t;
		return res;
	}

	@SafeVarargs
	public static <T> List<T> concat(List<T>... lists){
		List<T> res = new ArrayList<>();
		for (List<T> l : lists)
			res.addAll(l);
		return res;
	}
	
	@SafeVarargs
	public static <T> List<T> append(List<T> l, T... elems){
		List<T> res = new ArrayList<>();
		res.addAll(l);
		for (T t : elems)
			res.add(t);
		return res;
	}
	
	@SafeVarargs
	public static <T> List<T> remove(List<T> l, T... elems){
		List<T> res = new ArrayList<>();
		res.addAll(l);
		for (T t : elems)
			res.remove(t);
		return res;
	}
	
	/**
	 * Filter the map based on keys.
	 */
	public static <K,V> void filter(Map<K,V> m, Predicate<K> f) {
		m.keySet().stream().filter(f.negate()).collect(Collectors.toList()).forEach(k -> m.remove(k));
	}
	
	/**
	 * String representation of an array.
	 */
	public static <T> String toString(T[] a){
		String res = "[ ";
		for (T t : a)
			res += t + " ";
		res += "]";
		return res;
	}

	/**
	 * String representation of a int array.
	 */
	public static String toString(int[] a){
		String res = "[ ";
		for (int t : a)
			res += t + " ";
		res += "]";
		return res;
	}

	/**
	 * String representation of a double array.
	 */
	public static String toString(double[] a){
		String res = "[ ";
		for (double t : a)
			res += t + " ";
		res += "]";
		return res;
	}
	
	/**
	 * Creates list in place.
	 */
	@SafeVarargs
	public static <T> List<T> aList(T... elems){
		List<T> res = new ArrayList<>();
		for (T elem : elems)
			res.add(elem);
		return res;
	}
	
	public static <T> List<T> asList(Iterable<T> it){
		List<T> res = new ArrayList<>();
		for (T t : it)
			res.add(t);
		return res;
	}
	
	public static <T> List<T> asList(Stream<T> elems){
		List<T> res = new ArrayList<>();
		elems.forEach(elem -> res.add(elem));
		return res;
	}
	
	public static <T> List<T> asList(T[] elems){
		List<T> res = new ArrayList<>();
		for (T elem : elems)
			res.add(elem);
		return res;
	}
	
	public static List<Integer> asList(int[] elems){
		List<Integer> res = new ArrayList<>();
		for (int elem : elems)
			res.add(elem);
		return res;
	}
	
	public static List<Double> asList(double[] elems){
		List<Double> res = new ArrayList<>();
		for (double elem : elems)
			res.add(elem);
		return res;
	}
	
	public static List<Boolean> asList(boolean[] elems){
		List<Boolean> res = new ArrayList<>();
		for (boolean elem : elems)
			res.add(elem);
		return res;
	}
	
	/**
	 * Returns file extension.
	 */
	public static String getFileExtension(File f){
		return f.getName().substring(f.getName().lastIndexOf(".") + 1);
	}
		
	/**
	 * Generates all sublist of given size.
	 */
	public static <T> Iterable<List<T>> sublistsOfSize(List<T> l, int size){
		assert(size >= 0 && size <= l.size());
		return new Iterable<List<T>>() {
			@Override
			public Iterator<List<T>> iterator() {
				return new Iterator<List<T>>() {
					
					private int[] counter = new int[size];
					
					{
						for (int i = 0; i < size; ++i)
							counter[i] = i;
					}

					@Override
					public boolean hasNext() {
						return counter != null;
					}

					@Override
					public List<T> next() {
						List<T> res = new ArrayList<>();
						for (int i : counter)
							res.add(l.get(i));
						int i = size - 1;
						while (i >= 0 && counter[i] == l.size() - size + i)
							--i;
						if (i < 0)
							counter = null;
						else {
							++counter[i];
							for (int j = i + 1; j < size; ++j)
								counter[j] = counter[j-1] + 1;
						}
						return res;
					}
				};
			}
		};
	}
		
	/**
	 * Computing mean value.
	 */
	public static Double mean(Collection<Double> data){
		Double res = data.stream().reduce(0., Double::sum); 
		if (data.size() > 0)
			res /= data.size();
		return res;
	}
	
	/**
	 * Computing variance.
	 */
	public static Double variance(Collection<Double> data){
		if (data.size() > 0){
			double mean = Utils.mean(data);
			return data.stream().mapToDouble(x -> (x - mean)*(x - mean)).average().getAsDouble();
		} else
			return 0.;
	}
	
	/**
	 * Computing covariance.
	 */
	public static Double covariance(List<Double> data1, List<Double> data2){
		if (data1.size() > 0 && data1.size() == data2.size()){
			double mean1 = Utils.mean(data1);
			double mean2 = Utils.mean(data2);
			return IntStream.range(0, data1.size()).mapToDouble(i -> (data1.get(i) - mean1) * (data2.get(i) - mean2))
					.average().getAsDouble();
		} else
			return 0.;
	}
	
	/**
	 * Computing standard deviation.
	 */
	public static Double sd(Collection<Double> data){
		Double res = 0.;
		if (data.size() > 0){
			Double mean = mean(data);
			for (Double x : data)
				res += (x - mean)*(x - mean);
			res /= data.size();
			res = Math.sqrt(res);
		}
		return res;
	}
	
	/**
	 * Computing 95% confidence interval.
	 */
	public static Double conf95(Collection<Double> data){
		Double res = 0.;
		if (data.size() > 0){
			Double sd = sd(data);
			res = 1.96 * sd / Math.sqrt(data.size());
		}
		return res;
	}
}
