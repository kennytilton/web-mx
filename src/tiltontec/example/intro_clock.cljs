(ns tiltontec.example.intro-clock
  (:require
    [clojure.string :as str]
    [clojure.pprint :as pp]
    [tiltontec.cell.base :refer [minfo]]
    [tiltontec.cell.core :refer [cF cF+ cFonce cFn cI]]
    [tiltontec.cell.integrity :refer [with-cc]]
    [tiltontec.model.core
     :refer [mx-par mget mset! mswap! mset! mxi-find mxu-find-name fmu fasc] :as md]
    [tiltontec.web-mx.gen :refer [evt-md target-value]]
    [tiltontec.web-mx.gen-macro
     :refer [img section h1 h2 h3 input footer p a
             span i label ul li div button code pre]]
    [tiltontec.web-mx.style :refer [make-css-inline]]
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
    ;(h3 "A Manual Clock")
    (h3 "The time is now....")
    (div {:class   "intro-clock"
          :content (cF (if-let [now (mget me :now)]         ;; mget, the standard MX getter, can be used from any code,
                         (-> now .toTimeString              ;; but transparently establishes a dependency, or "subscribes",
                           (str/split " ") first)           ;; if called within a formula.
                         "---"))}
      {:name :the-clock
       ;; uncomment next line to run automatically
       ;; :ticker (cF (js/setInterval #(mset! me :now (js/Date.)) 1000))
       :now  (cI nil)})                                     ;; cI for "cell Input"; procedural code can write to these
    (refresh-button)))

;;; --- running clock example --------------------------------------------

(def running-clock-code
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
                                        (> new-val 3000))
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

(defn multi-clock []
  (div {} {:name           :clocks
           :selected-clock (cFn (second (mget me :clocks)))
           :clocks         [{:title "Manual Clock" :builder manual-clock :code manual-clock-code}
                            {:title "Running Clock" :builder running-clock :code running-clock-code}]}
    (div {:style {:display         :flex
                  :flex-direction  :row
                  :align-items     :center
                  :justify-content "center"                 ;; "space-around"
                  :padding         "3px"
                  :margin          "6px"
                  :gap             "1em"
                  }}
      (span "Pick one:")
      (doall (for [{:keys [title] :as clk} (mget (mx-par me) :clocks)]
               (button {:class   :pushbutton
                        :cursor  :finger
                        :style   (cF (let [curr-clk (mget (fasc :clocks me) :selected-clock)]
                                       {:border-color (if (= clk curr-clk)
                                                        "dark-gray" "white")
                                        :font-weight  (if (= clk curr-clk)
                                                        "bold" "normal")}))
                        :onclick (cF (fn [] (mset! (fmu :clocks) :selected-clock clk)))}
                 title))))
    (when-let [clk (mget me :selected-clock)]
      (div {:style {:display        :flex
                    :flex-direction :column-reverse
                    :gap            "1em"
                    }}
        (pre {:style {:margin-left "96px"}}
          (code (:code clk))) (div {:style {:border-color "gray"
                                            :border-style "solid"
                                            :border-width "2px"}}
                                ((:builder clk)))
        ))))

#_(defn nyc-std-clock [interval]
    (div {:class   "intro-clock"
          :style   "background:black"
          :content (cF (str (mget me :elapsed))
                     #_(let [c (mget me :elapsed)
                             ts (str (.toTimeString
                                       (js/Date. c)))]
                         (if (= interval 1000)
                           ts
                           (str (subs ts 0 8)
                             "."
                             (pp/cl-format nil "~3'0d" (mod c 1000))
                             ))))}
      {:name     :the-clock
       :ticking? (cI false)
       :ticker   (cF+ [:watch (fn [_ _ _ prior-value _]
                                (when (integer? prior-value)
                                  (js/clearInterval prior-value)))]
                   (when (mget me :ticking?)
                     (js/setInterval #(mset! me :elapsed (js/Date.)) interval)))
       :now      (cI nil)
       :start    (cI nil)
       :elapsed  (cF (if-let [start (mget me :start)]
                       (- (.getTime (js/Date.)) (.getTime start))
                       0))
       }))
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

(exu/main #(md/make ::intro
             :mx-dom (multi-clock)))

