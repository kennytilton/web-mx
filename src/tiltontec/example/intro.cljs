(ns tiltontec.example.intro
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
            [tiltontec.example.util :as exu]))

;;; -------------------------------------------------------


(defn matrix-build! []
  (md/make ::intro
    :mx-dom (div
              (h2 "Nothing to see here.")
              (p "But feel free to hang out.")
              (img {:alt "Female professional rock climber hanging under climbing wall overhang."
                    :src "image/Boulder_Worldcup_Vienna_29-05-2010b_final10_Chloé_Graftiaux.jpg"
                    :max-width "100%"
                    :max-height "100%"
                    :height :auto}))))

(exu/main matrix-build!)

