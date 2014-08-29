(ns subbox.youtube
  (:require [environ.core :refer [env]]))

(def ^:private api-key (env :google-api-key))
(def ^:private app-name "subbox")

(def ^:private youtube
  (let [http-transport      (com.google.api.client.http.javanet.NetHttpTransport.)
        json-factory        (com.google.api.client.json.jackson2.JacksonFactory.)
        request-initializer (reify
                              com.google.api.client.http.HttpRequestInitializer
                              (initialize [_ _] nil))]
    (-> (com.google.api.services.youtube.YouTube$Builder. http-transport json-factory request-initializer)
        (.setApplicationName app-name)
        (.build))))

(defn username->cid [username]
  "Fetches the channel ID for a YouTube username."
  (-> youtube
      .channels
      (.list "id")
      (.setKey api-key)
      (.setForUsername username)
      (.setFields "items(id)")
      .execute
      (get "items")
      first
      (get "id")))

(defn subscriptions [cid]
  "Fetches the subscriptions of the given channel (specified by channel ID)."
  (-> youtube
      .subscriptions
      (.list "snippet")
      (.setKey api-key)
      (.setChannelId cid)
      (.setFields "items(snippet/title)")
      .execute
      (get "items")
      (->> (map #(get-in % ["snippet" "title"])))))
