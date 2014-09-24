(ns subbox.dev
  (:require [figwheel.client :as fw :include-macros true]
            [schema.core :as schema]))

(enable-console-print!)

(schema/set-fn-validation! true)

(fw/watch-and-reload
  :websocket-url   "ws://localhost:3449/figwheel-ws"
  :jsload-callback (fn [] (print "reloaded")))
