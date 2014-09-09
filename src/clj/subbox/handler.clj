(ns subbox.handler
  (:require [environ.core :refer [env]]
            [ring.util.response :as resp]
            [ring.middleware.transit :refer [wrap-transit-response]]
            [compojure.core :refer [ANY GET defroutes]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [cemerick.friend :as friend]
            [friend-oauth2.util :refer [format-config-uri]]
            [friend-oauth2.workflow :as oauth2]
            [hiccup.page :as h]
            [subbox.youtube :as yt]))

(def ^:private yt-api
  (partial yt/api "subbox"))

(defn login-prompt []
  (h/html5
    [:head
     [:title "The Sub Box"]]
    [:body
     [:a {:href "/login"} "Login with Google"]]))

(defn index [req]
  (if-let [identity (friend/identity req)]
    (assoc-in (resp/file-response "app.html" {:root "resources"})
              [:headers "Content-Type"]
              "text/html")
    (login-prompt)))

(defn subscriptions [token]
  (->> (yt/my-subscriptions (yt-api token))
       (map #(get-in % ["snippet" "title"]))))



(defroutes app-routes
  (GET "/" req (index req))

  (GET "/subscriptions" req
    (if-let [identity (friend/identity req)]
      (subscriptions (get-in identity [:current :access-token]))
      {:status 401 :body "Unauthorized"}))

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
   ; :login-uri "/login"
   :workflows   [(oauth2/workflow
                  {:client-config client-config
                   :uri-config    uri-config
                   :credential-fn credential-fn})]})


(def app
  (-> app-routes
      wrap-transit-response
      (friend/authenticate friend-config)
      handler/site))
