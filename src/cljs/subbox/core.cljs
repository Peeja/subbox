(ns subbox.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [ajax.core :as aj]
            [clojure.browser.repl]
            [clojure.string :as string]
            [cljs.core.async :as async :refer [chan put!]]
            [cognitect.transit :as t]
            [schema.core :as s :refer-macros [defschema]]
            [om.core :as om]
            [om-tools.core :refer-macros [defcomponentk]]
            [om-tools.dom :as dom :include-macros true]))

(defonce app-state
  (atom {:selected-ref nil
         :subscriptions []}))

(defn ajax-get
  "Makes a GET request. Returns a channel which will deliver the result.
  Currently assumes the response is transit, and parses it."
  [url]
  (let [c (chan)]
    (aj/GET url
            {:handler (fn [response-transit]
                        (->> response-transit
                             (put! c)))})
    c))

(defn fetch!
  "Fetches data for a list."
  [list-cursor]
  (go
    (let [next-url (:list/next @list-cursor)
          next-list (<! (ajax-get next-url))]
      (om/transact! list-cursor
                    #(update-in next-list [:list/items] (partial into (:list/items %)))))))

(defn preserve-line-breaks
  [text]
  (interpose (dom/br nil) (string/split text #"\n")))

(defn paragraphs
  [text]
  (->> (string/split text #"\n\s*\n")
       (map #(dom/p (preserve-line-breaks %)))))


(defn thumbnail
  [{:keys [url width height] :as thmb}]
  (dom/img {:class "thumbnail" :src url :width width :height height}))


(defschema List
  {:list/items []
   :list/next js/String})

(defschema Channel
  {:youtube.channel/id            js/String
   :youtube.channel/snippet.title js/String
   :videos List})

(defschema ChannelListItem
  (assoc Channel :selected? js/Boolean))


(defcomponentk channel-list-item-view
  "A channel, as represented in the subscription list."
  [[:data {selected? false}
          [:youtube.channel/id :as id]
          [:youtube.channel/snippet.title :as title]
          [:youtube.channel/snippet.thumbnails :as thumbnails]] ; :- ChannelListItem
   [:opts select]]
  (render [_]
    (dom/li {:class (when selected? "selected")
             :on-click #(put! select [:youtube.channel/id id])}
            (thumbnail (:default thumbnails))
            (dom/span {:class "title"} title))))

(defcomponentk video-list-item-view
  [[:data [:youtube.video/snippet.title :as title]
          [:youtube.video/snippet.description :as description]
          [:youtube.video/snippet.thumbnails :as thumbnails]]]
  (render [_]
    (dom/li
      (dom/article {:class "video"}
        (thumbnail (:medium thumbnails))
        (dom/div {:class "info"}
          (dom/h1 {:class "title"} title)
          (dom/div {:class "description"} (paragraphs description)))))))

(defcomponentk main-view
  "The main area of the page, including the list of videos."
  [[:data [:youtube.channel/snippet.title :as title]
          videos]]; :- Channel]
   (render [_]
     (when-not (seq (:list/items videos))
       (fetch! videos))
     (dom/section {:class "main"}
       (dom/h1 title)
       (dom/ul {:class "videos"}
         (om/build-all video-list-item-view (:list/items videos))))))


(defcomponentk app-view
  "The entire application."
  [[:data selected-ref subscriptions :as app] state]

  (init-state [_]
    {:select (chan)})

  (will-mount [_]
    (go-loop []
      (let [new-selected-ref (<! (:select @state))]
        (om/update! app :selected-ref new-selected-ref)
        (recur))))

  (render [_]
    (let [selected? #(some #{selected-ref} %)
          selected-subscription (->> subscriptions
                                     (filter selected?)
                                     first)
          subscriptions-with-selected (map #(assoc % :selected? (= % selected-subscription)) subscriptions)]
      (dom/div {:class "app"}
        (dom/ul {:class "subscriptions"}
                (om/build-all channel-list-item-view
                              subscriptions-with-selected
                              {:opts (select-keys @state [:select])}))
        (when selected-subscription
          (om/build main-view selected-subscription))))))


(om/root app-view app-state
  {:target (. js/document (getElementById "app"))})


(aj/GET "/subscriptions"
        {:handler (fn [new-subscriptions-transit]
                    (let [new-subscriptions (t/read (t/reader :json) new-subscriptions-transit)]
                      (swap! app-state assoc :subscriptions (vec new-subscriptions))))})
