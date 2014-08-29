(defproject subbox "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [environ "1.0.0"]
                 [compojure "1.1.8"]
                 [hiccup "1.0.5"]
                 [com.google.api-client/google-api-client "1.19.0"]
                 [com.google.apis/google-api-services-youtube "v3-rev114-1.19.0"]]
  :plugins [[lein-environ "1.0.0"]
            [lein-ring "0.8.11"]]
  :ring {:handler subbox.handler/app
         :auto-refresh? true
         :nrepl {:start? true}}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}})
