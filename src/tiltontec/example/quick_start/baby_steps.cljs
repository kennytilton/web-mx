(ns tiltontec.example.quick-start.baby-steps
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


;;; --- 1. It's just html -------------------------------------
;;; We still program HTML. Please find detailed notes following the code.

(def just-html-code
  "(div {:class :intro}\n    (h2 \"The count is now....\")\n    (span {:class :digi-readout} \"42\")\n    (button {:class   :push-button\n             :onclick #(js/alert \"Increment Feature Not Yet Implemented\")}\n      \"+\"))")

(def just-html-preamble
  "We just write HTML and CSS.")

(defn just-html []
  (div {:class :intro}
    (h2 "The count is now....")
    (span {:class :digi-readout} "42")
    (button {:class   :push-button
             :onclick #(js/alert "Increment Feature Not Yet Implemented")}
      "+")))

;;; Where HTML has <tag attributes*> children* </tag>...
;;; Web/MX has (tag [HTML-attribute-map [custom-attr-map]] children*)
;;; Keywords become strings in HTML.
;;; Otherwise, MDN is your guide.

(def ex-just-html
  {:title    "It's Just HTML&trade;"
   :builder  just-html
   :preamble "We just write HTML and CSS. <a href=https://developer.mozilla.org/en-US/docs/Web/HTML>Mozilla HTML</a> is our manual."
   :code     "(div {:class :intro}\n    (h2 \"The count is now....\")\n    (span {:class :digi-readout} \"42\")\n    (button {:class   :push-button\n             :onclick #(js/alert \"Increment Feature Not Yet Implemented\")}\n      \"+\"))"
   :comment  "Feel free to experiment with other HTML tags.<br><br> Where HTML has:&nbsp;&lt;tag attributes*> children* &lt;/tag><br>\n   Web/MX has: (tag {attributes*} children*).<br><br>If you find some HTML that does not translate to Web/MX, please send that example along."})


;;; --- and-cljs --------------------------------------------------------
(defn and-cljs []
  (div {:class :intro}
    (h2 "The count is now...")
    (span {:class "digi-readout"} "42")
    (div {:style {:display :flex
                  :gap     "1em"}}
      (doall (for [opcode ["-" "=" "+"]]
               (button {:class   :push-button
                        :onclick #(js/alert "Feature Not Yet Implemented")}
                 opcode))))))

(def ex-and-cljs
  {:title    "...and CLJS" :builder and-cljs
   :preamble "We may just write HTML, but we can also intermingle CLJS."
   :code     "(div {:class :intro}\n    (h2 \"The count is now....\")\n    (span {:class :digi-readout} \"42\")\n    (doall (for [opcode [\"-\" \"=\" \"+\"]]\n             (button {:class   :push-button\n                      :onclick #(js/alert \"Feature Not Yet Implemented\")}\n               opcode))))"
   :comment  "In fact, all this code is CLJS. For example, DIV is a CLJS macro wrapping a function call.<br><br>
   The functions behind DIV, BUTTON, et al return proxy \"model\" objects capable of generating actual DOM. Models are Matrix
   objects that suppport reactive properties."})

;;; --- components realized --------------------------------

(defn opcode-button [label onclick]
  ;; this could be an elaborate component
  (button {:class   :push-button
           :onclick onclick}
    label))

(defn component-ish []
  (div {:class :intro}
    (h2 "The count is now....")
    (span {:class :digi-readout} "42")
    (div {:style {:display :flex
                  :gap     "1em"}}
      (mapv (fn [opcode]
              (opcode-button opcode
                #(js/alert "Feature Not Yet Implemented")))
        ["-" "=" "+"]))))

(def ex-component-ish
  {:title    "Components-Ish" :builder component-ish
   :preamble "Because it is all CLJS, we can move sub-structure into functions."
   :code     "(defn opcode-button [label onclick]\n  (button {:class   :push-button\n           :onclick onclick}\n    label))<br><br>(defn component-ish []\n  (div {:class :intro}\n    (h2 \"The count is now....\")\n    (span {:class :digi-readout} \"42\")\n    (div {:style {:display :flex\n                  :gap \"1em\"}}\n      (mapv (fn [opcode]\n              (opcode-button opcode\n               #(js/alert \"Feature Not Yet Implemented\")))\n        [\"-\" \"=\" \"+\"]))))"
   :comment  "Composing HTML is now as easy as function composition.
   In a sense, we have <a href=https://developer.mozilla.org/en-US/docs/Web/Web_Components>HTML Web Components</a>, but with the full power of CLJS.
   Which is nice."})

;;; --- cf-state ---------------------------------

(defn cf-state []
  (div {:class :intro}
    (h2 "The count is now...")
    (span {:class :digi-readout}
      {:mph 42}
      (str (mget me :mph) " mph"))))

(def ex-cf-state
  {:title    "cF/State" :builder cf-state
   :preamble "State. An optional second parameter, here <code>{:count 42}</code>, defines additional properties."
   :code     "(defn cf-state []\n  (div {:class :intro}\n    (h2 \"The count is now half of....\")\n    (span {:class :digi-readout}\n      {:count 42}\n      (str (mget me :count)))))"
   :comment  "We enjoy the power of the protoype model of objects, in which custom properties can be specified as needed to support a tag's (re-)use.
   The SPAN reads (<i>mgets</i>) the count to decide its full display."})

;;; --- handler mutation -----------------------------

(defn handler-mutation []
  (div {:class :intro}
    (h2 "The count is now...")
    (span {:class   :digi-readout
           :onclick #(mswap! (evt-md %) :mph inc)}
      {:mph (cI 42)}
      (str (mget me :mph) " mph"))
    (p "Click display to increment.")))

(def ex-handler-mutation
  {:title    "Mutation I" :builder handler-mutation
   :preamble "Mutating state. Event handlers can freely mutate 'input' properties using <code>mswap!</code>.<br><br>The
   readout keeps up automatically."
   :code     "(div {:class :intro}\n    (h2 \"The count is now...\")\n    (span {:class   :digi-readout\n           :onclick #(mswap! (evt-md %) :count inc)}\n      {:count (cI 42)}\n      (str (mget me :mph) \" mph\")))"
   :comment  "Just <i>reading</i> the <code>:mph</code> property via <code>mget</code> established the readout's dependency on <code>:mph</code>.
   No explicit subscription necessary."})

#_(defn cf-state []
    (div {:class :intro}
      (h2 "The count is now half of....")
      (span {:class :digi-readout}
        {:count        42
         :double-count (cF (* 2 (mget me :count)))}
        (str (mget me :double-count)))))
