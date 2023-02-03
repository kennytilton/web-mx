(ns tiltontec.example.ticktock
  (:require [clojure.string :as str]
            [goog.dom :as gdom]
            [tiltontec.cell.core :refer-macros [cF cFonce] :refer [cI]]
            [tiltontec.model.core
             :refer [the-kids matrix mx-par mget mget mset! mset! mxi-find mxu-find-name fmu] :as md]
            [tiltontec.web-mx.gen :refer [evt-mx target-value]]
            [tiltontec.web-mx.gen-macro
             :refer-macros [img section header h1 input footer p a
                            pre code span i label ul li div button br
                            svg g circle p span div text radialGradient defs stop
                            rect ellipse line polyline path polygon script use]]

            [tiltontec.web-mx.html :refer [tag-dom-create]]
            [tiltontec.example.util :as ex-util]))

;;; -------------------------------------------------------

(defn clock []
  (div {:class   "example-clock"
        :style   (cF (str "color:#"
                       (mget (mxu-find-name me :timecolor) :value)))
        :content (cF (if-let [tick (mget me :tick)]
                       (-> tick
                         .toTimeString
                         (str/split " ")
                         first)
                       "*checks watch*"))}
    {:tick   (cI nil)
     :ticker (cF (js/setInterval #(mset! me :tick (js/Date.)) 1000))}))

(defn color-input []
  (div {:class "color-input"}
    "Hex Time Color #"
    (input {:value    (cI "0FF")
            :tag/type "text"
            :style    {:width           "100%"
                       :display         :flex
                       :align-items     :top
                       :justify-content :center
                       :padding         "3px 6px"
                       :max-width       "48px"}
            :onchange #(mset! (evt-mx %)
                         :value (target-value %))}
      {:name :timecolor})))

(defn matrix-build! []
  (reset! matrix
    (md/make ::ticktock
      :mx-dom (cFonce (md/with-par me
                        [(div {:style {:margin  "24px"
                                       :padding "1em"}}
                           (h1 {} "Hello, world. 'Tis now....")
                           (clock)
                           (color-input))])))))



(defn main []
  (ex-util/main matrix-build!))

(main)