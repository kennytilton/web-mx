(defproject com.tiltontec/web-mx "2.0.1"
  :description "A Web Un-Framework, powered by Matrix(tm)"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :url "https://github.com/kennytilton/web-mx/"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.11.60"]
                 [org.slf4j/slf4j-nop       "1.7.30"]
                 [org.clojure/core.async "1.6.673"]
                 [com.tiltontec/matrix "5.0.0"]]
  :profiles
  {:dev
   {:dependencies [[org.clojure/clojurescript "1.11.60"]
                   [com.bhauman/figwheel-main "0.2.18"]]
    :resource-paths ["target"]
    :clean-targets ^{:protect false} ["target"]}}
  :aliases {"fig" ["trampoline" "run" "-m" "figwheel.main"]}

  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :source-paths ["src" "target/classes"]
  :clean-targets ["out" "release" :target-path]
  :target-path "target")