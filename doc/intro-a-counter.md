# Web/MX&trade; In a Nutshell
_Or, building a counter app._

Here is `Web/MX` in a tl;dr nutshell:
* the developer writes standard HTML, CSS, and SVG;
* component properties, GUI or domain, view or model, can be functions of any property of any other component;
* event handlers can update any designated input property of any component; and
* with more or less "glue" code, non-Matrix mechanisms can be made reactive.

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
The counter display will change when the counter changes, but we need the next example to change the counter.

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

#### 4. All-in reactivity (omipresence?)
Reactivity is neat, so we want to use it everywhere,even with software that knows nothing about Matrix reactive mechanisms. In this next example, we use some simple "glue code" to connect the non-reactive `js/setInterval` with reactive Matrix elements.
```clojure

(defn a-counter []
  (div {:class "intro"}
       {:name     :a-counter
        :count    (cI 0)
        :ticker   (cF (js/setInterval ;; 1
                         #(mswap! me :count inc) ;; 2
                         1000))}
    (h2 "The count is now&hellip;")
    (span {:class :intro-a-counter}
      (str (mget (mx-par me) :count)))))
```
1. By using a formula to create the interval, we get lexical acces to "me".
2. Intervals fire asynchronously, and intervals do not know about Matrix, but we use the API `mswap!` to accurately update the :count and the entire DAG. In the end, async mechanisms are no more "async" than a user deciding at will to click a button.

## Summary:
Rich, dynamic Web apps are easier to build from declarative component definitions with critical properties defined as functions of other properties. 

The resulting dependency graph, automatically detected, can be used by a generic engine to handle the tedious, error-prone work of keeping state consistent.

With the state management burden lifted, the developer can concentrate on the application, and is encouraged to make it even more responsive.

The more utilities and libraries we "wrap" with Matrix, the bigger the productivity win.
