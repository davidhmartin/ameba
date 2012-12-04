(ns ameba.document
  "data (model-layer) functions for :ameba.document entities"
  (:use [ameba.datomic :only [query-for-entities e entities entity]]
        [datomic.api :only [q] :as d]
        [ameba.structure :as structure]))

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

;; todo 
(defn document-with-id [db id]
  (entity db (ffirst (q '[:find ?d :in $ ?id :where [?d :document/unique-name ?id]] db id))))

(defn document-tag-names
  "Returns list of displayable tag names for a document"
  [document]
  (map :ameba.tag/label (:ameba.document/tag document)))


(defn document-contents-by-type
  "Returns the content items of a document, mapped by content type. Assumes
   there are not more than one content item with the same type."
  [document]
  (group-by :document.structure/type (:document/content document)))


(defn document-of
  "Given an entity which is part of a document's content, returns that document."
  [db content]
  (ffirst
  (q '[:find ?d :in $ ?c %
       :where
       [?d :ameba.document/type]
       [ancestor ?c ?d]]
     db (e content) structure/rules) ))

;; todo test this
(defn parent-of
  "Given an entity which is part of a structural hierarchy, returns its structural parent."
  [db content]
  (entity db (ffirst (q '[:find ?p :in $ ?c %
                             :where
                             [parent ?c ?p]]
                           db (e content) structure/parent-rule))))




(defmacro content-text-search-rule
  "Macro defining a recursive rule to execute a fulltext search for documents of the specified type,
   searching the text in content attributes of the specified content type"
  ([doctype text-attr]
     `'[[(find-document ?document ?term)
         [?document :ameba.document/type ~doctype]
         [(fulltext $ ~text-attr ?term) [[?content]]]
         [ancestor ?content ?document]]]))

(defmacro attr-text-search-rule
  "Macro defining a rule for fulltext search on a document of the specified type, searching the specified attribute.
   In this rule, the search is limited to a direct attribute of the document entity."
  ([doctype text-attr]
     `'[[(find-document ?document ?term)
         [?document :ameba.document/type ~doctype]
         [(fulltext $ ~text-attr ?term) [[?document]]]]]))


(def fulltext-query
  '[:find ?r
    :in $ % ?term
    :where
    (find-document ?r :maureenslist.document.type/recipe ?term)])

(def default-fulltext-search-rules
    (concat
          (doc-attr-search-rule :maureenslist.document.type/recipe :ameba.content/title)
          (doc-content-search-rule :maureenslist.document.type/recipe :ameba.content/text)
          (doc-content-search-rule :maureenslist.document.type/recipe :ameba.content/title)))


(defn fulltext-search
  ([rules db term index limit]
      (map #(entity db (first %))
           (->>     
            (q fulltext-query db (concat ameba.structure/rules rules) term)
            (drop index)
            (take limit))))
  ([db str] (search-all-recipe db str 0 1000)))

(defn fulltext-search
  ([attrs db term index limit]
      (map #(entity db (first %))
           (->>     
            (q fulltext-query db (concat ameba.structure/rules rules) term)
            (drop index)
            (take limit))))
  ([attrs db term] (search-all-recipe db term 0 1000)))




