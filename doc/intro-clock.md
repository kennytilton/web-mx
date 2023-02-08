# Introduction to Web/MX

Web/MX delivers a simple, surprisingly powerful GUI developer experience through several unconventional choices:
* "reactivity first": [Matrix](https://github.com/kennytilton/matrix/blob/main/cljc/matrix/README.md) property-to-property, transparent reactivity drives _everything_;
* state DAG is globally searchable and mutable. Any property can read any other property, any event handler can mutate any property; 
* state is managed "in place", gathered by app components as they require; and
* Web/MX is just [HTML](https://developer.mozilla.org/en-US/docs/Web/HTML). 

We next look at a simple example that touches on all that, a simple clock app.

#### Hello Clock
Follow these steps to clone Web/MX itself and run an example. 

> In a terminal:
```bash
git clone https://github.com/kennytilton/web-mx.git
cd web-mx
clojure -M -m figwheel.main --build intro-clock --repl
```
In a minute, look for this to appear in your browser at [localhost:9500/simpleclock](http://localhost:9500/intro-clock.html):

![Web MX](https://github.com/kennytilton/web-mx/blob/main/resources/public/image/intro-clock-checking.png)

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
     :onclick #(let [me (evt-md %)                          ; evt-md derives the MX model from the event; we then navigate
                     the-clock (fmu :the-clock me)]             ; the family up from me (fmu) to find the model named :the-clock
                 (mset! the-clock :now (js/Date.)))}            ; and reset its property :now, propagating fully to the DAG
    "Refresh"))                                             ; before returning.

(exu/main #(md/make ::intro
             :mx-dom (simple-clock)))
```

As promised:
* _"in place" state:_ the clock widget holds its own state, which others can read or mutate reactively;
* _property-to-property reactivity:_ the clock `content` read the clock `now` property, and the button handler altered the property `now`;
* _"global" state search and mutate:_ using `fmu` or other navigation utilities, widgets have unfettered access to application state.

So we have a clock we can check manually. Now let us see how to make it run automatically, but under the user's control.

#### The Running Clock
Our clock is accurate, but requires manual intervention to see the latest time. Not fun. Let's set it up run by itself, at the user's option.

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
       :ticking (cI true)
       :ticker (cF+ [:watch (fn [slot-name me new-value prior-value cell]
                              (when (integer? prior-value)
                                (js/clearInterval prior-value)))]
                 (when (mget me :ticking)
                   (js/setInterval #(mset! me :now (js/Date.)) 1000)))})
    (start-stop-button)))

(defn start-stop-button []
  (button
    {:class   :pushbutton
     :onclick #(let [me (evt-md %)
                     the-clock (fmu :the-clock me)]
                 (mswap! the-clock :ticking not))}
    (if (mget (fmu :the-clock me) :ticking)
      "Stop" "Start")))
```
Things to note:
* the `:ticker`'s `:watch` function, is dispatched when a property changes;
* the start-stop button adjusts its label to suit the app state (widget children get wrapped transparently in formulas);
* the interval function closes over `me`, and uses that to mutate the DAG as needed.

#### Web/MX in a nutshell
And that is Web/MX:
* declarative component definitions;
* reactive properties;
* unfettered access to application state, whether reading or mutating;
* and it is just HTML.