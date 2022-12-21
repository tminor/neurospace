(ns neurospace.asm-test
  (:import [neurospace TestMethods])
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
    (is (= (asm/distance m1 m1) 0.0)))
  (let [cn (asm/->class-node TestMethods)
        control-mn (seek #(= (.name %) "doFoo") (.methods cn))
        obmn1 (seek #(= (.name %) "xy") (.methods cn))
        obmn2 (seek #(= (.name %) "ab") (.methods cn))
        obmn3 (seek #(= (.name %) "bc") (.methods cn))
        m (asm/map->Method {:owner TestMethods :node control-mn})
        ob1 (asm/map->Method {:owner TestMethods :node obmn1})
        ob2 (asm/map->Method {:owner TestMethods :node obmn2})
        ob3 (asm/map->Method {:owner TestMethods :node obmn3})]
    ;; A lightly obfuscated version of the control method
    (is (<= (Math/floor (Math/log10 (asm/distance m ob1))) 2.0))
    ;; An obfuscated version of a method that isn't the control
    (is (>= (Math/floor (Math/log10 (asm/distance m ob2))) 2.0))
    ;; A more obfuscated version of the control method
    (is (<= (Math/floor (Math/log10 (asm/distance m ob3))) 2.0))))
