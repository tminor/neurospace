(ns neurospace.asm-test
  (:require [clojure.test :refer [deftest is]]
            [neurospace.asm :as asm]
            [neurospace.util :refer [seek]]))

(deftest Analyze-test
  (let [cn1 (asm/->class-node clojure.lang.RT)
        mn1 (seek #(= (.name %) "printInnerSeq") (.methods cn1))
        m1 (asm/map->Method {:owner clojure.lang.RT :node mn1})
        cn2 (asm/->class-node clojure.lang.RT)
        mn2 (seek #(= (.name %) "loadClassForName") (.methods cn2))
        m2 (asm/map->Method {:owner clojure.lang.RT :node mn2})]
    (is (> (asm/distance m1 m2) 0))
    (is (= (asm/distance m1 m1) 0.0))))
