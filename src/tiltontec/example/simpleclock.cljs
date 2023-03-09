(ns tiltontec.example.simpleclock
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [tiltontec.cell.core :refer [cF cF+ cFonce cI]]
            [tiltontec.model.core
             :refer [mget mset! mswap! mset! mxi-find mxu-find-name fmu] :as md]
            [tiltontec.web-mx.gen :refer [evt-md target-value]]
            [tiltontec.web-mx.gen-macro
             :refer [img section h1 h2 h3 input footer p a
                     span i label ul li div button br
                     svg g circle p span div]]
            [tiltontec.web-mx.style :refer [make-css-inline]]
            [tiltontec.example.util :as ex-util]))

;;; --- Simple Clock ----------------------------------------------------

(defn clock []
  (div {:class   "example-clock"
        :style   (cF (str "color:#"
                       ;; Next, mget transparently subscribes to the value of
                       ;; the widget named :timecolor.
                       (mget (mxu-find-name me :timecolor) :value)))
        :content (cF (if-let [tick (mget me :now)]
                       ;; Reading :now via mget also transparently subscribes
                       ;; so each time the interval mset!'s :now, this content gets rebuilt.
                       (-> tick
                         .toTimeString
                         (str/split " ")
                         first)
                       "*checking*"))}
    {:now
     ;; we /could/ initialize :now to (js/Date.), but this lets us demonstrate
     ;; how MX code handles async:
     ;;   -- create a cInput (cI) variable to receive the async response whenever it lands;
     ;;   -- have formulas that read the input var be prepared for "nothing yet"; and
     ;;   -- have the asynch handler write to the cInput var.
     ;;  MX mset! internals then bring the rest of the app current with the new change, before returning.
     ;;
     (cI nil)

     :ticker (cF+ [:watch (fn [prop-name me new-value prior-value cell]
                            ;; -- any cell can have an "on-change" callback we call "observers" or "watches".
                            (when (integer? prior-value)
                              (js/clearInterval prior-value)))]
               (js/setInterval
                 ;; nice unexpected benefit of a system that manages state change
                 ;; automatically is that asynch is no problem; just have a normal
                 ;; input variable where the asynch result can be mset!
                 #(mset! me :now (js/Date.))
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
                (color-input "57a8a4")
                (div {:style   {:display        :flex
                                :flex-direction :row
                                :flex-wrap      :wrap
                                :background     :yellow
                                :max-width      "200px"}
                      :onclick (cF (fn []
                                     (mswap! me :kid-values conj (+ 10 (rand-int 500)))))}
                  {
                   :kid-values  (cI (range 10))
                   :kid-factory (fn [parent fake-id]
                                  (span {:style {:padding "4px"}}
                                    {:fake-id fake-id}
                                    (str fake-id)))
                   :kid-key     (fn [k]
                                  (:fake-id @k))}
                  (md/kid-values-kids me _cache))))))

(ex-util/main matrix-build!)

