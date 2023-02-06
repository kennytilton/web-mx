(ns tiltontec.example.simpleclock
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [tiltontec.cell.core :refer-macros [cF cFonce] :refer [cI]]
            [tiltontec.model.core
             :refer [mx-par mget mset! mswap! mset! mxi-find mxu-find-name fmu] :as md]
            [tiltontec.web-mx.gen :refer [evt-mx target-value]]
            [tiltontec.web-mx.gen-macro
             :refer [img section h1 h2 h3 input footer p a
                     span i label ul li div button br
                     svg g circle p span div]]
            [tiltontec.web-mx.style :refer [make-css-inline]]
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
                       "*checking*"))}
    {:tick   (cI nil)
     :ticker (cF (js/setInterval #(mset! me :tick (js/Date.)) 1000))}))

(defn color-input [initial-color]
  (div {:class "color-input"} {:name :color-inpt}
    "Hex Time Color #&nbsp"
    (input {:tag/type "text"
            :value    (cI initial-color)
            :autofocus true
            :oninput #(mset! (evt-mx %) :value (target-value %))
            :title    "RGB color in hex format, either XXX or XXXXXX, without the octothorpe."
            :style    (cF (make-css-inline me
                            :width "100%"
                            :max-width "90px"
                            :padding "2px 6px"
                            :border :solid :border-width :thin
                            :background-color (cF (let [rgb-status (mget (mget me :tag) :rgb-status)]
                                                    (case rgb-status
                                                      :invalid "#fcc"
                                                      "white")))))}
      {:name         :timecolor
       :rgb-status (cF (let [rgb (mget me :value)]
                           (cond
                             (str/blank? rgb) :blank
                             (not-any? #{(count rgb)} [3 6]) :invalid
                             (re-matches #"[0-9a-fA-F]+" rgb) :valid
                             :else :invalid)))})))

(defn matrix-build! []
  (md/make ::simpleclock
    :mx-dom (cFonce
              (div {:class "ticktock"}
                (h2 "The time is now....")
                (clock)
                (color-input "57a8a4")))))

(ex-util/main matrix-build!)

