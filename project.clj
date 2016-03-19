(defproject sphex-machine "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/dmatysiak/sphex-machine.git"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.3"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 ;; Statistics
                 [incanter "1.5.7"]
                 ;; Web app
                 [ring "1.4.0"]
                 [compojure "1.4.0"]]
  :main ^:skip-aot sphex-machine.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
