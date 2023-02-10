# Introduction to Web/MX

Web/MX delivers a simple yet powerful developer experience via several unconventional choices:
* _transparent, fine-grained reactivity:_ [Matrix](https://github.com/kennytilton/matrix/blob/main/cljc/matrix/README.md) transparently and automatically records property-to-property dependencies, and uses that information to keep state self-consistent via glitch-free change propagation;
* _the application is the database:_ state is managed "in place", gathered locally by app components as needed to fulfill their functional specs. No separate store;
* _omiscience/omnipotence:_ when a widget concerns another widget, the state DAG is available for unrestricted outreach, starting from any node. Any property of any widget can read any other property, and any event handler can mutate any property; and
* Web/MX is just [HTML](https://developer.mozilla.org/en-US/docs/Web/HTML). 

Accurate but abstract. Here is what that means to Web/MX development:
* we start from static HTML/CSS;
* where a property needs to be dynamic, ie, change when other things change, we express it as a function of the other properties; 
* widget event handlers can change any "input" property of any other widget.

Let us look at some code that does all that, to make those ideas concrete.

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
