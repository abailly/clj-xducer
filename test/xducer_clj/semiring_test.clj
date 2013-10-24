;;  -*- coding: utf-8-unix; -*- 
(ns xducer-clj.semiring-test
  (:require [midje.sweet :refer [fact against-background before]] )
  (:use :reload-all [xducer-clj.semiring]))

(fact "there is a semiring defined for Long"
  (with-long-semiring (plus 1 2))  => 3
  (with-long-semiring zero)        => 0
  (with-long-semiring one)         => 1
  (with-long-semiring (times 2 2)) => 4)

(fact "there is a semiring defined for words (Vectors)"
  (with-sequence-semiring (times [[:a :b]] [[:c :d]]))          => [[:a :b :c :d]]
  (with-sequence-semiring (plus  [[:a :b]] [[:c :d]]))          => [[ :a :b] [ :c :d]]
  (with-sequence-semiring (times [[:a :b]  [:c :d]] [[:e :f]])) => [[:a :b :e :f]  [:c :d :e :f]])

(fact "zero is absorbing for multiplication and neutral for addition"
  (with-sequence-semiring (times [[:a :b]] zero)) => (with-sequence-semiring zero)
  (with-sequence-semiring (plus  [[:a :b]] zero)) => [[:a :b]])

(fact "one is neutral for multiplication"
  (with-sequence-semiring (times [[:a :b]] one))  =>  [[:a :b]])

(fact "there is a probability semiring for pairs of (string, double)"
  (with-string-proba-semiring (times [[ "foo" 0.12]] [[ "bar" 0.14]]))                => [[ "foobar" (* 0.12 0.14)]]
  (with-string-proba-semiring (plus  [[ "foo" 0.12]] [[ "bar" 0.14]]))                => [[ "bar" 0.14] [ "foo" 0.12] ]
  (with-string-proba-semiring (plus  [[ "foo" 0.12]] zero))                           => [[ "foo" 0.12] ]
  (with-string-proba-semiring (plus  [[ "foo" 0.12]] [[ "foo" 0.11]]))                => [[ "foo" (+ 0.12 0.11) ]]
  (with-string-proba-semiring (plus  [[ "foo" 0.12]] [[ "bar" 0.14] [ "foo" 0.11]]))  => [[ "bar" 0.14] [ "foo" (+ 0.12 0.11) ]]
  (with-string-proba-semiring (times  [[ "foo" 0.12] [ "bar" 0.14]] [[ "baz" 0.15]])) => [[ "foobaz" (* 0.12 0.15)] [ "barbaz" (* 0.14 0.15)]])

(def sum-of-log-probabilities
  (- (ln (+ (e 0.12) (e 0.11)))))

;.;. Excellence is not an act but a habit. -- Aristotle
(fact "there is a log semiring for pairs of (string, double)"
  (with-string-log-semiring (times [[ "foo" -0.12]] [[ "bar" -0.14]]))                => [[ "foobar" (+ -0.12 -0.14)]]
  (with-string-log-semiring (plus  [[ "foo" -0.12]] [[ "foo" -0.11]]))                => [[ "foo" sum-of-log-probabilities ]]
  (with-string-log-semiring (plus  [[ "foo" -0.12]] zero))                            => [[ "foo" -0.12]]
  (with-string-log-semiring (times  [[ "foo" -0.12]] zero))                           => (with-string-log-semiring zero)
  (with-string-log-semiring (plus  [[ "foo" -0.12]] [[ "bar" -0.14] [ "foo" -0.11]])) => [[ "bar" -0.14] [ "foo" sum-of-log-probabilities ]])

