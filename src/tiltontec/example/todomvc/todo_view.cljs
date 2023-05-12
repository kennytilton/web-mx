(ns tiltontec.example.todomvc.todo-view
  (:require
    [goog.dom :as dom]
    [goog.dom.classlist :as classlist]
    [goog.dom.forms :as form]

    [tiltontec.matrix.api
     :refer [unbound matrix make cF cF+ cF+n cFn cFonce cI cf-freeze
             def-mget mpar mget mset! mswap! mset! with-cc
             kid-values-kids mxu-find-type
             fasc fmu fm! minfo with-par]]

    [tiltontec.mxxhr.core
     :refer [make-xhr xhr-response]]

    [tiltontec.web-mx.api
     :refer [input-editing-start label li div input button span i]]

    [tiltontec.example.todomvc.todo
     :refer [now td-title td-created
             td-completed td-delete!
             td-id td-toggle-completed!]]
    [cljs.pprint :as pp]
    [clojure.string :as str]))


(defn todo-edit [e todo edit-commited?]
  (let [edt-dom (.-target e)
        li-dom (dom/getAncestorByTagNameAndClass edt-dom "li")]

    (when (classlist/contains li-dom "editing")
      (let [title (str/trim (form/getValue edt-dom))
            stop-editing #(classlist/remove li-dom "editing")]
        (cond
          edit-commited?
          (do
            (stop-editing)                                  ;; has to go first cuz a blur event will sneak in
            (if (= title "")
              (td-delete! todo)
              (mset! todo :title title)))

          (= (.-key e) "Escape")
          ;; this could leave the input field with mid-edit garbage, but
          ;; that gets initialized correctly when starting editing
          (stop-editing))))))

(defn todo-list-item [todo]
  ;; the structure below, and importantly its CSS, was authored
  ;; by the developers of the TodoMVC exercise. Nice!

  ;; Nothing new to note here, except
  (li
    {:class (cF (when (td-completed todo)
                  "completed"))}
    {:todo todo}
    (div {:class "view"}
      (input {:class       "toggle"
              ;; namespaced :type is for HTML attribute
              :type "checkbox"
              :checked     (cF
                             ;; completed is not a boolean, it is
                             ;; a timestamp that is nil? until the to-do is completed
                             (not (nil? (td-completed todo))))

              ;; td-toggle-completed! expands to an mset! of the JS epoch or nil
              :onclick     #(td-toggle-completed! todo)})

      (label {:ondblclick #(let [li-dom (dom/getAncestorByTagNameAndClass
                                          (.-target %) "li")
                                 edt-dom (dom/getElementByClass
                                           "edit" li-dom)]
                             (classlist/add li-dom "editing")
                             (input-editing-start edt-dom (td-title todo)))}
        (td-title todo))

      (button {:class   "destroy"
               ;; we actually have a td-delete! to hide the action, but
               ;; this is a tutorial so let's show the action and use mset!.
               ;; btw, yes, we extend here the spec to support logical deletion
               :onclick #(mset! todo :deleted (now))}))

    (input {:class     "edit"
            :onblur    #(todo-edit % todo true)
            :onkeydown #(todo-edit % todo (= (.-key %) "Enter"))})))