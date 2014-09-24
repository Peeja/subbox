(ns subbox.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [ajax.core :as aj]
            [clojure.browser.repl]
            [cljs.core.async :as async :refer [chan put!]]
            [cognitect.transit :as t]
            [schema.core :refer-macros [defschema]]
            [om.core :as om]
            [om-tools.core :refer-macros [defcomponentk]]
            [om-tools.dom :as dom :include-macros true]))

(defonce app-state
  (atom {:selected nil
         :subscriptions []}))


(def Channel
  {:youtube.channel/id            js/String
   :youtube.channel/snippet.title js/String})


(defcomponentk channel-view
  [[:data [:youtube.channel/id :as id]
          [:youtube.channel/snippet.title :as title]] :- Channel
   [:opts select]]
  (render [_]
    (dom/li {:on-click #(put! select [:youtube.channel/id id])} title)))


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
    (let [selected-subscription (first (filter #(some #{selected} %) subscriptions))]
      (dom/div
        (dom/p "Currently selected: " (get selected-subscription :youtube.channel/snippet.title "Nothing."))
        (dom/ul (om/build-all channel-view subscriptions {:opts (select-keys @state [:select])}))))))


(om/root app-view app-state
  {:target (. js/document (getElementById "app"))})


(aj/GET "/subscriptions"
        {:handler (fn [new-subscriptions-transit]
                    (let [new-subscriptions (t/read (t/reader :json) new-subscriptions-transit)]
                      (swap! app-state assoc :subscriptions new-subscriptions)))})
