(ns ^:figwheel-hooks tiltontec.example.rxtrak.core
  (:require
    [goog.dom :as dom]
    [tiltontec.model.core :refer [mget]]
    [tiltontec.web-mx.base :refer [ *web-mx-trace*]]
    [tiltontec.web-mx.html :refer [tag-dom-create io-truncate]]
    [tiltontec.example.rxtrak.rx :refer [RX_LS_PREFIX] :as rx]
    [tiltontec.example.rxtrak.build :as rxtrak]
    [taoensso.tufte :as tufte :refer (defnp p profiled profile)]))

(enable-console-print!)
(tufte/add-basic-println-handler! {})

(let [root (dom/getElement "app")
      app-matrix (rxtrak/matrix-build!)
      app-dom (tag-dom-create
                (mget app-matrix :mx-dom))]
  ;; (io-truncate rx/RX_LS_PREFIX)
  (set! (.-innerHTML root) nil)
  (dom/appendChild root app-dom)
  (when-let [route-starter (mget app-matrix :router-starter)]
    (route-starter)))
