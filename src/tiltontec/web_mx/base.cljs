(ns ^:figwheel-hooks tiltontec.web-mx.base
  (:require
    [clojure.string :as str]
    [clojure.walk :as walk]
    [goog.dom :as gdom]
    [goog.object :as gobj]
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
                (backdoor-reset! me :dom-cache (gdom/getElement (str id))))
        ]
    (when (nil? dom) (mxwprn :id-not-in-dom-or-cache id))
    (when (nil? dom-x) (mxwprn :no-dom-x (meta me)))
    (if (= dom dom-x)
      (mxwprn :dom-same!!!! dom)
      (mxwprn :don-not-eq dom (:dom-x (meta me))))

    (or dom dom-x)))

(defn jso-select-keys
  ([obj]
   (jso-select-keys (gobj/getKeys obj)))
  ([obj keyseq]
   (walk/keywordize-keys (zipmap keyseq
                           (map #(gobj/get obj (name %)) keyseq)))))

(defn css-to-map [css-string]
  (as-> css-string out
    (str/replace out #"\n" "")
    (str/split out #";")
    (map str/trim out)
    (map #(str/split % #":") out)
    (map (fn [[k v]]
           [(keyword k) (str/trim v)]) out)
    (into {} out)))

(comment
  #_(def cs "border-color: orange;\n  border-style: solid;\n  border-width: 2px;\n
    max-width: 40em;\n  min-width: 40em;\n  margin-top: 9px;")
  #_ (def cs "display:         flex;\n    flex-direction: column;\n    align-items:     center;\n    justify-content: start;\n    gap: 1em;\n    padding: 9px;")
  #_ (def cs "background-color: rgba(51, 51, 51, 0.05);\n  border-radius: 16px;\n  border-width: 0;\n  color: #333333;\n  cursor: pointer;\n  font-family: \"Haas Grot Text R Web\", \"Helvetica Neue\", Helvetica, Arial, sans-serif;\n  font-size: 64px;\n  min-width: 1em;\n  text-align: center;\n")
  (def cs "  background-color: rgba(51, 51, 51, 0.05);\n  border-radius: 8px;\n  border-width: 1px;\n  color: #333333;\n  cursor: pointer;\n  display: inline-block;\n  font-family: \"Haas Grot Text R Web\", \"Helvetica Neue\", Helvetica, Arial, sans-serif;\n  font-size: 14px;\n  font-weight: 500;\n  line-height: 20px;\n  list-style: none;\n  padding: 10px 12px;\n  text-align: center;\n  transition: all 200ms;\n  vertical-align: baseline;\n  white-space: nowrap;\n  user-select: none;\n  -webkit-user-select: none;\n  touch-action: manipulation;")


  (clojure.pprint/pprint (css-to-map cs)))