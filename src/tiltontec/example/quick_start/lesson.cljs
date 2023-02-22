(ns tiltontec.example.quick-start.lesson
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
  {:menu     "HTML/CSS"
   :title    "It's Just HTML and CSS. And SVG."
   :ns       "tiltontec.example.quick-start.lesson/just-html"
   :builder  just-html
   :preamble "We start simply, just coding with standard HTML and CSS. And SVG."
   :comment  "Most of the time, <a href=https://developer.mozilla.org/en-US/docs/Web/HTML>Mozilla HTML</a> will be our reference manual."
   :code     "(div {:class :intro}\n    (h2 \"The count is now....\")\n    (span {:class :digi-readout} \"42\")\n    (button {:class   :push-button\n             :onclick #(js/alert \"Increment Feature Not Yet Implemented\")}\n      \"+\"))"
   :exercise "Feel free to experiment with other HTML tags.<br><br>Where HTML has <code>&lt;tag attributes*> children*&lt;/tag></code><br>...Web/MX has: <code>(tag {attributes*} children*)</code>.<br><br> If you find some HTML that does not translate to Web/MX, please send that example along."})


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
  {:menu     "+ CLJS"
   :title    "...and CLJS" :builder and-cljs
   :preamble "We just write HTML, but CLJS is welcome, too."
   :code     "(div {:class :intro}\n    (h2 \"The count is now....\")\n    (span {:class :digi-readout} \"42\")\n    (doall (for [opcode [\"-\" \"=\" \"+\"]]\n             (button {:class   :push-button\n                      :onclick #(js/alert \"Feature Not Yet Implemented\")}\n               opcode))))"
   :comment  "In fact, all this code is CLJS. For example, DIV is a CLJS macro wrapping a function call.The DIV function returns a
   proxy \"model\" from which we build the actual DOM. Models are Matrix objects that suppport reactive properties."})

;;; --- components realized --------------------------------

(defn opcode-button [label onclick]
  ;; this could be an elaborate component
  (button {:class   :push-button
           :onclick onclick}
    label))

