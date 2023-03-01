(ns tiltontec.example.util
  (:require [goog.dom :as gdom]
            [tiltontec.cell.core :refer [cF cF+ cFn cFonce cI cf-freeze]]
            [tiltontec.cell.integrity :refer [with-cc]]
            [tiltontec.model.core
             :refer [mx-par mget mset! mswap! mset! mxi-find mxu-find-name fasc fmu fm!] :as md]
            [tiltontec.web-mx.gen :refer [evt-md target-value]]
            [tiltontec.web-mx.gen-macro
             :refer [title img section h1 h2 h3 input footer p a b h4 u
                     blockquote span i label ul li div button br pre code]]
            [tiltontec.web-mx.style :refer [make-css-inline]]
            [tiltontec.web-mx.html :refer [tag-dom-create]]))

(defn main [mx-builder]
  (println "[main]: loading")
  (let [root (gdom/getElement "app")
        ;; ^^^ "app" must be ID of DIV defined in index.html
        app-matrix (mx-builder)
        app-dom (tag-dom-create
                  (mget app-matrix :mx-dom))]
    (set! (.-innerHTML root) nil)
    (gdom/appendChild root app-dom)))


;;; --- sample input ---------------------
; [{:title "Manual Clock" :builder manual-clock :code manual-clock-code}
;                            {:title "Running Clock" :builder running-clock :code running-clock-code}]

(defn multi-demo-toolbar []
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

(defn multi-demo [demo-title start-demo-ix & demos]
  (div {} {:name          :demos
           :selected-demo (cFn (nth (mget me :demos)
                                 (cond
                                   (neg? start-demo-ix) 0
                                   (>= start-demo-ix (count demos)) (dec (count demos))
                                   :else start-demo-ix)))
           :demos         demos}

    (div {:style {:display :flex
                  :gap     "2em"}}
      (div {:style {:display         :flex
                    :flex-direction  :column
                    :align-items     :center
                    ;:background :gray
                    :justify-content :start
                    :border-right    "4mm ridge orange"     ;; "rgba(211, 220, 50, .6)"
                    }}
        (span {:style {:font-size      "24px"
                       :margin-bottom  "1em"
                       ;:background "yellow"
                       :padding-bottom "1em"
                       :text-align     :center}}
          demo-title)

        (multi-demo-toolbar))

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

;(exu/main #(md/make ::intro
;             :mx-dom (multi-demo {:title "Manual Clock" :builder manual-clock :code manual-clock-code}
;;                            {:title "Running Clock" :builder running-clock :code running-clock-code})))

