(ns ^:figwheel-hooks tiltontec.example.todomvc.core
  (:require
    [goog.dom :as dom]
    [taoensso.tufte :as tufte]

    [tiltontec.matrix.api
     :refer [matrix make cF cF+ cFn cFonce cI cf-freeze
             mpar mget mset! mswap! mset! with-cc
             fasc fmu fm! minfo with-par]]
    [tiltontec.web-mx.api
     :refer [tag-dom-create div section header h1 footer p ul
                    li span input label]]
    [tiltontec.example.todomvc.todo
     :refer [make-todo td-title td-completed] :as todo]
    [tiltontec.example.todomvc.component :as webco]
    [tiltontec.example.todomvc.todo-items-views
     :refer [mx-todo-items
             todo-items-list
             todo-items-dashboard]]
    [bide.core :as r]
    [clojure.string :as str]
    [goog.dom.forms :as form]))

(enable-console-print!)
(tufte/add-basic-println-handler! {})

(def mxtodo-credits
  ["Double-click a to-do list item to edit it."
   "Inspired by <a href=\"https://github.com/tastejs/todomvc/blob/master/app-spec.md\">TodoMVC</a>."
   "Created by <a href=\"https://github.com/kennytilton\">Kenneth Tilton</a> ."
   ;; todo why is ^^^ link not clickable without the next credit?
   "Copyright &copy; 2023 by Kenny Tilton"])

(defn todo-entry-field []
  (input {:class       "new-todo"
          :autofocus   true
          :placeholder "What needs doing?"
          :onkeypress  #(when (= (.-key %) "Enter")
                          (let [raw (form/getValue (.-target %))
                                title (str/trim raw)]
                            (when-not (str/blank? title)
                              (mswap! (mget @matrix :todos) :items-raw conj
                                (make-todo {:title title})))
                            (form/setValue (.-target %) "")))}))

(defn matrix-build! []
  (reset! matrix
    (make :todoApp
      ;; ^^^ we provide an optional "type" to support Matrix node space search
      ;;
      ;; HTML tag syntax is (<tag> [dom-attribute-map [custom-property map] children*]
      ;;
      :route (cI "All")
      :router-starter #(r/start! (r/router [["/" :All]
                                          ["/active" :Active]
                                          ["/completed" :Completed]])
                       {:default     :ignore
                        :on-navigate (fn [route params query]
                                       (when-let [mtx @matrix]
                                         (mset! mtx :route (name route))))})
      :todos (todo/todo-list)
      :mx-dom (cFonce
                (with-par me
                  (section {:class "todoapp" :style "padding:24px"}
                    ;(webco/wall-clock :date 60000 0 15)
                    ;(webco/wall-clock :time 1000 0 8)
                    (header {:class "header"}
                      (h1 "todos")
                      (todo-entry-field))
                    (todo-items-list)
                    (todo-items-dashboard)
                    (webco/app-credits mxtodo-credits)))))))

(let [root (dom/getElement "app")
      app-matrix (matrix-build!)]
  (set! (.-innerHTML root) nil)
  (dom/appendChild root
    (tag-dom-create
      (mget app-matrix :mx-dom)))

  (when-let [router-starter (mget app-matrix :router-starter)]
    (router-starter)))