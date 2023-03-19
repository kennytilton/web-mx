![Web MX](images/web-mx-chain-dag.jpg)

Web/MX: Web programming powered by the [Matrix](https://github.com/kennytilton/matrix/blob/main/cljc/matrix/README.md) state manager.

[![Clojars Project](https://img.shields.io/clojars/v/com.tiltontec/web-mx.svg)](https://clojars.org/com.tiltontec/web-mx)

### Overview

Optimally efficient, fine grained, truly reactive DOM programming in CLJS, without React, without _virtual dom_, without a separate store. Here is an in-depth yet succinct ["quick start"](https://kennytilton.github.io/web-mx-quickstart/#/). It is strongly recommended to newcomers and tire-kickers.

> Related work: [Web/JX](https://github.com/kennytilton/matrix/tree/main/js/matrix) drives this [AskHN "Who's Hiring?" browser](https://kennytilton.github.io/whoishiring/). Work on React/MX and RN/MX has reached POC, but we have shifted focus to [Flutter/MX](https://github.com/kennytilton/flutter-mx/blob/main/README.md). 

### Just Run It

Web/MX is a library, but can also be run as a Web app to try examples.

To run the current example, just:
```bash
git clone https://github.com/kennytilton/web-mx.git
cd web-mx
clojure -M -m figwheel.main --build quick-start --repl
```
After a minute or so the `quick-start` example should appear in your browser at `http://localhost:9500/quick-start.html`, leaving a CLJS REPL in the original terminal.

Other examples are `ticktock`, `todomvc`, and `rxtrak`.

### Other Working Examples
More working examples, including two versions of the TodoMVC classic can be found in the [Web/MX Sampler](https://github.com/kennytilton/web-mx-sampler). See also the [CLJS version](https://github.com/kennytilton/matrix/tree/main/cljc/whoshiring) of the aforementioned AskHN Who's Hiring browser.

### License

Copyright © 2016 Kenneth Tilton

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
