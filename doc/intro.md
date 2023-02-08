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
```agsl
(defn refresh-button []
  (button
    {:class   :pushbutton
     :onclick #(let [me (evt-md %)                          ; derive MX model from event; now we can search the whole MX
                     clock (fmu :the-clock me)]             ; navigate family up from me (fmu) to model named :the-clock
                 (mset! clock :now (js/Date.)))}            ; change the property :now of the clock and propagate fully
    "Refresh"))

(defn simple-clock []
  (div {:class [:intro :ticktock]}
    (h2 "The time is now....")
    (div {:class   "intro-clock"
          :content (cF (if-let [now (mget me :now)]
                         (-> now .toTimeString
                           (str/split " ") first)
                         "*checking*"))}
      {:name :the-clock
       :now  (cI nil)})                                     ;; cI for "cell Input"; procedural code can write to these
    (refresh-button)))
```

> Return to the terminal and hit ^C ^D to return to the shell prompt.

### Baby Steps: The Intro Example
Moving forward, we will be modifying one of the examples, `intro.cljs`.

Continue in the same terminal
```bash
cd web-mx # if necessary
clojure -M -m figwheel.main --build intro --repl
```
In a minute we should see a nonsense landing page at `localhost:9500/intro.html` in a new tab our browser.

> Now open the `web-mx` project in our favorite ClojureScript IDE, and open `example/intro.cljs`.

*Exercise*: Modify some displayed text, save, and confirm you see the change in the app. 

All good? Now let us add some DOM.

*Exercise:* Add this code just before the `(img ...)` widget:

```bash
(button
  {:class   "button-2"
   :onclick (fn [_] (prn :hi-mom!))}
  "Speak")
```
_Takeaway:_ `Web/MX` wraps HTML/CSS thinly. The syntax is nearly the same, modified to accommodate ClojureScript. Where HTML has:

```<tag attributes*> children* </tag>```

...Web/MX has:

```
(tag {attributes*} children*)
```

*Exercise:* Change the button code as shown, and add a function call:
```bash
(button
  {:class   :button-2
   :disabled false
   :onclick (fn [e] (prn :MX-widget-clicked> (evt-md e))))}
   {:name :speak-button}
  "Speak")
  (demo-svg)
```
_First takeaway:_ It does SVG, too.

![SVG example](https://github.com/kennytilton/web-mx/blob/main/resources/public/image/svg-climber.png)

Now we note one exception to the similarity. HTML boolean attributes such as `disabled` do not take a value; their presence decides the setting. CLJS maps do not work that way, so, if we will be dynamically toggling `disabled`, we must start it at `false`. As with HTML, omitting the `:disabled` attribute altogether enables the button.

_Second takeaway:_ Some HTML-ese needs to be expressed differently in CLJS.

A bigger exception is the second map, `{:name :speak-button}`. The first map is for HTML attributes, the second map is for any other state we might want to track for this widget, or for implementation state such as the model `:name`.

_Third takeaway:_ in Web/mx, HTML proxy models can be extended with custom state.

> *Exercise:* Confirm that the "Speak" responds to clicks, and displays the proxy button MX model with :name :speak-button.