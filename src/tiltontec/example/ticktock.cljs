(ns tiltontec.example.ticktock
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [goog.dom :as gdom]
            [tiltontec.cell.core :refer-macros [cF cFonce] :refer [cI]]
            [tiltontec.model.core
             :refer [the-kids matrix mx-par mget mget mset! mswap! mset! mxi-find mxu-find-name fmu] :as md]
            [tiltontec.web-mx.gen :refer [evt-mx target-value]]
            [tiltontec.web-mx.gen-macro
             :refer-macros [img section header h1 h2 h3 input footer p a
                            pre code span i label ul li div button br
                            svg g circle p span div text radialGradient defs stop
                            rect ellipse line polyline path polygon script use]]

            [tiltontec.web-mx.html :refer [tag-dom-create]]
            [tiltontec.example.util :as ex-util]))

;;; -------------------------------------------------------

(defn lawrence-welk [beats]
  (mapv #(p (str "...ah, " % "..."))
    (mapv (fn [bn] (pp/cl-format nil "~r" (inc bn)))
      (range beats))))

(defn time-color-value [me]
  (if-let [tc (mxu-find-name me :timecolor)]
    (mget tc :value)
    (throw (js/Error "time-color-value> Unable to find widget named :timecolor"))))

(defn clock []
  (div {:class   "example-clock"
        :style   (cF (str "color:#"
                       (time-color-value me)))
        :content (cF (if-let [tick (mget me :tick)]
                       (-> tick
                         .toTimeString
                         (str/split " ")
                         first)
                       "*checking*"))}
    {:tick   (cI nil)
     :ticker (cF (js/setInterval #(mset! me :tick (js/Date.)) 1000))}))

(defn color-input [initial-color]
  (div {:class "color-input"}
    "Hex Time Color #&nbsp"
    (input {:value    (cI initial-color)
            :tag/type "text"
            :title    "RGB color in hex format, either XXX or XXXXXX"
            :style    {:background "white"
                       :width      "100%"
                       :padding    "0px 6px"
                       :max-width  "96px"}
            :onchange #(mset! (evt-mx %) :value
                         (target-value %))}
      {:name :timecolor})))

(defn matrix-build! []
  (reset! matrix
    (md/make ::ticktock
      :mx-dom (cFonce (div {:class "ticktock"}
                        {:name    :app
                         :ticking (cI false)}
                        (h1 "Hello, world.")
                        (when false (h2 "Hi, Mom!"))
                        (when (mget me :ticking)
                          [(h2 "The time is now....")
                           (lawrence-welk 2)
                          (clock)])
                        (color-input "f0f")
                        (button
                          {:onclick #(mswap! (fmu :app (evt-mx %)) :ticking not)}
                          (if (mget (mx-par me) :ticking)
                             "Stop" "Start")))))))

(defn main []
  (ex-util/main matrix-build!))

(main)