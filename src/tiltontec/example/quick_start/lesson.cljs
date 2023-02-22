(ns tiltontec.example.quick-start.lesson
  (:require
    [clojure.string :as str]
    [clojure.pprint :as pp]
    [tiltontec.cell.base :refer [minfo]]
    [tiltontec.cell.core :refer [cF cF+ cFonce cI cf-freeze]]
    [tiltontec.cell.integrity :refer [with-cc]]
    [tiltontec.model.core
     :refer [mx-par mget mset! mswap! mset! mxi-find mxu-find-name mdv! fmu fm! fm-navig] :as md]
    [tiltontec.web-mx.gen :refer [evt-md target-value]]
    [tiltontec.web-mx.gen-macro
     :refer [img section h1 h2 h3 input footer p a
             span i label ul li div button br

             svg g circle p span div text radialGradient defs stop
             rect ellipse line polyline path polygon script use]]))


;;; --- 1. It's just html -------------------------------------
;;; We still program HTML. Please find detailed notes following the code.

(defn just-html []
  (div {:class :intro}
    (h2 "The count is now....")
    (span {:class :digi-readout} "42")
    (svg {:width   64 :height 64 :cursor :pointer
          :onclick #(js/alert "Increment Feature Not Yet Implemented")}
      (circle {:cx     "50%" :cy "50%" :r "40%"
               :stroke "orange" :stroke-width 5
               :fill   :transparent})
      (text {:class       :heavychar :x "50%" :y "70%"
             :text-anchor :middle} "+"))))

;;; Where HTML has <tag attributes*> children* </tag>...
;;; Web/MX has (tag [HTML-attribute-map [custom-attr-map]] children*)
;;; Keywords become strings in HTML.
;;; Otherwise, MDN is your guide.

(def ex-just-html
  {:menu     "Just HTML"
   :title    "It's Just HTML"
   :ns       "tiltontec.example.quick-start.lesson/just-html"
   :builder  just-html
   :preamble "We start with standard HTML, SVG, and CSS, thinly disguised as CLJS."
   :comment  ["When we are not writing business logic, <a href=https://developer.mozilla.org/en-US/docs/Web/HTML>Mozilla HTML</a> will be our reference."
              "Web/MX introduces no framework of its own. Aside from CLJS->JS, no preprocessor is involved. Matrix just manages state."]
   :code     "(div {:class :intro}\n    (h2 \"The count is now....\")\n    (span {:class :digi-readout} \"42\")\n    (svg {:width 64 :height 64 :cursor :pointer\n          :onclick #(js/alert \"Increment Feature Not Yet Implemented\")}\n      (circle {:cx \"50%\" :cy \"50%\" :r \"40%\"\n               :stroke  \"orange\" :stroke-width 5\n               :fill :transparent})\n      (text {:class :heavychar :x \"50%\" :y \"70%\"\n             :text-anchor :middle} \"+\")))"
   :exercise ["Feel free to experiment with other HTML or SVG tags."
              "Where HTML has <code>&lt;tag attributes*> children*&lt;/tag></code><br>...Web/MX has: <code>(tag {attributes*} children*)</code>."
              "If you find some HTML that does not translate to Web/MX, please send a failing example along."]})


;;; --- and-cljs --------------------------------------------------------
(defn and-cljs []
  (div {:class :intro}
    (h2 "The count is now...")
    (span {:class "digi-readout"} "42")
    (div {:style {:display :flex
                  :gap     "1em"}}
      (mapv (fn [opcode]
              (button {:class   :push-button
                       :onclick #(js/alert (str "Opcode \"" opcode "\" not yet implemented"))}
                opcode))
        ["-" "=" "+"]))))

(def ex-and-cljs
  {:menu     "+ CLJS"
   :title    "...and CLJS" :builder and-cljs
   :preamble "Maybe we \"just write HTML\", but CLJS is welcome, too."
   :code     "(div {:class :intro}\n    (h2 \"The count is now...\")\n    (span {:class \"digi-readout\"} \"42\")\n    (div {:style {:display :flex\n                  :gap     \"1em\"}}\n      (mapv (fn [opcode]\n              (button {:class   :push-button\n                       :onclick #(js/alert (str \"Opcode \\\"\" opcode \"\\\" not yet implemented\"))}\n                opcode))\n        [\"-\" \"=\" \"+\"])))"
   :comment  ["In fact, all this code is CLJS. For example, DIV is a CLJS macro that returns
    a Clojure <i>proxy</i> for a DOM DIV."
              "n.b. Proxies are not VDOM. Proxies are long-lived models that manage their DOM incarnations as events unfold."]})

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
   :title    "Functional Composition, for HTML"
   :builder  component-ish
   :preamble "Because it is all CLJS, we can move sub-structure into functions."
   :code     "(defn opcode-button [label onclick]\n  ;; this could be an elaborate component\n  (button {:class   :push-button\n           :onclick onclick}\n    label))\n\n(defn opcode-buttons [& opcodes]\n  (div {:style {:display :flex\n                :gap     \"1em\"}}\n    (mapv (fn [opcode]\n            (opcode-button opcode\n              #(js/alert \"Feature Not Yet Implemented\")))\n      opcodes)))\n\n(defn component-ish []\n  (div {:class :intro}\n    (h2 \"The count is now....\")\n    (span {:class :digi-readout} \"42\")\n    (opcode-buttons \"-\" \"=\" \"+\")))"
   :comment  "Composing HTML is just function composition.
   Think nested <a href=https://developer.mozilla.org/en-US/docs/Web/Web_Components>\"Web Components\"</a>."})

;;; --- custom-state ---------------------------------

(defn custom-state []
  (div {:class :intro}
    (h2 "The speed is now...")
    (span {:class :digi-readout}
      {:mph 42}
      (str (mget me :mph) " mph"))))

(def ex-custom-state
  {:menu     "In-place State"
   :title    "\"In-place\", local widget state"
   :builder  custom-state
   :preamble "Components can define their own local state."
   :code     "(div {:class :intro}\n    (h2 \"The speed is now...\")\n    (span {:class :digi-readout}\n      {:mph 42}\n      (str (mget me :mph) \" mph\")))"
   :comment  ["An optional second parameter map extends generic widgets with custom widget state. Here, <code>{:mph 42}</code> extends a generic <code>span</code> with useful state.
   The SPAN then reads the <code>:mph</code> to generate its full display content."
              "Matrix supports the <a href=https://en.wikipedia.org/wiki/Prototype-based_programming target=\"_blank\">prototype model</a>,
    allowing easy reuse of predefined tags such as DIV. Later, we will see how \"in-place\" state obviates the need for a separate store."]})

;;; --- derived state ------------------------------


(defn derived-state []
  (div {:class :intro}
    (h2 "The speed is now...")
    (span {:class :digi-readout}
      {:mph         65
       :too-fast?   (cF (> (mget me :mph) 55))
       :speedo-text (cF (str (mget me :mph) " mph"
                          (when (mget me :too-fast?) "<br>Slow down?")))}
      (mget me :speedo-text))))

(def ex-derived-state
  {:menu     "One-way DAG"
   :title    "Derived state forms a DAG"
   :builder  derived-state
   :code     "(div {:class :intro}\n    (h2 \"The speed is now...\")\n    (span {:class :digi-readout}\n      {:mph       65\n       :too-fast? (cF (> (mget me :mph) 55))\n       :speedo-text (cF (str (mget me :mph) \" mph\"\n                          (when (mget me :too-fast?) \"<br>Slow down?\")))}\n      (mget me :speedo-text)))"
   :preamble "A property can be expressed as a function of other model properties."
   :comment  ["Colloquially, we call these functions \"formulas\", after the familiar spreadsheet usage.
    The <code>:too-fast?</code> property is fed by the reactive formula <code>(cF (> (mget me :mph) 55))</code>.
    When <code>:mph</code> changes, <code>:too-fast?</code> will be recomputed."
              "A one-way DAG emerges naturally from the population of inter-dependent properties."]})

;;; --- Navigation ------------------------------

(defn navigation [geo-type]
  (div {:class :intro}
    {:name        :speed-zone
     :speed-limit 55}
    (h2 {}
      {:text (cF (let [limit (mget (fm-navig :speed-zone me) :speed-limit)
                       speed (mget (fm-navig :speedo me) :mph)]
                   (str "The speed is now "
                     (- speed limit) " mph over the speed limit.")))}
      (mget me :text))
    (span {:class :digi-readout}
      {:name      :speedo
       :mph       60
       :too-fast? (cF (> (mget me :mph)
                        #_(mdv! :speed-zone :speed-limit)
                        (mget (fmu :speed-zone) :speed-limit)))}
      (str (mget me :mph) " mph"
        (when (mget me :too-fast?) "<br>Slow down")))))

(def ex-navigation
  {:menu     "Navigation"
   :title    "Accessing DAG state"
   :builder  navigation
   :preamble "A widget can retrieve state as needed from any other component."
   :code     "(div {:class :intro}\n    {:name :speed-zone\n     :speed-limit 55}\n    (h2 {}\n      {:text (cF (let [limit (mget (fm-navig :speed-zone me) :speed-limit)\n                       speed (mget (fm-navig :speedo me) :mph)]\n                   (str \"The speed is now \"\n                     (- speed limit) \" mph over the speed limit.\")))}\n      (mget me :text))\n    (span {:class :digi-readout}\n      {:name :speedo\n       :mph 60\n       :too-fast? (cF (> (mget me :mph)\n                        (mget (fmu :speed-zone) :speed-limit)))}\n      (str (mget me :mph) \" mph\"\n        (when (mget me :too-fast?) \"<br>Slow down\"))))"
   :comment  ["The application is its own database. The headline needs the speed limit and current speed. The speedometer readout also needs
     the speed limit. We retrieve them from where they naturally reside."
              "n.b All Matrix reactivity is between properties. The dependency information, automatically
              detected by Matrix, forms the one-way DAG needed to enforce state consistency."]})

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
    (p "Click the readout to speed up.")))

(def ex-handler-mutation
  {:menu     "State change"
   :title    "Changing DAG state"
   :ns       "tiltontec.example.quick-start.lesson/handler-mutation"
   :builder  handler-mutation
   :preamble ["Event handlers can freely mutate 'input' properties. Derived values will keep up."
              "The speed limit is fifty-five, by the way."]
   :code     "(div {:class :intro}\n    (h2 \"The speed is now...\")\n    (span {:class   :digi-readout\n           :style   (cF {:color (if (> (mget me :mph) 55)\n                                  \"red\" \"cyan\")})\n           :onclick (fn [evt]\n                      (let [me (evt-md evt)]\n                        (mswap! me :mph inc)))}\n      {:mph       (cI 42)\n       :display   (cF (str (mget me :mph) \" mph\"))}\n      (mget me :display))\n    (p \"Click display to increment.\"))"
   :exercise "Add custom state <code>:throttled</code>, with a formula that computes <code>true</code> if <code>:mph</code> is
   fifty-five or more. Check <code>:throttled</code> in the <code>:onclick</code> handler before allowing increment."
   :comment  ["<code>cI</code> stands for \"cell Input\". <code>cF</code> stands for \"cell Formula\". Wrapping <code>:mph</code> in <code>(cI 42)</code> lets us mutate <code>:mph</code> imperatively, here
   in an event handler. "
              "In the formula <code>(cF (str (mget me :mph) \" mph\"))</code>, simply reading the <code>:mph</code> property
     transparently links <code>:display</code> and <code>:mph</code>.
   No explicit \"subscribe\" is necessary."
              "Just changing the <code>:mph</code> property, via <code>mswap!</code>, transparently updates all dependent properties.
    No pre-defined store update transaction is necessary."]})

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
   :comment  ["Open the browser console to see the 'watch' output. A 'watch' function fires when a cell value is initialized, and if it changes. They are used to
   dispatch actions outside Matrix state, if only for logging/debugging, as here."]})

;;; --- throttling watch -------------------

(defn watch-cc []
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

(def ex-watch-cc
  {:menu     "Watch Mutation"
   :title    "Exception: watches mutating the DAG"
   :builder  watch-cc
   :preamble "Watch functions must operate outside Matrix state flow, but they <i>can</i> enqueue <i>deferred</i> alterations of Matrix state."
   :code     "(div {:class :intro}\n    (h2 \"The speed is now...\")\n    (span {:class   :digi-readout\n           :onclick #(mswap! (evt-md %) :mph inc)}\n      {:mph     (cI 42 :watch (fn [slot me new-val prior-val cell]\n                                (when (> new-val 55)\n                                  (with-cc :speed-governor\n                                    (mset! me :mph 45)))))\n       :display (cF (str (mget me :mph) \" mph\"))}\n      (mget me :display))\n    (p \"Click display to increment.\"))"
   :comment  ["Try increasing the speed above 55. A watch function will intervene."
              "In our experience coding with Matrix, we frequently
   encounter opportunities for the app to usefully update state normally controlled by the user. The macro <code>with-cc</code> schedules the <code>mset!</code> mutation for execution
              immediately after the current propagation, when state consistency can be guaranteed."]})

(def ex-data-integrity
  {;; :menu     "Data Integrity"
   :title    "Data Integrity"
   :preamble "Matrix internals automatically identify the DAG implicit in the interfaces we build, dynamically reshapes
   the DAG as the user works, and guarantee a clear set of invariants in the face of DAG mutation. We reprise the prior
   example for the reader's contemplation."
   :builder watch-cc
   :code     "(div {:class :intro}\n    (h2 \"The speed is now...\")\n    (span {:class   :digi-readout\n           :onclick #(mswap! (evt-md %) :mph inc)}\n      {:mph     (cI 42 :watch (fn [slot me new-val prior-val cell]\n                                (when (> new-val 55)\n                                  (with-cc :speed-governor\n                                    (mset! me :mph 45)))))\n       :display (cF (str (mget me :mph) \" mph\"))}\n      (mget me :display))\n    (p \"Click display to increment.\"))"
   :comment  ["When application code assigns a value to some input cell X, the Matrix engine guarantees:
              <br><br>&nbsp;&bull; recomputation exactly once of all and only state affected by the change to X, directly or indirectly through some intermediate datapoint. Note that if A depends on B, and B depends on X, when B gets recalculated it may come up with the same value as before. In this case A is not considered to have been affected by the change to X and will not be recomputed;
              <br><br>&nbsp;&bull; recomputations, when they read other datapoints, must see only values current with the new value of X. Example: if A depends on B and X, and B depends on X, when X changes and A reads B and X to compute a new value, B must return a value recomputed from the new value of X;
              <br><br>&nbsp;&bull; similarly, client observer callbacks must see only values current with the new value of X;
              <br><br>&nbsp;&bull; a corollary: should a client observer MSET! a datapoint Y, all the above must happen with values current with not just X, but also with the value of Y prior to the change to Y; and
              <br><br>&nbsp;&bull; deferred “client” code must see only values current with X and not any values current with some subsequent change to Y enqueued by an observer."]})

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

