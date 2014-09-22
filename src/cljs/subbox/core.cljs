(ns subbox.core
  (:require [ajax.core :as aj]
            [clojure.browser.repl]
            [cognitect.transit :as t]
            [figwheel.client :as fw :include-macros true]
            [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]))

(def localhost? (-> js/window
                    (.-location)
                    (.-host)
                    (.indexOf "localhost")
                    (>= 0)))

(defonce app-state
  (atom {:subscriptions []}))

(defcomponent channel-view [channel owner]
  (render [_]
    (->> channel :youtube.channel/snippet.title dom/li)))

(defcomponent app-view [app owner]
  (render [_]
    (dom/ul (om/build-all channel-view (:subscriptions app)))))

(om/root app-view app-state
  {:target (. js/document (getElementById "app"))})

(aj/GET "/subscriptions"
        {:handler (fn [new-subscriptions-transit]
                    (let [new-subscriptions (t/read (t/reader :json) new-subscriptions-transit)]
                      (reset! app-state {:subscriptions new-subscriptions})))})

(if localhost?
  (enable-console-print!))

(if localhost?
 (fw/watch-and-reload
  :websocket-url   "ws://localhost:3449/figwheel-ws"
  :jsload-callback (fn [] (print "reloaded"))))
