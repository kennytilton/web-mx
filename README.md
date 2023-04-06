![Web MX](images/web-mx-chain-dag.jpg)

Web/MX&trade;: ClojureScript Web programming powered by the [Matrix](https://github.com/kennytilton/matrix/blob/main/cljc/matrix/README.md) state manager.

### Overview
Web/MX thinly wraps HTML with a functional API, plus fine-grained reactivity, coded declaratively and transparently, without boilerplate. 

If you are just looking around, here is an in-depth yet succinct ["quick start"](https://kennytilton.github.io/web-mx-quickstart/#/). It is strongly recommended to newcomers and tire-kickers.

> Related work: [Web/JX](https://github.com/kennytilton/matrix/tree/main/js/matrix) drives this [AskHN "Who's Hiring?" browser](https://kennytilton.github.io/whoishiring/). Work on React/MX and RN/MX has reached POC, but we have shifted focus to [Flutter/MX](https://github.com/kennytilton/flutter-mx/blob/main/README.md). 

Release: [![Clojars Project](https://img.shields.io/clojars/v/com.tiltontec/web-mx.svg)](https://clojars.org/com.tiltontec/web-mx)

WIP: [![Clojars Project](https://img.shields.io/clojars/v/com.tiltontec/web-mx.svg?include_prereleases)](https://clojars.org/com.tiltontec/web-mx)

### Just Run It
Web/MX is a library, but can also be run as a Web app to try examples.

To run the current example, just:
```bash
git clone https://github.com/kennytilton/web-mx.git
cd web-mx
clojure -M -m figwheel.main --build intro-clock --repl
```
After a minute or so the `quick-start` example should appear in your browser at `http://localhost:9500/intro-clock.html`, leaving a CLJS REPL in the original terminal.

### Other Working Examples
More working examples, including two versions of the TodoMVC classic can be found in the [Web/MX Sampler](https://github.com/kennytilton/web-mx-sampler). See also the [CLJS version](https://github.com/kennytilton/matrix/tree/main/cljc/whoshiring) of the aforementioned AskHN Who's Hiring browser.

The [Web/MX Workshop](https://github.com/kennytilton/web-mx-workshop/wiki/The-Evolution-of-a-Web-MX-Inspector) project takes a more in-depth, real-world approach to demonstrating Web/MX programming.

#### Support
Visit the #matrix channel on the Clojurians Slack for help.

### License

Copyright © 2016 Kenneth Tilton

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
