(ns subbox.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [ajax.core :as aj]
            [clojure.browser.repl]
            [clojure.string :as string]
            [cljs.core.async :as async :refer [chan put!]]
            [cognitect.transit :as t]
            [schema.core :as s :refer-macros [defschema]]
            [subbox.player :as player]
            [om.core :as om]
            [om-tools.core :refer-macros [defcomponentk]]
            [om-tools.dom :as dom :include-macros true]))

(player/enable!)

(defonce app-state
  (atom {:selected-channel-ref nil
         :watching-video nil
         :subscriptions []}))

(defn fetch!
  "Fetches data for a list."
  [list-cursor]
  (aj/GET (:list/next list-cursor)
          {:handler (fn [next-list]
                      (om/transact! list-cursor
                                    #(update-in next-list
                                                [:list/items]
                                                (partial into (:list/items %)))))}))

(defn direct-event?
  "Returns true iff the given event occurred directly on the element where the
  listener was attached. In other words, returns false iff the event bubbled
  from a child element."
  [event]
  (= (aget event "target")
     (aget event "currentTarget")))

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
   shared]
  (render [_]
    (dom/li {:class (when selected? "selected")
             :on-click #(put! (:select shared) [:youtube.channel/id id])}
            (thumbnail (:default thumbnails))
            (dom/span {:class "title"} title))))

(defcomponentk video-list-item-view
  [[:data [:youtube.video/snippet.title :as title]
          [:youtube.video/snippet.description :as description]
          [:youtube.video/snippet.thumbnails :as thumbnails]
          :as video]
   [:shared watching]]
  (render [_]
    (dom/li {:on-click #(put! watching video)}
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


(defcomponentk player-view
  [[:data [:youtube.video/id :as id]
          :as video]
   owner]
  ;; Also did-update
  (did-mount [_]
    (player/player (om/get-node owner) id))
  (render [_]
    (dom/div nil)))


(defcomponentk watch-screen-view
  [[:data [:youtube.video/snippet.title :as title]
          :as video]
   shared]
  (render [_]
          (dom/div {:class "watch-screen"
                    :on-click #(when (direct-event? %) (put! (:watching shared) :none))}
                   (->player-view video))))


(defcomponentk app-view
  "The entire application."
  [[:data selected-channel-ref
          watching-video
          subscriptions :as app]
   shared]

  (will-mount [_]
    (go-loop []
      (let [new-selected-channel-ref (<! (:select shared))]
        (om/update! app :selected-channel-ref new-selected-channel-ref)
        (recur)))
    (go-loop []
      (let [new-watching-video (<! (:watching shared))]
        (om/update! app :watching-video (when (not= :none new-watching-video) new-watching-video))
        (recur))))

  (render [_]
    (let [selected? #(some #{selected-channel-ref} %)
          selected-subscription (->> subscriptions
                                     (filter selected?)
                                     first)
          subscriptions-with-selected (map #(assoc % :selected? (= % selected-subscription)) subscriptions)]

      (dom/div {:class "app"}

        (when watching-video
          (->watch-screen-view watching-video))

        (dom/ul {:class "subscriptions"}
                (om/build-all channel-list-item-view
                              subscriptions-with-selected))
        (when selected-subscription
          (->main-view selected-subscription))))))


(om/root app-view app-state
  {:target (. js/document (getElementById "app"))
   :shared {:select (chan)
            :watching (chan)}})


(aj/GET "/subscriptions"
        {:handler (fn [new-subscriptions-transit]
                    (let [new-subscriptions (t/read (t/reader :json) new-subscriptions-transit)]
                      (swap! app-state assoc :subscriptions (vec new-subscriptions))))})
