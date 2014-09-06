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

(defn my-subscriptions [api]
  "Fetches the subscriptions of the authenticated user."
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
