(ns subbox.server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [subbox.handler :refer [app]])
  (:gen-class :name "subbox.Server"))

;;; For running the production server.

(defn -main []
  (let [port (Integer/parseInt (System/getenv "PORT"))]
    (run-jetty app {:port port})))
