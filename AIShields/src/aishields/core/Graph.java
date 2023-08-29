package aishields.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import aishields.utils.Utils;

/**
 * Representation of a graph.
 * 
 * @author Marcin Waniek
 */
public class Graph {
	
	private String name;
	private int n;
	private int m;
	private boolean directed;
	
	private ArrayList<Coalition> succs;
	private ArrayList<Coalition> preds;
	
	private Stack<Change> history;
	private Collection<GraphChangeListener> listeners;
	
	protected Graph(String name, int n, boolean directed, boolean genSuccs, boolean genPreds){
		this.name = name;
		this.n = n;
		this.m = 0;
		this.directed = directed;
		
		this.succs = null;
		if (genSuccs) {
			this.succs = new ArrayList<>();
			for(int i = 0; i < n; ++i)
				this.succs.add(new Coalition());
		}
		this.preds = null;
		if (genPreds){
			this.preds = new ArrayList<>();
			for(int i = 0; i < n; ++i)
				this.preds.add(new Coalition());
		}

		this.history = null;
		this.listeners = new ArrayList<>();
	}
	
	public Graph(String name, int n, boolean directed){
		this(name, n, directed, true, directed);
	}
	
	public Graph(String name, int n){
		this(name, n, false);
	}
	
	public Graph(Graph g){
		this(g.getName(), g.size(), g.isDirected());
		for (int i : g.nodes())
			for (Integer j : g.getSuccs(i))
				addEdge(i, j);		
	}

	public void subscribe(GraphChangeListener listener){
		unsubscribe(listener);
		listeners.add(listener);
	}
	
	public void unsubscribe(GraphChangeListener listener){
		listeners.remove(listener);
	}
	
	protected void notifyListenersAdd(Edge e){
		for (GraphChangeListener listener : listeners)
			listener.notifyAdd(this, e);
	}
	
	protected void notifyListenersRemove(Edge e){
		for (GraphChangeListener listener : listeners)
			listener.notifyRemove(this, e);
	}
	
	protected void notifyListenersOther(Edge e){
		for (GraphChangeListener listener : listeners)
			listener.notifyOther(this, e);
	}
	
