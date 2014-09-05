(ns subbox.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [friend-oauth2.util :refer [format-config-uri]]
            [cemerick.friend :as friend]
            [friend-oauth2.workflow :as oauth2]
            [hiccup.page :as h]
            [subbox.youtube :as yt]))

(def ^:private yt-api
  (partial yt/api (env :google-api-key) "subbox"))

(defn subscriptions [token]
  (yt/my-subscriptions (yt-api token)))

(defn logged-in [identity]
  [:p "Logged in as " [:strong "???" #_(get-github-handle (:current identity))]
   " with Google identity" (:current identity)]
  [:h1 "Subscriptions"]
  [:ul
   (map #(vector :li %) (subscriptions (get-in identity [:current :access-token])))])


(defn front-page [req]
  (h/html5
    [:head
     [:title "The Sub Box"]]
    [:body
     (if-let [identity (friend/identity req)]
       (logged-in identity)
       [:h3 [:a {:href "/login"} "Login with Google"]])]))

(defroutes app-routes
  (GET "/" req (front-page req))
  (route/resources "/")
  (route/not-found "Not Found"))

(defn credential-fn
  [token]
  {:identity token})

(def client-config
  {:client-id (env :google-client-id)
   :client-secret (env :google-client-secret)
   :callback {:domain (env :subbox-url) :path "/oauth2callback"}})

(def uri-config
  {:authentication-uri {:url "https://accounts.google.com/o/oauth2/auth"
                        :query {:client_id (:client-id client-config)
                                :response_type "code"
                                :redirect_uri (format-config-uri client-config)
                                :scope "https://www.googleapis.com/auth/youtube.readonly"}}

   :access-token-uri {:url "https://accounts.google.com/o/oauth2/token"
                      :query {:client_id (:client-id client-config)
                              :client_secret (:client-secret client-config)
                              :grant_type "authorization_code"
                              :redirect_uri (format-config-uri client-config)}}})

(def friend-config
  {:allow-anon? true
   ; :login-uri "/login"
   :workflows   [(oauth2/workflow
                  {:client-config client-config
                   :uri-config    uri-config
                   :credential-fn credential-fn})]})


(def app
  (-> app-routes
      (friend/authenticate friend-config)
      handler/site))
