(ns tiltontec.web-mx.core
  (:require
    [tiltontec.matrix.api :refer [ mget]]
    [tiltontec.web-mx.api :refer [tag-dom-create ]]
    [goog.dom :as dom]
    [tiltontec.web-mx.base :refer [ *web-mx-trace*]]
    [tiltontec.example.simpleclock :as app])
  (:import [goog.date UtcDateTime]))

(enable-console-print!)
;(tufte/add-basic-println-handler! {})

(let [root (dom/getElement "app")
      app-matrix (app/matrix-build!)
      app-dom (binding [*web-mx-trace* nil]                  ;; <-- set to nil if console too noisy
                (tag-dom-create
                  (mget app-matrix :mx-dom)))]

  (set! (.-innerHTML root) nil)
  (dom/appendChild root app-dom)
  (when-let [rs (mget app-matrix :router-starter)]
    ;; (prn :starting-router rs)
    (rs)))
