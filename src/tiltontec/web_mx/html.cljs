(ns tiltontec.web-mx.html
  (:require
    [clojure.string :as str]
    [clojure.walk :refer [stringify-keys]]
    [clojure.set :as set]
    [cljs.pprint :as pp]
    [tiltontec.util.core
     :refer [any-ref? rmap-setf err rmap-meta-setf set-ify pln]]
    [tiltontec.util.base :refer [mx-type]]
    [tiltontec.cell.base :refer [md-ref?  unbound minfo]]
    [tiltontec.cell.observer :refer [observe observe-by-type]]
    [tiltontec.cell.evaluate :refer [md-quiesce md-quiesce-self]]
    [tiltontec.model.core
     :refer-macros [the-kids mdv!]
     :refer [fm-navig mget mget? fasc fm! make mset! backdoor-reset!]
     :as md]

    [tiltontec.web-mx.base :refer [kw$ attr-val$ tag-dom *web-mx-trace*]]
    [tiltontec.web-mx.style
     :refer [style-string] :as tagcss]

    [goog.dom :as dom]
    [goog.dom.classlist :as classlist]
    [goog.html.sanitizer.HtmlSanitizer :as sanitizer]
    [goog.editor.focus :as focus]
    [goog.dom.selection :as selection]
    [goog.dom.forms :as form]))

(defn tagfo [me]
  (if (string? me)
    "string"
    (select-keys @me [:id :tag :class :name])))

(defn dom-has-class [dom class]
  (classlist/contains dom class))

(defn dom-ancestor-by-class [dom class]
  (dom/getAncestorByTagNameAndClass dom nil class))

(defn dom-ancestor-by-tag [dom tag]
  (dom/getAncestorByTagNameAndClass dom tag))

(declare tag-dom-create)

(defn map-less-nils [m]
  (apply dissoc m
    (for [[k v] m :when (nil? v)] k)))

(defn class-to-class-string [c]
  (if (coll? c)
    (str/join " " (map kw$ c))
    (kw$ c)))

;; goog.dom.setProperties = function(element, properties) {
;  goog.object.forEach(properties, function(val, key) {
;    if (val && val.implementsGoogStringTypedString) {
;      val = val.getTypedStringValue();
;    }
;    if (key == 'style') {
;      element.style.cssText = val;
;    } else if (key == 'class') {
;      element.className = val;
;    } else if (key == 'for') {
;      element.htmlFor = val;
;    } else if (goog.dom.DIRECT_ATTRIBUTE_MAP_.hasOwnProperty(key)) {
;      element.setAttribute(goog.dom.DIRECT_ATTRIBUTE_MAP_[key], val);
;    } else if (
;        goog.string.startsWith(key, 'aria-') ||
;        goog.string.startsWith(key, 'data-')) {
;      element.setAttribute(key, val);
;    } else {
;      element[key] = val;
;    }
;  });
;};

