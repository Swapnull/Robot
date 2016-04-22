(defproject robot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main ^:skip-aot robot.core
  :plugins [[io.aviso/pretty "0.1.26"]]
  :dependencies [[org.clojure/clojure "1.7.0"] 
  				 [org.clojure/math.combinatorics "0.1.1"]
  				 [io.aviso/pretty "0.1.26"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
