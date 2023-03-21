(ns ^:figwheel-hooks tiltontec.web-mx.base
  (:require
    [clojure.string :as str]
    [goog.dom :as dom]
    [tiltontec.matrix.api
     :refer [mget fasc fm! make mset! backdoor-reset! mx-type]]))

(defonce js-intervals (atom nil))

(defn js-interval-register [interval]
  (swap! js-intervals conj interval)
  interval)

(defn ^:before-load teardown []
  (doseq [i @js-intervals]
    (js/clearInterval i))
  (reset! js-intervals nil))

(def ^:dynamic *web-mx-trace* false)

(defn tag? [me]
  (= (mx-type me) :web-mx.base/tag))

(defn kw$ [kw]
  ;; use this wherever we might want to allow a keyword instead of an
  ;; attribute value or style value string, eg a class or color
  (if (keyword? kw)
    (name kw)
    kw))

(defn attr-val$ [val]
  (cond
    (string? val) val
    (keyword? val) (name val)
    (coll? val) (str/join " " (map attr-val$ val))
    (fn? val) val
    :else (str val)))

(defn mxwprn [& bits]
  (when *web-mx-trace*
    (apply prn :web-mx> bits)))

(defn tag-dom [me]
  ;; This will return nil when 'me' is being awakened and rules
  ;; are firing for the first time, because 'me' has not yet
  ;; been installed in the actual DOM, so call this only
  ;; from event handlers and the like.
  (let [id (mget me :id)
        dom-x (:dom-x (meta me))
        dom (or (:dom-cache @me)
                (backdoor-reset! me :dom-cache (dom/getElement (str id))))
        ]
    (when (nil? dom) (mxwprn :id-not-in-dom-or-cache id))
    (when (nil? dom-x) (mxwprn :no-dom-x (meta me)))
    (if (= dom dom-x)
      (mxwprn :dom-same!!!! dom)
      (mxwprn :don-not-eq dom (:dom-x (meta me))))

    (or dom dom-x)))