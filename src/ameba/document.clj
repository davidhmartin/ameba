(ns ameba.document
  "data (model-layer) functions for :ameba.document entities"
  (:use [ameba.datomic :only [entity query-for-entities]]
        [datomic.api :only [q] :as d]))

(defn all-documents
  "Returns sequence of all documents in the database"
  [db]
  (query-for-entities db
                      '[:find ?d
                        :where [?d :ameba.document/type]]
   )
  ;; (map #(d/entity db %)
  ;;      (flatten (seq (q '[:find ?d :where [?d :ameba.document/uniqueName]] db)))))
  )

(defn documents-of-type
  "Returns sequence of all documents with the specified type"
  [db type]
  (map #(d/entity db %)
       (flatten (seq (q '[:find ?d :in $ ?type :where [?d :ameba.document/type ?type]] db type)))))

(defn query-documents-with-tag [db tag-name]
             (q
            '[:find ?d
            :in $ ?name
            :where
              [?d :ameba.document/tag ?t]
              [?t :ameba.tag/name ?name]]
            db
            tag-name))

(defn documents-with-tag
  "Returns seq of documents with given tag"
  [db tag-name]
  (map #(d/entity db %)
  (flatten (seq
            (query-documents-with-tag db tag-name) )))
  )

(defn document-with-id [db id]
  (entity (ffirst (q '[:find ?d :in $ ?id :where [?d :document/unique-name ?id]] db id))))

(defn document-tag-names
  "Returns list of displayable tag names for a document"
  [document]
  (map :ameba.tag/label (:ameba.document/tag document)))


(defn document-contents-by-type
  "Returns the content items of a document, mapped by content type. Assumes
   there are not more than one content item with the same type."
  [document]
  (group-by :document.structure/type (:document/content document)))


