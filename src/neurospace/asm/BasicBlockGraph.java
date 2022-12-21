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
import org.objectweb.asm.tree.analysis.Frame;

public class BasicBlockGraph {
    private HashMap<Integer, List<Integer>> successorsMap;
    private MutableGraph<Integer> graph;
    private InsnList insns;
    private Frame[] frames;

    public Frame[] getFrames() {
        return frames;
    }

    public InsnList getInsns() {
        return insns;
    }

    public MutableGraph<Integer> getGraph() {
        return graph;
    }

    public List<Integer> getBlock(int idx) {
        List<Integer> block = new ArrayList<>();
        block.add(idx);
        if (successorsMap.get(idx) != null) {
            block.addAll(successorsMap.get(idx));
        }
        return block;
    }

    public List<AbstractInsnNode> getBlockInsns(int idx) {
        List<Integer> idxs = this.getBlock(idx);
        List<AbstractInsnNode> insns = new ArrayList<>();

        for (int i = 0; i < idxs.size(); i++) {
            if (i < this.insns.size()) {
                insns.add(this.insns.get(i));
            }
        }

        return insns;
    }

    public BasicBlockGraph(List<List<List<Integer>>> edges,
                           InsnList insns,
                           Frame[] frames) {
        this.insns = insns;
        this.frames = frames;
        this.successorsMap = new HashMap<>();

        MutableGraph<Integer> graph = GraphBuilder.directed()
            .allowsSelfLoops(true)
            .build();

        for (int i = 0; i < edges.size(); i++) {
            List<List<Integer>> e = edges.get(i);

            List<Integer> fromBlock = new ArrayList<>(e.get(0));
            List<Integer> toBlock = new ArrayList<>(e.get(1));

            int from = fromBlock.get(0);
            int to = toBlock.get(0);

            List<Integer> fromSuccessors = fromBlock.subList(1, fromBlock.size() - 1);
            List<Integer> toSuccessors = toBlock.subList(1, toBlock.size() - 1);

            this.successorsMap.put(from, fromSuccessors);
            this.successorsMap.put(to, toSuccessors);

            graph.putEdge(from, to);
        }

        this.graph = graph;
    }

    public List<List<Double>> traverse() {
        return traverse((n) -> n);
    }

    /**
     * Returns a list of results yielded by applying nodeFn to each
     * node in the traversal.
     * <p>
     * @param nodeFn A function applied to each {@code Node} in the
     *               traversal.
     * @return       The values returned by nodeFn.
     */
    public List<List<Double>> traverse(Function<List<Double>, List<Double>> nodeFn) {
        List<List<Double>> nodes = new ArrayList<>();
        Deque<Node> queue = new ArrayDeque<>();
        Node start = new Node(0, 0);
        HashMap<Integer, Boolean> visited = new HashMap<>();

        queue.add(start);

        while (!queue.isEmpty()) {
            Node current = queue.pop();

            if (visited.get(current.getValue()) != null) {
                continue;
            }

            nodes.add(nodeFn.apply(current.toList()));

            List<Integer> successors = new ArrayList<>(this.graph.successors(current.getValue()));
            for (int i = 0; i < successors.size(); i++) {
                int successor = successors.get(i);
                queue.add(new Node(current.getDepth() + 1, successor));
            }

            visited.put(current.getValue(), true);
        }

        return nodes;
    }

    public static class Node {
        private int depth;
        private int value;

        public Node(int depth, int value) {
            this.depth = depth;
            this.value = value;
        }

        public int getDepth() {
            return depth;
        }

        public int getValue() {
            return value;
        }

        private List<Double> toList() {
            List<Double> ret = new ArrayList<>();
            ret.add((double)this.value);
            ret.add((double)this.depth);
            return ret;
        }
    }
}
