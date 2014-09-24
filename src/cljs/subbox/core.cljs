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
  (atom {:selected nil
         :subscriptions []}))


(def Channel
  {(s/optional-key :selected?)    js/Boolean
   :youtube.channel/id            js/String
   :youtube.channel/snippet.title js/String})


(defcomponentk channel-view
  [[:data {selected? false}
          [:youtube.channel/id :as id]
          [:youtube.channel/snippet.title :as title]] ;:- Channel ; Schema validation isn't working for some reason.
   [:opts select]]
  (render [_]
    (dom/li {:class (when selected? "selected")
             :on-click #(put! select [:youtube.channel/id id])}
            title)))

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
    (let [selected? #(some #{selected} %)
          [selected-index selected-subscription] (->> subscriptions
                                                      (map #(when (selected? %2) [%1 %2]) (range))
                                                      (filter (complement nil?))
                                                      first)
          subscriptions-with-selected (if selected-subscription
                                        (assoc-in subscriptions [selected-index :selected? true])
                                        subscriptions)]
      (dom/div
        (dom/p "Currently selected: " (get selected-subscription :youtube.channel/snippet.title "Nothing.") " (#" selected-index ") ")
        (dom/ul (om/build-all channel-view
                              subscriptions-with-selected
                              {:opts (select-keys @state [:select])}))))))


(om/root app-view app-state
  {:target (. js/document (getElementById "app"))})


(aj/GET "/subscriptions"
        {:handler (fn [new-subscriptions-transit]
                    (let [new-subscriptions (t/read (t/reader :json) new-subscriptions-transit)]
                      (swap! app-state assoc :subscriptions (vec new-subscriptions))))})
