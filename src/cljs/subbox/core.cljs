(ns subbox.core
  (:require [ajax.core :as aj]
            [clojure.browser.repl]
            [cognitect.transit :as t]
            [goog.dom :as gdom]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def app-state
  (atom {:subscriptions []}))

(defn app-view [app owner]
  (om/component
    (apply dom/ul nil
      (map (partial dom/li nil) (:subscriptions app)))))

(om/root app-view app-state
  {:target (gdom/getElement "app")})

(aj/GET "/subscriptions"
        {:handler (fn [new-subscriptions-transit]
                    (let [new-subscriptions (t/read (t/reader :json) new-subscriptions-transit)]
                      (reset! app-state {:subscriptions new-subscriptions})))})
