# The Web/MX&trade; Quick Start
Web/MX works different. But in some ways, it works the same. Nothing is unique to Web/MX, but the same things are executed differently, and to a different degree.

Below, we quickly touch on a dozen essential Web/MX mechanisms that cover everything we need to know about Web/MX and the developer experience, or D/X, it supports.

The same content can be viewed in an executable fashion by running the code alongside this README file:
```bash
cd web-mx
clojure -M -m figwheel.main --build quick-start --repl
```
For those to content to read...
## It Is Just HTML
To begin with, we just write HTML, SVG, and CSS, each thinly disguised as CLJS.
```clojure
(div {:class :intro}
    (h2 "The count is now....")
    (span {:class :digi-readout} "42")
    (svg {:width 64 :height 64 :cursor :pointer
          :onclick #(js/alert "Increment Feature Not Yet Implemented")}
      (circle {:cx "50%" :cy "50%" :r "40%"
               :stroke  "orange" :stroke-width 5
               :fill :transparent})
      (text {:class :heavychar :x "50%" :y "70%"
             :text-anchor :middle} "+")))
```
![It's Just HTML](image/just-html.png)

Web/MX introduces no framework of its own, it just manages the DOM. Aside from CLJS->JS, no preprocessor is involved, and the stability of CLJS makes this one exception a net win.

Matrix just manages the state.

## ...and CLJS
It is just HTML _and_ CLJS.
![and CLJS](image/and-cljs.png)
```clojure
(div {:class :intro}
    (h2 "The count is now...")
    (span {:class "digi-readout"} "42")
    (div {:style {:display :flex
                  :gap     "1em"}}
      (mapv (fn [opcode]
              (button {:class   :push-button
                       :onclick #(js/alert (str "Opcode \"" opcode "\" not yet implemented"))}
                opcode))
        ["-" "=" "+"])))
```
In fact, all this code is CLJS. For example, DIV is a CLJS macro that returns a Clojure proxy for a DOM DIV. Proxies are not VDOM. Proxies are long-lived models that manage their DOM incarnations as events unfold.

## Functional GUI Composition
Because it is all CLJS, we can move sub-structure into functions.

```clojure
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
```
HTML composition becomes function composition. Always nice.

## "In-place" widget state, property by property
Widgets define local state as needed.
```clojure
(div {:class :intro}
    (h2 "The speed is now...")
    (span {:class :digi-readout}
      {:name :speedometer
       :mph 42}
      (str (mget me :mph) " mph")))
```
Tag macros take an optional second map of custom widget state. Here, a generic span embodying a speedometer thinks it might usefully have a {:mph 42} property. We will put that to use next.

Matrix follows the [prototype model](https://en.wikipedia.org/wiki/Prototype-based_programming), so generic tags can be re-used without subclassing.

## Functional, computed, reactive properties
A property can be expressed as a function, or "formula", of other properties.

```clojure
(div {:class :intro}
    (h2 "The speed is now...")
    (span {:class :digi-readout}
      {:mph       65
       :too-fast? (cF (> (mget me :mph) 55))
       :speedo-text (cF (str (mget me :mph) " mph"
                          (when (mget me :too-fast?) "
Slow down?")))}
      (mget me :speedo-text)))
```
The `too-fast?` property is fed by the reactive formula `(cF (> (mget me :mph) 55))`. When `mph` changes, `too-fast?` will be recomputed, then `speedo-text`.

Interdependent properties form the same coherent, one-way graph (DAG) as found in Flux derivatives, but without us doing anything: Matrix internals identify the DAG for us.

D/X note: different instances can have different formulas for the same property, extending the "prototype" reusability win.

## Random state access
A widget property can retrieve state as needed from any other component.
```clojure
(div {:class :intro}
    {:name :speed-zone
     :speed-limit 55}
    (h2 {}
      {:text (cF (let [limit (mget (fm-navig :speed-zone me) :speed-limit)
                       speed (mget (fm-navig :speedo me) :mph)]
                   (str "The speed is now "
                     (- speed limit) " mph over the speed limit.")))}
      (mget me :text))
    (span {:class :digi-readout}
      {:name :speedo
       :mph 60
       :too-fast? (cF (> (mget me :mph)
                        (mget (fmu :speed-zone) :speed-limit)))}
      (str (mget me :mph) " mph"
        (when (mget me :too-fast?)
          "Slow down"))))
```
The headline needs the speed limit and current speed for its text. The speedometer readout needs the speed limit, to decide its text color.

We retrieve values from named other widgets, using navigation utilities such as `fm-navig` and `fmu` to avoid hard-coding paths.

## Random state change
A widget event handler can mutate any property of any widget.
```clojure
(div {:class :intro}
    (h2 "The speed limit is 55mph. Your speed is now...")
    (span {:class   :digi-readout
           :style   (cF {:color (if (> (mget me :mph) 55)
                                  "red" "cyan")})}
      {:name :speedometer
       :mph     (cI 42)
       :display (cF (str (mget me :mph) " mph"))}
      (mget me :display))
    (svg-plus (fn [evt]
                (mswap! (fmu :speedometer (evt-md evt)) :mph inc))))
```
Wrapping the `mph` value in `(cI 42)`, `cI` for "cell Input", lets us mutate `mph` imperatively.

Here, an event handler navigates via utility `fmu` (search "family up") to the speedometer widget and mutates it.

## "On-change" watch functions
Any input or computed cell can specify an on-change 'watch' function to execute side-effects outside Matrix dataflow.

```clojure
(div {:class :intro}
    (h2 "The speed is now...")
    (span {:class   :digi-readout
           :onclick #(mswap! (evt-md %) :mph inc)}
      {:mph     (cI 42 :watch (fn [slot me new-val prior-val cell]
                                (prn :watch slot new-val)))
       :display (cF (str (mget me :mph) " mph"))}
      (mget me :display))
    (speed-plus (fn [evt]
                  (mswap! (fmu :speedometer (evt-md evt)) :mph inc))))
```
A watch function fires when a cell is initialized, and if its value changes. Watches are used to dispatch actions outside the Matrix, if only for logging/debugging, as here. (See the browser console.)

The watch function in this example simply logs the new value. Other watches could write to localStorage or dispatch XHR requests. Web/MX does all its dynamic DOM maintenance in watch functions.

## Watch state mutation
Watch functions must operate outside Matrix state flow, but _can_ enqueue alterations of Matrix state for execution after the _observed_ change finishes propagation.

```clojure
(div {:class :intro}
    (h2 "The speed limit is 55 mph. Your speed is now...")
    (span {:class   :digi-readout
           :onclick #(mswap! (evt-md %) :mph inc)}
      {:name :speedometer
       :mph     (cI 42 :watch (fn [slot me new-val prior-val cell]
                                (when (> new-val 55)
                                  (js/alert "Your speed as triggered the governor; resetting to 45 mph.")
                                  (with-cc :speed-governor
                                    (mset! me :mph 45)))))
       :display (cF (str (mget me :mph) " mph"))}
      (mget me :display))
    (speed-plus (fn [evt]
                  (mswap! (fmu :speedometer (evt-md evt)) :mph inc))))
```
Try increasing the speed above 55. A watch function will intervene.

In our experience coding with Matrix, we frequently encounter opportunities for the app to usefully update state normally controlled by the user. The macro with-cc schedules the mset! mutation for execution immediately after the current propagation, when state consistency can be guaranteed.

## Async event processing
An async response is just another "input" property mutation.
```clojure
(div {:class "intro"}
    (span {:class :push-button}
      "Cat Chat")
    (speed-plus #(mset! (fmu :cat-fact (evt-md %)) :get-new-fact? true))
    (div {:class :cat-chat}
      {:name :cat-fact
       :get-new-fact? (cI false :ephemeral? true)
       :cat-request   (cF+ [:watch (fn [_ me response-chan _ _]
                                     (when response-chan
                                       (go (let [response (<! response-chan)]
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
        "Click (+) to see a chat fact.")))
```
The cat-request property creates and dispatches an XHR via client/get, producing a core.async channel to receive the response. Its watch function awaits the async response and feeds it into a conventional input property.

We handle async events by directing them to input Cells purpose-created to receive their output, where Matrix handles them like any other input.

## Data integrity
Matrix silently maintains an internal DAG at run time by noting when one property formula reads another property. When a property is modified, Matrix uses the derived DAG to ensure the "data integrity" invariants listed below.

### The Data Integrity Contract
When application code assigns a value to some input cell X, the Matrix engine guarantees:

* recomputation exactly once of all and only state affected by the change to X, directly or indirectly through some intermediate datapoint. Note that if A depends on B, and B depends on X, when B gets recalculated it may come up with the same value as before. In this case A is not considered to have been affected by the change to X and will not be recomputed;

* recomputations, when they read other datapoints, will see only values current with the new value of X. Example: if A depends on B and X, and B depends on X, when X changes and A reads B and X to compute a new value, B must return a value recomputed from the new value of X;

* similarly, client observer callbacks will see only values current with the new value of X;

* a corollary: should a client observer MSET! a datapoint Y, all the above will happen with values current with not just X, but also with the value of Y prior to the change to Y; and

* deferred “client” code will see only values current with X and not any values current with some subsequent change to Y enqueued by an observer.

## Review
Our closing example reprises all the keyWeb/MX features.
```clojure
(div {:class :intro}
    (h2 (let [excess (- (mget (fmu :speedometer) :mph) 55)]
          (pp/cl-format nil "The speed is ~8,1f mph ~:[over~;under~] the speed limit."
            (Math/abs excess)  (neg? excess) )))
    (span {:class   :digi-readout
           :style   (cF {:color (if (> (mget me :mph) 55)
                                  "red" "cyan")})}
      {:name :speedometer
       :mph     (cI 42)
       :air-drag (cF (js/setInterval
                       #(mswap! me :mph * 0.98) 1000))}
      (pp/cl-format nil  "~8,1f mph" (mget me :mph)))
    (speed-plus #(mswap! (fmu :speedometer (evt-md %)) :mph inc)))
```
* it looks and works like standard HTML, SVG, CSS, and CLJS;
* all state dependencies are property to property;
* the `H2` computes its text by navigating to the speedometer widget to read the `mph` value;
* the `(speed-plus ...)` button navigates to the speedometer to mutate `mph` value;
* the `air-drag` async interval mutates the DAG, reducing the `mph`;
* function `speed-plus` demonstrates reusable composition.
