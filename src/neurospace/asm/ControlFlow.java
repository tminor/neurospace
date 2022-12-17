package neurospace.asm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Value;

/**
 * Utilities for control flow analysis.
 */
public class ControlFlow {
    private InsnList insns;
    private Frame[] frames;
    private HashMap<Integer, List<Integer>> insnToBlockMap;
    private List<List<List<Integer>>> edges;

    private Analyzer analyzer;

    public static BasicBlockGraph newBasicBlockGraph(String owner, MethodNode method) throws AnalyzerException {
        ControlFlow cf = new ControlFlow();

        cf.insns = method.instructions;
        cf.edges = new ArrayList<>();
        cf.insnToBlockMap = new HashMap<>();
        cf.insnToBlockMap.put(-1, new ArrayList<>());
        cf.insnToBlockMap.put(0, new ArrayList<>());

        cf.analyzer = new Analyzer(new BasicInterpreter()) {
                @Override
                protected void newControlFlowEdge(int from, int to) {
                    AbstractInsnNode fromInsn = cf.insns.get(from);
                    AbstractInsnNode toInsn = cf.insns.get(to);

                    // A JumpInsnNode represents the start of a new block
                    if (fromInsn instanceof JumpInsnNode) {
                        // Create a new block for this jump's target
                        // if the instructions aren't contiguous
                        if (Math.abs(from - to) > 1) {
                            if (cf.insnToBlockMap.get(to) == null) {
                                List<Integer> block = new ArrayList<>();
                                block.add(to);
                                cf.insnToBlockMap.put(to, block);
                            }
                        } else {
                            // Otherwise, create a new block with the
                            // jump instruction as the leader and the
                            // target as the first successor
                            List<Integer> block = new ArrayList<>();
                            block.add(from);
                            block.add(to);
                            cf.insnToBlockMap.put(from, block);
                            cf.insnToBlockMap.put(to, block);
                        }

                        // Instructions find their block by indexing a
                        // map using the previous instruction which
                        // ostensibly points to the correct block
                        List<Integer> fromBlock = cf.insnToBlockMap.get(from - 1);
                        List<Integer> toBlock = cf.insnToBlockMap.get(to);
                        List<List<Integer>> edge = new ArrayList<>();

                        edge.add(fromBlock);
                        edge.add(toBlock);
                        cf.edges.add(edge);
                    } else {
                        List<Integer> block = cf.insnToBlockMap.get(from - 1);

                        if (block == null) {
                            block = cf.insnToBlockMap.get(from);
                            if (block == null) {
                                block = new ArrayList<>();
                            }
                        }

                        block.add(from);

                        if (!(toInsn instanceof JumpInsnNode)) {
                            block.add(to);
                        }

                        cf.insnToBlockMap.put(from, block);
                    }
                }
            };

        cf.frames = cf.analyzer.analyze(owner, method);

        return cf.makeBasicBlockGraph();
    }

    private BasicBlockGraph makeBasicBlockGraph() {
        return new BasicBlockGraph(this.edges, this.insns, this.frames);
    }

    /**
     * Matches nodes in each set by minimum Euclidean distance in an
     * n-dimensional metric space.  The return value is a list of
     * results yielded by applying accumFn to each match.
     * <p>
     * @param  <T>     The return type of accumFn
     * @param  set1    The first set of nodes
     * @param  set2    The second set of nodes
     * @param  accumFn A function applied to each pair of matching nodes
     * @return         A list of values yielded by applying accumFn to
     *                 each pair of matching nodes
     */
    public static <T> List<T> hausdorffEditDistance(List<List<Double>> set1,
                                                    List<List<Double>> set2,
                                                    Function<List<List<Double>>, T> accumFn) {
        int set1Len = set1.size();
        int set2Len = set2.size();

        List<List<Double>> shorter = (set1Len > set2Len) ?
            new ArrayList<>(set2) : new ArrayList<>(set1);
        List<List<Double>> longer = (set1Len > set2Len) ?
            new ArrayList<>(set1) : new ArrayList<>(set2);

        int diff = Math.abs(set1Len - set2Len);

        List<Double> last = shorter.get(shorter.size() - 1);

        for (int i = 0; i <= diff; i++) {
            shorter.add(last);
        }

        List<T> accum = new ArrayList<>();

        for (List<Double> n1 : longer) {
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            List<Double> minNode = new ArrayList<>();
            List<Double> maxNode = new ArrayList<>();

            for (List<Double> n2 : shorter) {
                double dist = euclideanDistance(n1, n2);

                if (dist < min) {
                    min = dist;
                    minNode = new ArrayList<>(n2);
                }

                if (dist > max) {
                    max = dist;
                    maxNode = new ArrayList<>(n2);
                }
            }

            List<List<Double>> triple = new ArrayList<>();
            triple.add(n1);
            triple.add(minNode);
            triple.add(maxNode);
            accum.add(accumFn.apply(triple));
        }

        return accum;
    }

    public static double euclideanDistance(List<Double> point1,
                                           List<Double> point2) {
        double sum = 0;

        for (int i = 0; i < point1.size(); i++) {
            double diff = point1.get(i) - point2.get(i);
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    public static double distance(List<List<Double>> set1,
                                  List<List<Double>> set2) {
        Function<List<List<Double>>, Double> accumFn = (r) -> {
            double min = euclideanDistance(r.get(0),
                                           r.get(1));

            return min;
        };

        double ret = 0.0;

        List<Double> dists = hausdorffEditDistance(set1, set2, accumFn);

        for (int i = 0; i < dists.size(); i++) {
            double d = dists.get(i);
            ret += d;
        }

        return ret;
    }
}
