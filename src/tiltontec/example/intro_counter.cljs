(ns tiltontec.example.intro-counter
  (:require
    [clojure.string :as str]
    [clojure.pprint :as pp]
    [tiltontec.cell.base :refer [minfo]]
    [tiltontec.cell.core :refer [cF cF+ cFonce cI cf-freeze]]
    [tiltontec.cell.integrity :refer [with-cc]]
    [tiltontec.model.core
     :refer [mx-par mget mset! mswap! mset! mxi-find mxu-find-name fmu fm!] :as md]
    [tiltontec.web-mx.gen :refer [evt-md target-value]]
    [tiltontec.web-mx.gen-macro
     :refer [img section h1 h2 h3 input footer p a
             span i label ul li div button br
             defexample]]
    [tiltontec.web-mx.style :refer [make-css-inline]]
    [tiltontec.example.util :as exu]
    [cljs-http.client :as client]
    [cljs.core.async :refer [go <!]]))

;;; --- intro counter -----------------------------------

;;; --- 1. It's just html -------------------------------------
;;; We still program HTML. Please find detailed notes following the code.

(def counter-fnyi-code
  "(defn counter-fnyi []\n  (prn :counter-fnyi)\n  (div {:class :intro}\n    (h2 \"The count is now....\")\n    (span {:class :intro-counter} \"42\")\n    ;; CLJS can be embedded right in the \"HTML\".\n    ;; It is all CLJS, in fact.\n    (doall (for [txt [\"-\" \"=\" \"+\"]]\n             (button {:class   :push-button\n                      :onclick #(js/alert \"Feature Not Yet Implemented\")} txt)))))")

