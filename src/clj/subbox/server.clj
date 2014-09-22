(ns subbox.server
  (:require [environ.core :refer [env]]
            [ring.adapter.jetty :as jetty]
            [subbox.app :as app]))

(defn run [handler & [port]]
  (defonce ^:private server
    (jetty/run-jetty handler {:port (Integer. (or port (env :port) 10555))
                            :join? false}))
  server)

(defn -main [& [port]]
  (run #'app/app port))
