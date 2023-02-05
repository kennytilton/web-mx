(ns tiltontec.example.ticktock
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [tiltontec.cell.base :refer [md-ref? ia-type unbound minfo]]
            [tiltontec.cell.core :refer-macros [cF cFonce] :refer [cI]]
            [tiltontec.model.core
             :refer [mx-par mget mset! mswap! mset! mxi-find mxu-find-name fmu] :as md]
            [tiltontec.web-mx.gen :refer [evt-mx target-value]]
            [tiltontec.web-mx.html
             :refer [mxu-find-tag mxu-find-class]]
            [tiltontec.web-mx.gen-macro
             :refer [img section h1 h2 h3 input footer p a
                     pre code span i label ul li div button br
                     svg g circle p span div text radialGradient defs stop
                     rect ellipse line polyline path polygon script use]]
            [tiltontec.web-mx.style :refer [make-css-inline]]
            [tiltontec.example.util :as ex-util]))

;;; -------------------------------------------------------

(defn lawrence-welk [beats]
  ;; todo put on one line
  (div {:style {:display :flex}}
    (mapv #(p (str "ah, " % "..."))
      (mapv (fn [bn] (pp/cl-format nil "~r" (inc bn)))
        (range beats)))))

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
     ;; todo not-to-be handling
     :ticker (cF (js/setInterval #(mset! me :tick (js/Date.)) 1000))}))

(defn color-input [initial-color]
  (div {:class "color-input"}{:name :color-inpt}
    "Hex Time Color #&nbsp"
    (input {:value    (cI initial-color)
            :tag/type "text"
            :title    "RGB color in hex format, either XXX or XXXXXX"
            :style    (cF (make-css-inline me
                        :width "100%"
                        :padding "0px 6px"
                        :max-width "96px"
                        :font-size "24px"
                        :border :solid
                        :border-color (cF
                                        (prn :bcolor-sees (minfo me))
                                        (prn :bcolor-tag (minfo (mget me :tag)))
                                        (if (mget (:tag @me) :value-error)
                                            "red" "green"))
                        :display :block))
            #_{:background "white"
               :width      "100%"
               :padding    "0px 6px"
               :max-width  "96px"}
            :onchange #(mset! (evt-mx %) :value
                         (target-value %))}
      ;; todo add validation
      {:name        :timecolor
       :value-error true})))

(defn matrix-build! []
  (md/make ::ticktock
    :mx-dom (cFonce
              (div {:class "ticktock"}
                {:name    :app
                 :ticking (cI true)}
                (h1 "Hello, world.")
                (when false (h2 "Hi, Mom!"))
                (when (mget me :ticking)
                  [(h2 "The time is now....")
                   (lawrence-welk 2)
                   (clock)])
                (color-input "57a8a4")
                (button
                  {:class   "button-2"
                   :style   {:margin-top "16px"}
                   :onclick #(mswap! (fmu :app (evt-mx %)) :ticking not)}
                  (if (mget (fmu :app) :ticking)
                    "Stop" "Start"))))))

(ex-util/main matrix-build!)

#_#_(defn main []
      (ex-util/main matrix-build!))

        (main)