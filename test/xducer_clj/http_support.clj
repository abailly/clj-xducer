(ns xducer-clj.http-support
  (:use [clojure.tools.logging])
  (:import [java.net InetSocketAddress])
  (:import [com.sun.net.httpserver HttpServer HttpHandler]))


(def response
  "<!doctype html> <html> <head> <title>Example Domain</title>

        <meta charset=\"utf-8\" />
        <meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\" />
        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />
        <style type=\"text/css\">
        body {
                background-color: #f0f0f2;
                margin: 0;
                padding: 0;
                font-family: \"Open Sans\", \"Helvetica Neue\", Helvetica, Arial, sans-serif;

        }
        div {
                width: 600px;
                margin: 5em auto;
                padding: 3em;
                background-color: #fff;
                border-radius: 1em;
        }
        a:link, a:visited {
                color: #38488f;
                text-decoration: none;
        }
        @media (max-width: 600px) {
                body {
                        background-color: #fff;
                }
                div {
                        width: auto;
                        margin: 0 auto;
                        border-radius: 0;
                        padding: 1em;
                }
        }
        </style>
</head>

<body>
<div>
        <h1>Example Domain</h1>
        <p>This domain is established to be used for illustrative examples in documents. You do not need to
                coordinate or ask for permission to use this domain in examples, and it is not available for
                registration.</p>
        <p><a href=\"http://www.iana.org/domains/special\">More information...</a></p>
</div>
</body>
</html>")

(def constant-handler 
  (proxy [HttpHandler]
      []
      (handle [httpExchange]
        (let [headers (.getRequestHeaders httpExchange)
              os (.getResponseBody httpExchange)]
          (do 
            (info "responding" headers)
            (.sendResponseHeaders httpExchange 200 0)
            (doto os
              (.write (.getBytes response))
              (.close)))))))
        
(defn start-server
  []
  (do (info "starting server") 
      (doto (HttpServer/create (InetSocketAddress. 34569) 1)
        (.createContext "/" constant-handler)
        (.setExecutor nil)
        (.start))))

(defn stop-server 
  [server]
  (do (info "stopping server")
      (.stop server 0)
      (info "stopped server")
      ))

(defmacro with-server 
  [form]
  `(let [server# (start-server)]
     (try
       (do 
         (info "started server")
         ~form)
       (finally (stop-server server#)))))

