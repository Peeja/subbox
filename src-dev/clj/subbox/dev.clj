(ns subbox.dev
  (:require [subbox.handler :as handler]))

(def repl-env (reset! cemerick.austin.repls/browser-repl-env
                      (cemerick.austin/repl-env)))

(def app handler/app)
