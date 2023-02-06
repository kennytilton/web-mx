# Introduction to Web/MX

Web/MX delivers a powerful yet simple developer experience, first by leveraging [Matrix](https://github.com/kennytilton/matrix/blob/main/cljc/matrix/README.md), and second by breaking most rules for GUI libraries.

### Pedigree
Well, Web/MX does not really break any rules. The rules did not exist thirty years ago when we applied a similar approach to programming desktop Macs. Later we adapted the approach to the Web, in this Common Lisp JS over-the-wire [Algebra app](http://tiltonsalgebra.com/#), and later still in this Hacker News [AskHN Who's Hiring](https://kennytilton.github.io/whoishiring/) browser developed in [Javascript/MX](https://github.com/kennytilton/matrix/tree/main/js/matrix).

### The Elephant in the Room
Web/MX delivers power, productivity, simplicity, and downright fun. That is what we want to talk about. If that is all our reader cares about, they can safely skip to the next section. But we _do_ see that elephant over there, so let us speak to it: Web/MX works different.
* property-to-property, fine-grained reactivity. No view functions with subscriptions to a separate store, and no `setState` to trigger redraws;
* transparent reactivity. No publish/subscribe. Just read and mutate reactive properties using MX API `mget` and `mset!`/`mswap!`;
* "in-place" state. No [Flux](https://facebook.github.io/flux/) pattern separate store;
* global scope. No "pure" view functions limited to props and subscriptions for data. Event handlers can reach and mutate any application property;
* functions like `(div...)` and `(span...)` to specify DOM. No \<JSX> or [Hiccup];
* direct DOM manipulation (handled transparently). No VDOM, no `shouldComponentUpdate` required to avoid excess VDOM generation and diffing.

So Web/MX is different, but not uniquely so. Some of the same differences are found in [the original MobX](https://mobx.js.org/README.html), [binding.Scala](https://github.com/ThoughtWorksInc/Binding.scala/blob/12.x/README.md), [Hoplon/Javelin](https://github.com/hoplon/javelin), [SolidJS](https://www.solidjs.com/), and more prior art than you can imagine, especially a venerable Lisp framework, [Garnet/KR](https://sourceforge.net/projects/garnetlisp/). So we do not feel _too_ bad. Let's get on with the D/X.

### The Developer Experience
The only way to grok the Web/MX Difference&trade; is to code with it. We begin.

#### Hello Clock
Begin by cloning Web/MX itself and running one of the examples:
```bash
git clone https://github.com/kennytilton/web-mx.git
cd web-mx
clojure -M -m figwheel.main --build simpleclock --repl
```

![Web MX](images/simpleclock.png)