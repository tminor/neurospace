(ns neurospace.asm
  (:require [neurospace.util :as util]
            [neurospace.asm.analysis :as a])
  (:import [neurospace.asm BasicBlockGraph BlockMetrics ControlFlow]
           [org.objectweb.asm ClassReader ClassVisitor Opcodes]
           [org.objectweb.asm.tree ClassNode MethodNode]))

(defn- ->method-node
  "Returns a MethodNode instance."
  ^MethodNode [access name desc sig ex]
  (MethodNode. access name desc sig ex))

(defn- ->class-visitor
  "Returns a proxied instance of ClassVisitor with an implementation of
  visitMethod."
  ^ClassVisitor [^ClassNode class-node]
  (proxy [ClassVisitor] [Opcodes/ASM9 class-node]
    (visitMethod [access name desc sig ex]
      (let [method-visitor (->method-node access name desc sig ex)]
        (.add ^java.util.List (.methods class-node) method-visitor)
        method-visitor))))

(defn ->class-node
  "Returns a new ClassNode instance for clazz."
  ^ClassNode [^Class clazz]
  (try
    (let [class-bytes (util/class->input-stream clazz)
          ^ClassReader class-reader (ClassReader. class-bytes)
          ^ClassNode class-node (ClassNode.)]
      (.accept class-reader
               (->class-visitor class-node)
               (bit-or ClassReader/SKIP_DEBUG ClassReader/SKIP_FRAMES))
      class-node)
    (catch java.io.IOException _)))

(defprotocol Analyze
  (->basic-block-graph [this])
  (->metrics [this])
  (distance [this that]))

(defrecord Method [owner ^MethodNode node caller]
  Analyze
  (->basic-block-graph [this]
    (a/make-basic-block-graph (.getName ^Class (:owner this))
                              (:node this)))
  (->metrics [this]
    (let [^BasicBlockGraph graph (->basic-block-graph this)]
      (a/traverse (fn [idx depth]
                    (let [insns (.getBlockInsns graph (int idx))]
                      (into [idx depth] (BlockMetrics/calculate insns))))
                  graph)))
  (distance [this that]
    (ControlFlow/distance (->metrics this) (->metrics that))))
