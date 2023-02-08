(ns tiltontec.example.intro-clock
  (:require
    [clojure.string :as str]
    [tiltontec.cell.core :refer [cF cF+ cFonce cI]]
    [tiltontec.model.core
     :refer [mx-par mget mset! mswap! mset! mxi-find mxu-find-name fmu] :as md]
    [tiltontec.web-mx.gen :refer [evt-md target-value]]
    [tiltontec.web-mx.gen-macro
     :refer [img section h1 h2 h3 input footer p a
             span i label ul li div button br]]
    [tiltontec.example.util :as exu]))

;;; --- intro clock starter code -----------------------------------

(defn refresh-button []
  (button
    {:class   :pushbutton
     :onclick #(let [me (evt-md %)                          ; derive MX model from event; now we can search the whole MX
                     clock (fmu :the-clock me)]             ; navigate family up from me (fmu) to model named :the-clock
                 (mset! clock :now (js/Date.)))}            ; change the property :now of the clock and propagate fully
    "Refresh"))

(defn simple-clock []
  (div {:class [:intro :ticktock]}
    (h2 "The time is now....")
    (div {:class   "intro-clock"
          :content (cF (if-let [now (mget me :now)]
                         (-> now .toTimeString
                           (str/split " ") first)
                         "*checking*"))}
      {:name :the-clock
       :now  (cI nil)})                                     ;; cI for "cell Input"; procedural code can write to these
    (refresh-button)))

(defn matrix-build! []
  (md/make ::intro
    :mx-dom (simple-clock)))

(exu/main matrix-build!)

