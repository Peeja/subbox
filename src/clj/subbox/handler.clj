(ns subbox.handler
  (:require [cemerick.friend :as friend]
            [compojure.core :refer [GET defroutes]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [friend-oauth2.util :refer [format-config-uri]]
            [friend-oauth2.workflow :as oauth2]
            [hiccup.page :as h]
            [ring.util.response :refer [file-response]]
            [subbox.youtube :as yt]))

(def ^:private yt-api
  (partial yt/api "subbox"))

(defn subscriptions [token]
  (->> (yt/my-subscriptions (yt-api token))
       (map #(get-in % ["snippet" "title"]))))

(defn logged-in [identity]
  [:p "Logged in as " [:strong "???" #_(get-github-handle (:current identity))]
   " with Google identity" (:current identity)]
  [:h1 "Subscriptions"]
  [:ul
   (map #(vector :li %) (subscriptions (get-in identity [:current :access-token])))])


(defn login-prompt [req]
  (h/html5
    [:head
     [:title "The Sub Box"]]
    [:body
     [:a {:href "/login"} "Login with Google"]]))

(defn index [req]
  (if-let [identity (friend/identity req)]
    (assoc-in (file-response "app.html" {:root "resources"})
              [:headers "Content-Type"]
              "text/html")
    (login-prompt req)))



(defroutes app-routes
  (GET "/" req (index req))
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
