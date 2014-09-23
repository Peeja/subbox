(ns subbox.dev
  (:require [cemerick.austin :as austin]
            [cemerick.austin.repls :as repls :refer [browser-connected-repl-js]]
            [net.cgrand.enlive-html :as enlive]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [subbox.app :as app]
            [subbox.server :as server]))

(def add-dev-scripts
  (enlive/append (enlive/html [:script (browser-connected-repl-js)]
                              [:script "goog.require('subbox.dev');"])))

(defn wrap-with-body-transform
  [handler transform]
  (fn [request]
    (binding [app/*body-transform* transform]
      (handler request))))

(def handler
  (-> app/app
      (wrap-with-body-transform add-dev-scripts)
      wrap-stacktrace
      wrap-reload))

(defn run []
  (reset! repls/browser-repl-env
          (austin/repl-env))
  (server/run #'handler))
