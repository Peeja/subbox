(ns subbox.core
  (:require [clojure.browser.repl]
            [goog.dom :as gdom]
            [cognitect.transit :as t]
            [ajax.core :as aj]))

(enable-console-print!)

(def app-state
  (atom {:subscriptions []}))

(aj/GET "/subscriptions" {:handler #(println "Subscriptions: " (t/read (t/reader :json) %))})
