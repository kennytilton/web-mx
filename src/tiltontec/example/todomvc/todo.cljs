(ns tiltontec.example.todomvc.todo
  (:require
    [tiltontec.util.core :as util :refer [pln now map-to-json json-to-map uuidv4]]
    [tiltontec.cell.base :refer [unbound] :as cbase]
    [tiltontec.cell.core
     :refer-macros [cF cFn] :refer [cI]]
    [tiltontec.cell.observer :refer [observe-by-type]]
    [tiltontec.model.core :as md :refer [make mget mset! mswap! def-mget]]
    [tiltontec.web-mx.html :refer [io-upsert io-read io-find io-truncate]]))

(def TODO_LS_PREFIX "todos-matrixcljs.")

(declare td-upsert td-deleted td-completed make-todo load-all)

(defn todo-list []
  (md/make ::todo-list
    ;; the bulk of the to-do app does not care about deleted to-dos,
    ;; so we use a clumsy name "items-raw" for the true list of items
    ;; ever created, and save "items" for the ones actually used.
    ;;
    ;; we will skip peristence for a while and play a while with
    ;; to-dos (a) in memory (b) created only at start-up.
    :items-raw (cFn (load-all)) #_ (cFn (for [td seed-todos]
                      (make-todo td)))
    ;; doall is needed so formula actually runs when asked, necessary
    ;; so read of any dependencies happens while dependent property
    ;; is bound as necessary. Look for this to get baked into the
    ;; Matrix internals.
    ;;
    :items (cF (doall (remove td-deleted (mget me :items-raw))))
    :items-completed (cF (doall (filter td-completed (mget me :items))))
    :items-active (cF (doall (remove td-completed (mget me :items))))

    :empty? (cF (empty? (mget me :items)))))

(defn make-todo
  "Make a matrix incarnation of a todo on initial entry"
  [islots]
  ;; we go further than the spec requires:
  ;;  we key off a UUID for when we get to persistence;
  ;;  record a fixed creation time;
  ;;  use a timestamp to denote "completed"; and
  ;;  use a timestamp to denote "due-by" (for future development); and
  ;;  use another timestamp for logical deletion.
  ;;
  (let [net-slots (merge
                    {:type      ::todo
                     :id        (str TODO_LS_PREFIX (uuidv4))
                     :created   (util/now)
                     ;; now wrap mutable slots as Cells...
                     :title     (cI (:title islots))
                     :completed (cI nil)
                     :due-by    (cI (+ (now) (* 4 24 60 60 1000)))
                     :deleted   (cI nil)})]
    (td-upsert
      (apply md/make (flatten (into [] net-slots))))))


;;; --------------------------------------------------------
;;; --- handy accessors to hide mget etc ------------------
;;; look for a macro RSN to auto-generate these

(def-mget td- id title due-by created completed deleted)

;;; ---------------------------------------------
;;; --- dataflow triggering setters to hide mset!

(defn td-delete! [td]
  (mset! td :deleted (util/now)))

(defn td-toggle-completed! [td]
  (mswap! td :completed
    #(when-not % (util/now))))

;;; --------------------------------------------------------------
;;; --- persistence, part II -------------------------------------
;;; An observer updates individual todos in localStorage, including
;;; the 'deleted' property. If we wanted to delete physically, we could
;;; keep the 'deleted' property on in-memory todos and handle the physical deletion
;;; in this same observer when we see the 'deleted' property go truthy.

(defmethod observe-by-type [::todo] [slot me new-val old-val c]
  ;; localStorage does not update columns, so regardless of which
  ;; slot changed we update the entire instance.

  ;; unbound as the prior value means this is the initial observation fired off
  ;; on instance initialization (to get them into the game, if you will), so skip upsert
  ;; since we store explicitly after making a new to-do. Yes, this is premature optimization.

  (when-not (= old-val unbound)
    (td-upsert me)))

;;; --- loading from localStorage ----------------

(defn- remake-todo [{:keys [title completed due-by deleted] :as islots}]
  (apply md/make
    (flatten
      (into []
        (merge islots
          {:type      ::todo
           ;; next, we wrap in cells those reloaded slots we might
           ;; mutate (not id or created)...
           :title     (cI title)
           :completed (cI completed)
           :due-by    (cI due-by) ;; an enhancement over the official spec
           :deleted   (or deleted ;; no change allowed once deleted
                        (cI nil))})))))

(defn- load-all []
  (let [keys (io-find TODO_LS_PREFIX)]
    (map (fn [td-id]
           (remake-todo
             (json-to-map
               (.parse js/JSON (io-read td-id)))))
      (io-find TODO_LS_PREFIX))))

;;; ---- updating in localStorage ----------------------

(defn- td-to-json [todo]
  (map-to-json (into {} (for [k [:id :created :title :completed :deleted :due-by]]
                          [k (mget todo k)]))))

(defn- td-upsert [td]
  (io-upsert (:id @td)
    (.stringify js/JSON
      (td-to-json td)))
  td)



