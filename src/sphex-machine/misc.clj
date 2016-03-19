(ns sphex-machine.misc
  (:require [clojure.math.numeric-tower :as math]))

(defn merge-sorted
  "Merges two sorted sequences."
  ([lst-1 lst-2]
   (merge-sorted lst-1 lst-2 []))
  ([lst-1 lst-2 acc]
   (cond
     (empty? lst-1)
     (concat acc lst-2)
     
     (empty? lst-2)
     (concat acc lst-1)
     
     (< (first lst-1) (first lst-2))
     (merge-sorted (rest lst-1) lst-2 (conj acc (first lst-1)))
     
     :else
     (merge-sorted lst-1 (rest lst-2) (conj acc (first lst-2))))))

(defn init
  [seq]
  (take (-> seq count dec) seq))

(def boolean->int {false 0 true 1})

;;
;; Perceptron
;;
(defn perceptron
  [& weights]
  (fn [& vals]
    (when (= (dec (count weights)) (count vals))
      (boolean->int
       (> (reduce + (map * weights (conj (vec vals) 1)))
          0)))))

(def nand (perceptron -2 -2 3))

;;
;; Sigmoid
;;
(defn sigmoid
  [& weights]
  (fn [& vals]
    (when (= (dec (count weights)) (count vals))
      (/ 1 (+ 1 (math/expt java.lang.Math/E
                           (- (reduce + (map * weights (conj (vec vals) 1))))))))))

