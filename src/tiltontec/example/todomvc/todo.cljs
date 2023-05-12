(ns tiltontec.example.todomvc.todo
  (:require
    [tiltontec.cell.poly :refer [watch watch-by-type]]
    [tiltontec.matrix.api
     :refer [unbound matrix make cF cF+ cFn cFonce cI cf-freeze
             def-mget mpar mget mset! mswap! mset! with-cc
             fasc fmu fm! minfo with-par]]
    [tiltontec.web-mx.api :refer [map-to-json json-to-map]]
    [tiltontec.web-mx.html :refer [io-upsert io-read io-find io-truncate]]))

;;; --- util --------------------------------

(defn now []
  (.getTime (js/Date.)))

(defn uuidv4 []
  (letfn [(hex [] (.toString (rand-int 16) 16))]
    (let [rhex (.toString (bit-or 0x8 (bit-and 0x3 (rand-int 16))) 16)]
      (uuid
        (str (hex) (hex) (hex) (hex)
          (hex) (hex) (hex) (hex) "-"
          (hex) (hex) (hex) (hex) "-"
          "4" (hex) (hex) (hex) "-"
          rhex (hex) (hex) (hex) "-"
          (hex) (hex) (hex) (hex)
          (hex) (hex) (hex) (hex)
          (hex) (hex) (hex) (hex))))))

(def TODO_LS_PREFIX "todos-matrixcljs.")

(declare td-upsert td-deleted td-completed make-todo load-all)

(defn todo-list []
  (make ::todo-list
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
                    {:mx-type      ::todo
                     :id        (str TODO_LS_PREFIX (uuidv4))
                     :created   (now)
                     ;; now wrap mutable slots as Cells...
                     :title     (cI (:title islots))
                     :completed (cI nil)
                     :due-by    (cI (+ (now) (* 4 24 60 60 1000)))
                     :deleted   (cI nil)})]
    (td-upsert
      (apply make (flatten (into [] net-slots))))))

;;; --------------------------------------------------------
;;; --- handy accessors to hide mget etc ------------------
;;; look for a macro RSN to auto-generate these

(def-mget td- id title due-by created completed deleted)

;;; ---------------------------------------------
;;; --- dataflow triggering setters to hide mset!

(defn td-delete! [td]
  (mset! td :deleted (now)))

(defn td-toggle-completed! [td]
  (mswap! td :completed
    #(when-not % (now))))

;;; --------------------------------------------------------------
;;; --- persistence, part II -------------------------------------
;;; An watch updates individual todos in localStorage, including
;;; the 'deleted' property. If we wanted to delete physically, we could
;;; keep the 'deleted' property on in-memory todos and handle the physical deletion
;;; in this same watch when we see the 'deleted' property go truthy.

(defmethod watch-by-type [::todo] [slot me new-val old-val c]
  ;; localStorage does not update columns, so regardless of which
  ;; slot changed we update the entire instance.

  ;; unbound as the prior value means this is the initial watch fired off
  ;; on instance initialization (to get them into the game, if you will), so skip upsert
  ;; since we store explicitly after making a new to-do. Yes, this is premature optimization.

  (when-not (= old-val unbound)
    (td-upsert me)))

;;; --- loading from localStorage ----------------

(defn- remake-todo [{:keys [title completed due-by deleted] :as islots}]
  (apply make
    (flatten
      (into []
        (merge islots
          {:mx-type      ::todo
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



