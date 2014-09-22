(ns subbox.dev
  (:require [ring.middleware.reload :as reload]
            [subbox.app :as app]
            [subbox.server :as server]))

(def handler
  (-> app/app
      reload/wrap-reload))

(defn run []
  (server/run #'handler))