(defn opcode-buttons [& opcodes]
  (div {:style {:display :flex
                :gap     "1em"}}
    (mapv (fn [opcode]
            (opcode-button opcode
              #(js/alert "Feature Not Yet Implemented")))
      opcodes)))

(defn component-ish []
  (div {:class :intro}
    (h2 "The count is now....")
    (span {:class :digi-readout} "42")
    (opcode-buttons "-" "=" "+")))

(def ex-component-ish
  {:menu     "Composition"
   :title    "HTML + Functional Composition"
   :builder  component-ish
   :preamble "Because it is all CLJS, we can move sub-structure into functions."
   :code     "(defn opcode-button [label onclick]\n  ;; this could be an elaborate component\n  (button {:class   :push-button\n           :onclick onclick}\n    label))\n\n(defn opcode-buttons [& opcodes]\n  (div {:style {:display :flex\n                :gap     \"1em\"}}\n    (mapv (fn [opcode]\n            (opcode-button opcode\n              #(js/alert \"Feature Not Yet Implemented\")))\n      opcodes)))\n\n(defn component-ish []\n  (div {:class :intro}\n    (h2 \"The count is now....\")\n    (span {:class :digi-readout} \"42\")\n    (opcode-buttons \"-\" \"=\" \"+\")))"
   :comment  "Composing HTML is now as easy as function composition.
   Think HTML <a href=https://developer.mozilla.org/en-US/docs/Web/Web_Components>\"Web Components\"</a>, built with the power of HLL."})

;;; --- custom-state ---------------------------------

(defn custom-state []
  (div {:class :intro}
    (h2 "The count is now...")
    (span {:class :digi-readout}
      {:mph 42}
      (str (mget me :mph) " mph"))))

(def ex-custom-state
  {:menu     "In-place State"
   :title    "\"In-place\", local widget state"
   :builder  custom-state
   :preamble "An optional second parameter map defines custom widget properties."
   :comment  "We enjoy the power of the prototype model of objects, in which custom properties can be specified as needed to support a tag's (re-)use.
   <br><br>Here, <code>{:mph 42}</code> lets us extend the <code>span</code> with useful state.
   The SPAN reads (<i>mgets</i>) the count to decide its full display.<br><br>
   Later, we see how \"in-place\" state obviates the need for a separate store."})

;;; --- handler mutation -----------------------------

(defn handler-mutation []
  (div {:class :intro}
    (h2 "The speed is now...")
    (span {:class   :digi-readout
           :style   (cF {:color (if (> (mget me :mph) 55)
                                  "red" "cyan")})
           :onclick (fn [evt]
                      (let [me (evt-md evt)]
                        (mswap! me :mph inc)))}
      {:mph     (cI 42)
       :display (cF (str (mget me :mph) " mph"))}
      (mget me :display))
    (p "Click display to increment.")))

(def ex-handler-mutation
  {:menu     "State change"
   :title    "State change + Derived state"
   :ns       "tiltontec.example.quick-start.lesson/handler-mutation"
   :builder  handler-mutation
   :preamble "Event handlers can freely mutate 'input' properties using <code>mswap!</code> or
   aliases <code>mset!/mreset!</code>.
   <br><br>Derived values, wrapped in <code>(cF ...etc...)</code>, re-run as needed.
   <br><br>Speed limit is fifty-five, by the way."
   :code     "(div {:class :intro}\n    (h2 \"The speed is now...\")\n    (span {:class   :digi-readout\n           :style   (cF {:color (if (> (mget me :mph) 55)\n                                  \"red\" \"cyan\")})\n           :onclick (fn [evt]\n                      (let [me (evt-md evt)]\n                        (mswap! me :mph inc)))}\n      {:mph       (cI 42)\n       :display   (cF (str (mget me :mph) \" mph\"))}\n      (mget me :display))\n    (p \"Click display to increment.\"))"
   :exercise "Add custom state <code>:throttled</code>, with a formula that computes <code>true</code> if <code>:mph</code> is
   fifty-five or more. Check <code>:throttled</code> in the <code>:onclick</code> handler before incrementing."
   :comment  "Wrapping <code>:mph</code> in <code>(cI 42)</code> lets us mutate <code>:mph</code> from arbitrary code.
   <br><br>In the formula <code>(cF (str (mget me :mph) \" mph\"))</code>, simply <i>reading</i> the <code>:mph</code> property via <code>mget</code> transparently establishes
    the dependency on <code>:mph</code>.
   No explicit subscription necessary.
   <br><br>Just changing the <code>:mph</code> property, via <code>mswap!</code>, transparently updates all dependent properties.
    No pre-defined store transaction necessary."})

;;; --- watches ----------------------------------

(defn watches []
  (div {:class :intro}
    (h2 "The speed is now...")
    (span {:class   :digi-readout
           :onclick #(mswap! (evt-md %) :mph inc)}
      {:mph     (cI 42 :watch (fn [slot me new-val prior-val cell]
                                (prn :watch slot new-val)))
       :display (cF (str (mget me :mph) " mph"))}
      (mget me :display))
    (p "Click display to increment.")))

(def ex-watches
  {:menu     "Watch Functions"
   :title    "\"On-change\" watch functions"
   :builder  watches
   :preamble "Any input or computed cell can be assigned a 'watch' function."
   :code     "(div {:class :intro}\n    (h2 \"The count is now...\")\n    (span {:class   :digi-readout\n           :onclick #(mswap! (evt-md %) :mph inc)}\n      {:mph (cI 42 :watch (fn [slot me new-val prior-val cell]\n                            (prn :watch slot new-val)))\n       :display (cF (str (mget me :mph) \" mph\"))}\n      (mget me :display))\n    (p \"Click display to increment.\"))"
   :comment  ["Please open the browser JS console to see the output."
              "A 'watch' function fires when a cell value is initialized, and if it changes. They are used to
   dispatch actions outside Matrix state, if only logging, as here."]})

;;; --- throttling watch -------------------

(defn throttle []
  (div {:class :intro}
    (h2 "The speed is now...")
    (span {:class   :digi-readout
           :onclick #(mswap! (evt-md %) :mph inc)}
      {:mph     (cI 42 :watch (fn [slot me new-val prior-val cell]
                                (when (> new-val 55)
                                  (with-cc :speed-governor
                                    (mset! me :mph 45)))))
       :display (cF (str (mget me :mph) " mph"))}
      (mget me :display))
    (p "Click display to increment.")))

(def ex-throttle
  {:menu     "Watch Mutation"
   :title    "Matrix state mutation by watches"
   :builder  throttle
   :preamble ["Watch functions must operate outside Matrix state flow, but they <i>can</i> indirectly alter the Matrix,
   by enqueueing a deferred alteration."
              "Try increasing the speed above 55."]
   :code     "(div {:class :intro}\n    (h2 \"The speed is now...\")\n    (span {:class   :digi-readout\n           :onclick #(mswap! (evt-md %) :mph inc)}\n      {:mph     (cI 42 :watch (fn [slot me new-val prior-val cell]\n                                (when (> new-val 55)\n                                  (with-cc :speed-governor\n                                    (mset! me :mph 45)))))\n       :display (cF (str (mget me :mph) \" mph\"))}\n      (mget me :display))\n    (p \"Click display to increment.\"))"
   :comment  ["In our experience of Matrix coding, we frequently spot opportunities where the app could usefully
   update state normally controlled by the user."
              "The macro <code>(with-cc :my-tag (mset! ...))</code> schedules the mutation for execution
              immediately after the current propagation."]})

(defn ephemeral []
  (div {:class :intro}
    {:name        :roulette
     :bet         (cI nil :ephemeral? true
                    :obs (fn [_ me new-val _ _]
                           (prn :bet-obs-sees new-val (mget me :bet)
                             (mget me :spin) (mget me :outcome) (mget me :bet-history)))) ;; <====== ephemeral
     :bet-history (cF (when-let [bet (mget me :bet)]
                        (conj _cache bet)))                 ;; <====== _cache
     :spin        (cF (when (mget me :bet)
                        (if (zero? (rand-int 2))
                          :black :red)))
     :outcome     (cF (when-let [bet (mget me :bet)]
                        (if (= bet (mget me :spin))
                          :win :loss)))}
    (h2 (str "Faites jeux #" (inc (count (mget (mx-par me) :bet-history)))))
    (div {:style {:display :flex :gap "1em"}}
      (mapv (fn [color]
              (opcode-button color
                #(mset! (fmu :roulette) :bet (keyword color))))
        ["red" "black"]))
    (span {:style (cF (str "visibility:" (name (if (mget (fmu :roulette) :bet)
                                                 :visible :hidden))))}
      "The background below shows the spin.")
    (span {:style (cF (str "font-size:28px; padding:9px; color:white; background:" (if-let [spin (mget (fmu :roulette) :spin)]
                                                                                     (name spin) :white)))}
      (case (mget (fmu :roulette) :outcome)
        :win "Wins!"
        :loss "Loses :("
        "..."))))

(def ex-ephemeral
  {:title    "Ephemerals" :builder ephemeral
   :preamble "When processing events, consecutive identical events are still two different events."
   :code     "(div {:class :intro}\n    {:name    :roulette\n     :bet     (cI nil :ephemeral? true) ;; <====== ephemeral\n     :bet-history (cF (when-let [bet (mget me :bet)]\n                        (conj _cache bet))) ;; <====== _cache\n     :spin    (cF (when (mget me :bet)\n                    (if (zero? (rand-int 2))\n                      :black :red)))\n     :outcome (cF (when-let [bet (mget me :bet)]\n                    (if (= bet (mget me :spin))\n                      :win :loss)))}\n    (h2 (str \"Faites jeux #\" (inc (count (mget (mx-par me) :bet-history)))))\n    (div {:style {:display :flex :gap     \"1em\"}}\n      (mapv (fn [color]\n              (opcode-button color\n                #(mset! (fmu :roulette) :bet (keyword color))))\n        [\"red\" \"black\"]))\n    (span {:style (cF (str \"visibility:\" (name (if (mget (fmu :roulette) :bet)\n                                                 :visible :hidden))))}\n      \"The background below shows the spin.\")\n    (span {:style (cF (str \"font-size:28px; padding:9px; color:white; background:\" (if-let [spin (mget (fmu :roulette) :spin)]\n                                                       (name spin) :white)))}\n      (case (mget (fmu :roulette) :outcome)\n        :win \"Wins!\"\n        :loss \"Loses :(\"\n        \"...\")))"
   :comment  "Ephemeral cells start at nil. When changed to some value X, they propagate fully, then revert silently to nil.
   When they are changed to X again, it is still recognized as a change.
   <br><br>The lexically injected <code>_cache</code> lets us consider history in formulas."})

