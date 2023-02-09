# Introduction to Web/MX

Web/MX delivers a simple, surprisingly powerful GUI developer experience through several unconventional choices:
* "reactivity first": [Matrix](https://github.com/kennytilton/matrix/blob/main/cljc/matrix/README.md) property-to-property, transparent reactivity drives _everything_;
* state DAG is globally searchable and mutable from anywhere. Any property of any widget can read any other property, and any event handler can mutate any property; 
* state is managed "in place", gathered by app components as they find necessary; and
* Web/MX is just [HTML](https://developer.mozilla.org/en-US/docs/Web/HTML). 

Accurate but abstract. Let us look at actual code that does all that, a simple clock app.

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

And now the code:
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
                     ; then search the family up from me (fmu) to find 
                     ; the model named :the-clock...
                     clock (fmu :the-clock me)] 
                 ; ...and reset its property :now, transparently triggering
                 ; full propagation across the DAG:
                 (mset! clock :now (js/Date.)))}
    "Refresh"))

(exu/main #(md/make ::intro
             :mx-dom (simple-clock)))
```

And now we can connect the "unconventional choices" with concrete code:
* _"in place" state:_ the clock widget holds its own state, which others can read or mutate reactively;
* _property-to-property reactivity:_ the clock `content` consumed the clock `now` property, and the button handler altered the same property `now`;
* _"global" state search and mutate:_ using `fmu` or other navigation utilities, widgets have unfettered access to application state; and
* otherwise, it is just HTML.

So far, so simple. Will it stay that way as we elaborate the app?

#### The Running Clock
Our clock is accurate, but requires manual intervention to see the latest time. Not fun. Let's have it run by itself, at the user's option. Look for the crucial `ticking?` property below.

```clojure
(declare start-stop-button)

(defn running-clock []
  (div {:class [:intro :ticktock]}
    (h2 "The time is now....")
    (div {:class   "intro-clock"
          :content (cF (if-let [now (mget me :now)]
                         (-> now .toTimeString (str/split " ") first)
                         "---"))}
      {:name :the-clock
       :now  (cI nil)
       :ticking? (cI true)
       :ticker (cF+ [:watch (fn [slot-name me new-value prior-value cell]
                              (when (integer? prior-value)
                                (js/clearInterval prior-value)))]
                 (when (mget me :ticking?)
                   (js/setInterval #(mset! me :now (js/Date.)) 1000)))})
    (start-stop-button)))

(defn start-stop-button []
  (button
    {:class   :pushbutton
     :onclick #(let [me (evt-md %)
                     the-clock (fmu :the-clock me)]
                 (mswap! the-clock :ticking? not))}
    (if (mget (fmu :the-clock me) :ticking?)
      "Stop" "Start")))
```
...and change the launch code:
```clojure
(exu/main #(md/make ::intro
             :mx-dom (running-clock)))
```
Things to note:
* the `:ticker`'s `:watch` function. `Watche`s fire when a property changes;
* the start-stop button reactively adjusts its label to suit the app state (widget children get wrapped transparently in formulas);
* the interval function closes over `me`, and navigates from there to mutate the DAG as needed.

#### Web/MX in a nutshell
And that is Web/MX:
* declarative component definitions;
* reactive properties, connected transparently;
* unfettered access to application state, whether reading or mutating; and
* otherwise, it is just HTML.

In the next exercise we will build a modestly richer stopwatch app, and see whether those ingredients suffice to build efficient, reliable self-documenting applications.
