(ns tiltontec.example.svg-examples
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [goog.dom :as gdom]
            [goog.object :as gobj]
            [tiltontec.cell.base :refer [unbound] :as cbase]
            [tiltontec.cell.core :refer-macros [cF cF+ cI cFn cFonce] :refer [cI]]
            [tiltontec.model.core
             :refer [cFkids fmu matrix mx-par mget mget mset! mset! mswap! mxi-find mxu-find-name] :as md]
            [tiltontec.web-mx.base :as wbase]
            [tiltontec.web-mx.gen :refer [evt-mx target-value make-svg]]
            [tiltontec.web-mx.gen-macro
             :refer [jso-map]
             :refer-macros [svg g circle p span div text radialGradient defs stop
                            rect ellipse line polyline path polygon script use]]
            [tiltontec.web-mx.html :refer [tag-dom-create]]))

(defn wall-clock []
  (div {:class   "example-clock"
        :style   "color:cyan"
        :content (cF (if (mget me :tick)
                       (-> (js/Date.) .toTimeString (str/split " ") first)
                       "*checks watch*"))}
    {:name   :clock
     :tick   (cI (.getSeconds (js/Date.)))
     :ticker (cF+ [:obs (fn [_ _ nv ov c]
                          (when (not= ov unbound)
                            (js/clearInterval ov)))]
               (wbase/js-interval-register
                 ;; needed during development so hot reload does not pile up intervals
                 (js/setInterval #(mset! me :tick (.getSeconds (js/Date.))) 1000)))}))

(defn three-circles []
  (svg {:viewBox "0 0 300 100"
        :stroke  "red"
        :fill    "grey"}
    (circle {:cx 50 :cy 50 :r 40})
    (circle {:cx 150 :cy 50 :r 10})
    (svg {:viewBox "0 0 10 10"
          :x       200
          :width   100}
      (circle {:cx 5 :cy 5 :r 4}))))

(defn radial-gradient []
  ;; https://developer.mozilla.org/en-US/docs/Web/SVG/Tutorial/Gradients
  (svg {:width   120 :height 240}
    (defs
      (radialGradient {:id :RG1}
        (stop {:offset "0%" :stop-color :red})
        (stop {:offset "100%" :stop-color :blue})))
    (rect {:x    10 :y 10 :rx 15 :ry 15 :width 100 :height 100
           :fill "url(#RG1"})))


(defn basic-shapes []
  ;; https://developer.mozilla.org/en-US/docs/Web/SVG/Tutorial/Basic_Shapes
  (div
    (svg {:width 200 :height 250}
      (rect {:x            10 :y 10 :width 30 :height 30
             :stroke       (cF (if (even? (mget (fmu :clock) :tick)) :red :black))
             :onclick      (cF (fn foo [e]
                                 (prn :on-click-hi-mom e me)))
             :stroke-width 5 :fill :transparent})
      (rect {:x       60 :y 10 :rx 10 :ry 10 :width 30 :height 30
             :stroke  :black :stroke-width 5 :fill (cI :transparent)
             :onclick (cF (fn foo [e]
                            (mset! me :fill :red)))})
      (circle {:cx 25 :cy 75 :r 20 :stroke :red :stroke-width 5 :fill :transparent})
      (ellipse {:cx (cF (let [tick (mget (fmu :clock) :tick)]
                          (+ 75 (* 10 (- tick (* 10 (Math/floor (/ tick 10))))))))
                :cy 75 :rx 20 :ry 5 :stroke :red :stroke-width 5 :fill :transparent})
      (line {:x1 10 :x2 50 :y1 110 :y2 150 :stroke :orange :stroke-width 5})
      (polyline {:points [60 110 65 120 70 115 75 130 80 125 85 140 90 135 95 150 100 145]
                 :stroke :orange :stroke-width 5 :fill :transparent})
      (polygon {:points [50 160 55 180 70 180 60 190 65 205 50 195 35 205 40 190 30 180 45 180]
                :stroke :green :stroke-width 5 :fill :transparent})
      (path {:d    ["M20,230" "Q40,205" "50,230" "T90,230"]
             :fill :none :stroke :blue :stroke-width 5}))))

(defn domx [me]
  (:dom-x (meta me)))

(defn make-svg-test []
  (div
    (make-svg :svg (merge {:height 100} {:viewBox "0 0 40 10"})
      (assoc {:name :includer}
        :include-other? (cI true))
      [(make-svg :circle
         {:id   "myCircle-0" :cx 5 :cy 5 :r 4 :stroke-width 1
          :fill :black :stroke :yellow})
       (make-svg "circle"
         {:id   "myCircle-1" :cx 15 :cy 5 :r 4 :stroke-width 1
          :fill :black :stroke (cF (if (even? (mget me :my-tick)) :orange :yellow))}
         {:my-tick (cF (mget (fmu :clock) :tick))})
       (make-svg ":circle"
         {:id   "myCircle-2" :cx 25 :cy 5 :r 4 :stroke-width 1
          :fill :black :stroke :blue})
       (make-svg :circle)])))

(defn use-blue []
  ;; https://developer.mozilla.org/en-US/docs/Web/SVG/Element/use
  ;;
  ;; Interesting if we watch the console: click either clone and the listeners for
  ;; both the original and clone fire. A feature, I think. Differentiate in original by
  ;; checking if event target is its dom.
  ;;
  (div
    (p "Try clicking and shift-clicking each circle. Re-load page to reset; undo is undone.")
    (svg {:viewBox "0 0 40 10"}
      {:include-other? (cI true)}
      (circle {:id           "myCircle" :cx 5 :cy 5
               :r            (cI 4)
               :stroke-width (cI 1)
               :fill         (cI :black)
               :onclick      (cF (fn [evt]
                                   (let [e (walk/keywordize-keys
                                             (jso-map evt))]
                                     ;; check that it was not a "use" clone that got clicked.
                                     (when (= (domx me) (:target e))
                                       (prn :onclick-circle-original)
                                       (if (.-shiftKey evt)
                                         (mset! me :stroke-width 1.5)
                                         (mset! me :fill :orange))))))
               :stroke       (cF (let [tick (mget (fmu :clock) :tick)]
                                   (if (even? tick) :red :blue)))}
        {:name :used-circle})
      (use {:id      "use-2"
            :href    "#myCircle" :x 10 :fill :blue
            :onclick (cF (fn [evt]
                           (if (.-shiftKey evt)
                             (mset! (fmu :used-circle) :fill :transparent)
                             (mset! (fmu :used-circle) :stroke-width 2))))}
        {:name :user-2})
      (use {:id      "use-3"
            :href    "#myCircle" :x 20 :fill :white
            :onclick (cF (fn [evt]
                           (if (.-shiftKey evt)
                             (mset! (fmu :used-circle) :r 2)
                             (mset! (fmu :used-circle) :stroke-width 0.2))))
            ;; we demonstrate next that most attribute overrides are ignored by USE
            :stroke  :red}
        {:name :user-3})
      (when (mget me :include-other?)
        (circle {:id           "myOtherCircle" :cx 35 :cy 5 :r 2
                 :stroke-width (cI 1)
                 :fill         (cI :cyan)
                 :onclick      (cF (fn [evt]
                                     (if (.-shiftKey evt)
                                       (mset! (mx-par me) :include-other? false)
                                       (mset! me :fill :yellow))))
                 :stroke       (cF (if (even? (mget (fmu :clock) :tick)) :green :brown))}
          {:name :used-circle})))))

(defn dyno-kids []
  (div
    (make-svg :svg {:version "1.1" ;; hhack added version
                    :height 100
                    :viewBox "0 0 40 10"}
      {:name :includer
      :include-other? (cI false)}
      (cFkids
        (circle {:id      "myCircle" :cx 5 :cy 5 :r 4 :stroke-width 1
                 :fill    (cI :black)
                 :stroke  (cF (if (mget (mx-par me) :include-other?) :orange :yellow))
                 :onclick (cF (fn [evt]
                                (if (not (.-shiftKey evt))
                                  (mswap! (mx-par me) :include-other? not)
                                  (mset! me :fill :yellow))))})
        (g {:id "the-G"}
          (circle {:id     "fixedCircle" :cx 15 :cy 5 :r 2 :stroke-width 2 :fill :cyan
                   :stroke :red})
          (let [inker (fmu :includer)]
            (when (mget inker :include-other?)
              ;; (prn :making-optcircle!!!!!!)
              (circle {:id     "optCircle" :cx 25 :cy 5 :r 2 :stroke-width 2 :fill :cyan
                       :stroke :green}))))))))

(defn matrix-build! []
  (reset! matrix
    (md/make
      :mx-dom (cFonce (md/with-par me
                        (div
                          (wall-clock)
                          (div {:style {:background-color "cyan"}}
                            (span (str "Hi, Mom! " (rand-int 9999)))
                            ;(make-svg-test)
                            #_(three-circles)
                            #_(radial-gradient)
                            ;(basic-shapes)
                            ;(use-blue)
                            (dyno-kids))))))))