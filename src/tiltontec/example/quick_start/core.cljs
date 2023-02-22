(ns tiltontec.example.quick-start.core
  (:require
    [tiltontec.model.core :as md]
    [tiltontec.example.util :as exu]
    [tiltontec.example.quick-start.lesson :as baby]))

(exu/main #(md/make ::intro
             :mx-dom (exu/multi-demo "Web/MX&trade;<br>Quick Start" 4
                       baby/ex-just-html
                       baby/ex-and-cljs
                       baby/ex-component-ish
                       baby/ex-custom-state
                       baby/ex-derived-state
                       baby/ex-handler-mutation
                       baby/ex-watches
                       baby/ex-throttle
                       ;; todo the cells manifesto
                       baby/ex-ephemeral
                       #_ {:title "Counter Omniscient" :builder counter-omniscience :code counter-omniscience-code}
                       #_ {:title "Counter Omnipotent" :builder counter-omnipotent :code counter-omnipotent-code}
                       #_ {:title "Reactivity All-In" :builder reactivity-all-in :code reactivity-all-in-code}
                       #_  {:title "Mini test" :builder minitest :code minitest-code})))
