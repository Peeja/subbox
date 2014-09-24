(ns subbox.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [ajax.core :as aj]
            [clojure.browser.repl]
            [cljs.core.async :as async :refer [chan put!]]
            [cognitect.transit :as t]
            [schema.core :as s :refer-macros [defschema]]
            [om.core :as om]
            [om-tools.core :refer-macros [defcomponentk]]
            [om-tools.dom :as dom :include-macros true]))

(defonce app-state
  (atom {:selected-ref nil
         :subscriptions []}))


(def Channel
  {:selected?                     js/Boolean
   :youtube.channel/id            js/String
   :youtube.channel/snippet.title js/String})


(defcomponentk channel-view
  [[:data {selected? false}
          [:youtube.channel/id :as id]
          [:youtube.channel/snippet.title :as title]] :- Channel
   [:opts select]]
  (render [_]
    (dom/li {:class (when selected? "selected")
             :on-click #(put! select [:youtube.channel/id id])}
            title)))


(defcomponentk app-view
  [[:data selected-ref subscriptions :as app] state]

  (init-state [_]
    {:select (chan)})

  (will-mount [_]
    (go-loop []
      (let [new-selected-ref (<! (:select @state))]
        (om/transact! app :selected-ref (constantly new-selected-ref))
        (recur))))

  (render [_]
    (let [selected? #(some #{selected-ref} %)
          selected-subscription (->> subscriptions
                                     (filter selected?)
                                     first)
          subscriptions-with-selected (map #(assoc % :selected? (= % selected-subscription)) subscriptions)]
      (dom/div
        (dom/p "Currently selected: " (get selected-subscription :youtube.channel/snippet.title "Nothing."))
        (dom/ul (om/build-all channel-view
                              subscriptions-with-selected
                              {:opts (select-keys @state [:select])}))))))


(om/root app-view app-state
  {:target (. js/document (getElementById "app"))})


(aj/GET "/subscriptions"
        {:handler (fn [new-subscriptions-transit]
                    (let [new-subscriptions (t/read (t/reader :json) new-subscriptions-transit)]
                      (swap! app-state assoc :subscriptions (vec new-subscriptions))))})
