(ns tiltontec.example.quick-start
  (:require
    [clojure.string :as str]
    [clojure.pprint :as pp]
    [tiltontec.cell.base :refer [minfo]]
    [tiltontec.cell.core :refer [cF cF+ cFonce cI cf-freeze]]
    [tiltontec.cell.integrity :refer [with-cc]]
    [tiltontec.model.core
     :refer [mx-par mget mset! mswap! mset! mxi-find mxu-find-name fmu fm!] :as md]
    [tiltontec.web-mx.gen :refer [evt-md target-value]]
    [tiltontec.web-mx.gen-macro
     :refer [img section h1 h2 h3 input footer p a
             span i label ul li div button br
             defexample]]
    [tiltontec.web-mx.style :refer [make-css-inline]]
    [tiltontec.example.util :as exu]
    [cljs-http.client :as client]
    [cljs.core.async :refer [go <!]]))

;;; --- intro counter -----------------------------------

;;; --- 1. It's just html -------------------------------------
;;; We still program HTML. Please find detailed notes following the code.

(def just-html-code
  "(div {:class :intro}\n    (h2 \"The count is now....\")\n    (span {:class :intro-counter} \"42\")\n    (button {:class   :push-button\n             :onclick #(js/alert \"Increment Feature Not Yet Implemented\")}\n      \"+\"))")

(def just-html-preamble
  "We just write HTML and CSS.<br><br> Where HTML has:&nbsp;&lt;tag attributes*> children* &lt;/tag><br>
   Web/MX has: (tag {attributes*} children*).")

(defn just-html []
  (div {:class :intro}
    (h2 "The count is now....")
    (span {:class :intro-counter} "42")
    (button {:class   :push-button
             :onclick #(js/alert "Increment Feature Not Yet Implemented")}
      "+")))

;;; Where HTML has <tag attributes*> children* </tag>...
;;; Web/MX has (tag [HTML-attribute-map [custom-attr-map]] children*)
;;; Keywords become strings in HTML.
;;; Otherwise, MDN is your guide.

;;; --- and-cljs --------------------------------------------------------
(defn and-cljs []
  (div {:class :intro}
    (h2 "The count is now....")
    (span {:class :intro-counter} "42")
    (div {:style {:display :flex
                  :gap "1em"}}
      (doall (for [opcode ["-" "=" "+"]]
             (button {:class   :push-button
                      :onclick #(js/alert "Feature Not Yet Implemented")}
               opcode))))))

(def ex-and-cljs
  {:title "...and CLJS" :builder and-cljs
   :preamble "\"Just HTML\", but we can write CLJS, too. Indeed, is all CLJS. eg, DIV is a CLJS macro wrapping  a function call."
   :code "(div {:class :intro}\n    (h2 \"The count is now....\")\n    (span {:class :intro-counter} \"42\")\n    (doall (for [opcode [\"-\" \"=\" \"+\"]]\n             (button {:class   :push-button\n                      :onclick #(js/alert \"Feature Not Yet Implemented\")}\n               opcode))))"
   :comment "For the curious, the functions behind DIV, BUTTON, et al return proxy objects capable of generating actual DOM."})

;;; --- components realized --------------------------------

(defn opcode-button [label onclick]
  ;; this could be an elaborate component
  (button {:class   :push-button
           :onclick onclick}
    label))

(defn component-ish []
  (div {:class :intro}
    (h2 "The count is now....")
    (span {:class :intro-counter} "42")
    (div {:style {:display :flex
                  :gap "1em"}}
      (doall (for [opcode ["-" "=" "+"]]
               (opcode-button opcode
                 #(js/alert "Feature Not Yet Implemented")))))))

(def ex-component-ish
  {:title "Components-Ish" :builder component-ish
   :preamble "Because it is all CLJS, we can move sub-structure into functions, as if they were HTML components,
   but with the full power of CLJS. They just have to return a w/mx DOM proxy."
   :code "(defn opcode-button [label onclick]\n  ;; this could be an elaborate component\n  (button {:class   :push-button\n           :onclick onclick}\n    label))\n\n(div {:class :intro}\n    (h2 \"The count is now....\")\n    (span {:class :intro-counter} \"42\")\n    (div {:style {:display :flex\n                  :gap \"1em\"}}\n      (doall (for [opcode [\"-\" \"=\" \"+\"]]\n               (opcode-button opcode\n                 #(js/alert \"Feature Not Yet Implemented\"))))))"
   })

;;; --- cf-state ---------------------------------

(defn cf-state []
  (div {:class :intro}
    (h2 "The count is now half of....")
    (span {:class :intro-counter}
      {:count 42
       :double-count (cF (* 2 (mget me :count)))}
      (str (mget me :double-count)))))

(def ex-cf-state
  {:title "cF/State" :builder cf-state
   :preamble "State rears its ugly head."
   :code "(div {:class :intro}\n    (h2 \"The count is now half of....\")\n    (span {:class :intro-counter}\n      {:count 42\n       :double-count (cF (* 2 (mget me :count)))}\n      (str (mget me :double-count))))"
   })

(exu/main #(md/make ::intro
             :mx-dom (exu/multi-demo 99
                       {:title "Just HTML&trade;" :preamble just-html-preamble :builder just-html :code just-html-code}
                       ex-and-cljs
                       ex-component-ish
                       ex-cf-state
                       #_ {:title "Counter Omniscient" :builder counter-omniscience :code counter-omniscience-code}
                       #_ {:title "Counter Omnipotent" :builder counter-omnipotent :code counter-omnipotent-code}
                       #_ {:title "Reactivity All-In" :builder reactivity-all-in :code reactivity-all-in-code}
                      #_  {:title "Mini test" :builder minitest :code minitest-code})))
