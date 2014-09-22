(defproject subbox "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2311"]
                 [cljs-ajax "0.2.6"]
                 [com.cemerick/friend "0.2.1"]
                 [com.cognitect/transit-cljs "0.8.188"]
                 [com.google.api-client/google-api-client "1.19.0"]
                 [com.google.apis/google-api-services-youtube "v3-rev114-1.19.0"]
                 [compojure "1.1.8"]
                 [enlive "1.1.5"]
                 [environ "1.0.0"]
                 [figwheel "0.1.3-SNAPSHOT"]
                 [friend-oauth2 "0.1.1"]
                 [hiccup "1.0.5"]
                 [om "0.7.3"]
                 [prismatic/om-tools "0.3.2"]
                 [ring "1.2.2"]
                 [ring-transit "0.1.2"]]

  :plugins [[com.cemerick/austin "0.1.5-SNAPSHOT"]
            [lein-cljsbuild "1.0.3"]
            [lein-environ "1.0.0"]]

  :min-lein-version "2.0.0"

  :hooks [leiningen.cljsbuild]

  :uberjar-name "subbox.jar"

  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:output-to     "resources/public/app.js"
                                   :output-dir    "resources/public/out"
                                   :optimizations :none
                                   :pretty-print  true
                                   :source-map    true}}]}

  :profiles {:dev {:repl-options {:init-ns subbox.server}
                   :plugins [[lein-figwheel "0.1.3-SNAPSHOT"]
                             [lein-pdo "0.1.1"]]
                   :figwheel {:http-server-root "public"
                              :port 3449 }
                   :aliases {"go" ["with-profile" "+go" "pdo" "repl" ":headless," "figwheel"]}}

             :go {:repl-options {:init (run)}}

             :production {:env {:production true}}

             ;; Define :local in profiles.clj as needed.
             :default [:base :system :user :provided :dev :local]
             :local {}})
