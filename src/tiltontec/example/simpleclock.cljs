(ns tiltontec.example.simpleclock
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [tiltontec.cell.core :refer [cF cF+ cFonce cI]]
            [tiltontec.model.core
             :refer [mx-par mget mset! mswap! mset! mxi-find mxu-find-name fmu] :as md]
            [tiltontec.web-mx.gen :refer [evt-md target-value]]
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
                       ;; Next, mget transparently subscribes to the value of
                       ;; the widget named :timecolor.
                       (mget (mxu-find-name me :timecolor) :value)))
        :content (cF (if-let [tick (mget me :tick)]
                       ;; Reading :tick via mget also transparently subscribes
                       ;; so each time the interval mset!'s :tick, this content gets rebuilt.
                       (-> tick
                         .toTimeString
                         (str/split " ")
                         first)
                       "*checking*"))}
    {:tick
     ;; we /could/ initialize the tick to (js/Date.), but this lets us demonstrate
     ;; how to MX handles async naturally. Note the clock 'content' above. It simply needs
     ;; to be aware that 'tick' may be pending an async arrival of data.
     (cI nil)

     :ticker (cF+ [:watch (fn [_ _ new prior-value _]
                          (prn :obs!!!!!! new)
                          (when (integer? prior-value)
                            (js/clearInterval prior-value)))]
               (js/setInterval
                 ;; nice unexpected benefit of a system that manages state change
                 ;; automatically is that asynch is no problem; just have a normal
                 ;; input variable where the asynch result can be mset!
                 #(mset! me :tick (js/Date.))
                 1000))}))

(defn color-input [initial-color]
  (div {:class "color-input"} {:name :color-inpt}
    "Hex Time Color #&nbsp"
    (input {:tag/type  "text"
            ;; Below we bind an input cell (cI) to the value so it can be changed by the user.
            ;; Without that, we would get a runtime error when we try to change it.
            ;; Values that can be changed from outside the system must be flagged as such
            ;; so that MX knows to record them as dependencies when read by a formula.
            :value     (cI initial-color)
            :autofocus true
            :oninput   #(mset! (evt-md %)
                          ;; ^^ web/mx can navigate from the event to the model.
                          ;; mset! changes the value and before returning propagates
                          ;; the change to all dependencies direct or indirect
                          :value (target-value %))
            :title     "RGB color in hex format, either XXX or XXXXXX, without the octothorpe."
            :style     (cF (make-css-inline me
                             ;; we pass "me", the input model, to the maker so the CSS model
                             ;; knows which tag (element) it will be styling.
                             :width "100%" :max-width "90px" :padding "2px 6px"
                             :border :solid :border-width :thin
                             :background (cF (let [inpt (mget me :tag)
                                                   rgb-status (mget inpt :rgb-status)]
                                               ;; now the styling can reflect the correctness
                                               ;; of the user-entered hex RGB value:
                                               ;; when this "background" value changes, web/mx
                                               ;; does a goog.style setStyle on the style background
                                               (case rgb-status
                                                 :blank "#FFFFCC"
                                                 :invalid "#fcc" ;; light pink
                                                 "white")))))}
      {:name       :timecolor
       :rgb-status (cF (let [rgb (mget me :value)]
                         ;; simply reading (via mget) the :value property establishes a dependency,
                         ;; meaning this evaluation will run each time the value changes.
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

