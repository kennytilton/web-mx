(ns tiltontec.web-mx.api
  (:refer-clojure :exclude [map meta time])
  (:require [clojure.walk :as walk]
            [cljs.pprint :as pp]
            [clojure.string :as str]
            [goog.object :as gobj]
            [goog.dom.forms :as form]
            [goog.style :as gstyle]
            [tiltontec.matrix.api :refer
             [minfo md-ref? unbound make mget mget?
              the-kids mdv! any-ref? rmap-meta-setf mx-type
              fm-navig mget mget? fasc fm! mset! backdoor-reset!]]
            [tiltontec.cell.poly :refer [watch watch-by-type
                                         md-quiesce md-quiesce-self] :as cw]
            [tiltontec.web-mx.base :refer [tag? kw$ tag-dom] :as wbase]
            [tiltontec.web-mx.gen :refer [make-tag dom-tag +tag-sid+ tag-by-id]])
  (:require-macros [tiltontec.web-mx.api]))

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
                  (wbase/attr-val$ id)
                  ;; we'll piggyback some of the tag infrastructure
                  (str svg "-" (swap! +tag-sid+ inc)))
         mx-svg (apply make
                  :mx-type :web-mx.base/svg
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

(defn js-interval-register [i]
  (wbase/js-interval-register i))

(defn make-css-inline [tag & stylings]
  (assert (tag? tag) (str "make-css-inline> tag param not a tag "
                       (mx-type tag) :tag tag))
  #_(prn :mkcss-sees tag (for [[k _] (partition 2 stylings)] k)
      stylings)
  (apply make
    :name :inline-css
    :mx-type :web-mx.css/css
    :tag tag
    :css-keys (for [[k _] (partition 2 stylings)] k)
    stylings))

(defn style-string [s]
  (let [ss (cond
             (string? s) s
             (nil? s) ""

             (map? s)
             (str/join ";"
               (for [[k v] s
                     :when v]
                 (pp/cl-format nil "~a:~a" (name k) (kw$ v))))

             (= :web-mx.css/css (mx-type s))
             (style-string (select-keys @s (:css-keys @s)))

             :default
             (do
               (prn :ss-unknown s (type s))
               ""))]
    ;; (pln :mxw-gens-ss ss)
    ss))

(defmethod watch-by-type [:web-mx.css/css] [slot me newv oldv _]
  (when (not= oldv unbound)
    (let [dom (tag-dom (mget me :tag))]
      (gstyle/setStyle dom (name slot) (kw$ newv)))))

(defn jso-map
  "Uses the Google Closure object module to get the keys and values of any JavaScript Object
  and put them into a ClojureScript map"
  [obj]
  (walk/keywordize-keys (zipmap (gobj/getKeys obj) (gobj/getValues obj))))

(defn evt-mx [e]
  ;; deprecated. "md" for "model" is better
  (dom-tag (.-target e)))

(defn evt-md [e]
  (dom-tag (.-target e)))

(defn target-value [evt]
  (form/getValue (.-target evt)))