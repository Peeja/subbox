(ns subbox.core
  (:require [ajax.core :as aj]
            [clojure.browser.repl]
            [cljs.core.async :as async :refer [chan put!]]
            [cognitect.transit :as t]
            [om.core :as om]
            [om-tools.core :refer-macros [defcomponentk]]
            [om-tools.dom :as dom :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defonce app-state
  (atom {:selected ""
         :subscriptions []}))

(defcomponentk channel-view
  [[:data :as channel] [:opts select]]
  (render [_]
    (let [title (->> channel :youtube.channel/snippet.title )]
      (dom/li {:on-click #(put! select title)} title))))

(defcomponentk app-view
  [[:data selected subscriptions :as app] state]
  (init-state [_]
    {:select (chan)})
  (will-mount [_]
    (go-loop []
      (let [new-selected (<! (:select @state))]
        (om/transact! app :selected (constantly new-selected))
        (recur))))
  (render [_]
    (dom/div
      (dom/p "Currently selected: " selected)
      (dom/ul (om/build-all channel-view subscriptions {:opts (select-keys @state [:select])})))))

(om/root app-view app-state
  {:target (. js/document (getElementById "app"))})

(aj/GET "/subscriptions"
        {:handler (fn [new-subscriptions-transit]
                    (let [new-subscriptions (t/read (t/reader :json) new-subscriptions-transit)]
                      (swap! app-state assoc :subscriptions new-subscriptions)))})
