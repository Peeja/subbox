(ns subbox.handler
  (:require [environ.core :refer [env]]
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.page :as h]
            [subbox.youtube :as yt]))

(def ^:private yt-api
  (yt/api (env :google-api-key) "subbox"))

(defn subscriptions [username]
  (yt/subscriptions yt-api (yt/username->cid yt-api username)))

(defn front-page []
  (h/html5
    [:head
     [:title "The Sub Box"]]
    [:body
     [:h1 "Subscriptions"]
     [:ul
      (map #(vector :li %) (subscriptions "peeja"))]]))


(defroutes app-routes
  (GET "/" [] (front-page))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
