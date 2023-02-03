(ns tiltontec.web-mx.core
  (:require
    [clojure.string :as str]

    [taoensso.tufte :as tufte :refer [defnp p profiled profile]]
    [cljs-time.coerce :refer [from-long to-string] :as tmc]


    [tiltontec.model.core :as md]

    [goog.dom :as dom]
    [tiltontec.web-mx.base :refer [ *web-mx-trace*]]
    [tiltontec.web-mx.html :refer [tag-dom-create ]]

    ;; [tiltontec.web-mx.mxintro.rxtrak :as app] ;; Intro app for Lisp-NYC, 2018

    ;;[tiltontec.example.gloss :as app]
    ;;[tiltontec.example.testing :as app]
    ;;[tiltontec.example.todomvc :as app]
    ;;[tiltontec.example.gentle-intro :as app]
    [tiltontec.example.ticktock :as app] ;; use ticktock.html to get css
    ;;[tiltontec.example.pipeline.core :as app] ;; use pipeline.html to get css
    ;;[tiltontec.example.startwatch :as app] ;; use startwatch.html to get css
    ;[tiltontec.example.flux-challenge :as app]
    )
  (:import [goog.date UtcDateTime]))

(enable-console-print!)
(tufte/add-basic-println-handler! {})

(let [root (dom/getElement "tagroot")
      app-matrix (app/matrix-build!)
      app-dom (binding [*web-mx-trace* nil]                  ;; <-- set to nil if console too noisy
                (tag-dom-create
                  (md/mget app-matrix :mx-dom)))

      start-ms (.getTime (js/Date.))
      start$ (tmc/to-string (tmc/from-long start-ms))]

  (set! (.-innerHTML root) nil)
  (dom/appendChild root app-dom)
  (when-let [route-starter (md/mget app-matrix :router-starter)]
    ;; (prn :starting-router)
    (route-starter)))
