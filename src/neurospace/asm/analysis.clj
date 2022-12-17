(ns neurospace.asm.analysis
  (:import [org.objectweb.asm.tree MethodNode]
           [neurospace.asm BasicBlockGraph BlockMetrics ControlFlow]))

(defn make-basic-block-graph
  ^BasicBlockGraph [^String owner ^MethodNode method]
  (ControlFlow/newBasicBlockGraph owner method))

(defn traverse
  ([^BasicBlockGraph basic-block-graph]
   (.traverse basic-block-graph))
  ([f ^BasicBlockGraph basic-block-graph]
   (.traverse basic-block-graph
              (reify
                java.util.function.Function
                (apply [_ [idx depth]]
                  (f idx depth))))))