(defn tag-properties [mx]
  (let [beef (remove nil?
               (for [k (:attr-keys @mx)]
                 (when-let [v (when-not (some #{k} [:list])
                                ;; :list gets set via setAttribute; cannot be set as property
                                (when-not (contains? @mx k)
                                  (prn :so-sign-of-attr-key k :in @mx)
                                  (assert (contains? @mx k)))
                                (mget mx k))]
                   [(kw$ k) (case k
                              :style (tagcss/style-string v)
                              :class (class-to-class-string v)
                              (kw$ v))])))]
    (apply js-obj
      (apply concat beef))))

(defn svg-dom-create [me dbg]
  (cond
    ; var textNode = document.createTextNode("milind morey");
    ;
    (string? me) (.createTextNode js/document me)
    :else (let [svg (.createElementNS js/document "http://www.w3.org/2000/svg"
                      (mget me :tag))]
            ;; todo sort these next two out
            (rmap-meta-setf [:dom-x me] svg)
            (rmap-meta-setf [:svg-x me] svg)
            (.setAttributeNS svg
              ;; hhack - should this just be "xmlns"?
              "http://www.w3.org/2000/xmlns/"
              "xmlns:xlink"
              "http://www.w3.org/1999/xlink")
            (doseq [ak (:attr-keys @me)
                    :let [ak$ (name ak)
                          av (ak @me)]]
              (if (fn? av)
                (.addEventListener svg
                  (if (= "on" (subs ak$ 0 2))
                    (subs ak$ 2) ak$)
                  ;; todo add warning
                  av)
                (.setAttributeNS svg nil ak$ (attr-val$ av))))
            (doseq [kid (mget? me :kids)]
              (.appendChild svg (svg-dom-create kid dbg)))
            svg)))

(defn tag-dom-create
  ([me] (tag-dom-create me false))
  ([me dbg]
   (cond
     (string? me) (dom/safeHtmlToNode (sanitizer/sanitize me))
     (coll? me) (let [frag (.createDocumentFragment js/document)]
                  (doseq [tag me]
                    (when tag                               ;; tolerate nils
                      (dom/appendChild frag (tag-dom-create tag))))
                  frag)
     (= "svg" (mget me :tag)) (svg-dom-create me dbg)
     :default
     (do
       ;(pln :tagdomcre (mget me :tag) :attrs (:attr-keys @me))
       (let [dom (apply dom/createDom (mget me :tag)
                   (tag-properties me)
                   (concat
                     (map #(tag-dom-create % dbg) (mget? me :kids))
                     (when-let [c (mget? me :content)]
                       [(tag-dom-create c)])))]
         (rmap-meta-setf [:dom-x me] dom)
         (when (some #{:list} (:attr-keys @me))
           ;; if offered as property to createDom we get:
           ;; Cannot set property "list" of #<HTMLInputElement> which has only a getter
           ;; which is misleading: we /can/ set the attribute.
           (when-let [list-id (mget? me :list)]
             (.setAttribute dom "list" (attr-val$ list-id))))
         (doseq [attr-key (:attr-keys @me)]
           (when (str/includes? (name attr-key) "-")
             (when-let [attr-val (mget me attr-key)]
               ;; (prn :setting-attr (name attr-key) (attr-val$ attr-val))
               (.setAttribute dom (name attr-key) (attr-val$ attr-val)))))
         dom)))))

(defn tag [me]
  (mget? me :tag))

(defmethod observe [:kids :web-mx.base/tag] [_ me newv oldv _]
  (when (not= oldv unbound)
    ;; oldv unbound means initial build and this incremental add/remove
    ;; is needed only when kids change post initial creation
    #_(println :obstagkids!!!!! (tagfo me)
        :counts-new-old (count newv) (count oldv)
        :same-kids (= oldv newv)
        :same-kid-set (= (set newv) (set oldv)))
    (do                                                     ;; p ::observe-kids
      (let [pdom (tag-dom me)
            lost (clojure.set/difference (set oldv) (set newv))
            gained (clojure.set/difference (set newv) (set oldv))]
        ;(prn :kids-lost (count lost))
        ;(prn :kids-gained (count gained))
        (cond
          (and (= (set newv) (set oldv))
            (not (= oldv newv)))
          ;; simply reordered children
          (let [frag (.createDocumentFragment js/document)]
            (doseq [newk newv]
              (dom/appendChild frag
                (.removeChild pdom (tag-dom newk))))
            ;; should not be necessary...
            ;;(prn :reorder-rmechild pdom (dom/getFirstElementChild pdom))
            (dom/removeChildren pdom)
            (dom/appendChild pdom frag))

          (empty? gained)
          ;; just lose the lost
          (do
            (doseq [oldk lost]
              (.removeChild pdom (tag-dom oldk))
              (when-not (string? oldk)
                ;; (println :obs-tag-kids-dropping (tagfo oldk))
                (try
                  (md-quiesce oldk)
                  (catch js/Error e
                    (println "An md-quiesce-error occurred:" e)
                    false))
                )))

          :default (let [frag (.createDocumentFragment js/document)]
                     (doseq [oldk lost]
                       (when-not (string? oldk)
                         ;; no need to remove dom, all children replaced below.
                         (md-quiesce oldk)))
                     (doseq [newk newv]
                       (dom/appendChild frag
                         (if (some #{newk} oldv)
                           (.removeChild pdom (tag-dom newk))
                           (tag-dom-create newk))))
                     (dom/removeChildren pdom)
                     (dom/appendChild pdom frag)))))))

(defn svg-dom [me]
  (:dom-x (meta me)))

(defmethod observe [:kids :web-mx.base/svg] [_ me newv oldv _]
  (when (not= oldv unbound)
    ;; (prn :svkids-change!!!!!! (count newv) (count oldv))
    (let [pdom (svg-dom me)
          lost (set/difference (set oldv) (set newv))
          gained (set/difference (set newv) (set oldv))
          kept (set/intersection (set newv) (set oldv))]
      (assert pdom)
      #_(do (prn :kept!!! kept)
            (prn :gained!!!!! gained)
            (prn :lost!!!!!!! lost))
      (cond
        (and (= (set newv) (set oldv))
          (not (= oldv newv)))
        ;; simply reordered children
        (let [frag (.createDocumentFragment js/document)]
          ;; (prn :svg-reorder!!)
          (doseq [newk newv]
            (dom/appendChild frag
              (.removeChild pdom (svg-dom newk))))
          ;; should not be necessary...
          ;; (dom/removeChildren pdom)
          (dom/appendChild pdom frag))

        (empty? gained)
        ;; just lose the lost
        (do                                                 ;; (prn :no-gained-losing-lost (count lost))
          (doseq [oldk lost]
            (.removeChild pdom (svg-dom oldk))
            (when-not (string? oldk)
              ; (println :obs-tag-kids-dropping (tagfo oldk))
              (md-quiesce oldk))))

        (empty? lost)
        (do                                                 ;; (prn :no-lost-adding-gained!!! (count gained))
          (doseq [newk gained]
            (let [new-dom (or (svg-dom newk)
                            (svg-dom-create newk false))]
              (.appendChild pdom new-dom))))


        :default (let [frag (.createDocumentFragment js/document)]
                   #_(do (prn :mix-kept!!! kept)
                         (prn :mix-gained!!!!! gained)
                         (prn :mix-lost!!!!!!! lost))
                   ;; GC lost from matrix;
                   ;; move retained kids from pdom into fragment,
                   ;; add all new kids to fragment, and do so preserving
                   ;; order dictated by newk:
                   (doseq [oldk lost]
                     (when-not (string? oldk)
                       ;; no need to remove dom, all children replaced below.
                       ;;(prn :md-quiesce!!!!! oldk)
                       (md-quiesce oldk)))

                   (doseq [newk newv]
                     ;;(prn :adding-newk newk)
                     (let [new-dom (if (some #{newk} oldv)
                                     (.removeChild pdom (svg-dom newk))
                                     (do
                                       ;; (println :obs-tag-kids-building-new-dom (tagfo newk))
                                       (svg-dom-create newk false)))]
                       (dom/appendChild frag new-dom)))

                   (do
                     ;; (prn :BAM-remove-append)
                     (dom/removeChildren pdom)
                     (dom/appendChild pdom frag))

                   )))))

(def +inline-css+ (set [:display]))

(defn mixo [me]
  (cond
    (nil? me) :NIL-MD
    (not (any-ref? me)) :NOT-ANY-REF
    (not (md-ref? me)) :NOT-MD
    :else [(or (:name @me) :anon)
           (:state (meta me))]))

(defmethod observe-by-type [:web-mx.base/tag] [slot me newv oldv _]
  (when (not= oldv unbound)
    (when-let [dom (tag-dom me)]
      #_(when *web-mx-trace*
          (when-not (some #{slot} [:tick])
            (pln :observing-tagtype (tagfo me) slot newv oldv)))

      (cond
        (= slot :content)
        (do
          ; (pln :setting-html-content slot newv oldv  (mixo me))
          ;;(set! (.-innerHTML dom) newv) #_
          (.requestAnimationFrame js/window
            #(do                                            ;;(prn :ani-frame! newv)
               (set! (.-innerHTML dom) newv))))

        (some #{slot} (:attr-keys @me))
        (do
          ;;(pln :dom-hit-attr!!!! (tagfo me) slot newv oldv)
          (case slot
            :style (do
                     ;;(prn :obs-style (style-string newv))
                     (set! (.-style dom) (style-string newv)))

            :hidden (set! (.-hidden dom) newv)
            :disabled (if newv
                        (.setAttribute dom "disabled" true)
                        (.removeAttribute dom "disabled"))
            :class (classlist/set dom (class-to-class-string newv))
            :checked (set! (.-checked dom) newv)
            :value (.setAttribute dom "value" (str newv))   ;; eg, progress indicator
            ;; todo cleanup
            :stroke (do (prn :ignore-stroke newv))
            (do
              ;; (pln :obs-by-type-setAttr-onknown (name slot) (minfo me) newv)
              (.setAttribute dom (name slot) newv))))

        #_#_(+inline-css+ slot)
                (throw (js/Error. (str "tiltontec.web-mx obs sees oldskool style: " slot)))))))

(defmethod observe-by-type [:web-mx.base/svg] [slot me newv oldv _]
  (when (not= oldv unbound)
    (cond
      (some #{slot} (:attr-keys @me))
      (if-let [svg (:dom-x (meta me))]
        (.setAttributeNS svg nil (name slot)
          (attr-val$ newv))
        (do #_(prn :no-svg-but (keys (meta me)) me)))
      :else (do #_(prn :ignoring-svg-prop-change slot)))))

;;; --- local storage ------------------------

(defn mxu-find-class
  "Search up the matrix from node 'where' looking for element with class"
  [where class]
  ;; todo is this too expensive? will there be much usage of this?
  (fm-navig #(when-let [its-class (mget? % :class)]
           (str/includes? (or (class-to-class-string its-class) "makebetter") (kw$ class)))
    where :me? false :up? true))

(defn mxu-find-tag
  "Search up the matrix from node 'where' looking for element of a certain tag"
  [where tag]
  (let [n (name tag)]
    (fm-navig #(= n (mget? % :tag))
      where :me? false :up? true)))

(defn mxu-find-id
  "Search up the matrix from node 'where' looking for element with a certain :id"
  [where id]
  (fm-navig #(= (name id) (mget? % :id))
    where :me? false :up? true))

;;; --- localStorage io implementation --------------------------------

(defn io-upsert [key val]
  (.setItem (.-localStorage js/window) key val))

(defn io-read [key]
  (.getItem (.-localStorage js/window) key))

(defn io-delete [key]
  (.removeItem (.-localStorage js/window) key))

(defn io-clear-storage []
  (.clear (.-localStorage js/window)))

(defn io-all-keys []
  (.keys js/Object (.-localStorage js/window)))

(defn io-find [key-prefix]
  (loop [keys (io-all-keys)
         found []]
    (if (seq keys)
      (recur (rest keys)
        (if (str/starts-with? (first keys) key-prefix)
          (conj found (first keys))
          found))
      found)))

(defn io-truncate [key-prefix]
  (doseq [key (io-find key-prefix)]
    (prn :io-trunc-nails key)
    (io-delete key)))

;;; ---- tiltontec.web-mx-specific utilities ----------------------------------------------

(defn input-editing-start [dom initial-value]
  (form/setValue dom initial-value)
  (focus/focusInputField dom)
  ;; a lost bit of sound U/X: select all text when starting edit of a populated field...
  (selection/setStart dom 0)
  (selection/setEnd dom (count initial-value)))