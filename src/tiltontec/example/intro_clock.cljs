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
     :onclick #(let [me (evt-md %)                          ; evt-md derives the MX model from the event; we then navigate
                     the-clock (fmu :the-clock me)]             ; the family up from me (fmu) to find the model named :the-clock
                 (mset! the-clock :now (js/Date.)))}            ; and reset its property :now, propagating fully to the DAG
    "Refresh"))                                             ; before returning.

(defn manual-clock []
  (div {:class [:intro :ticktock]}
    (h2 "The time is now....")
    (div {:class   "intro-clock"
          :content (cF (if-let [now (mget me :now)]         ;; mget, the standard MX getter, can be used from any code,
                         (-> now .toTimeString              ;; but transparently establishes a dependency, or "subscribes",
                           (str/split " ") first)           ;; if called within a formula.
                         "---"))}
      {:name :the-clock
       :now  (cI nil)})                                     ;; cI for "cell Input"; procedural code can write to these
    (refresh-button)))

(defn start-stop-button []
  (button
    {:class   :pushbutton
     :onclick #(let [me (evt-md %)
                     the-clock (fmu :the-clock me)]
                 (mswap! the-clock :ticking? not))}
    (if (mget (fmu :the-clock me) :ticking?)
      "Stop" "Start")))

(defn running-clock []
  (div {:class [:intro :ticktock]}
    (h2 "The time is now....")
    (div {:class   "intro-clock"
          :content (cF (if-let [now (mget me :now)]
                         (-> now .toTimeString (str/split " ") first)
                         "---"))}
      {:name :the-clock
       :now  (cI nil)
       :ticking? (cI true)
       :ticker (cF+ [:watch (fn [_ _ _ prior-value _]
                              (when (integer? prior-value)
                                (js/clearInterval prior-value)))]
                 (when (mget me :ticking?)
                   (js/setInterval #(mset! me :now (js/Date.)) 1000)))})
    (start-stop-button)))

(exu/main #(md/make ::intro
             :mx-dom (running-clock)))

