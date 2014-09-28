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

(def ^private ready-state (atom false))
(set! js/onYouTubeIframeAPIReady #(reset! ready-state true))

(defn ready?
  "Returns true iff the iframe API has loaded. Using the API before it's ready
  will rais an exception."
  []
  @ready-state)

(defn player
  "Returns a new YT.Player, built on the given DOM element."
  [element video-id]
  (js/YT.Player. element #js {:videoId video-id}))
