;;  -*- coding: utf-8-unix; -*- 

(ns xducer-clj.core-test
  (:require [midje.sweet :refer [fact]] )
  (:require :reload-all [xducer-clj.core :refer [xduce-outputs 
                                                 xduce
                                                 complete-to-init]]))

(def xducer {:states [0 1]
             :end [1]
             :delta [{:from 0 
                      :in   "a" 
                      :out  "b"
                      :to   1 }]})

(fact "xducer with single transition outputs b on input a and ignores unknown inputs"
  (xduce-outputs xducer ["a"])         =>  [["b"]]
  (xduce-outputs xducer ["b"])         => []
  (xduce-outputs xducer ["b" "a"])     => [["b"]]
  (xduce-outputs xducer ["b" "a" "a"]) => [["b"]])

(def xducer2 {:states [0 1 2]
              :end [2]
              :delta [{:from 0 :in "a" :out  "b" :to  1 }
                      {:from 1 :in "b" :out  "c" :to  2 }]})

(fact "deterministic xducer with two transitions in sequence outputs matching transitions"
  (xduce-outputs xducer2 ["a" "b"]) => [ ["b" "c"]]
  (xduce-outputs xducer2 ["c" "a" "b" "b"]) => [ ["b" "c"]])
  

(def xducer-with-loop
  {:states [0 1 2]
   :end [2]
   :delta [{:from 0 :in "a" :out  "b" :to  1 }
           {:from 1 :in "b" :out  "c" :to  1 }
           {:from 1 :in "a" :out  "b" :to  2}]})

(fact "transducer with a loop produces multiple tokens"
  (xduce-outputs xducer-with-loop ["a" "b" "b" "b" "a"]) => [ ["b" "c" "c" "c" "b"]])

(fact "transducer outputs nothing if input does not get it into end state"
  (xduce-outputs xducer-with-loop ["a"]) => [])

(def xducer-with-silent-transition
  {:states [0 1 2 3]
   :end [2 3]
   :delta [{:from 0 :in "a" :out  nil :to  1 }
           {:from 0 :in "b" :out  "d" :to  2 }
           {:from 1 :in "b" :out  "c" :to  3 }]})

(fact "a transducer can contains silent transitions"
  (xduce-outputs xducer-with-silent-transition ["a" "b"]) => [ ["c"]])

(fact "a transducer can be 'completed' to go back to init"
  (xduce-outputs xducer2                    ["a" "c" "b"]) => [ ["b" "c"]]
  (xduce-outputs (complete-to-init xducer2) ["a" "c" "b"]) => [])


(def xducer-with-custom-comparison
  {:states [0 1]
   :compare-with #'re-matches
   :end [1 2]
   :delta [{:from 0 :in #".*aleph" :out  "beth"  :to  1}
           {:from 1 :in #"alpha"   :out  "gamma" :to  1}
           {:from 1 :in #"alf.*"   :out  "beta"  :to  2}]})

(fact "a transducer can be given a custom comparison operator"
  (xduce-outputs xducer-with-custom-comparison ["aleph" "alpha"]) => [ ["beth" "gamma"]]
  (xduce-outputs xducer-with-custom-comparison ["maleph" "alfa"]) => [ ["beth" "beta"]])

(def nondeterministic-xducer
  {:states [0 1 2 3]
   :end [3]
   :delta [{:from 0 :in "a" :out  "b" :to  1 }
           {:from 0 :in "a" :out  "c" :to  2 }
           {:from 1 :in "b" :out  "c" :to  3 }
           {:from 2 :in "c" :out  "d" :to  3 }]})

(def other-nondeterministic-xducer
  {:states [0 1 2 3]
   :end [3]
   :delta [{:from 0 :in "a" :out  "b" :to  1 }
           {:from 0 :in "a" :out  "c" :to  2 }
           {:from 1 :in "b" :out  "c" :to  3 }
           {:from 2 :in "b" :out  "d" :to  3 }]})

(fact "a transducer can be non-deterministic"
  (xduce-outputs nondeterministic-xducer ["a" "b"]) => [["b" "c"]]
  (xduce-outputs nondeterministic-xducer ["a" "c"]) => [["c" "d"]]
  (xduce-outputs other-nondeterministic-xducer ["a" "b"]) => [["b" "c"] ["c" "d"]])

(fact "a transducer can output position of matches"
  (xduce (complete-to-init nondeterministic-xducer) ["c" "c" "a" "b"]) => [ {:begin 2 :length 2 :output ["b" "c"]}])
  
(def xducer-with-any-transition
  {:states [0 1 2]
   :end [1 2]
   :delta [{:from 0 :in "a"  :out  "b" :to  1 }
           {:from 1 :in :any :out  "e" :to  1 }
           {:from 1 :in "d"  :out  "c" :to  2}]})

(fact "a transition marked :any matches all input"
  (xduce-outputs xducer-with-any-transition ["a" "b" "c" "d"]) => [["b" "e" "e" "e"]  ["b" "e" "e" "c"]])
  
(def xducer-with-else-transition
  {:states [0 1 2]
   :end [1 2]
   :delta [{:from 0 :in "a"  :out  "b" :to  1 }
           {:from 1 :in :else :out  "e" :to  1 }
           {:from 1 :in "d"  :out  "c" :to  2}]})

;.;. If this isn't nice, I don't know what is. -- Vonnegut
(fact "a transition marked :else matches all input except explicit inputs"
  (xduce-outputs xducer-with-else-transition ["a" "b" "c" "d"]) => [["b" "e" "e" "c"]]
  (xduce-outputs xducer-with-else-transition ["a" "b" "c"])     => [["b" "e" "e"]]
  (xduce-outputs xducer-with-else-transition ["a" "d"])         => [["b" "c"]])

(fact "result from a transducer can be reduced through semiring operations"
    (xduce-outputs xducer-with-any-transition ["a" "b" "c" "d"]) => [["b" "e" "e" "e"]  ["b" "e" "e" "c"]])
