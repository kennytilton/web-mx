![Web MX](images/web-mx-chain-dag.jpg)

Web/MX: Web programming powered by the [Matrix](https://github.com/kennytilton/matrix/blob/main/cljc/matrix/README.md) state manager.

An [mxWeb Training Guide](https://github.com/kennytilton/mxweb-trainer/wiki) was started. It contains a series of graded exercises, each with its own tutorial doc and coding exercise(s). Give it a look, but we plan a different approach to doc.

Work on React/MX and RN/MX has reached POC, but we have shifted focus to [Flutter/MX](https://github.com/kennytilton/flutter-mx/blob/main/README.md).

### Just Run It

Web/MX is a library, but can also be run as a Web app to try examples.

To run the current example, just:
```bash
git clone https://github.com/kennytilton/web-mx.git
cd web-mx
lein fig -- --build example --repl
```
Edit `tiltontec.example.core.cljs` to have the app run different examples.

### Overview

Optimally efficient, fine grained, truly reactive DOM programming in CLJS, without React, without _virtual dom_.

### Working Example

The [AskHN Who's Hiring Browser](https://github.com/kennytilton/matrix/tree/main/cljc/whoshiring) is the CLJS port of the JS version of Web/MX, which you can find [live here](https://kennytilton.github.io/whoishiring/)

### Other Working Examples
The classic, [TodoMVC](https://github.com/kennytilton/mxtodomvc) with some tutorial doc.

[rxTrak](https://github.com/kennytilton/matrix/tree/master/cljs/rxtrak) takes [TodoMVC](https://todomvc.com/) to a new level by incorporating an AJAX lookup of each "to-do", now rX prescription, on the [FDA Drug database](https://open.fda.gov/apis/) looking adverse events with that drug. 

[rxTrak](https://github.com/kennytilton/matrix/tree/master/cljs/rxtrak) demonstrates the grace with which Matrix handles async as does the [Matrix Flux Challenge](https://github.com/kennytilton/matrix/tree/master/cljs/fluxchallenge) implementation. The [Flux Challenge](https://github.com/staltz/flux-challenge) originated from author Staltz's concern: "It's my personal belief that Flux does not provide an elegant way of coordinating multiple async data sources...".

And of course the [training guide](https://github.com/kennytilton/mxweb-trainer/wiki) is full of smaller working examples.

### License

Copyright © 2016 Kenneth Tilton

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
