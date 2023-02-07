# Introduction to Web/MX

Web/MX delivers a powerful yet simple developer experience:
* first by letting HTML/CSS be [HTML/CSS](https://developer.mozilla.org/en-US/docs/Web/HTML);
* second by leveraging [Matrix](https://github.com/kennytilton/matrix/blob/main/cljc/matrix/README.md), proving fine-grained, transparent reactivity; and
* third by breaking most rules for GUI libraries.

### Why another Web framework?
Because Mr. Hickey is right. We have made a mess of UI programming.

<iframe width="560" height="315" src="https://www.youtube.com/embed/2V1FtfBDsLU?start=1272" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>

### Pedigree
Well, Web/MX does not really break any rules. The rules did not exist thirty years ago when we applied a similar approach to programming desktop Macs. Later we adapted the approach to the Web, in this Common Lisp JS over-the-wire [Algebra app](http://tiltonsalgebra.com/#), and later still in this Hacker News [AskHN Who's Hiring](https://kennytilton.github.io/whoishiring/) browser developed in [Javascript/MX](https://github.com/kennytilton/matrix/tree/main/js/matrix).

### The Elephant in the Room
Web/MX delivers power, productivity, simplicity, and downright fun. That is what we want to talk about. If that is all our reader cares about, they can safely skip to the next section. But we _do_ see that elephant over there, so let us speak to it: Web/MX works different.
* _property-to-property, fine-grained reactivity:_ No view functions with subscriptions to a separate store, and no `setState` to trigger redraws;
* _transparent reactivity_: No publish/subscribe. Just read and mutate reactive properties using MX API `mget` and `mset!`/`mswap!`;
* _"in-place" state:_ No [Flux](https://facebook.github.io/flux/) pattern separate store;
* global scope. No "pure" view functions limited to props and subscriptions for data. Event handlers can reach and mutate any application property;
* _functions like `(div...)` and `(span...)` to specify DOM:_ No \<JSX> or [Hiccup];
* _direct DOM manipulation (handled transparently):_ No VDOM, no `shouldComponentUpdate` required to avoid excess VDOM generation and diffing.

So Web/MX is different, but not uniquely so. Some of the same differences are found in [the original MobX](https://mobx.js.org/README.html), [binding.Scala](https://github.com/ThoughtWorksInc/Binding.scala/blob/12.x/README.md), [Hoplon/Javelin](https://github.com/hoplon/javelin), [SolidJS](https://www.solidjs.com/), and more prior art than you can imagine, especially a venerable Lisp framework, [Garnet/KR](https://sourceforge.net/projects/garnetlisp/). So we do not feel _too_ bad. Let us get on with the D/X.

### The Developer Experience
The only way to grok the Web/MX Difference&trade; is to code with it. We begin.

#### Hello Clock
Begin by cloning Web/MX itself and running one of the examples. In a terminal:
```bash
git clone https://github.com/kennytilton/web-mx.git
cd web-mx
clojure -M -m figwheel.main --build simpleclock --repl
```
In a minute, look for this to appear in a browser near you at [localhost:9500/simpleclock](http://localhost:9500/simpleclock.html):

![Web MX](../images/simpleclock.png)

The reader should be able to edit the hex color and, when it is valid, see the clock digits change to that color. Invalid values will make the field background turn pink and the digits revert to black. The curious reader will find the somewhat heavily documented [code here](https://github.com/kennytilton/web-mx/blob/main/src/tiltontec/example/simpleclock.cljs). 

Now we can return to the terminal and enter ^C ^D to return to the shell prompt.

Before continuing, for convenience we can add this function to our shell startup:
```
figo () {
    echo "figwheel building and running $1"
    clojure -M -m figwheel.main --build $1 --repl
}
```
### Baby Steps: The Intro Example
We will be modifying one of the examples, `intro.cljs`. Let us fire that up. 

#### Preamble
In a terminal:
```bash
cd web-mx
clojure -M -m figwheel.main --build intro --repl
```
In a minute we should see a blank (kinda) landing page at `localhost:9500/intro.html`.
### Baby Step 1
Now we need to open the `web-mx` project in our favorite ClojureScript IDE, and open `example/intro.cljs`.

> Your task: Just modify some displayed text, save, and confirm you see the change in the app. 

All good? Now let us add some DOM.
##### Web/MX Syntax
Web/MX functions for generating DOM mirror the syntax of HTML. For example, where HTML has:
```html
<tag attributes*> children* </tag>
```
...Web/MX has:
```clojure
(tag [{attributes*}] children*)
```
##### Your task
* add a button just above the image;
* specify `class` as "button-2"
* the button text should be "Speak";
* when clicked it should print "Hi, mom!" in the browser console.

##### Spoiler alert!
We can complete this task by placing this code above the `(img...)`.
```bash
(button
  {:class   "button-2"
   :onclick (fn [_] (prn :hi-mom!))}
  "Speak")
```
Once you have that working, we can come back here and draw a lesson.

#### Baby Step 1 Summary
`Web/MX` wraps HTML/CSS thinly. The syntax is nearly the same, modified to accommodate Clojurescript syntax instead of Javascript. MDN is your main reference requirement. And if you already know HTML/CSS, you know most of Web/MX.

###
