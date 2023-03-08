(ns tiltontec.web-mx.gen
  (:refer-clojure :exclude [map meta time])
  (:require
    [clojure.string :as str]
    #?(:cljs
       [goog.dom.forms :as form]
       )
    #?(:clj  [clojure.pprint :refer :all]
       :cljs [cljs.pprint :refer [pprint cl-format]])
    [tiltontec.cell.base :refer [md-ref? ia-type unbound]]
    [tiltontec.cell.evaluate :refer [md-quiesce md-quiesce-self]]
    [tiltontec.model.core :refer [make mget] :as md]
    ))

(defn tagfo [me]
  (select-keys @me [:id :tag :class :name]))

(def +tag-sid+ (atom -1))

(defn web-mx-init! []
  (reset! +tag-sid+ -1))

(def tag-by-id (atom {}))

(defn dom-tag [dom]
  (cond
    (nil? dom) (do                                          ;; (println :outthetop!!!)
                 nil)
    ;; where we specify string content to eg button we get an
    ;; automatic span for the string that has no ID. Hopefully where
    ;; dom-tiltontec.web-mx is requested they will be OK with us tracking the nearest ascendant.
    (= "" (.-id dom)) (do (println :no-id-try-pa (.-parentNode dom))
                          (dom-tag (.-parentNode dom)))
    :default (let [tag (get @tag-by-id (.-id dom))]
               (assert tag (str "dom-tiltontec.web-mx did not find js for id " (.-id dom)
                             " of dom " dom))
               tag)))

(defn attr-val$ [val]
  (cond
    (string? val) val
    (keyword? val) (name val)
    (coll? val) (str/join " " (mapv attr-val$ val))
    (fn? val) (do
                (prn "gen/attr-val$ raw!!!!")
                val)
    :else (str val)))

;;; --- TAG --------------------------------------------------

(defn make-tag [tag attrs aux cFkids]
  ;; (prn :make-tag tag :attrs (keys attrs) :aux (keys aux))
  (let [tag-id (if-let [id (:id attrs)]
                 (attr-val$ id)
                 (str tag "-" (swap! +tag-sid+ inc)))
        mx-tag (apply make
                 :type :web-mx.base/tag
                 :tag tag
                 :id tag-id
                 :attr-keys (distinct (conj (keys attrs) :id))
                 :kids cFkids
                 (concat (vec (apply concat (seq (dissoc attrs :id))))
                   (vec (apply concat (seq aux)))))]
    ;;(println :made-tiltontec.web-mx!! tiltontec.web-mx-id (keys @mx-tiltontec.web-mx))
    (swap! tag-by-id assoc tag-id mx-tag)
    mx-tag))

(defmethod md-quiesce [:web-mx.base/tag] [me]
  ;; todo: worry about leaks
  ;; (println :md-quiesce-tiltontec.web-mx!!! (tagfo me))

  (when-let [style (:style @me)]
    (when (md-ref? style)
      ;;(println :popping-style style)
      (md-quiesce style)))

  (doseq [k (:kids @me)]
    (when (md-ref? k)
      (md-quiesce k)))
  (swap! tag-by-id dissoc (mget me :id))
  (md-quiesce-self me))

;;; --- SVG --------------------------------------------------

(defn make-svg
  ([svg]
   (make-svg svg {}))
  ([svg attrs]
   (make-svg svg attrs {}))
  ([svg attrs custom-props]
   (make-svg svg attrs custom-props nil))
  ([svg attrs aux cFkids]
   ;; (prn :make-svg svg :attrs (keys attrs) :aux (keys aux))
   (let [svg-id (if-let [id (:id attrs)]
                  (attr-val$ id)
                  ;; we'll piggyback some of the tag infrastructure
                  (str svg "-" (swap! +tag-sid+ inc)))
         mx-svg (apply make
                  :type :web-mx.base/svg
                  :tag (cond
                         (keyword? svg) (name svg)
                         (string? svg) (if (= \: (first svg))
                                         (subs svg 1) svg)
                         :else (str svg))
                  :id svg-id
                  :attr-keys (distinct (conj (keys attrs) :id))
                  :kids cFkids
                  (concat (vec (apply concat (seq (dissoc attrs :id))))
                    (vec (apply concat (seq aux)))))]
     ;;(println :made-tiltontec.web-mx!! tiltontec.web-mx-id (keys @mx-tiltontec.web-mx))
     (swap! tag-by-id assoc svg-id mx-svg)
     mx-svg)))

(defmethod md-quiesce [:web-mx.base/svg] [me]
  ;; todo: worry about leaks
  (when-let [style (:style @me)]
    (when (md-ref? style)
      (md-quiesce style)))

  (doseq [k (:kids @me)]
    (when (md-ref? k)
      (md-quiesce k)))

  (swap! tag-by-id dissoc (mget me :id))
  (md-quiesce-self me))

;;; --- event conveniences -------------------

(defn evt-mx [e]
  ;; deprecated. "md" for "model" is better
  (dom-tag (.-target e)))

(defn evt-md [e]
  (dom-tag (.-target e)))

#?(:cljs
   (defn target-value [evt]
     (form/getValue (.-target evt))))

