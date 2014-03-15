(ns ameba.datomic
  (:use [datomic.api :only [db q] :as d]
        [clojure.walk :as walk ]))


;; lifted from
;; https://github.com/Datomic/day-of-datomic/blob/master/src/datomic/samples/query.clj

(defprotocol Eid
  (e [_])
  (entity-id [_])
  (entity [db _]))

(extend-protocol Eid
  java.lang.Long
  (e [n] n)
  (entity-id [n] n)
  (entity [db n] (d/entity db n))

  datomic.Entity
  (e [ent] (:db/id ent))
  (entity-id [ent] (:db/id ent))
  (entity [db ent] ent)

  nil
  (e [_] nil)
  (entity-id [_] nil)
  (entity [db ent] nil))

;; lifted from https://gist.github.com/3150938





;; (def ^{:dynamic true :doc "A Datomic connection bound for the life of a Ring request."} *connection*)

;; (defn init-connection []
;;   (def ^:dynamic *connection* (d/connect datomic-uri)))

;; (defn transact
;;   "Run a transaction with a request-consistent connection."
;;   [tx]
;;   (d/transact *connection* tx))

;; (defn q
;;   "Runs the given query over a request-consistent database as well as
;;   the other given sources."
;;   [query & sources]
;;   (apply d/q query (d/db *connection* sources))

;; (defn wrap-datomic
;;   "A Ring middleware that provides a request-consistent database connection and
;;   value for the life of a request."
;;   [handler uri]
;;   (fn [request]
;;     (binding [*connection* (d/connect uri)]
;;       (handler request))))


(defn- entity?
  [entity]
  (boolean (:db/id entity)))

(defn- entity-collection?
  [coll]
  (and
   (coll? coll)
   (not (map? coll))
   (every? :db/id coll)))

(defn- children
  [entity]
  (filter entity? (vals entity)))

(defn- unpack-ids
  [entity]
  (into {}
        (map (fn [[k v]]
               [k (cond
                   (entity? v)            (:db/id v)
                   (entity-collection? v) (map :db/id v)
                   :default               v)])
             entity)))

(defn map->tx
  "Flattens a tree-like structure of nested maps into a Datomic
  transaction"
  [entity]
  (->> entity
       (tree-seq #(or (entity? %)
                      (entity-collection? %))
                 #(cond
                   (entity? %) (children %)
                   (entity-collection? %) (map children %)
                   :default nil))
       reverse
       (map unpack-ids)
       (into [])))

(defn entity->map
  "Recursively converts entities to tree-like structures of nested
  maps."
  [entity]
  (walk/prewalk
   (fn [e]
     (if (:db/id e)
       (select-keys e
                    (conj (keys e)
                          :db/id))
       e))
   entity))





(defn id-to-entity
  "Returns the entity if passed an id that is not false-y."
  [db id]
  (when id
    (d/entity db id)))

(defn resolve-id
  "Returns the entity if passed an id that is not false-y."
  [db id]
  (when id
    (d/entity db id)))


(defn entities
  "Returns a set of entities from a [:find ?e ...] query."
  [db query-results]
  (into #{}
        (map (comp (partial id-to-entity db) first)
             query-results)))

(defn find-all
  "Returns the set of all results of query over sources."
  [db query & sources]
  (entities
   (apply q query sources)))

(defn find-first
  "Returns the first result from query over sources."
  [db query & sources]
  (first (apply (partial find-all db) query sources)))



(defn query-for-entities
  [db query & params]
  (entities db (apply q query db params)))




(defn head
  "Given a key and a collection of entities comprising the elements of a 'list' linked via the
   key attribute, this function evaluates to the head of that list.
   It evaluates to nil if the collection is empty or nil. If the collection does not
   comprise the elements of a single list linked via 'next-key', the result is
   undefined."
  [next-key entities]
  (first (reduce  #(disj % (get %2 next-key)) (set entities) entities)))

(defn head-fn
  ""
  [next-key]
  (partial head next-key))

(defn first-child
  "Given an entity with linked-list children, returns the head of that list"
  [next-key child-key ent]
  (head next-key (get ent child-key)))

(defn first-child-fn
  ([next-key]
     (partial first-child next-key))
  ([next-key child-key]
      (partial first-child next-key child-key)))

(defn first-child-query
  [next-key child-key db ent]
  (first (difference
          (query-for-entities db '[:find ?child :in $ ?parent ?children-attr
                                   :where
                                   [?parent ?children-attr ?child]] (e ent) child-key)
          (query-for-entities db '[:find ?child :in $ ?parent ?children-attr
                                   :where
                                   [?parent ?children-attr ?child]
                                   [_ ?next-attr ?child]] (e ent) child-key))))

(defn first-child-query-fn
  [next-key child-key]
  (partial first-child-query next-key child-key))

(defn as-seq
  [next-key elem]
  (lazy-seq
   (cons elem
         (if (nil? (get elem next-key))
           nil
           (as-seq next-key (get elem next-key))))))
  
(defn as-seq-fn
    [next-key]
    (partial as-seq next-key))

