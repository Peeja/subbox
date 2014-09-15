(defproject subbox "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [environ "1.0.0"]
                 [compojure "1.1.8"]
                 [com.cemerick/friend "0.2.1"]
                 [friend-oauth2 "0.1.1"]
                 [ring-transit "0.1.2"]
                 [hiccup "1.0.5"]
                 [enlive "1.1.5"]
                 [com.google.api-client/google-api-client "1.19.0"]
                 [com.google.apis/google-api-services-youtube "v3-rev114-1.19.0"]
                 [org.clojure/clojurescript "0.0-2311"]
                 [com.cognitect/transit-cljs "0.8.188"]
                 [cljs-ajax "0.2.6"]
                 [om "0.7.3"]
                 [prismatic/om-tools "0.3.2"]]

  :plugins [[lein-environ "1.0.0"]
            ; [lein-ring "0.8.11"]
            [dev.peeja/lein-ring "0.8.11-SNAPSHOT"] ; https://github.com/weavejester/lein-ring/pull/123
            [lein-pprint "1.1.1"]
            [lein-pdo "0.1.1"]
            [lein-cljsbuild "1.0.3"]]

  :ring {:handler subbox.dev/app
         :reload-paths ["src/clj" "src-dev/clj"]
         :nrepl   {:start? true
                   :middlewares [cemerick.piggieback/wrap-cljs-repl]}}

  :source-paths ["src/clj" "src-dev/clj" "src/cljs"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]

  :cljsbuild {
    :builds [{:source-paths ["src/cljs"]
              :compiler {
                :output-to "resources/public/js/main.js"
                :output-dir "resources/public/js/out"
                :optimizations :none
                :source-map true}}]}

  :profiles {:default [:base :system :user :provided :dev :local]
             :dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]
                   :plugins [[com.cemerick/austin "0.1.5"]]
                   :aliases {"go" ["pdo" "cljsbuild" "auto," "ring" "server-headless"]}}
             :local {#_(override this in profiles.clj as needed)}})
