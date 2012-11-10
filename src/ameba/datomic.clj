(ns ameba.datomic
  (:use [datomic.api :only [db q] :as d]
        [clojure.walk :as walk ]))

;; The URI to datomic comes from environment variable
;; AMEBA_DATOMIC_URI, defaulting to an in-memory database if not specified.

(def default-datomic-uri "datomic:free://localhost:4334/ameba")

;(def default-datomic-uri "datomic:mem://ameba")

;;;;(d/create-database default-datomic-uri)

(def ^:dynamic datomic-uri (get (System/getenv) "AMEBA_DATOMIC_URI" default-datomic-uri))

;;;;;(def conn (d/connect datomic-uri))



;; lifted from https://gist.github.com/3150938

(def ^{:dynamic true :doc "A Datomic connection bound for the life of a Ring request."} *connection*)

(defn init-connection []
  (def ^:dynamic *connection* (d/connect datomic-uri)))

(defn transact
  "Run a transaction with a request-consistent connection."
  [tx]
  (d/transact *connection* tx))

;; (defn q
;;   "Runs the given query over a request-consistent database as well as
;;   the other given sources."
;;   [query & sources]
;;   (apply d/q query (d/db *connection* sources))

(defn wrap-datomic
  "A Ring middleware that provides a request-consistent database connection and
  value for the life of a request."
  [handler uri]
  (fn [request]
    (binding [*connection* (d/connect uri)]
      (handler request))))


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





(defn entity
  "Returns the entity if passed an id that is not false-y."
  [db id]
  (when id
    (d/entity db id)))

(defn entities
  "Returns a set of entities from a [:find ?e ...] query."
  [db query-results]
  (into #{}
        (map (comp (partial entity db) first)
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
  (entities db (q query db params)))



;; todo don't need this

;; unique id generator. Appends a 16-bit sequence number to a 48-bit
;; timestamp. Should provide sufficiently safe uniqueness. Might revisit.
(def counter (ref 0))
(defn- next-counter [] (dosync (alter counter #(bit-and (inc %) 0xffff))))
(defn next-uid []
  (bit-or (bit-shift-left (System/currentTimeMillis) 16) (next-counter))) 


(defn generate-unique-string
  [text is-unique-fn str-generator]
  (if (is-unique-fn text)
    text
    (recur (str text (str-generator)) is-unique-fn str-generator)))
