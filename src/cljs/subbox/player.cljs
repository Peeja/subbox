(ns subbox.player
  (:require [goog.dom :as gdom]))

(defn enable!
  "Adds the YouTube iframe API script to the document, if it hasn't already
  been added. Call this before using other functions in this namespace."
  []
  (defonce ^:private iframe-api-script
    (let [script (gdom/createDom "script" #js {:src "https://www.youtube.com/iframe_api" :async true})]
      (gdom/appendChild (.-body js/document) script)
      script)))

(let [ready-state (atom false)]
  (set! js/onYouTubeIframeAPIReady #(reset! ready-state true))
  (defn ready?
    "Returns true iff the iframe API has loaded. Using the API before it's ready
    will throw an exception."
    []
    @ready-state))


(defn- player-state-name
  "Translates a player state integer into a corresponding keyword."
  [state]
  ({js/YT.PlayerState.UNSTARTED :unstarted
    js/YT.PlayerState.ENDED     :ended
    js/YT.PlayerState.PLAYING   :playing
    js/YT.PlayerState.PAUSED    :paused
    js/YT.PlayerState.BUFFERING :buffering
    js/YT.PlayerState.CUED      :cued}
   state))

(defn player
  "Returns a new YT.Player, built on the given DOM element."
  [element video-id & {:keys [player-vars on-state-change]}]
  (js/YT.Player. element (clj->js {:videoId video-id
                                   :playerVars (or player-vars {})
                                   :events (when on-state-change
                                             {:onStateChange (fn [event]
                                                (-> (.-data event)
                                                    player-state-name
                                                    on-state-change))})})))
