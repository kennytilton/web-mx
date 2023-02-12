A work in progress documenting Web/MX in the context of a simple counter program. Look for NS `tiltontec.example.intro-a-counter` in this repo to run it live.

And now, Web/MX in a nutshell.
#### --- just html -------------------------------------
We still program HTML. 

```clojure
(defn a-counter []
  (div {:class :intro}
    (h2 "The count is now....")
    (p {:class :intro-a-counter} "42")
    (button {:class   :push-button
             :onclick #(js/alert "RSN")} "+")))
```
Where HTML has <tag attributes*> children* </tag>...

...Web/MX has (tag [HTML-attribute-map [custom-attr-map]] children*)

Keywords become strings in HTML. Otherwise, MDN is your guide.

#### --- omniscience -----------------------------
Any component can pull information it needs from anywhere, using "formulas" that can navigate to any other object to read any property.
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
      (range (mget (fmu :a-counter me) :count))))))
```

#### --- omnipotence -----------------------------
Any handler can navigate to any property to change it, with all dependencies being updated before the MSET! or MSWAP! call returns.
```clojure
(defn a-counter []
  (div {:class [:intro]}
    (div {:class "intro"}
      {:name  :a-counter
       :count (cI 3)}                                     ;; 1
  (h2 "The count is now&hellip;") 
  (p {:class :intro-a-counter}
    (str "&hellip;" (mget (mx-par me) :count)))
  (button {:class   :push-button
   :onclick (cF (fn [event] ;; 2
                  (let [counter (fm! :a-counter me)] ;; 3
                     (mswap! counter :count inc))))} ;; 4
   "+"))
  (div (mapv (fn [idx] (span (str idx "...")))
     (range (mget (fmu :a-counter me) :count))))))
```

1. `(cI <value>)` tells MX that the property :count can and might be changed by imperative code;
2. we generate the event handler in a formula for handy access to "me"
3. we use the `FM!` family search utility to navigate to the :a-counter;
4. mutate the property (and dependent state) using MSWAP!


