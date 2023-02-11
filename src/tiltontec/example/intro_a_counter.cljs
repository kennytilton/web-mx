(ns tiltontec.example.intro-a-counter
  (:require
    [clojure.string :as str]
    [clojure.pprint :as pp]
    [tiltontec.cell.core :refer [cF cF+ cFonce cI]]
    [tiltontec.model.core
     :refer [mx-par mget mset! mswap! mset! mxi-find mxu-find-name fmu fm!] :as md]
    [tiltontec.web-mx.gen :refer [evt-md target-value]]
    [tiltontec.web-mx.gen-macro
     :refer [img section h1 h2 h3 input footer p a
             span i label ul li div button br]]
    [tiltontec.web-mx.style :refer [make-css-inline]]
    [tiltontec.example.util :as exu]))

;;; --- intro counter starter code -----------------------------------

;;; --- just html -------------------------------------
;;; We still program HTML. Please find detailed notes following the code.
#_(defn a-counter []
    (div {:class :intro}
      (h2 "The count is now....")
      (p {:class :intro-a-counter} "42")
      (button {:class   :push-button
               :onclick #(js/alert "Feature Not Yet Implemented")} "+")))
;;; Where HTML has <tag attributes*> children* </tag>...
;;; Web/MX has (tag [HTML-attribute-map [custom-attr-map]] children*)
;;; Keywords become strings in HTML.
;;; Otherwise, MDN is your guide.

;;; --- omniscience -----------------------------
;;; Any component can pull information it needs from anywhere, using
;;; "formulas" that can navigate to any object for any property.
#_
(defn a-counter []
    (div {:class [:intro]}
      (div {}
        {:name  :a-counter                                  ;; 1
         :count 3}                                          ;; 2
        (h2 "The count is now&hellip;")
        (p {:class :intro-a-counter}
          (str "&hellip;" (mget (mx-par me) :count))))      ;; 3
      ;; todo CSS for all this, overall and spans
      (div (mapv (fn [idx] (span (str idx "...")))          ;; 4
             (range (mget (fmu :a-counter me)               ;; 5a
                      :count))))))                          ;; 5b
;;; 1. give the <p> a name, namely, :a-counter.
;;; 2. give :a-counter some custom state. Call it :count.
;;; 3. derive the text for the counter display by navigating
;;;    to the parent and reading the :count.
;;; 3 & 4. two ways to navigate: by "hardpath" parent or 'kids', or by name via FMU
;;; 4. children of tags are wrapped in implicit formulas, and
;;;    can include arbitrary HLL code, accessing "me" as if it were "this" or "self"
;;; 5. derive the "...N" spans by
;;;   a. navigating to the widget named :a-counter; and
;;;   b. reading its :count property.
;;;
;;; FYI: Dependency cycles throw an exception at runtime.

;;; --- omnipotence -----------------------------
;;; Any handler can navigate to any property to change it, with all
;;; dependencies updated before the MSET! or MSWAP! call returns.
(defn a-counter []
    (div {:class [:intro]}
      (div {:class "intro"}
        {:name  :a-counter
         :count (cI 3)}                                     ;; 1
        (h2 "The count is now&hellip;")
        (p {:class :intro-a-counter}
          (str "&hellip;" (mget (mx-par me) :count)))
        (button {:class   :push-button
                 :onclick (cF (fn [event] ;; 2
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


(exu/main #(md/make
             :mx-dom (a-counter)))