(defn counter-fnyi []
  (prn :counter-fnyi)
  (div {:class :intro}
    (h2 "The count is now....")
    (span {:class :intro-counter} "42")
    ;; ...but CLJS can be embedded right in the "HTML".
    ;; It is all CLJS, in fact.
    (div {:class :toolbar}
      (doall (for [txt ["-" "=" "+"]]
             (button {:class   :push-button
                      :onclick #(js/alert "Feature Not Yet Implemented")} txt))))))

;;; Where HTML has <tag attributes*> children* </tag>...
;;; Web/MX has (tag [HTML-attribute-map [custom-attr-map]] children*)
;;; Keywords become strings in HTML.
;;; Otherwise, MDN is your guide.


;;; --- unlimited access to program state, aka omniscience -----------------------------
;;; Any component can pull the information it needs to do its job from anywhere, by having
;;; "formula" code that can navigate to any object for any property.

(def counter-omniscience-code
  "   (div {:class \"intro\"}\n      (div {}\n        {:name  :a-counter                                  ;; 1\n         :count 3}                                          ;; 2\n        (h2 \"The count is now&hellip;\")\n        (span {:class :intro-counter}\n          (str \"&hellip;\" (mget (mx-par me) :count))))      ;; 3\n      ;; just demoing navigation by name, and dynamic content:\n      (div (mapv (fn [idx] (span (str idx \"...\")))          ;; 4\n             (range (mget (fmu :a-counter me)               ;; 5a\n                      :count)))))")
(defn counter-omniscience []
    (div {:class "intro"}
      (div {}
        {:name  :a-counter                                  ;; 1
         :count 3}                                          ;; 2
        (h2 "The count is now&hellip;")
        (span {:class :intro-counter}
          (str "&hellip;" (mget (mx-par me) :count))))      ;; 3
      ;; just demoing navigation by name, and dynamic content:
      (div (mapv (fn [idx] (span (str idx "...")))          ;; 4
             (range (mget (fmu :a-counter me)               ;; 5a
                      :count))))))                          ;; 5b
;;; 1. We use the second custom property map to give the <span> a name, namely, :a-counter...
;;; 2. ...and also give the widget some custom state, the :count.
;;; 3. We derive the text for the counter display by navigating
;;;    to the parent and reading the :count.
;;; 3 & 4. two ways to navigate: by "hardpath" parent or 'kids', or by name via FMU
;;; 4. children of tags are wrapped in implicit formulas, and
;;;    can include arbitrary HLL code, accessing "me" as if it were "this" or "self"
;;; 5. derive the "...N" spans by
;;;   a. navigating to the widget named :a-counter; and
;;;   b. reading its :count property.
;;;
;;; For those wondering, dependency cycles throw an exception at runtime.

;;; --- omnipotence -----------------------------
;;; Any handler can navigate to any property to change it, with all
;;; dependencies updated before the MSET! or MSWAP! call returns.

(defn counter-omnipotent []
    (div {:class [:intro]}
      (div {:class "intro"}
        {:name  :a-counter
         :count (cI 2)}                                     ;; 1
        (h2 "The count is now&hellip;")
        (span {:class :intro-counter}
          (str (mget (mx-par me) :count)))
        (button {:class   :push-button
                 :onclick (cF (fn [event]                   ;; 2
                                (let [counter (fm! :a-counter me)] ;; 3
                                  (mswap! counter :count inc))))} ;; 4
          "+"))
      (div (mapv (fn [idx] (span (str idx "...")))
             (range (mget (fmu :a-counter me) :count))))))

;;; 1. `(cI <value>)` tells MX that the property :count can and
;;;    might be changed by imperative code;
;;; 2. we generate the event handler in a formula for handy access to "me";
;;; 3. Use `FM!` family search utility to navigate to the :a-counter MX proxy;
;;; 4. Mutate the property (and dependent state) using MSWAP!

(def counter-omnipotent-code
  "(defn counter-omnipotent []\n    (div {:class [:intro]}\n      (div {:class \"intro\"}\n        {:name  :a-counter\n         :count (cI 2)}                                     ;; 1\n        (h2 \"The count is now&hellip;\")\n        (span {:class :intro-counter}\n          (str (mget (mx-par me) :count)))\n        (button {:class   :push-button\n                 :onclick (cF (fn [event]                   ;; 2\n                                (let [counter (fm! :a-counter me)] ;; 3\n                                  (mswap! counter :count inc))))} ;; 4\n          \"+\"))\n      (div (mapv (fn [idx] (span (str idx \"...\")))\n             (range (mget (fmu :a-counter me) :count))))))")

;;; --- omnipresence ------------------
;;; Reactivity is neat, so we want to use it everywhere, even with software that
;;; knows nothing about Matrix reactive mechanisms. In this next example, we use
;;; some simple "glue code" to connect the non-reactive `js/setInterval` with our Matrix.

#_ (defn a-counter []
    (div {:class "intro"}
      {:name   :a-counter
       :ticker (cF (js/setInterval                          ;; 1
                     #(mswap! me :count inc)                ;; 2
                     1000))
       :count  (cI 0)}
      (h2 "The count is now&hellip;")
      (span {:class :intro-counter}
        (str (mget (mx-par me) :count)))))

;;; In a more elaborate example, we show how to make an async XHR request reactive:

(def random-joke-uri "https://official-joke-api.appspot.com/random_joke")
(def cat-fact-uri "https://catfact.ninja/fact")

(defn reactivity-all-in []
  (div {:class "intro"}
    {:name         :a-counter
     :danger-count 10
     :ticker       (cF (js/setInterval #(mswap! me :count inc) 1000))
     :count        (cI 0)
     }
    (h2 "The count is now&hellip;")
    (span {:class :intro-counter}
      (str (mget (mx-par me) :count)))
    (span {:style {:display :block
                   :text-align :left
                   :padding "6px"
                   :max-width "40em"
                   :min-width "40em"
                   :min-height "6em"}}
      {:cat-request  (cF+ [:watch (fn [_ me response-chan _ _]
                                    (when response-chan
                                      (go (let [response (<! response-chan)]
                                            (with-cc :set-cat
                                              (mset! me :cat-response response))))))]
                       (when (zero? (mod (mget (mx-par me) :count) 5))
                         (client/get cat-fact-uri {:with-credentials? false})))
       :cat-response (cI nil)}
      (if-let [jr (mget me :cat-response)]
        (if (:success jr)
          (p (get-in jr [:body :fact]))
          (str "Error>  " (:error-code jr)
            ": " (:error-text jr)))
        "no cats yet"))))

(def reactivity-all-in-code
  "(div {:class \"intro\"}\n    {:name         :a-counter\n     :danger-count 10\n     :ticker       (cF (js/setInterval #(mswap! me :count inc) 1000))\n     :count        (cI 0)\n     }\n    (h2 \"The count is now&hellip;\")\n    (span {:class :intro-counter}\n      (str (mget (mx-par me) :count)))\n    (div {:style {:display        :flex\n                  :flex-direction :column\n                  :gap            \"6px\"}}\n      {:cat-request  (cF+ [:watch (fn [_ me response-chan _ _]\n                                    (when response-chan\n                                      (go (let [response (<! response-chan)]\n                                            (with-cc :set-cat\n                                              (mset! me :cat-response response))))))]\n                       (when (and (zero? (mod (mget (mx-par me) :count) 5))\n                               #_(< (mget (mx-par me) :count) 21))\n                         (client/get cat-fact-uri {:with-credentials? false})))\n       :cat-response (cI nil)}\n      (if-let [jr (mget me :cat-response)]\n        (if (:success jr)\n          (span (get-in jr [:body :fact]))\n          (str \"Error>  \" (:error-code jr)\n            \": \" (:error-text jr)))\n        \"no cats yet\")))")

(defn minitest []
  ;; (prn :defexample!!! (defexample "mini test 2" (defn minitest2 [] (span "boom"))))
  (div {:class "intro"}
    {:name   :a-counter
     :count  (cI 0)}
    (h2 "The count is now&hellip;")
    (span {:class :intro-counter}
      (str (mget (mx-par me) :count)))))

(def minitest-code "(defn minitest []\n  (div {:class \"intro\"}\n    {:name   :a-counter\n     :count  (cI 0)}\n    (h2 \"The count is now&hellip;\")\n    (span {:class :intro-counter}\n      (str (mget (mx-par me) :count)))))")


(exu/main #(md/make ::intro
             :mx-dom (exu/multi-demo 99
                       {:title "Just HTML&trade;" :builder counter-fnyi :code counter-fnyi-code}
                       {:title "Counter Omniscient" :builder counter-omniscience :code counter-omniscience-code}
                       {:title "Counter Omnipotent" :builder counter-omnipotent :code counter-omnipotent-code}
                       {:title "Reactivity All-In" :builder reactivity-all-in :code reactivity-all-in-code}
                      #_  {:title "Mini test" :builder minitest :code minitest-code})))

;;; --- observer/watch dataflow initiation ----------------------
;;; It is not uncommon, when developing MX code, to encounter
;;; a use case where the dataflow can detect a need to mutate an input
;;; cell. These are often cases where the user has control, but the system
;;; wants to offer a U/X nicety by doing what the user would do. But user
;;; input comes from controls operating on MX input cells.
;;; To this end, MX allows observers to enqueue, via `with-cc`, mset!/mswap! code for execution
;;; immediately following the processing of the current mutation.

;;; In the example below, we want the user to control the counter, but we also want
;;; an automatic safeguard should the count reach a "dangerous" level

(defn start-stop-button []
  (button {:class   :pushbutton
           :style   "border-color:white"
           :onclick #(mswap! (fmu :a-counter (evt-md %)) :ticking? not)}
    (if (mget (fmu :a-counter me) :ticking?)
      "Stop" "Start")))

#_(defn a-counter []
    (div {:class "intro"}
      {:name         :a-counter
       :danger-count 10
       :ticking?     (cI false)
       :ticker       (cF+ [:watch (fn [_ _ newv prior _]
                                    (when (integer? prior)
                                      (js/clearInterval prior)))]
                       (when (mget me :ticking?)
                         (js/setInterval #(mswap! me :count inc) 1000)))
       :count        (cI 0 :watch (fn [_ me new-ct _ _]
                                    (when (> new-ct (+ 5 (mget me :danger-count)))
                                      (with-cc :tickofff
                                        (mset! me :ticking? false)))))
       }
      (h2 "The count is now&hellip;")
      (span {:class :intro-counter
             :style (cF (str "background:"
                          (let [ct (mget (mx-par me) :count)
                                danger (mget (mx-par me) :danger-count)]
                            (cond
                              (< ct danger) "black"
                              (= ct danger) "yellow"
                              :else "red"))))}
        (str (mget (mx-par me) :count)))
      (p {}
        {:joke-request  (cF+ [:watch (fn [_ me response-chan _ _]
                                       (when response-chan
                                         (go (let [response (<! response-chan)]
                                               (with-cc :setjoke
                                                 (prn :set-up (get-in response [:body :setup]))
                                                 ;; check your js console to see the joke
                                                 (mset! me :joke-response response))))))]
                          (when (even? (mget (mx-par me) :count))
                            (client/get random-joke-uri {:with-credentials? false})))
         :joke-response (cI nil)}
        (if-let [jr (mget me :joke-response)]
          (if (:success jr)
            (get-in jr [:body :punchline])
            (str "Error>  " (:error-code jr)
              ": " (:error-text jr)))
          "no joke yet"))
      (start-stop-button)))

;;; 1. By using a formula to create the interval,we get lexical acces to "me"
;;; 2. Intervals fire asynchronously, and intervals do not know about Matrix,
;;;    but in this case, we make them reactive by having the handler
;;;    use the API `mswap!` to accurately update the :count and the entire DAG.

;;; --- user control ------------------------------------
#_(defn a-counter []
    (div {:class "intro"}
      {:name     :a-counter
       :ticking? (cI false)                                 ;; 0
       :ticker   (cF+ [:watch (fn [prop-name me new-value prior-value cell] ;; 1
                                (when (integer? prior-value) ;; not initially
                                  (js/clearInterval prior-value)))] ;; 2
                   (when (mget me :ticking?)
                     (js/setInterval #(mswap! me :count inc) 1000))) ;; 3
       :count    (cI 0)}
      (h2 "The count is now&hellip;")
      (span {:class :intro-counter}
        (str (mget (mx-par me) :count)))
      (button
        {:class   :push-button
         :style   {:min-width "96px"}
         :title   "Click to start or stop counting."
         :onclick #(mswap! (fmu :a-counter (evt-md %)) :ticking? not)} ;; 4
        (if (mget (fmu :a-counter me) :ticking?)
          "||" ">>"))))

;;; Notes on the above:
;;; 0. Since the user will be controlling whether the clock is `ticking?`, we make that an input Cell (cI).
;;; 1. We introduce "watch" functions, available for side-effects outside the MX dataflow;
;;; 2. in this case, we use the watch simply to scavenge obsolete timers;
;;; 3. the interval handler fires asynchronously, using the Matrix MSWAP! function to mutate state accurately.

;;; --- Putting it all together
;;; In the final version, we bring back the user-activated [+] button, an inverse [-] button, and
;;; let the user use both, and also whether or not the automatic increment is running.
;;; We also show a running "count per seconds", starting from the first change.

#_(defn a-counter []
    (div {:class "intro"}
      {:name       :a-counter
       :count      (cI 0)
       :ticking?   (cI false)
       :tick-start (cF
                     ;; we arbitrarily start a timer when count is first not= 0
                     ;; this could happen from the manual bumpers or the automatic bumper
                     ;; so it seemed better to work off the actual change in value.
                     (when (not= 0 (mget me :count))
                       (cf-freeze (.getTime (js/Date.)))))
       :tick-rate  (cF (when-let [start (mget me :tick-start)]
                         (let [elapsed (- (.getTime (js/Date.)) start)]
                           (/ (mget me :count) (max 1 (/ elapsed 1000.0))))))
       :ticker     (cF+ [:watch (fn [_ _ _ prior-value _]
                                  (when (integer? prior-value) ;; on initial "echo", prior-value is unbound
                                    (js/clearInterval prior-value)))]
                     (when (mget me :ticking?)
                       (js/setInterval #(mswap! me :count inc) 1000)))
       }
      (h2 "The count is now&hellip;")
      (span {:class :intro-counter}
        (str (mget (mx-par me) :count)))
      (span {:class :intro-counter
             :style "font-size:64px"}
        (if-let [r (mget (mx-par me) :tick-rate)]
          (pp/cl-format nil "~5,2f" r)
          "0.00"))
      (div {:class "control-panel"}
        (button {:class   :push-button
                 :onclick (cF (fn [event]
                                (let [counter (fm! :a-counter me)]
                                  (mswap! counter :count dec))))}
          "-")
        (button
          {:class   :push-button
           :style   {:min-width "96px"}
           :onclick #(mswap! (fmu :a-counter (evt-md %)) :ticking? not)}
          (if (mget (fmu :a-counter me) :ticking?)
            "||" ">>"))
        (button {:class   :push-button
                 :onclick (cF (fn [event]
                                (let [counter (fm! :a-counter me)]
                                  (mswap! counter :count inc))))}
          "+"))))

#_(exu/main #(md/make
               :mx-dom (a-counter)))

