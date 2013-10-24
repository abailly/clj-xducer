;;  -*- coding: utf-8-unix; -*- 
(ns xducer-clj.semiring)

(defmacro ln 
  [x]
  `(Math/log ~x))

(defmacro e
  [x]
  `(Math/exp ~x))

(def Semiring 
  "A semiring is a structure that contains two semigroups,
   one of them being commutative."
  (create-struct 
   :zero
   :plus
   :one
   :times))

(defmacro with-semiring
  [sr & body]
  `(let [{:keys ~'[zero plus one times]}
         ~sr]
     ~@body
     ))

(def long-sr  
  (struct Semiring
          0
          #'clojure.core/+
          1
          #'clojure.core/*
          ))

(defmacro with-long-semiring
  "A Semiring for numeric values"
  [& body]
  `(with-semiring long-sr
     ~@body
     ))

(defn- cartesian-product
  [a b]
  (for [s a t b] (concat s t)))

(def sequence-semiring
  (struct Semiring
          []
          #'concat
          [[]]
          #'cartesian-product
          ))

(defmacro with-sequence-semiring
  "A Semiring for sequences"
  [& body]
  `(with-semiring sequence-semiring
     ~@body
     ))

(defn- product-pairwise-with
 [f x y]
  (let  [binary-product #(let [[s1 p1 s2 p2] %]
                            [(str s1 s2) (f p1 p2)])]
    (map binary-product (cartesian-product x y))))

(defn- reduce-pairwise-with
  "Given two sequences of pairs of values, and a binary operation f,
   computes the reduced values applying f on consecutive elements where
   first element of pair is similar. 

   This function first sorts the concatenation of the two sequences, hence it
   produces a (lexicographically) sorted new list where no two pairs share the same
   first element"
  [f x y]
  (let  [probas (sort (concat x y))]
    (loop [ps probas 
           result []]
      (if-let [[p & ps'] (seq ps)]
        (if-let [[p' & ps''] (seq ps')]
          (if (= (first p) (first p'))
            (recur (conj  ps'' [(first p) 
                                (f (second p) (second p'))]) 
                   result)
            (recur ps' (conj result p)))
          (conj result p))
        result))))

(def proba-semiring
  (struct Semiring
          []
          #(reduce-pairwise-with #'+ %1 %2)
          [[ "" 1]]
          #(product-pairwise-with #'* %1 %2)
          ))

(defmacro with-string-proba-semiring
  "A Semiring for probabilities over strings
   Each element of this semiring is a probability distribution over strings."
  [& body]
  `(with-semiring proba-semiring
     ~@body
     ))

(defn log+ 
  [x y]
  (- (ln (+ (e (- x)) (e (- y))))))

(def log-semiring
  (struct Semiring
          []
          #(reduce-pairwise-with #'log+ %1 %2)
          [[ "" 1]]
          #(product-pairwise-with #'+ %1 %2)
          ))

(defmacro with-string-log-semiring
  "A Semiring for log-reduced probabilities over strings."
  [& body]
  `(with-semiring log-semiring
     ~@body
     ))


