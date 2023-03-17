(ns tiltontec.example.intro-clock
  (:require
    [clojure.string :as str]
    [tiltontec.matrix.api
     :refer [make cF cF+ cFn cFonce cI cf-freeze
             mpar mget mset! mswap! mset! with-cc
             fasc fmu fm! minfo]]
    [tiltontec.web-mx.api
     :refer [evt-md target-value img section h1 h2 h3 input footer p a
             span i label ul li div button code pre]]
    [tiltontec.example.util :as exu]))

;;; --- intro clock starter code -----------------------------------

(defn refresh-button []
  (button
    {:class   :pushbutton
     :onclick #(let [me (evt-md %)                          ; evt-md derives the MX model from the event; we then navigate
                     the-clock (fmu :the-clock me)]         ; the family up from me (fmu) to find the model named :the-clock
                 (mset! the-clock :now (js/Date.)))}        ; and reset its property :now, propagating fully to the DAG
    "Refresh"))                                             ; before returning.


(def manual-clock-code
  "(defn refresh-button []
  (button
    {:class   :pushbutton
     :onclick #(let [me (evt-md %)
                     the-clock (fmu :the-clock me)]
                 (mset! the-clock :now (js/Date.)))}
    \"Refresh\"))

(div {:class [:intro :ticktock]}
  (h2 \"The time is now....\")
  (div {:class   \"intro-clock\"
        :content (cF (if-let [now (mget me :now)]
                       (-> now .toTimeString
                           (str/split \" \") first)
                       \"---\"))}
       {:name :the-clock
        :now  (cI nil)})
  (refresh-button))")

(defn manual-clock []
  (div {:class [:intro :ticktock]}
    (h3 "The time is now....")
    (div {:class   "intro-clock"
          :content (cF (if-let [now (mget me :now)]         ;; mget, the standard MX getter, can be used from any code,
                         (-> now .toTimeString              ;; but transparently establishes a dependency, or "subscribes",
                           (str/split " ") first)           ;; if called within a formula.
                         "__:__:__"))}
      {:name :the-clock
       ;; Uncomment the next line to make the clock run
       ;; :ticker (cF (js/setInterval #(mset! me :now (js/Date.)) 1000))
       :now  (cI nil)})                                     ;; cI for "cell Input"; procedural code can write to these
    (refresh-button)))

;;; --- running clock example --------------------------------------------

(def running-clock-code
  "(div {:class [:intro :ticktock]}
    {:name :running-clock}
    (h3 \"The time is now....\")
    (div {:class   \"intro-clock\"
          :style   (cF (str \"background:black; color:\"
                         (if (mget me :ticking?) \"cyan\" \"red\")))
          :content (cF (if-let [now (mget me :now)]
                         (-> now .toTimeString (str/split \" \") first)
                         \"__:__:__\"))}
      {:name     :the-clock
       :now      (cI nil)
       :ticking? (cI false)
       :start    (cF (when (mget me :ticking?)
                       (js/Date.)))
       :elapsed  (cF+ [:watch (fn [_ me new-val _ _]
                                (when (and (mget me :ticking?)
                                        (> new-val 5000))
                                  (with-cc :stop-after-3
                                    (mset! me :ticking? false))))]
                   (when-let [start (mget me :start)]
                     (when (> (mget me :now) start)
                       (- (mget me :now) start))))
       :ticker   (cF+ [:watch (fn [_ _ new-value prior-value _]
                                (when (integer? prior-value)
                                  (js/clearInterval prior-value)))]
                   (when (mget me :ticking?)
                     (js/setInterval #(mset! me :now (js/Date.)) 1000)))})
    (start-stop-button))"
  #_
  "(defn start-stop-button []\n  (button\n    {:class   :pushbutton\n     :onclick #(mswap! (fmu :the-clock (evt-md %)) :ticking? not)}\n    (if (mget (fmu :the-clock me) :ticking?)\n      \"Stop\" \"Start\")))\n\n(defn running-clock []\n  (div {:class [:intro :ticktock]}\n    (h2 \"The time is now....\")\n    (div {:class   \"intro-clock\"\n          :style   (cF (str \"background:black; color:\"\n                         (if (mget me :ticking?) \"cyan\" \"red\")))\n          :content (cF (if-let [now (mget me :now)]\n                         (-> now .toTimeString (str/split \" \") first)\n                         \"__:__:__\"))}\n      {:name     :the-clock\n       :now      (cI nil)\n       :ticking? (cI false)\n       :ticker   (cF+ [:watch (fn [_ _ _ prior-value _]\n                                (when (integer? prior-value)\n                                  (js/clearInterval prior-value)))]\n                   (when (mget me :ticking?)\n                     (js/setInterval #(mset! me :now (js/Date.)) 1000)))})\n    (start-stop-button)))")

(defn start-stop-button []
  (button
    {:class   :pushbutton
     :onclick #(mswap! (fmu :the-clock (evt-md %)) :ticking? not)}
    (if (mget (fmu :the-clock me) :ticking?)
      "Stop" "Start")))

(defn running-clock []
  (div {:class [:intro :ticktock]}
    {:name :running-clock}
    (h3 "The time is now....")
    (div {:class   "intro-clock"
          :style   (cF (str "background:black; color:"
                         (if (mget me :ticking?) "cyan" "red")))
          :content (cF (if-let [now (mget me :now)]
                         (-> now .toTimeString (str/split " ") first)
                         "__:__:__"))}
      {:name     :the-clock
       :now      (cI nil)
       :ticking? (cI false)
       :start    (cF (when (mget me :ticking?)
                       (js/Date.)))
       :elapsed  (cF+ [:watch (fn [_ me new-val _ _]
                                (when (and (mget me :ticking?)
                                        (> new-val 5000))
                                  (with-cc :stop-after-3
                                    (mset! me :ticking? false))))]
                   (when-let [start (mget me :start)]
                     (when (> (mget me :now) start)
                       (- (mget me :now) start))))
       :ticker   (cF+ [:watch (fn [_ _ new-value prior-value _]
                                (when (integer? prior-value)
                                  (js/clearInterval prior-value)))]
                   (when (mget me :ticking?)
                     (js/setInterval #(mset! me :now (js/Date.)) 1000)))})
    (start-stop-button)))

(exu/main #(make ::intro
             :mx-dom (exu/multi-demo "Clock Demos" 0
                       {:title "Manual Clock" :builder manual-clock :code manual-clock-code}
                       {:title "Running Clock" :builder running-clock :code running-clock-code})))

#_(defn stop-watch []
    (div {:class [:intro :ticktock]}
      (h2 "On your mark..get set...")
      (nyc-std-clock 100)
      (button
        {:class   :pushbutton
         :onclick #(let [clk (fmu :the-clock (evt-md %))]
                     (mswap! clk :ticking? not)
                     (mset! clk :start (js/Date.)))}
        (if (mget (fmu :the-clock me) :ticking?)
          "Stop" "Start"))))



