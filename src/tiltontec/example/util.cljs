(ns tiltontec.example.util
  (:require [goog.dom :as gdom]
            [tiltontec.cell.core :refer [cF cF+ cFn cFonce cI cf-freeze]]
            [tiltontec.cell.integrity :refer [with-cc]]
            [tiltontec.model.core
             :refer [mx-par mget mset! mswap! mset! mxi-find mxu-find-name fasc fmu fm!] :as md]
            [tiltontec.web-mx.gen :refer [evt-md target-value]]
            [tiltontec.web-mx.gen-macro
             :refer [img section h1 h2 h3 input footer p a
                     span i label ul li div button br pre code]]
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
(defn multi-demo [start-demo-ix & demos]
  (div {} {:name          :demos
           :selected-demo (cFn (nth (mget me :demos)
                                 (cond
                                   (neg? start-demo-ix) 0
                                   (>= start-demo-ix (count demos)) (dec (count demos))
                                   :else start-demo-ix)))
           :demos         demos}
    (div {:class :toolbar}
      ;;(span "Pick one:")
      (doall (for [{:keys [title] :as clk} (mget (mx-par me) :demos)]
               (button {:class   :pushbutton
                        :cursor  :finger
                        :style   (cF (let [curr-clk (mget (fasc :demos me) :selected-demo)]
                                       {:border-color (if (= clk curr-clk)
                                                        "orange" "white")
                                        :font-weight  (if (= clk curr-clk)
                                                        "bold" "normal")}))
                        :onclick (cF (fn [] (mset! (fmu :demos) :selected-demo clk)))}
                 title))))
    (when-let [clk (mget me :selected-demo)]
      (div {:style {:display        :flex
                    :flex-direction :column-reverse
                    :gap            "1em"
                    :padding "36px"
                    }}
        (when-let [c (:comment clk)]
          (p {:class :preamble} c))
        (pre {:style {:margin-left "96px"}}
          (code (:code clk)))
        (div {:style {:border-color "orange"
                      :border-style "solid"
                      :border-width "2px"}}
          ((:builder clk)))
        (p {:class :preamble}
          (:preamble clk "No preamble."))))))

;(exu/main #(md/make ::intro
;             :mx-dom (multi-demo {:title "Manual Clock" :builder manual-clock :code manual-clock-code}
;;                            {:title "Running Clock" :builder running-clock :code running-clock-code})))

