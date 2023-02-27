(ns tiltontec.example.quick-start.core
  (:require
    [tiltontec.model.core :as md]
    [tiltontec.example.util :as exu]
    [tiltontec.example.quick-start.lesson :as lesson]))

(exu/main #(md/make ::intro
             :mx-dom (exu/multi-demo "Web/MX&trade;<br>Quick Start" 9
                       lesson/ex-just-html
                       lesson/ex-and-cljs
                       lesson/ex-html-composition
                       lesson/ex-custom-state
                       lesson/ex-derived-state

                       lesson/ex-navigation
                       lesson/ex-handler-mutation
                       lesson/ex-watches
                       lesson/ex-watch-cc
                       ;; lesson/ex-async-throttle
                       lesson/ex-async-cat

                       lesson/ex-data-integrity
                       lesson/ex-in-review
                       ;;lesson/ex-ephemeral ;; too much?
                       #_ {:title "Counter Omniscient" :builder counter-omniscience :code counter-omniscience-code}
                       #_ {:title "Counter Omnipotent" :builder counter-omnipotent :code counter-omnipotent-code}
                       #_ {:title "Reactivity All-In" :builder reactivity-all-in :code reactivity-all-in-code}
                       #_  {:title "Mini test" :builder minitest :code minitest-code})))

;; todo merge with lesson.cljs
