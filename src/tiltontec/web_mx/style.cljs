(ns tiltontec.web-mx.style
  (:require
    [tiltontec.cell.poly :refer [watch watch-by-type
                                 md-quiesce md-quiesce-self] :as cw]
    [tiltontec.matrix.api :refer
     [minfo md-ref? unbound make mget mget?
      the-kids mdv! any-ref? rmap-meta-setf mx-type
      fm-navig mget mget? fasc fm! mset! backdoor-reset!]]
    [tiltontec.web-mx.base :refer [tag? kw$ tag-dom]]
    [goog.dom.classlist :as classlist]
    [goog.style :as gstyle]
    [goog.dom :as dom]
    [cljs.pprint :as pp]
    [clojure.string :as str]))

(defn make-css-inline [tag & stylings]
  (assert (tag? tag) (str "make-css-inline> tag param not a tag "
                       (mx-type tag) :meta (meta tag)))
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