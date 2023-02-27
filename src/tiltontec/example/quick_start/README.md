# The Web/MX&trade; Quick Start
Web/MX works different. But in some ways, it works the same. Nothing is unique to Web/MX, but the same things are executed differently, and to a different degree.

Below, we quickly touch on a dozen essential Web/MX mechanisms that cover everything we need to know about Web/MX and the developer experience, or D/X, it supports.

The same content can be viewed in an executable fashion by running the code alongside this README file:
```bash
cd web-mx
clojure -M -m figwheel.main --build quick-start --repl
```
For those to content to read...
## It Is Just HTML
To begin with, we just write HTML, SVG, and CSS, each thinly disguised as CLJS.
```clojure
(div {:class :intro}
    (h2 "The count is now....")
    (span {:class :digi-readout} "42")
    (svg {:width 64 :height 64 :cursor :pointer
          :onclick #(js/alert "Increment Feature Not Yet Implemented")}
      (circle {:cx "50%" :cy "50%" :r "40%"
               :stroke  "orange" :stroke-width 5
               :fill :transparent})
      (text {:class :heavychar :x "50%" :y "70%"
             :text-anchor :middle} "+")))
```
Web/MX introduces no framework of its own, it just manages the DOM. Aside from CLJS->JS, no preprocessor is involved, and the stability of CLJS makes this one exception a net win.

Matrix just manages the state.