	protected void notifyListenersReset(){
		for (GraphChangeListener listener : listeners)
			listener.notifyReset(this);
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int size(){
		return n;
	}
	
	public Integer edgesCount() {
		return m;
	}
	
	public Long nonEdgesCount() {
		if (isDirected())
			return (long)size() * (size()-1) - edgesCount();
		else
			return (long)size() * (size()-1) / 2 - edgesCount();
	}
	
	public Boolean isDirected(){
		return directed;
	}
	
	public Edge e(int i, int j) {
		return new Edge(i, j, isDirected());
	}
	
	public boolean addEdge(int i, int j){
		if (i != j && !containsEdge(i, j)){
			performAddEdge(i, j);
			if (history != null)
				history.push(new Addition(i, j));
			notifyListenersAdd(new Edge(i, j, isDirected()));
			return true;
		} else
			return false;
	}
	
	public boolean addEdge(Edge e){
		return addEdge(e.i(), e.j());
	}
	
	public boolean removeEdge(int i, int j){
		if (i != j && containsEdge(i, j)){
			performRemoveEdge(i, j);
			if (history != null)
				history.push(new Removal(i, j));
			notifyListenersRemove(new Edge(i, j, isDirected()));
			return true;
		} else
			return false;
	}
	
	public boolean removeEdge(Edge e){
		return removeEdge(e.i(), e.j());
	}
	
	public boolean swapEdge(int i, int j){
		if (containsEdge(i, j))
			return removeEdge(i, j);
		else
			return addEdge(i, j);
	}
	
	public boolean swapEdge(Edge e){
		return swapEdge(e.i(), e.j());
	}
	
	protected void performAddEdge(int i, int j){
		succs.get(i).add(j);
		if (isDirected())
			preds.get(j).add(i);
		else
			succs.get(j).add(i);
		++m;
	}
	
	protected void performRemoveEdge(int i, int j){
		succs.get(i).remove(j);
		if (isDirected())
			preds.get(j).remove(i);
		else
			succs.get(j).remove(i);
		--m;
	}
	
	public void disconnectNode(int i) {
		for (int j : new Coalition(getSuccs(i)))
			removeEdge(i, j);
		if (isDirected())
			for (int j : new Coalition(getPreds(i)))
				removeEdge(j, i);
	}
	
	public boolean containsEdge(int i, int j){
		return i < size() && j < size() && getSuccs(i).contains(j);
	}
	
	public boolean containsAnyDirectionEdge(int i, int j){
		return containsEdge(i, j) || containsEdge(j, i);
	}
	
	public boolean containsEdge(Edge e){
		return containsEdge(e.i(), e.j());
	}
	
	public Graph addNodes(int k) {
		Graph res = new Graph(getName(), size() + k, isDirected());
		edgesStream().forEach(e -> res.addEdge(e));
		return res;
	}
	
	public Coalition getSuccs(int i){
		return succs.get(i);
	}
	
	public IntStream getSuccsStream(int i){
		return getSuccs(i).stream();
	}
	
	public Coalition getPreds(int i){
		if (isDirected())
			return preds.get(i);
		else
			return getSuccs(i);
	}
	
	public IntStream getPredsStream(int i){
		return getPreds(i).stream();
	}
	
	public Coalition getNeighs(int i){
		if (isDirected())
			return Coalition.add(getSuccs(i), getPreds(i));
		else
			return getSuccs(i);
	}
	
	public IntStream getNeighsStream(int i){
		return getNeighs(i).stream();
	}
	
	public IntStream getSuccsStream(Coalition c){
		return c.stream().flatMap(i -> getSuccsStream(i)).distinct().filter(i -> !c.contains(i));
	}
	
	public IntStream getPredsStream(Coalition c){
		return c.stream().flatMap(i -> getPredsStream(i)).distinct().filter(i -> !c.contains(i));
	}
	
	public IntStream getNeighsStream(Coalition c){
		return c.stream().flatMap(i -> getNeighsStream(i)).distinct().filter(i -> !c.contains(i));
	}
	
	public Coalition getCommonSuccs(int i, int j){
		return Coalition.intersect(getSuccs(i), getSuccs(j));
	}
	
	public Coalition getCommonPreds(int i, int j){
		return Coalition.intersect(getPreds(i), getPreds(j));
	}
	
	public Coalition getCommonNeighs(int i, int j){
		if (isDirected())
			return Coalition.add(getCommonSuccs(i, j), getCommonPreds(i, j));
		else
			return getCommonSuccs(i, j);
	}
	
	public long getNumberOfCommonNeighs(int i, int j){
		return getNeighs(i).inplaceIntersect(getNeighs(j)).count();
	}
	
	public int getOutDegree(int i){
		return getSuccs(i).size();
	}
	
	public int getInDegree(int i){
		return getPreds(i).size();
	}

	public int getDegree(int i){
		return getOutDegree(i) + (isDirected() ? getInDegree(i) : 0);
	}
	
	public Iterable<Integer> nodes(){
		return new Iterable<Integer>() {
			@Override
			public Iterator<Integer> iterator() {
				return nodesStream().iterator();
			}
		};
	}
	
	public IntStream nodesStream(){
		return IntStream.range(0, size());
	}
	
	public Coalition nodesCoalition(){
		return Coalition.getFull(size());
	}
	
	public Iterable<Edge> possibleEdges(){
		return new Iterable<Edge>() {
			@Override
			public Iterator<Edge> iterator() {
				return new PossibleEdgesIterator(null);
			}
		};
	}
	
	public Iterable<Edge> edges(){
		return new Iterable<Edge>() {
			@Override
			public Iterator<Edge> iterator() {
				return new EdgesIterator();
			}
		};
	}
	
	public Stream<Edge> edgesStream(){
		return StreamSupport.stream(edges().spliterator(), false);
	}
	
	public Edge getRandomEdge(){
		int r;
		if (isDirected())
			r = Utils.RAND.nextInt(edgesCount());
		else
			r = Utils.RAND.nextInt(2 * edgesCount());
		for (int i : nodes())
			if (r < getOutDegree(i))
				return new Edge(i, getSuccs(i).getRandom().intValue(), isDirected());
			else
				r -= getOutDegree(i);
		return null;
	}

	public Iterable<Edge> nonEdges(){
		return new Iterable<Edge>() {
			@Override
			public Iterator<Edge> iterator() {
				return new PossibleEdgesIterator(false);
			}
		};
	}

	public Stream<Edge> nonEdgesStream(){
		return StreamSupport.stream(nonEdges().spliterator(), false);
	}
	
	public Edge getRandomNonEdge(){
		long r;
		if (isDirected())
			r = Math.abs(Utils.RAND.nextLong()) % nonEdgesCount();
		else
			r = Math.abs(Utils.RAND.nextLong()) % (2 * nonEdgesCount());
		for (int i : nodes())
			if (r < size() - 1 - getOutDegree(i)) {
				for (int j : nodes())
					if (i != j && !getSuccs(i).contains(j)) {
						if (r == 0)
							return new Edge(i, j, isDirected());
						else
							--r;
					}
			} else
				r -= size() - 1 - getOutDegree(i);
		return null;
	}
	
	public Edge getRandomNonEdgeSparse(){
		Edge e = null;
		do {
			e = e(Utils.RAND.nextInt(size()), Utils.RAND.nextInt(size()));
		} while (e.i() == e.j() || containsEdge(e));
		return e;
	}
	
	public void startRecordingHistory(){
		history = new Stack<>();
	}
	
	public void stopRecordingHistory(){
		history = null;
	}
	
	public boolean isRecordingHistory(){
		return history != null;
	}
	
	public Edge getLastChange() {
		return history.peek().getEdge();
	}
	
	public Stream<Edge> getChanges() {
		return history.stream().map(c -> c.getEdge());
	}
	
	public void resetGraph(){
		if (history != null){
			while (!history.empty())
				history.pop().revert();
			notifyListenersReset();
		}
	}
	
	public void revertChanges(int k){
		if (history != null){
			for (int i = 0; i < k; ++i)
				if (!history.empty())
					history.pop().revert();
		}
	}
	
	public int historySize(){
		return history == null ? 0 : history.size();
	}
	
	public Graph getUndirected() {
		if (isDirected()) {
			Graph res = new Graph(getName(), size(), false);
			nodesStream().forEach(i -> getSuccs(i).forEach(j -> res.addEdge(i, j)));
			return res;
		} else
			return this;
	}
	
	public boolean isConnected(){
		return size() == 0 || getConnectedComponent(0).size() == size();
	}
	
	public Integer getNumberOfConnectedComponents(){
		return getConnectedComponents().size();
	}
	
	public List<Coalition> getConnectedComponents(){
		return getConnectedComponents(null);
	}
	
	public List<Coalition> getConnectedComponents(Coalition allowed){
		List<Coalition> res = new ArrayList<>();
		Coalition rest = allowed == null ? Coalition.getFull(size()) : new Coalition(allowed);
		while (!rest.isEmpty()){
			Coalition c = getConnectedComponent(rest.getAny(), allowed);
			rest.remove(c);
			res.add(c);
		}
		return res;
	}
	
	public Coalition getGiantComponent(){
		return Utils.argmax(getConnectedComponents(), c -> c.size());
	}
		
	public Coalition getConnectedComponent(int v){
		return getConnectedComponent(v, null);
	}
	
	public Coalition getConnectedComponent(int v, Coalition allowed){
		Coalition res = new Coalition();
		Coalition q = new Coalition(v);
		while (!q.isEmpty()){
			int i = q.removeAny();
			res.add(i);
			for (int j : getSuccs(i))
				if (!res.contains(j) && (allowed == null || allowed.contains(j)))
					q.add(j);
			if (isDirected())
				for (int j : getPreds(i))
					if (!res.contains(j) && (allowed == null || allowed.contains(j)))
						q.add(j);
		}
		return res;
	}

	public List<Edge> getIncindentEdges(int v){
		List<Edge> res = new ArrayList<>();
		for (int w : getNeighs(v)) {
			if (containsEdge(v, w))
				res.add(new Edge(v, w, isDirected()));
			if (isDirected() && containsEdge(w, v))
				res.add(new Edge(w, v, isDirected()));
		}
		return res;
	}

	public List<Edge> getInducedEdges(Coalition c){
		List<Edge> res = new ArrayList<>();
		Integer[] nodes = c.asArray(); 
		for (int i = 0; i < nodes.length; ++i)
			for (int j = i + 1; j < nodes.length; ++j){
				if (containsEdge(nodes[i], nodes[j]))
					res.add(new Edge(nodes[i], nodes[j], isDirected()));
				if (isDirected() && containsEdge(nodes[j], nodes[i]))
					res.add(new Edge(nodes[j], nodes[i], isDirected()));
			}
		return res;
	}
	
	public List<Edge> getInducedEdges(IntPredicate p){
		Coalition c = new Coalition();
		this.nodesStream().filter(p).forEach(v -> c.add(v));
		return getInducedEdges(c);
	}
	
	public void forceConnectivity(BiFunction<Coalition, IntStream, Edge> choose, Consumer<Edge> handler){
		Coalition last = null;
		for (Coalition c : getConnectedComponents()){
			if (last != null) {
				Edge e = choose.apply(c, last.stream());
				addEdge(e);
				handler.accept(e);
			}
			last = c;
		}
	}
	
	public void forceConnectivity() {
		forceConnectivity((c, rest) -> e(c.getRandom().intValue(), rest.findAny().getAsInt()), e -> {});
	}
	
	public double getAverageDegree() {
		return nodesStream().mapToDouble(i -> getDegree(i)).sum() / size();
	}
	
	public void printDescriptionLn() {
		printDescriptionLn(false);
	}
	
	public void printDescriptionLn(boolean ifConnected) {
		printDescription(ifConnected);
		System.out.println();
	}
	
	public void printDescription() {
		printDescription(false);
	}
	
	public void printDescription(boolean ifConnected) {
		System.out.println("Name:\t\t" + getName());
		System.out.println("Nodes:\t\t" + size());
		System.out.println("Edges:\t\t" + edgesCount());
		System.out.println("Directed:\t" + isDirected());
		if (ifConnected)
			System.out.println("Connected:\t" + isConnected());
	}
	
	@Override
	public String toString() {
		return "(" + getName() + ",n=" + size() + ",m=" + edgesCount() + (isDirected() ? ",directed" : "") + ")";
	}
	
	private abstract class Change {
		
		protected int i;
		protected int j;
		
		public Change(int i, int j) {
			this.i = i;
			this.j = j;
		}

		public Edge getEdge() {
			return Graph.this.e(i, j);
		}
		
		public abstract void revert();
	}
	
	private class Addition extends Change {
		
		public Addition(int i, int j) {
			super(i, j);
		}
		
		@Override
		public void revert() {
			performRemoveEdge(i, j);
			notifyListenersRemove(new Edge(i, j, isDirected()));
		}
	}
	
	private class Removal extends Change {
		
		public Removal(int i, int j) {
			super(i, j);
		}
		
		@Override
		public void revert() {
			performAddEdge(i, j);
			notifyListenersAdd(new Edge(i, j, isDirected()));
		}
	}
	
	private class PossibleEdgesIterator implements Iterator<Edge>{
		
		private int i;
		private int j;
		private Boolean existing;
		
		public PossibleEdgesIterator(Boolean existing){
			this.i = 0;
			this.j = 0;
			this.existing = existing;
			findNext();
		}

		@Override
		public boolean hasNext() {
			return i < size();
		}

		@Override
		public Edge next() {
			Edge res = e(i, j);
			findNext();
			return res;
		}
		
		private void findNext(){
			do {
				j = (j + 1) % size();
				if (j == 0){
					++i;
					if (!isDirected())
						if (i < size() - 1)
							j = i + 1;
						else
							i = size();
				}
				if (i != j && i < size() && j < size()
						&& ((existing == null) || (existing && containsEdge(i, j)) || (!existing && !containsEdge(i, j))))
					return;
			} while (i < size());
		}
	}
	
	private class EdgesIterator implements Iterator<Edge>{
		
		private int i;
		private int next;
		private Iterator<Integer> iter;
		
		public EdgesIterator() {
			this.i = 0;
			this.next = 0;
			this.iter = getSuccs(i).iterator();
			findNext();
		}

		@Override
		public boolean hasNext() {
			return i < size();
		}

		@Override
		public Edge next() {
			Edge res = e(i, next);
			findNext();
			return res;
		}
		
		private void findNext(){
			do {
				while (iter.hasNext()){
					next = iter.next();
					if (i < next || isDirected())
						return;
				}
				++i;
				if (i < size())
					iter = getSuccs(i).iterator();
			} while (i < size());
		}
	}
}
