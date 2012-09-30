(ns ameba.models.document
  "data (model-layer) functions for :ameba.document entities"
  (:use [ameba.datomic :only [conn resolve-id resolve-ids query-for-entities]]
        [datomic.api :only [db q] :as d]))

(defn all-documents
  "Returns sequence of all documents in the database"
  []
  (query-for-entities
   {:find [?d]
    :where [?d :ameba.document/uniqueName]}
   )
  (map #(d/entity (db conn) %)
       (flatten (seq (q '[:find ?d :where [?d :ameba.document/uniqueName]] (db conn))))))

(defn documents-of-type
  "Returns sequence of all documents with the specified type"
  [type-keyword]
  (map #(d/entity (db conn) %)
       (flatten (seq (q '[:find ?d :in $ ?type :where [?d :ameba.document/type ?type]] (db conn) type/keyword)))))

(defn query-documents-with-tag [tag-name]
             (q
            '[:find ?d
            :in $ ?name
            :where
              [?d :document/tag ?t]
              [?t :tag/name ?name]]
            (db conn)
            tag-name))

(defn documents-with-tag
  "Returns seq of documents with given tag"
  [tag-name]
  (map #(d/entity (db conn) %)
  (flatten (seq
            (query-documents-with-tag tag-name) )))
  )

(defn document-with-id [id]
  (resolve-id (ffirst (q '[:find ?d :in $ ?id :where [?d :document/uniqueName ?id]] (db conn) id))))

(defn document-tag-names
  "Returns list of displayable tag names for a document"
  [document]
  (map :tag/label (:document/tag document)))


(defn document-contents-by-type
  "Returns the content items of a document, mapped by content type. Assumes
   there are not more than one content item with the same type."
  [document]
  (group-by :document.structure/type (:document/content document)))


