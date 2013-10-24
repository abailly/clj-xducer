(ns xducer-clj.http
  (:require [pl.danieljanus.tagsoup :as tagsoup])
  (:require [clj-http.client :as client]))

(defmacro dbg 
  "provide debugging trace for a given expression"
  [x] 
  `(let [x# ~x] (debug "dbg:" '~x "=" x#) x#))

(defn stringify 
  [data]
  (do 
    (cond 
     (keyword? data)   []
     (string? data)    [data]
     (map? data)       []
     (empty? data)     []
     (seq? (seq data)) (if (= (first data) :style)
                         []
                         (concat (stringify (first data)) 
                                 (stringify (rest data))))
     true (recur (rest data)))))

(defn read-url 
  [url]
  (let [body (:body (client/get url))]
    (mapcat (partial re-seq #"[^\s]+") (stringify (tagsoup/parse-string body))))
  )
