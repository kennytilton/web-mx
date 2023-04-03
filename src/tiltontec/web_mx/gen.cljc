(ns tiltontec.web-mx.gen
  #?(:cljs
     (:require-macros [tiltontec.web-mx.gen]))
  (:refer-clojure :exclude [map meta time])
  (:require
    [clojure.string :as str]
    #?(:cljs
       [goog.dom.forms :as form]
       )
    [tiltontec.matrix.api :refer [md-ref? mx-type minfo unbound make mget]]
    [tiltontec.cell.poly :refer [md-quiesce md-quiesce-self] :as cw]))

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
    ;; dom-tag is requested they will be OK with us tracking the nearest ascendant.
    (= "" (.-id dom)) (dom-tag (.-parentNode dom))
    :default (let [tag (get @tag-by-id (.-id dom))]
               (assert tag (str "dom-tag> dict tag-by-id has no entry for id <" (.-id dom)
                             "> of dom " dom))
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

(defn dom-quiesce [me]
  ;; todo: worry about leaks
  (when-let [style (:style @me)]
    (when (md-ref? style)
      (md-quiesce style)))

  (doseq [k (mget me :kids)]
    (when (md-ref? k)
      (md-quiesce k)))

  (swap! tag-by-id dissoc (mget me :id))
  (md-quiesce-self me))

;;; --- TAG --------------------------------------------------

(defn make-tag [tag attrs aux cFkids]
  ;; (prn :make-tag tag :attrs (keys attrs) :aux (keys aux))
  (let [tag-id (if-let [id (:id attrs)]
                 (attr-val$ id)
                 (str tag "-" (swap! +tag-sid+ inc)))
        mx-tag (apply make
                 :mx-type :web-mx.base/tag
                 :tag tag
                 :id tag-id
                 :attr-keys (distinct (conj (keys attrs) :id))
                 :kids cFkids
                 (concat (vec (apply concat (seq (dissoc attrs :id))))
                   (vec (apply concat (seq aux)))))]
    (swap! tag-by-id assoc tag-id mx-tag)
    mx-tag))

(defmethod md-quiesce :web-mx.base/tag [me]
  (dom-quiesce me))

;;; --- SVG --------------------------------------------------

(defmethod md-quiesce :web-mx.base/svg [me]
  (dom-quiesce me))

;;; --- event conveniences -------------------

(defn evt-md [e]
  (dom-tag (.-target e)))

(defmacro deftag [tag]
  (let [kids (gensym "kids")
        vargs (gensym "vargs")
        tag-name (gensym "web-mx-name")]
    `(defmacro ~tag [& ~vargs]
       (let [~tag-name (str '~tag)]
         (cond
           (nil? ~vargs)
           `(tiltontec.web-mx.gen/make-tag ~~tag-name {} {} nil)

           (map? (first ~vargs))
           (cond
             (map? (second ~vargs))
             `(tiltontec.web-mx.gen/make-tag ~~tag-name ~(first ~vargs) ~(second ~vargs)
                ~(when-let [~kids (seq (nthrest ~vargs 2))]
                   `(tiltontec.model.core/cFkids ~@~kids)))

             :default `(tiltontec.web-mx.gen/make-tag
                         ~~tag-name ~(first ~vargs)
                         {}
                         ~(when-let [~kids (seq (nthrest ~vargs 1))]
                            `(tiltontec.model.core/cFkids ~@~kids))))

           :default `(tiltontec.web-mx.gen/make-tag
                       ~~tag-name {} {}
                       (tiltontec.model.core/cFkids ~@~vargs)))))))


(defmacro defsvg [svg]
  (let [kids (gensym "kids")
        vargs (gensym "vargs")
        svg-name (gensym "web-mx-name")]
    `(defmacro ~svg [& ~vargs]
       (let [~svg-name (str '~svg)]
         (cond
           (nil? ~vargs)
           `(tiltontec.web-mx.api/make-svg ~~svg-name {} {} nil)

           (map? (first ~vargs))
           (cond
             (map? (second ~vargs))
             `(tiltontec.web-mx.api/make-svg ~~svg-name ~(first ~vargs) ~(second ~vargs)
                ~(when-let [~kids (seq (nthrest ~vargs 2))]
                   `(tiltontec.model.core/cFkids ~@~kids)))

             :default `(tiltontec.web-mx.api/make-svg
                         ~~svg-name ~(first ~vargs)
                         {}
                         ~(when-let [~kids (seq (nthrest ~vargs 1))]
                            `(tiltontec.model.core/cFkids ~@~kids))))

           :default `(tiltontec.web-mx.api/make-svg
                       ~~svg-name {} {}
                       (tiltontec.model.core/cFkids ~@~vargs)))))))

(defmacro deftags [& tags]
  `(do ~@(for [tag tags]
           `(deftag ~tag))))

(defmacro defsvgs [& svgs]
  `(do ~@(for [svg svgs]
           `(defsvg ~svg))))