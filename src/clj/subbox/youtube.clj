(ns subbox.youtube
  (:require [clojure.walk :refer [prewalk keywordize-keys]])
  (:import (com.google.api.client.auth.oauth2 BearerToken Credential)
           (com.google.api.client.http.javanet NetHttpTransport)
           (com.google.api.client.json.jackson2 JacksonFactory)
           (com.google.api.services.youtube YouTube$Builder)))

(defn api [app-name token]
   (let [http-transport      (NetHttpTransport.)
         json-factory        (JacksonFactory.)
         request-initializer (-> (Credential. (BearerToken/authorizationHeaderAccessMethod))
                                 (.setAccessToken token))]
     (-> (YouTube$Builder. http-transport json-factory request-initializer)
         (.setApplicationName app-name)
         (.build))))

(defprotocol AsPersistent
  (as-persistent [o]))

(extend-protocol AsPersistent
  java.util.AbstractMap
  (as-persistent [m] (into {} m))
  java.util.ArrayList
  (as-persistent [al]
    (into [] al))
  java.lang.Object
  (as-persistent [o] o))

(defn persistentize
  [form]
  (prewalk as-persistent form))

(defn my-subscriptions
  "Fetches the subscriptions of the authenticated user."
  [api]
  (loop [items-so-far []
         page-token nil]
    (let [response
          (-> api
              .subscriptions
              (.list "snippet")
              (.setMine true)
              (.setOrder "alphabetical")
              (.setMaxResults 50)
              (.setPageToken page-token)
              .execute
              persistentize
              keywordize-keys)
          next-page-token (:nextPageToken response)
          page-items      (:items response)
          items           (concat items-so-far page-items)]
      (if next-page-token
        (recur items next-page-token)
        items))))

(defn channel
  "Fetches the specified channel."
  [api channel-id]
  (-> api
      .channels
      (.list "contentDetails")
      (.setId channel-id)
      .execute
      persistentize
      keywordize-keys
      :items
      first))

(defn playlist-items-in-playlist
  "Fetches the videos (playlistItems) in the specified playlist."
  [api playlist-id]
  (loop [items-so-far []
         page-token nil]
    (let [response
          (-> api
              .playlistItems
              (.list "snippet")
              (.setPlaylistId playlist-id)
              (.setMaxResults 50)
              (.setPageToken page-token)
              .execute
              persistentize
              keywordize-keys)
          next-page-token (:nextPageToken response)
          page-items      (:items response)
          items           (concat items-so-far page-items)]

      items ; For now, just the first page, always.
      #_(if next-page-token
        (recur items next-page-token)
        items))))

(defn playlist-items-in-channel
  "Fetches the videos (playlistItems) in the specified channel."
  [api channel-id]
  (-> api
      (channel channel-id)
      (get-in [:contentDetails :relatedPlaylists :uploads])
      (->> (playlist-items-in-playlist api))))
