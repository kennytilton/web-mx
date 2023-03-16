(ns tiltontec.example.util
  (:require [goog.dom :as gdom]

            [tiltontec.matrix.api
             :refer [cF cF+ cFn cFonce cI cf-freeze
                     mpar mget mset! mswap! mset!
                     fasc fmu fm! minfo]]

            [tiltontec.web-mx.gen-macro
             :refer [title img section h1 h2 h3 input footer p a b h4 u table th tr td
                     blockquote span i label ul li div button br pre code]]

            [tiltontec.web-mx.html :refer [tag-dom-create]]))

(defn main [mx-builder]
  (println "[main]: loading")
  ;; (prn :html!!!! (.-outerHTML (tag-dom-create (span "Hi mom"))))
  (let [root (gdom/getElement "app")
        ;; ^^^ "app" must be ID of DIV defined in index.html
        app-matrix (mx-builder)
        app-dom (tag-dom-create
                  (mget app-matrix :mx-dom))]
    (set! (.-innerHTML root) nil)
    (gdom/appendChild root app-dom)))

(defn dumpasc [tag me]
  (cond
    (nil? me) (prn :dasc> tag :topped-out)
    :else (do
            (prn :dasc> tag (:name @me) (minfo me))
            (dumpasc tag (mpar me)))))

(defn multi-demo-toolbar []
  (div {:class :toolbar
        :style {:flex-direction  :column
                :align-items     :start
                :justify-content :start
                }}
    (doall
      (for [{:keys [menu title] :as clk} (mget (fasc :demos me) :demos)]
        (button {:class   :pushbutton
                 :cursor  :finger
                 :style   (cF
                            (when (not (fasc :demos me #_#_:must? true))
                              (dumpasc :no-demo me)
                              (prn :no-demmos!! (minfo me)))
                            (let [curr-clk (mget (fasc :demos me #_#_:must? true) :selected-demo)]
                              {:min-width    "144px"
                               :border-color (if (= clk curr-clk)
                                               "orange" "white")
                               :font-weight  (if (= clk curr-clk)
                                               "bold" "normal")}))
                 :onclick (cF (fn [] (mset! (fasc :demos me :must? true) :selected-demo clk)))}
          {:name :button}
          (or menu title))))))

(defn multi-demo-dashboard [demo-title]
  (div {:style {:display         :flex
                :flex-direction  :column
                :align-items     :center
                ;:background :gray
                :justify-content :start
                :border-right    "4mm ridge orange"         ;; "rgba(211, 220, 50, .6)"
                }}
    (span {:style {:font-size      "24px"
                   :margin-bottom  "1em"
                   ;:background "yellow"
                   :padding-bottom "1em"
                   :text-align     :center}}
      demo-title)
    (multi-demo-toolbar)))

(defn multi-demo [demo-title start-demo-ix & demos]
  (div {} {:name           :demos
           :selected-demo  (cFn (nth (mget me :demos)
                                  (cond
                                    (neg? start-demo-ix) 0
                                    (>= start-demo-ix (count demos)) (dec (count demos))
                                    :else start-demo-ix)))
           :demos          demos
           :show-glossary? (cI false)}

    (div {:style {:display :flex
                  :gap     "2em"}}
      (multi-demo-dashboard demo-title)
      (div {:style {:display        :flex
                    :flex-direction :column
                    :padding        "6px"}}
        {:name :demo
         :demo (cF (mget (fasc :demos me :must? true) :selected-demo))}
        (let [demo (mget me :demo)]
          [(h1 (:title demo))
           ; --- preamble if any
           (when-let [preamble (:preamble demo)]
             (if (string? preamble)
               (p {:class :preamble} preamble)
               (doall (for [elt preamble]
                        (p {:class :preamble} elt)))))
           ; --- the live demo
           (div {:style {:border-color "orange"
                         :border-style "solid"
                         :border-width "2px"}}
             ((:builder demo)))
           ; --- demo code
           (pre {:class :lesson-code}
             (code {:style {:font-size "16px"}}
               (:code demo)))
           ; --- comments
           (when-let [c (:comment demo)]
             (if (string? c)
               (p {:class :preamble} c)
               (doall (for [cx c]
                        (p {:class :preamble} cx)))))])))))
