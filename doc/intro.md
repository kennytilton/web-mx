# Introduction to Web/MX

Web/MX delivers a radically more powerful yet simpler GUI developer experience, thanks to two unconventional choices:
* "reactivity first": [Matrix](https://github.com/kennytilton/matrix/blob/main/cljc/matrix/README.md) fine-grained, transparent reactivity sits at the heart of Web/MX. It drives _everything_; and
* let HTML be HTML; [MDN](https://developer.mozilla.org/en-US/docs/Web/HTML) will be your only Web/MX reference after a week.

### Why another Web framework?
Why yet another Web framework? Because [Mr. Hickey was right](https://youtu.be/2V1FtfBDsLU?t=1261): the effort of UI coding dwarfs the functionality delivered, even after decades of work producing dozens of GUI frameworks.

> "I don't do that part." -- Rich Hickey on UI coding, ClojureConj 2017

We meant well, but we have made a mess of UI programming. Every effort to improve things ended up adding another layer of cruft, another preprocessor, and a bundler to control it all. 

Web/MX took another tack, simply wrapping HTML/CSS with programmer-friendly state management. A powerful, minimalist, fun framework emerged. Rich Hickey, call your office.

### The Developer Experience
At pain of stating the obvious, the only way to grok the Web/MX Difference&trade; is to code with it. From here on we guide the reader through actual coding of a trivial web app, trivial but sufficient to cover the special qualities of Web/MX.

#### Hello Clock
Start by cloning Web/MX itself and running one of the examples. 

> In a terminal:
```bash
git clone https://github.com/kennytilton/web-mx.git
cd web-mx
clojure -M -m figwheel.main --build simpleclock --repl
```
In a minute, look for this to appear in your browser at [localhost:9500/simpleclock](http://localhost:9500/simpleclock.html):

![Web MX](../images/simpleclock.png)

We can edit the hex color and, when the value is valid 3 or 6 hex digits, see the clock digits change to that color. Invalid values will make the field background turn pink and the digits revert to black. The somewhat heavily documented code is [here](https://github.com/kennytilton/web-mx/blob/main/src/tiltontec/example/simpleclock.cljs). 

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