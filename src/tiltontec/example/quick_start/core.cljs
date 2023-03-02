(ns tiltontec.example.quick-start.core
  (:require
    [tiltontec.cell.core :refer [cF cF+ cFn cFonce cI cf-freeze]]
    [tiltontec.model.core
     :refer [mx-par mpar mget mset! mswap! mset! mxi-find mxu-find-name fasc fmu fm!] :as md]
    [tiltontec.web-mx.gen :refer [evt-md target-value]]
    [tiltontec.web-mx.gen-macro
     :refer [title img section h1 h2 h3 input footer p a b h4 u table th tr td
             blockquote span i label ul li div button br pre code]]
    [tiltontec.example.util :as exu]
    [tiltontec.example.quick-start.lesson :as lesson]))

(defn quick-start-toolbar []
  (div {:class :toolbar
        :style {:flex-direction  :column
                :align-items     :start
                ;:background :pink
                :justify-content :start
                }}
    ;;(span "Pick one:")
    (doall (for [{:keys [menu title] :as clk} (mget (fasc :demos me) :demos)]
             (button {:class   :pushbutton
                      :cursor  :finger
                      :style   (cF (let [curr-clk (mget (fasc :demos me) :selected-demo)]
                                     {:min-width    "144px"
                                      :border-color (if (= clk curr-clk)
                                                      "orange" "white")
                                      :font-weight  (if (= clk curr-clk)
                                                      "bold" "normal")}))
                      :onclick (cF (fn [] (mset! (fmu :demos) :selected-demo clk)))}
               (or menu title))))))

(defn quick-start [demo-title start-demo-ix & demos]
  (div {} {:name          :demos
           :selected-demo (cFn (nth (mget me :demos)
                                 (cond
                                   (neg? start-demo-ix) 0
                                   (>= start-demo-ix (count demos)) (dec (count demos))
                                   :else start-demo-ix)))
           :demos         demos
           :show-glossary? (cI false)}

    (div {:style {:display :flex
                  :gap     "2em"}}
      (div {:style {:display         :flex
                    :flex-direction  :column
                    :align-items     :center
                    :justify-content :start
                    :border-right    "4mm ridge orange"     ;; "rgba(211, 220, 50, .6)"
                    }}
        (span {:style {:font-size      "24px"
                       :margin-bottom  "1em"
                       :padding-bottom "1em"
                       :text-align     :center}}
          demo-title)

        (quick-start-toolbar))

      (when-let [clk (mget (fasc :demos me) :selected-demo)]
        (div {:style {:display        :flex
                      :flex-direction :column
                      :padding        "6px"}}
          (h1 (:title clk))
          (when-let [preamble (:preamble clk)]
            (if (string? preamble)
              (p {:class :preamble} preamble)
              (doall (for [elt preamble]
                       (p {:class :preamble} elt)))))
          (div {:style {:border-color "orange"
                        :border-style "solid"
                        :border-width "2px"}}
            ((:builder clk)))

          (pre {:class :lesson-code}
            (code {:style {:font-size "16px"}}
              (:code clk)))

          (div {:style {:display :flex
                        :flex-direction :row
                        :gap "6px"
                        :margin-top "9px"}}
            {:name :glossary}
            (span {:class :pushbutton
                   :onclick #(mswap! (fasc :demos (evt-md %)) :show-glossary? not)}
              "Glossary")
            (div {:style (cF (str "display:" (if (mget (fasc :demos me) :show-glossary?)
                                               "block" "none")))}
              (table
                (tr
                  (th "Symbol")
                  (th "Comments"))
                (mapv (fn [[usage description]]
                        (tr
                          (td usage)
                          (td description)))
                  [["(mget <i>model</i> <i>property</i>)"
                    "The MX getter. Can be called from anywhere. When called in the scope of a Cell formula,
                  establishes a reactive dependency on the gotten property."]
                   ["(mset! <i>model</i> <i>property</i> <i>value</i>)"
                    "The MX setter. Alias <code>mreset!</code>. Call from any imperative code. When calling
                  from a <code>watch/observer</code>, must be wrapped in <code>(with-cc :tag setter)</code>"]
                   ["(mswap! md prop fn & args)" "mx swap!"]
                   ["(with-cc tag & body)"
                    "Required wrapper for MX mutation in scope of a watch function."]
                   ["(cI value & option-values)"
                    "Marks the associated property as an MX input. eg, `:answer (cI 42)`"]
                   ["(cF & body)"
                    "Provides a derived value for a property. Hidden parameter `me`. eg :answer (cF (* 6 9))"]
                   ["(cF+ [& option-values] & body)"
                    "A version of `cF` that takes cell options such as :watch."]
                   ["(cFn & body)"
                    "Start as formula for initial value computation, then convert to input cell. Akin to
                    \"constructor initialization\"."]
                   ["(cFonce & body)"
                    "Start as formula for initial computation, then behave as immutable property. Alias `cF1`."]
                   ["(fm-navig seeking starting-at & options)"
                    "Search MX nodes for node matching `seeking`."]
                   ["(fmu seeking & starting)"
                    "Search `up and around` from starting node, which defaults to lexical `me`."]
                   ["(fasc seeking starting)"
                    "Search ascending parent chain from starting."]
                   ]))))

          (when-let [c (:comment clk)]
            (if (string? c)
              (p {:class :preamble} c)
              (doall (for [cx c]
                       (p {:class :preamble} cx)))))
          #_ (when-let [ex (:exercise clk)]
               (blockquote {:class :exercise}
                 (p (str "Give it a try. Modify <i>" (:ns clk "the code") "</i>."))
                 (if (string? ex)
                   (p  ex)
                   (doall (for [elt ex]
                            (p  elt)))))))))))

(exu/main #(md/make ::intro
             :mx-dom (quick-start "Web/MX&trade;<br>Quick Start" 0
                       lesson/ex-just-html
                       lesson/ex-and-cljs
                       lesson/ex-html-composition
                       lesson/ex-custom-state
                       lesson/ex-derived-state

                       lesson/ex-navigation
                       lesson/ex-handler-mutation
                       lesson/ex-watches
                       lesson/ex-watch-cc
                       ;; lesson/ex-async-throttle
                       lesson/ex-async-cat

                       lesson/ex-data-integrity
                       lesson/ex-in-review
                       ;;lesson/ex-ephemeral ;; too much?
                       #_ {:title "Counter Omniscient" :builder counter-omniscience :code counter-omniscience-code}
                       #_ {:title "Counter Omnipotent" :builder counter-omnipotent :code counter-omnipotent-code}
                       #_ {:title "Reactivity All-In" :builder reactivity-all-in :code reactivity-all-in-code}
                       #_  {:title "Mini test" :builder minitest :code minitest-code})))

