(ns subbox.youtube
  (:import (com.google.api.client.auth.oauth2 BearerToken Credential)
           (com.google.api.client.http.javanet NetHttpTransport)
           (com.google.api.client.json.jackson2 JacksonFactory)
           (com.google.api.services.youtube YouTube$Builder)))

(defn api [api-key app-name token]
  {:api-key api-key
   :youtube-api
   (let [http-transport      (NetHttpTransport.)
         json-factory        (JacksonFactory.)
         request-initializer (-> (Credential. (BearerToken/authorizationHeaderAccessMethod))
                                 (.setAccessToken token))]
     (-> (YouTube$Builder. http-transport json-factory request-initializer)
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

(defn my-subscriptions [api]
  "Fetches the subscriptions of the authenticated user."
  (-> (:youtube-api api)
      .subscriptions
      (.list "snippet")
      (.setKey (:api-key api))
      (.setMine true)
      (.setOrder "alphabetical")
      (.setMaxResults 50)
      (.setFields "items(snippet/title)")
      .execute
      (get "items")
      (->> (map #(get-in % ["snippet" "title"])))))
