(ns tiltontec.web-mx.style
  (:require
    [tiltontec.util.base :refer [type-cljc]]
    [tiltontec.util.core :refer [pln]]
    [tiltontec.cell.base :refer [md-ref? ia-type unbound minfo]]
    [tiltontec.cell.observer :refer [observe observe-by-type]]
    [tiltontec.cell.evaluate :refer [finalize finalize-self]]
    [tiltontec.model.core
     :refer-macros [the-kids mdv!]
     :refer [mget fasc fm! make mset! backdoor-reset!]
     :as md]
    [tiltontec.web-mx.base :refer [tag? kw$ tag-dom]]
    [goog.dom.classlist :as classlist]
    [goog.style :as gstyle]
    [goog.dom :as dom]
    [cljs.pprint :as pp]
    [clojure.string :as str]))

(defn make-css-inline [tag & stylings]
  (assert (tag? tag))
  #_ (prn :mkcss-sees tag (for [[k _] (partition 2 stylings)] k)
    stylings)
  (apply make
    :name :inline-css
    :type :web-mx.css/css
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

             (= :web-mx.css/css (ia-type s))
             (style-string (select-keys @s (:css-keys @s)))

             :default
             (do
               (pln :ss-unknown s (type s))
               ""))]
    ;; (pln :mxw-gens-ss ss)
    ss))

(defmethod observe-by-type [:web-mx.css/css] [slot me newv oldv _]
  (when (not= oldv unbound)
    (let [dom (tag-dom (mget me :tag))]
      (gstyle/setStyle dom (name slot) (kw$ newv)))))