(ns subbox.app
  (:require [cemerick.friend :as friend]
            [clojure.java.io :as io]
            [compojure.core :refer [ANY GET defroutes]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [crypto.random :as random]
            [environ.core :refer [env]]
            [friend-oauth2.util :refer [format-config-uri]]
            [friend-oauth2.workflow :as oauth2]
            [hiccup.page :as h]
            [net.cgrand.enlive-html :as enlive]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.middleware.transit :refer [wrap-transit-response]]
            [ring.util.response :as resp]
            [subbox.youtube :as yt]))

(def
  ^{:dynamic true
    :doc "An Enlive transformation function which will be applied to the body
         of the main app page. (Used for development tricks.)"}
  *body-transform*
  identity)

(enlive/deftemplate page (io/resource "index.html") []
  [:body] *body-transform*)

(defn login-prompt []
  (h/html5
    [:head
     [:title "The Sub Box"]]
    [:body
     [:a {:href "/login"} "Login with Google"]]))

(defn or-login-prompt [handler req]
  (if-let [identity (friend/identity req)]
    (handler)
    (login-prompt)))

(def ^:private yt-api
  (partial yt/api "subbox"))

(defn subscriptions [token]
  (->> (yt/my-subscriptions (yt-api token))
       (map (fn [subscription]
              {:youtube.channel/snippet.title (get-in subscription ["snippet" "title"])}))))


(defroutes app-routes
  (GET "/" req (or-login-prompt page req))

  (wrap-transit-response
    (GET "/subscriptions" req
      (if-let [identity (friend/identity req)]
        (subscriptions (get-in identity [:current :access-token]))
        {:status 401 :body "Unauthorized"})))

  (friend/logout (ANY "/logout" request (resp/redirect "/")))

  (route/resources "/")
  (route/not-found "Not Found"))


(defn credential-fn
  [token]
  {:identity token :roles #{::user}})

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
   :workflows   [(oauth2/workflow
                   {:client-config client-config
                    :uri-config    uri-config
                    :credential-fn credential-fn})]})

(defonce secret-token
  (random/bytes 16))

(def app
  (-> app-routes
      (friend/authenticate friend-config)
      (handler/site {:session {:store (cookie-store {:key secret-token})}})))