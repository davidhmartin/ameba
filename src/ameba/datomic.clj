(ns ameba.datomic
  (:use [datomic.api :only [db q] :as d]))

;; The URI to datomic comes from environment variable
;; AMEBA_DATOMIC_URI, defaulting to an in-memory database if not specified.

;(def default-datomic-uri "datomic:free://localhost:4334/ameba")

(def default-datomic-uri "datomic:mem://ameba")
(d/create-database default-datomic-uri)

(def datomic-uri (get (System/getenv) "AMEBA_DATOMIC_URI" default-datomic-uri))

(def conn (d/connect datomic-uri))

(defn resolve-id [id]
  "Returns the entity for the given ID."
  (d/entity (db conn) id))

(defn resolve-ids
  "Given a nested list of id's, returns a list of entities."
  [id-lists]
  (map resolve-id (flatten (seq id-lists))))

(defn query-for-entities
  [query & params]
  (resolve-ids (q query (db conn) params)))


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
