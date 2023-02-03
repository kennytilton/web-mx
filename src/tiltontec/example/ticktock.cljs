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

            [tiltontec.web-mx.html :refer [tag-dom-create]]))

;;; -------------------------------------------------------

(defn clock []
  (div {:class   "example-clock"
        :style   (cF (str "color:#" (mget (mxu-find-name me :timecolor) :value)))
        :content (cF (if (mget me :tick)
                       (-> (js/Date.)
                         .toTimeString
                         (str/split " ")
                         first)
                       "*checks watch*"))}
    {:tick   (cI false :ephemeral? true)
     :ticker (cF (js/setInterval #(mset! me :tick true) 1000))}))

(defn color-input []
  (div {:class "color-input"}
    "Hex Time Color #"
    (input {:style    {:width     "100%"
                       :padding   "3px 6px"
                       :max-width "48px"}
            :value    (cI "0FF")
            :onchange #(mset! (evt-mx %)
                         :value (target-value %))}
      {:name     :timecolor
       :tag/type "text"})))

(defn matrix-build! []
  (reset! matrix
    (md/make ::ticktock
      :mx-dom (cFonce (md/with-par me
                        [(div {}
                           (h1 {} "Hello, world. 'Tis now....")
                           (clock)
                           (color-input))])))))



(defn main []
  (println "[main]: loading")
  (let [root (gdom/getElement "app")                        ;; must be defined in index.html
        app-matrix (matrix-build!)
        app-dom (tag-dom-create
                  (mget app-matrix :mx-dom))]
    (set! (.-innerHTML root) nil)
    (gdom/appendChild root app-dom)))

(main)