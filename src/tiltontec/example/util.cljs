(ns tiltontec.example.util
  (:require [goog.dom :as gdom]
            [tiltontec.model.core :refer [mget]]
            [tiltontec.web-mx.html :refer [tag-dom-create]]))

(defn main [mx-builder]
  (println "[main]: loading")
  (let [root (gdom/getElement "app")
        ;; ^^^ "app" must be ID of DIV defined in index.html
        app-matrix (mx-builder)
        app-dom (tag-dom-create
                  (mget app-matrix :mx-dom))]
    (set! (.-innerHTML root) nil)
    (gdom/appendChild root app-dom)))