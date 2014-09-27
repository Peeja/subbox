(defproject subbox "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2311"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [cljs-ajax "0.3.0"]
                 [com.cemerick/friend "0.2.1" :exclusions [org.clojure/core.cache]]
                 [com.cognitect/transit-clj "0.8.259"]
                 [com.cognitect/transit-cljs "0.8.188"]
                 [com.google.api-client/google-api-client "1.19.0"]
                 [com.google.apis/google-api-services-youtube "v3-rev114-1.19.0"]
                 [compojure "1.1.8"]
                 [enlive "1.1.5"]
                 [environ "1.0.0"]
                 [figwheel "0.1.4-SNAPSHOT"]
                 [friend-oauth2 "0.1.1"]
                 [hiccup "1.0.5"]
                 [om "0.7.3"]
                 [prismatic/om-tools "0.3.3"]
                 [prismatic/schema "0.3.0"]
                 [ring "1.2.2"]
                 [ring-transit "0.1.2"]]

  :plugins [[com.cemerick/austin "0.1.5-SNAPSHOT" :exclusions [org.clojure/clojure]]
            [lein-cljsbuild "1.0.3"]
            [lein-environ "1.0.0"]

            ;; https://github.com/noprompt/lein-garden/pull/23
            #_[lein-garden "0.2.1"]
            [org.clojars.peeja/lein-garden "0.2.2-SNAPSHOT"]]

  :min-lein-version "2.0.0"

  :hooks [leiningen.cljsbuild]

  :uberjar-name "subbox.jar"

  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:output-to     "resources/public/js/app.js"
                                   :output-dir    "resources/public/js/out"
                                   :optimizations :none
                                   :pretty-print  true
                                   :source-map    true}}]}

  :garden {:builds [{:id "screen"
                     :source-paths ["src/styles"]
                     :stylesheet subbox.styles/screen
                     :compiler {:output-to "resources/public/css/screen.css"
                                :pretty-print? false}}]}

  :profiles {:dev {:repl-options {:init-ns subbox.dev}
                   :plugins [[lein-figwheel "0.1.4-SNAPSHOT"]
                             [lein-pdo "0.1.1"]]
                   :figwheel {:http-server-root "public"
                              :port 3449
                              :css-dirs ["resources/public/css"]}
                   :aliases {"go" ["with-profile" "+go" "pdo" "repl" ":headless,"
                                                              "figwheel,"
                                                              "garden" "auto"]}}

             :go {:repl-options {:init (run)}}

             :production {:env {:production true}}

             :uberjar {:hooks [leiningen.garden]}

             ;; Define :local in profiles.clj as needed.
             :default [:base :system :user :provided :dev :local]
             :local {}})
