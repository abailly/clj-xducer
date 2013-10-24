(ns xducer-clj.http-test
  (:require [midje.sweet :refer [fact against-background before]] )
  (:require :reload-all [xducer-clj.http-support :as support])
  (:require :reload-all [xducer-clj.http :as http])
  (:require :reload-all [xducer-clj.core :as core]))

(fact "stringifying a tagsoup output yields a sequence of strings"
  (http/stringify [:title {} "Example Web Page"]) => ["Example Web Page"]
  (http/stringify [:head {} [:title {} "Example Web Page"]]) => ["Example Web Page"]
  (http/stringify [:style {:type "text/css"} "Example Web Page"]) => []
  (http/stringify [:html {}
                   [:head {}
                    [:title {} "Example Web Page"]]
                   [:body {}
                    [:p {} "You have reached this web page by typing \"example.com\",\n\"example.net\",\n  or \"example.org\" into your web browser."]
                    [:p {} "These domain names are reserved for use in documentation and are not available \n  for registration. See "
                     [:a {:shape "rect", :href "http://www.rfc-editor.org/rfc/rfc2606.txt"} "RFC \n  2606"]
                     ", Section 3."]]]) => ["Example Web Page" 
                                            "You have reached this web page by typing \"example.com\",\n\"example.net\",\n  or \"example.org\" into your web browser."
                                            "These domain names are reserved for use in documentation and are not available \n  for registration. See "
                                            "RFC \n  2606"
                                            ", Section 3."])


(def xducer 
  {:states [0 1 2 3]
   :end [3]
   :delta [{:from 0 :in "domain"     :out nil      :to 1}
           {:from 1 :in "coordinate" :out "COORD"  :to 2}
           {:from 2 :in "domain"     :out "DOMAIN" :to 3}]})

(fact "xducer interprets a stream of tokens from a URL"
 (core/xduce xducer 
             (http/read-url "http://localhost:34569")) => [ {:begin 41, :length 0, :output ["COORD" "DOMAIN"]}]
             (against-background (around :facts (support/with-server ?form))))
