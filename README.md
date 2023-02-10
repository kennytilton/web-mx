![Web MX](images/web-mx-chain-dag.jpg)

Web/MX: Web programming powered by the [Matrix](https://github.com/kennytilton/matrix/blob/main/cljc/matrix/README.md) state manager.

[![Clojars Project](https://img.shields.io/clojars/v/com.tiltontec/web-mx.svg)](https://clojars.org/com.tiltontec/web-mx)

### Overview

Optimally efficient, fine grained, truly reactive DOM programming in CLJS, without React, without _virtual dom_, without a separate store.This new, [hands-on](https://github.com/kennytilton/web-mx/blob/main/doc/intro-clock.md) introduction will explain the vital "why" behind these fundamental architectural differences. It is strongly recommended to newcomers and tire-kickers.

> Related work: [Javascript/MX](https://github.com/kennytilton/matrix/tree/main/js/matrix) drives this [AskHN "Who's Hiring?" browser](https://kennytilton.github.io/whoishiring/). Work on React/MX and RN/MX has reached POC, but we have shifted focus to [Flutter/MX](https://github.com/kennytilton/flutter-mx/blob/main/README.md). 

### Just Run It

Web/MX is a library, but can also be run as a Web app to try examples.

To run the current example, just:
```bash
git clone https://github.com/kennytilton/web-mx.git
cd web-mx
clojure -M -m figwheel.main --build ticktock --repl
```
After a minute or so the `ticktock` example should appear in your browser at `http://localhost:9500/ticktock.html`, leaving a CLJS REPL in the original terminal.

Other examples, besides `ticktock`, are `todomvc` and `rxtrak`.

### Other Working Examples
The classic, [TodoMVC](https://github.com/kennytilton/mxtodomvc) with some tutorial doc.

[rxTrak](https://github.com/kennytilton/matrix/tree/master/cljs/rxtrak) takes [TodoMVC](https://todomvc.com/) to a new level by incorporating an AJAX lookup of each "to-do", now rX prescription, on the [FDA Drug database](https://open.fda.gov/apis/) looking adverse events with that drug. 

A full [Web/MX "Who's Hiring?"](https://github.com/kennytilton/matrix/tree/main/cljc/whoshiring) app

And of course the [training guide](https://github.com/kennytilton/mxweb-trainer/wiki) is full of smaller working examples (but needs updating).

### License

Copyright © 2016 Kenneth Tilton

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
