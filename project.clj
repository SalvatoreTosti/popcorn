(defproject manager "0.1.0-SNAPSHOT"
  :description "A basic movie store database manager, created for CS596 at SDSU."
  :url "http://example.com/FIXME"
  :license {:name "Artistic License 2.0"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [seesaw "1.4.4"]
                 [clj-time "0.8.0"]
                 [me.raynes/fs "1.4.6"]]
  :main ^:skip-aot manager.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
