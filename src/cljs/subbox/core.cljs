(ns subbox.core
  (:require [cognitect.transit :as t]
            [ajax.core :as aj]))

(enable-console-print!)

(aj/GET "/subscriptions" {:handler #(println "Subscriptions: " (t/read (t/reader :json) %))})
