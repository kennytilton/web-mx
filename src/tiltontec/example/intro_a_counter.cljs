(ns tiltontec.example.intro-a-counter
  (:require
    [clojure.string :as str]
    [clojure.pprint :as pp]
    [tiltontec.cell.core :refer [cF cF+ cFonce cI cf-freeze]]
    [tiltontec.model.core
     :refer [mx-par mget mset! mswap! mset! mxi-find mxu-find-name fmu fm!] :as md]
    [tiltontec.web-mx.gen :refer [evt-md target-value]]
    [tiltontec.web-mx.gen-macro
     :refer [img section h1 h2 h3 input footer p a
             span i label ul li div button br]]
    [tiltontec.web-mx.style :refer [make-css-inline]]
    [tiltontec.example.util :as exu]))

;;; --- intro counter -----------------------------------
;;; We look at three core ideas:
;;; 1. We just do standard HTML/CSS;
;;; 2. "omniscience": formulas can work off any app state; and
;;; 3. "omnipotence": event handlers can mutate at will.

;;; --- 1. It's just html -------------------------------------
;;; We still program HTML. Please find detailed notes following the code.
#_(defn a-counter []
    (div {:class :intro}
      (h2 "The count is now....")
      (span {:class :intro-a-counter} "42")
      (button {:class   :push-button
               :onclick #(js/alert "Feature Not Yet Implemented")} "+")))
;;; Where HTML has <tag attributes*> children* </tag>...
;;; Web/MX has (tag [HTML-attribute-map [custom-attr-map]] children*)
;;; Keywords become strings in HTML.
;;; Otherwise, MDN is your guide.

;;; --- omniscience -----------------------------
;;; Any component can pull information it needs from anywhere, using
;;; "formulas" that can navigate to any object for any property.
#_(defn a-counter []
    (div {:class "intro"}
      (div {}
        {:name  :a-counter                                  ;; 1
         :count 3}                                          ;; 2
        (h2 "The count is now&hellip;")
        (span {:class :intro-a-counter}
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
#_(defn a-counter []
    (div {:class [:intro]}
      (div {:class "intro"}
        {:name  :a-counter
         :count (cI 2)}                                     ;; 1
        (h2 "The count is now&hellip;")
        (span {:class :intro-a-counter}
          (str "&hellip;" (mget (mx-par me) :count)))
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

;;; --- omnipresence ------------------
;;; Reactivity is neat, so we want to use it everywhere, even with things that
;;; do know about Matrix internals. In this next evolution of our example we make
;;; our counter run by itself, with the button now starting and stopping the counter,
;;; by leveraging js/setInterval to automatically increment the Matrix counter property.

#_(defn a-counter []
    (div {:class "intro"}
      {:name     :a-counter
       :ticking? (cI false)
       :ticker   (cF+ [:watch (fn [prop-name me new-value prior-value cell] ;; 1
                                (when (integer? prior-value) ;; on initial "echo", prior-value is unbound
                                  (js/clearInterval prior-value)))] ;; 2
                   (when (mget me :ticking?)
                     (js/setInterval #(mswap! me :count inc) 1000))) ;; 3
       :count    (cI 0)}
      (h2 "The count is now&hellip;")
      (span {:class :intro-a-counter}
        (str "&hellip;" (mget (mx-par me) :count)))
      (button
        {:class   :push-button
         :style   {:min-width "96px"}
         :onclick #(mswap! (fmu :a-counter (evt-md %)) :ticking? not)} ;; 4
        (if (mget (fmu :a-counter me) :ticking?)
          "||" ">>"))))

;;; Notes on the above:
;;; 1. We introduce "watch" functions, available for side-effects outside the MX dataflow;
;;; 2. in this case, we use the watch simply to scavenge obsolete timers;
;;; 3. the interval handler gets called asynchronously, but it does no more than use
;;;    the regulation MSWAP! function to mutate state, so async is no problem.



;;; --- Putting it all together
;;; In the final version, we bring back the user-activated [+] button, an inverse [-] button, and
;;; let the user use both whether or not the automatic increment is running.

(defn a-counter []
  (div {:class "intro"}
    {:name       :a-counter
     :ticking?   (cI false)
     :tick-start (cF (prn :tickstart? (nil? _cache)(number? _cache))
                   #_ (when (not= 0 (mget me :count))
                     (cf-freeze (.getTime (js/Date.))))
                   ;; workaround for bug in cf-freeze
                   (if (number? _cache)
                     (cf-freeze _cache)
                     (when (not= 0 (mget me :count))
                       (cf-freeze (.getTime (js/Date.))))))
     :tick-rate  (cF (when-let [start (mget me :tick-start)]
                       (let [elapsed (- (.getTime (js/Date.)) start)]
                         (/ (mget me :count) (/ elapsed 1000.0)))))
     :ticker     (cF+ [:watch (fn [_ _ _ prior-value _]
                                (when (integer? prior-value) ;; on initial "echo", prior-value is unbound
                                  (js/clearInterval prior-value)))] ;; 2
                   (when (mget me :ticking?)
                     (js/setInterval #(mswap! me :count inc) 1000))) ;; 3
     :count      (cI 0)}
    (h2 "The count is now&hellip;")
    (span {:class :intro-a-counter}
      (str (mget (mx-par me) :count)))
    (span {:class :intro-a-counter}
      (if-let [r (mget (mx-par me) :tick-rate)]
        (pp/cl-format nil "~6,3f" r)
        "rate"))
    (div {:class "control-panel"}
      (button {:class   :push-button
               :onclick (cF (fn [event]
                              (prn :tickstart (mget (fmu :a-counter) :tick-start))
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

(exu/main #(md/make
             :mx-dom (a-counter)))

