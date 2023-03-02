(ns tiltontec.example.quick-start.lesson
  (:require
    [clojure.string :as str]
    [clojure.pprint :as pp]
    [cljs.core.async :refer [go <!]]
    [cljs-http.client :as client]
    [tiltontec.cell.core :refer [cF cF+ cFonce cI cf-freeze]]
    [tiltontec.cell.integrity :refer [with-cc]]
    [tiltontec.model.core
     :refer [mx-par mget mset! mswap! mset! mxi-find mxu-find-name mdv! fasc fmu fm! fm-navig] :as md]
    [tiltontec.web-mx.gen :refer [evt-md target-value]]
    [tiltontec.web-mx.gen-macro
     :refer [img section h1 h2 h3 input footer p a
             span i label ul li div button br
             jso-map
             svg g circle p span div text radialGradient defs stop
             rect ellipse line polyline path polygon script use]]))


;;; --- 1. It's just html -------------------------------------

(defn just-html []
  (div {:class :intro}
    ;; <b>^^ if the first argument to any tag is a literal map, the key-values</b>
    ;; <b>become HTML element attribute-values, with keywords => strings</b>

    (h2 "The count is now....")
    (span {:class :digi-readout} "42")
    ;; <b>^^ arguments following the optional maps become children, or text content</b>

    (svg {:width   64 :height 64
          ;; <b> ^^^ numbers also get string-ified for the DOM constructors</b>
          :cursor  :pointer
          :onclick #(js/alert "Increment Feature Not Yet Implemented")}
      (circle {:cx     "50%" :cy "50%" :r "40%"
               :stroke "orange" :stroke-width 5
               :fill   :transparent})
      (text {:class       :heavychar
             :x           "50%" :y "70%"
             :text-anchor :middle} "+"))))

(def ex-just-html
  {:menu     "Just HTML"
   :title    "It's Just HTML"
   :ns       "tiltontec.example.quick-start.lesson/just-html"
   :builder  just-html
   :preamble "To begin with, we just write HTML, SVG, and CSS, each thinly disguised as CLJS."
   :comment  ["Web/MX introduces no framework of its own, it just manages the DOM.
    Aside from CLJS->JS, no preprocessor is involved, and the stability of CLJS makes this one exception
    a net win."
              "Matrix just manages the state."]
   :code     "(div {:class :intro}\n    ;; <b>^^ if the first argument to any tag is a literal map, the key-values</b>\n    ;; <b>become HTML element attribute-values, with keywords => strings</b>\n\n    (h2 \"The count is now....\")\n    (span {:class :digi-readout} \"42\")\n    ;; <b>^^ arguments following the optional maps become children, or text content</b>\n\n    (svg {:width   64 :height 64\n          ;; <b> ^^^ numbers also get string-ified for the DOM constructors</b>\n          :cursor :pointer\n          :onclick #(js/alert \"Increment Feature Not Yet Implemented\")}\n      (circle {:cx     \"50%\" :cy \"50%\" :r \"40%\"\n               :stroke \"orange\" :stroke-width 5\n               :fill   :transparent})\n      (text {:class       :heavychar\n             :x \"50%\" :y \"70%\"\n             :text-anchor :middle} \"+\")))"
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
      ;; <b>children, below built into a vector, are automatically flattened, with any nils removed</b>
      (mapv (fn [opcode]
              (when (= 1 (count opcode))
                (button {:class   :push-button
                         :onclick #(js/alert (str "Opcode \"" opcode "\" not yet implemented"))}
                  opcode)))
        ["-" "=" "+" "boom"]))))

(def ex-and-cljs
  {:menu     "...and CLJS"
   :title    "...and CLJS" :builder and-cljs
   :preamble "It is just HTML <i>and</i> CLJS."
   :code     "(div {:class :intro}\n    (h2 \"The count is now...\")\n    (span {:class \"digi-readout\"} \"42\")\n    (div {:style {:display :flex\n                  :gap     \"1em\"}}\n      ;; <b>children, below built into a vector, are automatically flattened, with any nils removed</b>\n      (mapv (fn [opcode]\n              (when (= 1 (count opcode))\n                (button {:class   :push-button\n                         :onclick #(js/alert (str \"Opcode \\\"\" opcode \"\\\" not yet implemented\"))}\n                  opcode)))\n        [\"-\" \"=\" \"+\" \"boom\"])))"
   :comment  ["In fact, all this code is CLJS. For example, DIV is a CLJS macro that returns
    a Clojure <i>proxy</i> for a DOM DIV. Proxies are not VDOM. Proxies are long-lived models that manage their DOM incarnations as events unfold."]})

;;; --- components realized --------------------------------

(defn opcode-button [label onclick]
  (button {:class   :push-button
           :onclick onclick}
    label))

(defn math-keypad [& opcodes]
  (div {:style {:display :flex
                :gap     "1em"}}
    (mapv (fn [opcode]
            (opcode-button opcode
              #(js/alert "Feature Not Yet Implemented")))
      opcodes)))

(defn html-composition []
  (div {:class :intro}
    (h2 "The count is now....")
    (span {:class :digi-readout} "42")
    (math-keypad "-" "=" "+")))

(def ex-html-composition
  {:menu     "Composable<br>Widgets"
   :title    "Functional GUI Composition"
   :builder  html-composition
   :preamble "Because it is all CLJS, we can move sub-structure into functions."
   :code     "(defn opcode-button [label onclick]\n  (button {:class   :push-button\n           :onclick onclick}\n    label))\n\n(defn math-keypad [& opcodes]\n  (div {:style {:display :flex\n                :gap     \"1em\"}}\n    (mapv (fn [opcode]\n            (opcode-button opcode\n              #(js/alert \"Feature Not Yet Implemented\")))\n      opcodes)))\n\n(defn html-composition []\n  (div {:class :intro}\n    (h2 \"The count is now....\")\n    (span {:class :digi-readout} \"42\")\n    (math-keypad \"-\" \"=\" \"+\")))"
   :comment  ["HTML composition becomes function composition. Always nice."]})

;;; --- custom-state ---------------------------------

(defn custom-state []
  (div {:class :intro}
    (h2 "The speed is now...")
    (span {:class :digi-readout}
      ;; <b>An optional second map is for custom state.</b>
      {:mph 42}

      ;; <b>below: mget, short for "model-get", is the MX "getter" for model (object) properties</b>
      ;; <b>n.b. Tag children, even plain strings, always start out in an auto-genned formula.</b>
      (str (mget me :mph) " mph"))))

(def ex-custom-state
  {:menu     "In-place<br>State"
   :title    "\"In-place\" widget state, property by property"
   :builder  custom-state
   :preamble "Widgets define local state as needed."
   :code     "(div {:class :intro}\n    (h2 \"The speed is now...\")\n    (span {:class :digi-readout}\n      ;; <b>An optional second map is for custom state.</b>\n      {:mph  42}\n\n      ;; <b>below: mget, short for \"model-get\", is the MX \"getter\" for model (object) properties</b>\n      ;; <b>n.b. Tag children, even plain strings, always start out in an auto-genned formula.</b>\n      (str (mget me :mph) \" mph\")))"
   :comment  ["Tag macros take an optional second map of custom widget state. The map for custom state is identified
   positionally, so an empty first map must be coded even if no HTML attributes need specification."
              "Here, a generic <code>span</code> embodying a speedometer thinks it might usefully have a <code>{:mph 42}</code> property.
   We will put that to use next."
              "`mget` can be used anywhere. Inside a formula, it transparently subscribes to the property being read."
              "Big picture: Matrix follows the <a href=https://en.wikipedia.org/wiki/Prototype-based_programming target=\"_blank\">prototype model</a>,\n
                     so generic tags can be re-used without subclassing."]})

;;; --- derived state ------------------------------


(defn derived-state []
  (div {:class :intro}
    (h2 "The speed is now...")
    (span {:class :digi-readout}
      {:name        :speedometer
       :mph         65

       ;; <b>'cF', or "cell formula", defines a computed/derived property.</b>
       ;; <b>When properties, such as 'mph', are read by a formula, the formula is re-run.</b>
       :too-fast?   (cF (> (mget me :mph) 55))
       :speedo-text (cF (str (mget me :mph) " mph"
                          (when (mget me :too-fast?) "<br>Slow down")))}
      (mget me :speedo-text))))

(def ex-derived-state
  {:menu     "Functional<br>Properties"
   :title    "Functional, computed, reactive properties"
   :builder  derived-state
   :code     "(div {:class :intro}\n    (h2 \"The speed is now...\")\n    (span {:class :digi-readout}\n      {:name :speedometer\n       :mph         65\n       \n       ;; <b>'cF', or \"cell formula\", defines a computed/derived property.</b>\n       ;; <b>When properties, such as 'mph', are read by a formula, the formula is re-run.</b>\n       :too-fast?   (cF (> (mget me :mph) 55))\n       :speedo-text (cF (str (mget me :mph) \" mph\"\n                          (when (mget me :too-fast?) \"Slow down\")))}\n      (mget me :speedo-text)))"
   :preamble "A property can be expressed as a function, or \"formula\", of other properties."
   :comment  ["The <code>too-fast?</code> property is fed by the reactive formula <code>(cF (> (mget me :mph) 55))</code>.
    When <code>mph</code> changes, <code>too-fast?</code> will be recomputed, then <code>speedo-text</code>."
              "Interdependent properties form the same coherent, one-way graph (DAG) as found in Flux derivatives,
              but without us doing anything; Matrix internals identify the DAG for us."
              "D/X note: different instances can have different formulas for the same property,
              extending the \"prototype\" reusability win.</li>"]})

;;; --- Navigation ------------------------------

(defn navigation [geo-type]
  (div {:class :intro}
    {:name        :speed-zone
     :speed-limit 55}
    (h2 {}
      ;; <b>`fasc` searches up the parent chain only</b>
      {:text (cF (let [zone (fasc :speed-zone me)
                       speedo (fmu :speedometer)]
                   (pp/cl-format nil "The speed is now ~a mph over the speed limit."
                     (- (mget speedo :mph) (mget zone :speed-limit)))))}
      (mget me :text))
    (span {:class :digi-readout}
      {:name      :speedometer
       :mph       60
       :too-fast? (cF (> (mget me :mph)
                        (mget (fasc :speed-zone me) :speed-limit)))}
      (str (mget me :mph) " mph"
        (when (mget me :too-fast?) "<br>Slow down")))))

(def ex-navigation
  {:menu     "Random State<br>Access"
   :title    "Random state access"
   :builder  navigation
   :preamble "A widget property can retrieve state as needed from any other component."
   :code     "(div {:class :intro}\n    {:name        :speed-zone\n     :speed-limit 55}\n    (h2 {}\n      ;; <b>`fasc` searches up the parent chain only</b>\n      {:text (cF (let [zone (fasc :speed-zone me)\n                       speedo (fmu :speedometer)]\n                   (pp/cl-format nil \"The speed is now ~a mph over the speed limit.\"\n                     (- (mget speedo :mph) (mget zone :speed-limit)))))}\n      (mget me :text))\n    (span {:class :digi-readout}\n      {:name      :speedometer\n       :mph       60\n       :too-fast? (cF (> (mget me :mph)\n                        (mget (fasc :speed-zone me) :speed-limit)))}\n      (str (mget me :mph) \" mph\"\n        (when (mget me :too-fast?) \"Slow down\"))))"
   :comment  ["The headline needs the speed limit and current speed for its text. The speedometer readout needs
     the speed limit, to decide its text color."
              "We retrieve values from named other widgets, using navigation
     utilities such as <code>fasc</code> and <code>fmu</code> to avoid hard-coding paths."
              "About navigation: MX models are like the DOM; every element but the root has one parent and knows that parent as a fixed property,
               and everyone has zero or more children. So should any convoluted organization need to be navigated, a dev can write their own
                bespoke navigation code."
              "Second, the provided fm-navig utility takes a \"test\" function as its first parameter, which by default
              tests the :name of every node for a match with the sought name. Here again a dev can write a test function
              of arbitrary complexity if needed."
              "Just to fill in the navigation picture a bit, <code>fm-navig</code> by default does a depth-first, left-right
               search starting at the provided start node, recursing up to the start's parent if necessary. So search for
               a name benefits from a natural scoping, and duplicate names arising from a row of similar components works out fine."
              "A cautionary note on navigation: a formula computing a widget's children has to use the option that tells
               fm-navig to search \"up only\", because otherwise it will ask for the children being computed and
               a \"cyclic dependency\" exception will be thrown."]})

;;; --- handler mutation -----------------------------

(defn speed-plus [onclick]
  (svg {:width   64 :height 64 :cursor :pointer
        :onclick onclick}
    (circle {:cx     "50%" :cy "50%" :r "40%"
             :stroke "orange" :stroke-width 5
             :fill   :transparent})
    (text {:class       :heavychar :x "50%" :y "70%"
           :text-anchor :middle} "+")))

(defn handler-mutation []
  (div {:class :intro}
    (h2 "The speed limit is 55 mph. Your speed is now...")
    (span {:class :digi-readout
           :style (cF {:color (if (> (mget me :mph) 55)
                                "red" "cyan")})}
      {:name    :speedometer
       ;; <b>If we intend to mutate a property, we must wrap the value in `cI`, short for "cell input"</b>
       :mph     (cI 42)
       :display (cF (str (mget me :mph) " mph"))}
      (mget me :display))
    (speed-plus (fn [evt]
                  ;; <b>`evt-md` (event model) determines the MX proxy/model associated with a handler event.</b>
                  ;; <b>'mswap!' performs a Clojure 'swap!' on the ':mph' property of the model.</b>
                  (mswap! (fmu :speedometer (evt-md evt)) :mph inc)))))

(def ex-handler-mutation
  {:menu     "Random State<br>Mutation"
   :title    "Random state DAG change"
   :ns       "tiltontec.example.quick-start.lesson/handler-mutation"
   :builder  handler-mutation
   :preamble ["A widget event handler can mutate any property of any widget. (This button works.)"]
   :code     "(defn speed-plus [onclick]\n  (svg {:width   64 :height 64 :cursor :pointer\n        :onclick onclick}\n    (circle {:cx     \"50%\" :cy \"50%\" :r \"40%\"\n             :stroke \"orange\" :stroke-width 5\n             :fill   :transparent})\n    (text {:class       :heavychar :x \"50%\" :y \"70%\"\n           :text-anchor :middle} \"+\")))\n\n(defn handler-mutation []\n  (div {:class :intro}\n    (h2 \"The speed limit is 55 mph. Your speed is now...\")\n    (span {:class :digi-readout\n           :style (cF {:color (if (> (mget me :mph) 55)\n                                \"red\" \"cyan\")})}\n      {:name    :speedometer\n       ;; <b>If we intend to mutate a property, we must wrap the value in `cI`, short for \"cell input\"</b>\n       :mph     (cI 42)\n       :display (cF (str (mget me :mph) \" mph\"))}\n      (mget me :display))\n    (speed-plus (fn [evt]\n                  ;; <b>`evt-md` (event model) determines the MX proxy/model associated with a handler event.</b>\n                  ;; <b>'mswap!' performs a Clojure 'swap!' on the ':mph' property of the model.</b>\n                  (mswap! (fmu :speedometer (evt-md evt)) :mph inc)))))"
   :exercise "Add custom state <code>throttled</code>, with a formula that computes <code>true</code> if <code>mph</code> is
   fifty-five or more. Check <code>throttled</code> in the <code>onclick</code> handler before allowing increment."
   :comment  ["Wrapping <code>mph</code> value in <code>(cI 42)</code>, <code>cI</code> for \"cell Input\",
    lets us mutate <code>mph</code> imperatively."
              "Here, an event handler navigates via
    utility <code>fmu</code> (search \"family up\") to the speedometer widget and mutates it."]})

;;; --- watches ----------------------------------

(defn watches []
  (div {:class :intro}
    (h2 "The speed is now...")
    (span {:class   :digi-readout
           :onclick #(mswap! (evt-md %) :mph inc)}
      {:name    :speedometer
       :mph     (cI 42 :watch (fn [slot me new-val prior-val cell]
                                ;; <b>`cI`, cell input, takes a :watch option for an "on change" function</b>
                                (prn :watch-sees-change slot new-val)))
       :display (cF (str (mget me :mph) " mph"))}
      (mget me :display))
    (speed-plus (fn [evt]
                  (mswap! (fmu :speedometer (evt-md evt)) :mph inc)))))

(def ex-watches
  {:menu     "State Watch<br>Functions"
   :title    "\"On-change\" watch functions"
   :builder  watches
   :preamble "Any input or computed cell can specify an on-change 'watch' function to execute side-effects outside Matrix dataflow."
   :code     "(div {:class :intro}\n    (h2 \"The speed is now...\")\n    (span {:class   :digi-readout\n           :onclick #(mswap! (evt-md %) :mph inc)}\n      {:name    :speedometer\n       :mph     (cI 42 :watch (fn [slot me new-val prior-val cell]\n                                ;; <b>`cI`, cell input, takes a :watch option for an \"on change\" function</b>\n                                (prn :watch-sees-change slot new-val)))\n       :display (cF (str (mget me :mph) \" mph\"))}\n      (mget me :display))\n    (speed-plus (fn [evt]\n                  (mswap! (fmu :speedometer (evt-md evt)) :mph inc))))"
   :comment  ["A watch function fires when a cell value is initialized, and if the value changes. Watches are used to
   dispatch actions outside the Matrix, if only for logging/debugging, as here. (See the browser console.)"
              "The watch function in this example simply logs the new value. Other watches could write to
              localStorage or dispatch XHR requests. Web/MX does all its dynamic DOM maintenance in watch functions."]})

;;; --- throttling watch -------------------

(defn watch-cc []
  (div {:class :intro}
    (h2 "The speed limit is 55 mph. Your speed is now...")
    (span {:class   :digi-readout
           :onclick #(mswap! (evt-md %) :mph inc)}
      {:name    :speedometer
       :mph     (cI 42 :watch (fn [slot me new-val prior-val cell]
                                (when (> new-val 55)
                                  (js/alert "You have triggered the speed governor; auto-resetting to 45 mph.")

                                  ;; <b>`with-cc` must wrap any DAG mutation by a watch function </b>
                                  (with-cc :speed-governor
                                    ;; <b>'mset!', like its alias 'mreset!, performs a 'reset!' of a model property.</b>
                                    (mset! me :mph 45)))))
       :display (cF (str (mget me :mph) " mph"))}
      (mget me :display))
    (speed-plus (fn [evt]
                  (mswap! (fmu :speedometer (evt-md evt)) :mph inc)))))

(def ex-watch-cc
  {:menu     "Watch State<br>Mutation"
   :title    "Exception: how watches can mutate state"
   :builder  watch-cc
   :preamble "Watch functions must operate outside Matrix state flow, but <i>can</i> enqueue alterations
    of Matrix state for execution."
   :code     "(div {:class :intro}\n    (h2 \"The speed limit is 55 mph. Your speed is now...\")\n    (span {:class   :digi-readout\n           :onclick #(mswap! (evt-md %) :mph inc)}\n      {:name    :speedometer\n       :mph     (cI 42 :watch (fn [slot me new-val prior-val cell]\n                                (when (> new-val 55)\n                                  (js/alert \"You have triggered the speed governor; auto-resetting to 45 mph.\")\n                                  \n                                  ;; <b>`with-cc` must wrap any DAG mutation by a watch function </b>\n                                  (with-cc :speed-governor\n                                    ;; <b>'mset!', like its alias 'mreset!, performs a 'reset!' of a model property.</b>\n                                    (mset! me :mph 45)))))\n       :display (cF (str (mget me :mph) \" mph\"))}\n      (mget me :display))\n    (speed-plus (fn [evt]\n                  (mswap! (fmu :speedometer (evt-md evt)) :mph inc))))"
   :comment  ["Try increasing the speed above 55. A watch function will intervene."
              "In our experience coding with Matrix, we frequently
   encounter opportunities for the app to usefully update state normally controlled by the user. The macro <code>(with-cc TAG & BODY)</code> schedules the <code>mset!</code> mutation for execution
              immediately after the current propagation, when state consistency can be guaranteed. TAG is just for debugging."]})

;;; --- async --------------------------------------------------------

(defn throttle-button [[opcode factor :as setting]]
  (button {:class   :push-button
           :style   (cF (let [[current-opcode] (mget (fmu :throttle) :setting)]
                          {:min-width  "96px"
                           :background (if (= opcode current-opcode)
                                         "cyan" "linen")
                           :font-size  "18px"}))
           :onclick (cF #(mset! (fmu :throttle) :setting setting))}
    (name opcode)))

(defn speedometer []
  (span {:class :digi-readout
         :style (cF {:min-width "5em"
                     :color     (if (> (mget me :mph) 55)
                                  "red" "cyan")})}
    {:mph     (cI 42)
     :time    (cF (js/setInterval
                    (fn [] (let [mph-now (mget me :mph)
                                 throttle (fmu :throttle)]
                             (when throttle
                               (mswap! me :mph *
                                 (second (mget throttle :setting))))))
                    1000))
     :display (cF (pp/cl-format nil "~8,1f mph" (mget me :mph)))}
    (mget me :display)))

(defn async-throttle []
  (let [settings [[:maintain 1] [:coast .98] [:brake-gently .8] [:panic-stop .60]
                  [:speed-up 1.1] [:floor-it 1.3]]]
    (div {:class :intro}
      (h2 "The speed is now...")
      (speedometer)
      (div {:style {:display :flex
                    :gap     "1em"}}
        {:name    :throttle
         :setting (cI (second settings))}
        (mapv throttle-button settings)))))

(def ex-async-throttle
  {:menu     "Async mutation"
   :title    "Handling async"
   :builder  async-throttle
   :preamble "Async processing can be challenging, but in Matrix are just mutations of normal \"input\" properties."
   :code     "(defn throttle-button [[opcode factor :as setting]]\n  (button {:class   :push-button\n           :style   (cF (let [[current-opcode] (mget (fmu :throttle) :setting)]\n                          {:min-width  \"96px\"\n                           :background (if (= opcode current-opcode)\n                                         \"cyan\" \"linen\")\n                           :font-size  \"18px\"}))\n           :onclick (cF #(mset! (fmu :throttle) :setting setting))}\n    (name opcode)))\n\n(defn speedometer []\n  (span {:class :digi-readout\n         :style (cF {:color (if (> (mget me :mph) 55)\n                              \"red\" \"cyan\")})}\n    {:mph     (cI 42)\n     :time    (cF (js/setInterval\n                    (fn [] (let [mph-now (mget me :mph)]\n                             (mswap! me :mph *\n                               (second (mdv! :throttle :setting)))))\n                    1000))\n     :display (cF (pp/cl-format nil \"~8,1f mph\" (mget me :mph)))}\n    (mget me :display)))\n\n(defn async-throttle []\n  (let [settings [[:maintain 1] [:coast .95] [:brake-gently .8] [:panic-stop .60]\n                  [:speed-up 1.1] [:floor-it 1.3]]]\n    (div {:class :intro}\n      (h2 \"The speed is now...\")\n      (speedometer)\n      (div {:style {:display :flex\n                    :gap     \"1em\"}}\n        {:name    :throttle\n         :setting (cI (second settings))}\n        (mapv throttle-button settings)))))"
   :comment  ["We handle async events by directing them to input Cells."]})

;;; --- data integrity ---------------------------------

(def ex-data-integrity
  {:title    "Data Integrity"
   :preamble ["Matrix silently maintains an internal DAG at run time by noting when one property formula reads
    another property. When a property is modified, Matrix uses the derived DAG to ensure
     the \"data integrity\" invariants listed below."]
   :builder  watch-cc
   :code     "(div {:class :intro}\n    (h2 \"The speed is now...\")\n    (span {:class   :digi-readout\n           :onclick #(mswap! (evt-md %) :mph inc)}\n      {:mph     (cI 42 :watch (fn [slot me new-val prior-val cell]\n                                (when (> new-val 55)\n                                  (with-cc :speed-governor\n                                    (mset! me :mph 45)))))\n       :display (cF (str (mget me :mph) \" mph\"))}\n      (mget me :display))\n    (p \"Click display to increment.\"))"
   :comment  ["<h3>The Data Integrity Contract</h3> When application code assigns a value to some input cell X, the Matrix engine guarantees:
              <br><br>&nbsp;&bull; recomputation exactly once of all and only state affected by the change to X, directly or indirectly through some intermediate datapoint. Note that if A depends on B, and B depends on X, when B gets recalculated it may come up with the same value as before. In this case A is not considered to have been affected by the change to X and will not be recomputed;
              <br><br>&nbsp;&bull; recomputations, when they read other datapoints, will see only values current with the new value of X. Example: if A depends on B and X, and B depends on X, when X changes and A reads B and X to compute a new value, B must return a value recomputed from the new value of X;
              <br><br>&nbsp;&bull; similarly, client observer callbacks will see only values current with the new value of X;
              <br><br>&nbsp;&bull; a corollary: should a client observer MSET! a datapoint Y, all the above will happen with values current with not just X, but also with the value of Y prior to the change to Y; and
              <br><br>&nbsp;&bull; deferred “client” code will see only values current with X and not any values current with some subsequent change to Y enqueued by an observer."]})

;;; --- ajax cats ---------------------------------------------------

(def cat-fact-uri "https://catfact.ninja/fact")

(defn async-cat []
  (div {:class "intro"}
    (span {:class :push-button}
      "Cat Chat")
    (speed-plus #(mset! (fmu :cat-fact (evt-md %)) :get-new-fact? true))
    (div {:class :cat-chat}
      {:name          :cat-fact
       :get-new-fact? (cI false

                        ;; <b>The "plus" widget will just set this property repeatedly to the same value, 'true'.</b>
                        ;; <b>Declaring this input Cell "ephemeral?" means it will fire each time that same value is set.</b>
                        :ephemeral? true)
       :cat-request   (cF+
                        ;; <b>`cF+`, or "cell formula plus", accepts cell options in a vector first parameter</b>
                        [:watch (fn [_ me response-chan _ _]
                                     (when response-chan
                                       (go (let [response (<! response-chan)]

                                             ;; <b>whenever the XHR responds, we just `mset!` the waiting input cell</b>
                                             (with-cc :set-cat
                                               (mset! me :cat-response response))))))]
                        (when (mget me :get-new-fact?)
                          (client/get cat-fact-uri {:with-credentials? false})))
       :cat-response  (cI nil)}

      (if-let [response (mget me :cat-response)]
        (if (:success response)
          (span (get-in response [:body :fact]))
          (str "Error>  " (:error-code response)
            ": " (:error-text response)))
        "Click (+) to see a chat fact."))))

(def ex-async-cat
  {:menu     "Async"
   :title    "Async event processing"
   :builder  async-cat
   :preamble "Async processing can be a challenge, but in Matrix an async response is just another \"input\" property mutation."
   :code     "(div {:class \"intro\"}\n    (span {:class :push-button}\n      \"Cat Chat\")\n    (speed-plus #(mset! (fmu :cat-fact (evt-md %)) :get-new-fact? true))\n    (div {:class :cat-chat}\n      {:name          :cat-fact\n       :get-new-fact? (cI false\n\n                        ;; <b>The \"plus\" widget will just set this property repeatedly to the same value, 'true'.</b>\n                        ;; <b>Declaring this input Cell \"ephemeral?\" means it will fire each time that same value is set.</b>\n                        :ephemeral? true)\n       :cat-request   (cF+ \n                        ;; <b>`cF+`, or \"cell formula plus\", accepts cell options in a vector first parameter</b>\n                        [:watch (fn [_ me response-chan _ _]\n                                     (when response-chan\n                                       (go (let [response (&lt;! response-chan)]\n\n                                             ;; <b>whenever the XHR responds, we just `mset!` the waiting input cell</b>\n                                             (with-cc :set-cat\n                                               (mset! me :cat-response response))))))]\n                        (when (mget me :get-new-fact?)\n                          (client/get cat-fact-uri {:with-credentials? false})))\n       :cat-response  (cI nil)}\n\n      (if-let [response (mget me :cat-response)]\n        (if (:success response)\n          (span (get-in response [:body :fact]))\n          (str \"Error>  \" (:error-code response)\n            \": \" (:error-text response)))\n        \"Click (+) to see a chat fact.\")))"
   :comment  ["The <code>cat-request</code> property creates and dispatches an XHR via <code>client/get</code>, producing a core.async channel
   to receive the response. Its watch function awaits the async response and feeds it into a conventional input property."
              "We handle async events by directing them to input Cells purpose-created to receive their output, where
              Matrix handles them like any other input."]})

;;; --- ephemeral roulette ------------------------------------------


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
    (h2 "Faites votre pari, s'il vous plaît")
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

(defn in-review []
  (div {:class :intro}
    (h2 (let [excess (- (mget (fmu :speedometer) :mph) 55)]
          (pp/cl-format nil "The speed is ~8,1f mph ~:[over~;under~] the speed limit."
            (Math/abs excess) (neg? excess))))
    (span {:class :digi-readout
           :style (cF {:color (if (> (mget me :mph) 55)
                                "red" "cyan")})}
      {:name     :speedometer
       :mph      (cI 42)
       :air-drag (cF (js/setInterval
                       #(mswap! me :mph * 0.98) 1000))}
      (pp/cl-format nil "~8,1f mph" (mget me :mph)))
    (speed-plus #(mswap! (fmu :speedometer (evt-md %)) :mph inc))))

(def ex-in-review
  {:title    "Review"
   :builder  in-review
   :preamble "Our closing example reprises all the keyWeb/MX features."
   :code     "(div {:class :intro}\n    (h2 (let [excess (- (mget (fmu :speedometer) :mph) 55)]\n          (pp/cl-format nil \"The speed is ~8,1f mph ~:[over~;under~] the speed limit.\"\n            (Math/abs excess)  (neg? excess) )))\n    (span {:class   :digi-readout\n           :style   (cF {:color (if (> (mget me :mph) 55)\n                                  \"red\" \"cyan\")})}\n      {:name :speedometer\n       :mph     (cI 42)\n       :air-drag (cF (js/setInterval\n                       #(mswap! me :mph * 0.98) 1000))}\n      (pp/cl-format nil  \"~8,1f mph\" (mget me :mph)))\n    (speed-plus #(mswap! (fmu :speedometer (evt-md %)) :mph inc)))"
   :comment  "
   <li>it looks and works like standard HTML, SVG, CSS, and CLJS;</li>
   <li>all state dependencies are property to property;</li>
   <li>the <code>H2</code> computes its text by navigating to the speedometer widget to read the <code>mph</code> value;</li>
   <li>the <code>(speed-plus ...)</code> button navigates to the speedometer to mutate <code>mph</code> value;</li>
   <li>the <code>air-drag</code> async interval mutates the DAG, reducing the <code>mph</code>;</li>
   <li>function <code>speed-plus</code> demonstrates reusable composition.</li>"})