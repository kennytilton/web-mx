# Web/MX: Into the Weeds
> WARNING: We discuss below the _internal_ architecture of `Web/MX`, like of interest only to other UI architects.

Web/MX delivers a simple yet powerful developer experience through several unconventional design choices, none unique to Web/MX, but most executed differently in important ways:
* **transparent, fine-grained reactivity:** the underlying [Matrix](https://github.com/kennytilton/matrix/blob/main/cljc/matrix/README.md) state manager transparently detects property-to-property dependencies. It uses that information to keep state self-consistent when any property changes. By contraast, almost every UI framework has a "bulk" dependency of a so-called view function on any number of subscriptions to more or less granular nodes in an external store, in the [Facebook Flux](https://facebook.github.io/flux/docs/in-depth-overview/#) model.

* **the application is the database:** state is managed "in place", gathered locally by app components as needed. No defining, updating, or accessing a separate store; and

* **global reach:** the formula for a derived property of a widget can read any property of any other widget. Any event handler can mutate any property. 

* **all reactive all the time:** where we want to use a library such as localStorage, or XHR, or a charting library, we do not have to wrap it in Matrix, but doing so will extend the overall reactive win more than commensurately. 

HTML and CSS remain as is. 

Here is what all that means to the Web/MX developer:
* we think in static HTML/CSS;
* if a property needs to change when other app things change, we express it as a function of those other things; 
* event handlers can change any designated "input" property of any other widget.

### How can this possibly work?
That is a lot of easy expressiveness, but is "ask anybody anything" or "fire at will" state manageable? Is that not why we need a secondary store, in the Flux pattern? We can explain why this works, but first, here are two live existence proofs you can try now:
* a simulation of a [private Algebra tutor](http://tiltonsalgebra.com/#); and
* a browser for the monthly Hacker News [askHN: Who's Hiring?](https://kennytilton.github.io/whoishiring/) question.

Now _why_ it works, in Q&A form:

Q: So how does unfettered state dependency work, without a "separate store" as a single source of truth where integrity can be enforced?

A: As formulas for specific widget properties run, and as those formulas read other properties, Matrix quietly weaves a one-way DAG in memory, recording dependencies between computed and read properties. We call this `in-place state management`.

Q: What about changing state, without having pre-defined transactions to control change?

A: Because a Web/MX app implicitly defines its DAG property-to-property, with full record of specific dependencies, Matrix internals can propagate any change to any affected properties completely, consistently, and non-redendantly; in a sense, the DAG dependency information defines executable transactions as emergent properties.

### The GUI problem
A state manager, after a change, must know:
* what other properties must be recomputed;
* in which order should they be recomputed; and
* how do we orchestrate any side effects required by given recomputations?

If those questons are not answered well after a state change, we risk:
* unnecessary recomputation;
* incomplete recomputation (worse); and
* duplicate, inconsistent recomputation, aka glitches.

Let us look at some code that addresses all that.

#### Hello Clock
Follow these steps to clone Web/MX itself and run an example. 

> In a terminal:
```bash
git clone https://github.com/kennytilton/web-mx.git
cd web-mx
clojure -M -m figwheel.main --build intro-clock --repl
```
In a minute, look for this to appear in your browser at [localhost:9500/intro-clock](http://localhost:9500/intro-clock.html):

![Web MX](https://github.com/kennytilton/web-mx/blob/main/resources/public/image/intro-checking.jpg)

Click "Refresh" to see the time. The code, with tutorial comments:
```clojure
(declare refresh-button)

(defn simple-clock []
  (div {:class [:intro :ticktock]}
    (h2 "The time is now....")
    (div {:class   "intro-clock"
          :content (cF (if-let [now (mget me :now)]         ;; mget, the standard MX getter, can be used from any code,
                         (-> now .toTimeString              ;; but transparently establishes a dependency, or "subscribes",
                           (str/split " ") first)           ;; if called within a formula.
                         "*checking*"))}
      {:name :the-clock
       :now  (cI nil)})                                     ;; cI for "cell Input"; procedural code can write to these
    (refresh-button)))

(defn refresh-button []
  (button
    {:class   :pushbutton
     :onclick #(let [me (evt-md %) 
                     ; evt-md ^^ derives the MX model from the event;
                     ; Next, we search the family up from me (fmu) to find 
                     ; the model named :the-clock...
                     clock (fmu :the-clock me)] 
                 ; ...and reset its property :now, transparently triggering
                 ; full propagation across the DAG:
                 (mset! clock :now (js/Date.)))}
    "Refresh"))

(exu/main #(md/make ::intro
             :mx-dom (simple-clock)))
```

Let us pause to highlight specifically where each unconventional choice manifests itself in concrete code:
* _"in place" state:_ the clock widget holds its own `now` state, which others can read or mutate reactively;
* _property-to-property reactivity:_ the clock `content` consumes the clock `now` property, and the button handler alters the same property `now`;
* _"global" state:_ using `fmu` or other navigation utilities, widgets have unfettered access to application state; and
* otherwise, it is just HTML.

So far, so simple. Will it stay that way as we elaborate the app? We continue.

#### The Running Clock
Our clock is accurate, but requires manual intervention to see the latest time. Not fun. Let's have it run by itself.

> Exercise #1: 

In the function `manual-clock`, add this line after the line `:name :the-clock`:
```
:ticker (cF (js/setInterval #(mset! me :now (js/Date.)) 1000))
```
Save and the clock should run by itself, driven by async mutation of the `now` property.

That is great, but now let us allow the user to control things.

> Exercise #2

Switch to the next example by modifying the launch code at the bottom of the source thus:

```
(exu/main #(md/make ::intro
             :mx-dom (running-clock)))
```
Save and, after the rebuild, the browser app should show a blank, stopped clock. Click "Start" to get the clock running. 

> Exercise #3

Examine the source of the `running-clock` function to see how the crucial `TICKING?` property is used to give the user control. For your convenience:

```clojure
(defn start-stop-button []
  (button
    {:class   :pushbutton
     :onclick #(mswap! (fmu :the-clock (evt-md %)) :TICKING? not)}
    (if (mget (fmu :the-clock me) :TICKING?)
      "Stop" "Start")))

(defn running-clock []
  (div {:class [:intro :ticktock]}
    (h2 "The time is now....")
    (div {:class   "intro-clock"
          :style   (cF (str "color:"
                         (if (mget me :TICKING?) "cyan" "red")))
          :content (cF (if-let [now (mget me :now)]
                         (-> now .toTimeString (str/split " ") first)
                         "__:__:__"))}
      {:name     :the-clock
       :now      (cI nil)
       :ticking? (cI false)
       :ticker   (cF+ [:watch (fn [prop-name me new-value prior-value cell]
                                (when (integer? prior-value)
                                  (js/clearInterval prior-value)))]
                   (when (mget me :TICKING?)
                     (js/setInterval #(mset! me :now (js/Date.)) 1000)))})
    (start-stop-button)))
```

Things to note:
* something new, a `:watch` function on the `ticker` property. `Watch` functions fire when a property changes; here we just scavenge intervals;
* the start-stop button reactively adjusts its label to suit the app state. Same with the `color` property of the clock style;
* the interval function closes over `me` and navigates the DAG from there to mutate state as needed.

#### Web/MX in a nutshell
That is how we build applications with `Web/MX`:
* straight HTML/CSS;
* declarative component definitions, with dynamic properties transparently defined as reactive functions of other MX properties;
* direct mutation of select "input" MX properties by event or async handlers, transparently refreshing dependent state;
* in derivations or mutation, unfettered access to other MX properties.

In the next exercise we will build a modestly richer stopwatch app, and see how well those ingredients hold up.
