(ns subbox.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [ajax.core :as aj]
            [clojure.browser.repl]
            [clojure.string :as string]
            [cljs.core.async :as async :refer [chan put!]]
            [cognitect.transit :as t]
            [subbox.player :as player]
            [om.core :as om]
            [om-tools.core :refer-macros [defcomponentk]]
            [om-tools.dom :as dom :include-macros true]))

(player/enable!)


(defonce app-state
  (atom {:selected-channel-id nil
         :watching-video nil
         :entities {:channels {}
                    :videos {}}
         :subscriptions {:list/items []
                         :list/next (str "/subscriptions")}}))


(defn on-ajax-error
  [{:keys [status status-text]}]
  (when (= 401 status)
    ;; We're logged out, so log in again.
    (js/location.assign "/login")))

(defn fetch!
  "Fetches data for a list."
  [list-cursor entity-store-cursor id-fn]
  (aj/GET (:list/next list-cursor)
          {:error-handler on-ajax-error
           :handler (fn [next-list]
                      (om/transact! entity-store-cursor
                                    #(into % (map (juxt id-fn identity) (:list/items next-list))))
                      (om/transact! list-cursor
                                    (fn [old-items]
                                      (update-in next-list
                                                [:list/items]
                                                #(into (:list/items old-items) (map id-fn %))))))}))

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
  [{:keys [url width height]}]
  (dom/img {:class "thumbnail" :src url :width width :height height}))



(defcomponentk channel-list-item-view
  "A channel, as represented in the subscription list."
  [[:data {selected? false}
          [:youtube.channel/id :as id]
          [:youtube.channel/snippet.title :as title]
          [:youtube.channel/snippet.thumbnails :as thumbnails]]
   [:shared channels]]
  (render [_]
    (dom/li {:class (when selected? "selected")
             :on-click #(put! (:select channels) id)}
            (thumbnail (:default thumbnails))
            (dom/span {:class "title"} title))))

(defcomponentk video-list-item-view
  [[:data [:youtube.video/snippet.title :as title]
          [:youtube.video/snippet.description :as description]
          [:youtube.video/snippet.thumbnails :as thumbnails]
          :as video]
   [:shared channels]]
  (render [_]
    (dom/li {:on-click #(put! (:watching channels) video)}
      (dom/article {:class (dom/class-set {"video"   true
                                           "watched" (:subbox.video/watched video)})}
        (thumbnail (:medium thumbnails))
        (dom/div {:class "info"}
          (dom/h1 {:class "title"} title)
          (dom/div {:class "description"} (paragraphs description)))))))

(defcomponentk main-view
  "The main area of the page, including the list of videos."
  [[:data [:channel [:youtube.channel/snippet.title :as title]
                    videos]
          entities]]
   (render [_]
     (when-not (seq (:list/items videos))
       (fetch! videos (:videos entities) :youtube.video/id))
     (dom/section {:class "main"}
       (dom/h1 title)
       (dom/ul {:class "videos"}
         (om/build-all video-list-item-view (mapv (:videos entities) (:list/items videos)))))))


(defcomponentk player-view
  [[:data [:youtube.video/id :as id]
          :as video]
   [:shared channels]
   owner]
  ;; Also did-update
  (did-mount [_]
    (player/player (om/get-node owner) id
                   :player-vars {:autoplay 1}
                   :on-state-change (fn [event-type]
                                      (when (= :ended event-type)
                                        (put! (:watched channels) video)))))
  (render [_]
    (dom/div nil)))


(defcomponentk watch-screen-view
  [[:data [:youtube.video/snippet.title :as title]
          :as video]
   [:shared channels]]
  (render [_]
          (dom/div {:class "watch-screen"
                    :on-click #(when (direct-event? %) (put! (:watching channels) :none))}
                   (->player-view video))))


(defcomponentk app-view
  "The entire application."
  [[:data selected-channel-id
          watching-video
          entities
          subscriptions :as app]
   [:shared channels]]

  (will-mount [_]
    (go-loop []
      (let [new-selected-channel-id (<! (:select channels))]
        (om/update! app :selected-channel-id new-selected-channel-id)
        (recur)))

    (go-loop []
      (let [new-watching-video (<! (:watching channels))]
        (om/update! app :watching-video (when (not= :none new-watching-video) new-watching-video))
        (recur)))

    (go-loop []
      (let [video (<! (:watched channels))]
        (om/update! video :subbox.video/watched true)
        (recur))))

  (render [_]
    (when-not (seq (:list/items subscriptions))
      (fetch! subscriptions (:channels entities) :youtube.channel/id))

    (let [selected-channel ((:channels entities) selected-channel-id)
          channels (map (:channels entities) (:list/items subscriptions))
          channels-with-selected (mapv #(assoc % :selected? (= % selected-channel)) channels)]
      (dom/div {:class "app"}
        (when watching-video
          (->watch-screen-view watching-video))
        (dom/ul {:class "subscriptions"}
                (om/build-all channel-list-item-view
                              channels-with-selected))
        (when selected-channel
          (->main-view {:channel selected-channel :entities entities}))))))


(om/root app-view app-state
  {:target (. js/document (getElementById "app"))
   :shared {:channels {:select   (chan)
                       :watching (chan)
                       :watched  (chan)}}})
