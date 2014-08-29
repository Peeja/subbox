(ns subbox.youtube)

(defn api [api-key app-name]
  {:api-key api-key
   :youtube-api
   (let [http-transport      (com.google.api.client.http.javanet.NetHttpTransport.)
         json-factory        (com.google.api.client.json.jackson2.JacksonFactory.)
         request-initializer (reify
                               com.google.api.client.http.HttpRequestInitializer
                               (initialize [_ _] nil))]
     (-> (com.google.api.services.youtube.YouTube$Builder. http-transport json-factory request-initializer)
         (.setApplicationName app-name)
         (.build)))})

(defn username->cid [api username]
  "Fetches the channel ID for a YouTube username."
  (-> (:youtube-api api)
      .channels
      (.list "id")
      (.setKey (:api-key api))
      (.setForUsername username)
      (.setFields "items(id)")
      .execute
      (get "items")
      first
      (get "id")))

(defn subscriptions [api cid]
  "Fetches the subscriptions of the given channel (specified by channel ID)."
  (-> (:youtube-api api)
      .subscriptions
      (.list "snippet")
      (.setKey (:api-key api))
      (.setChannelId cid)
      (.setFields "items(snippet/title)")
      .execute
      (get "items")
      (->> (map #(get-in % ["snippet" "title"])))))
