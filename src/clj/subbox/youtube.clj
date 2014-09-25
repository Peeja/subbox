(ns subbox.youtube
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
              .execute)
          next-page-token (get response "nextPageToken")
          page-items      (get response "items")
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
      (get "items")
      first))

(defn videos-in-playlist
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
              .execute)
          next-page-token (get response "nextPageToken")
          page-items      (get response "items")
          items           (concat items-so-far page-items)]

      items ; For now, just the first page, always.
      #_(if next-page-token
        (recur items next-page-token)
        items))))

(defn videos-in-channel
  "Fetches the videos (playlistItems) in the specified channel."
  [api channel-id]
  (-> api
      (channel channel-id)
      (get-in ["contentDetails" "relatedPlaylists" "uploads"])
      (->> (videos-in-playlist api))))
