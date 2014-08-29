(ns subbox.handler
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.page :as h]
            [subbox.youtube :as yt]))

(defn subscriptions [username]
  (yt/subscriptions (yt/username->cid username)))

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
