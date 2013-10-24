(defproject xducer-clj "0.1.0-SNAPSHOT"
  :description "A library for rational transducer based stream analysis"
  :url "https://bitbucket.org/abailly/wincomparator-stream-query/wiki/Home"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clj-http "0.6.3"]
                 [org.slf4j/slf4j-api "1.6.4"]
                 [ch.qos.logback/logback-core "1.0.1"]
                 [ch.qos.logback/logback-classic "1.0.1"]
                 [org.clojure/tools.logging "0.2.3"]
                 [clj-tagsoup "0.3.0"]
                 [midje "1.5-alpha8"]]
  :main xducer-clj.core
  )
