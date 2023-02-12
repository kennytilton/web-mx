# Web/MX&trade; In a Nutshell
_Or, building a counter app._

This write-up is also a working app, one of the Web/MX examples. . Look for NS `tiltontec.example.intro-a-counter` in this repo if you would like to run it live while reading.

And now, Web/MX in a nutshell:
* the developer just writes standard HTML, CSS, and SVG;
* responsive behavior comes from property formulas that reactively access any app state to determine behavior and rendering;
* the Matrix state engine automatically detects formula dependencies, and propagates change automatically; and
* any event handler can update any state.

Let us look at each of those in the context of a simple counter app.

#### 1. We just write HTML
We still program with HTML and CSS:

```clojure
(defn a-counter []
  (div {:class :intro}
    (h2 "The count is now....")
    (p {:class :intro-a-counter} "42")
    (button {:class   :push-button
             :onclick #(js/alert "RSN")} "+")))
```
Where HTML has:

`<tag attributes*> children* </tag>`

Web/MX has:

`(tag [HTML-attributes* [custom-state]] children*)`

Also, CLJS keywords become strings in HTML. Otherwise, [MDN](https://developer.mozilla.org/en-US/docs/Web/Guide) is your guide.

#### 2. Omniscience
Any component can pull information it needs from anywhere, using "formulas" that can (1) navigate to any other object to (2) simply read its properties.
```clojure
(defn a-counter []
  (div {:class [:intro]}
    (div {}
      {:name  :a-counter                                  
       :count 3}                                          
      (h2 "The count is now&hellip;")
      (p {:class :intro-a-counter}
        (str "&hellip;" (mget (mx-par me) :count))))  ;; <======
    (div (mapv (fn [idx] (span (str idx "...")))      
      (range (mget (fmu :a-counter me) :count)))))).  ;; <======
```

#### 3. Omnipotence
Any handler can navigate to any property to change it, with all dependencies being updated before the MSET! or MSWAP! call returns.
```clojure
(defn a-counter []
  (div {:class [:intro]}
    (div {:class "intro"}
      {:name  :a-counter
       :count (cI 3)}                                ;; 1
  (h2 "The count is now&hellip;") 
  (p {:class :intro-a-counter}
    (str "&hellip;" (mget (mx-par me) :count)))
  (button {:class   :push-button
   :onclick (cF (fn [event]                          ;; 2
                  (let [counter (fm! :a-counter me)] ;; 3
                     (mswap! counter :count inc))))} ;; 4
  ; just a random demonstration of dynamic, interdependent state...
  (div (mapv (fn [idx] (span (str idx "...")))
     (range (mget (fmu :a-counter me) :count))))))
```

1. `(cI <value>)` tells MX that the property :count can and might be changed by imperative code;
2. we generate the event handler in a formula for handy access to "me"
3. we use the `FM!` family search utility to navigate to the :a-counter;
4. mutate the property (and dependent state) using MSWAP!

## Summary:
Rich, dynamic Web apps are greatly easier to build when we can write declarative component definitions with critical properties defined as functions of other properties. 

The resulting dependency graph, automatically detected, can be used by a generic engine to handle the tedious, error-prone work of keeping state consistent.

With the state management burden lifted, the developer can concentrate on the application, and is encouraged to make it even more responsive.
