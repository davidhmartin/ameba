(ns ameba.search
  "Functions, queries, and rules for searching documents"
  (:use [ameba.structure :as structure]))

(defmacro doc-content-search-rule
  "Macro defining a recursive rule to execute a fulltext search for documents of the specified type,
   searching the text in content attributes of the specified content type"
  ([doctype text-attr]
     `'[[(find-document ?document ?term)
         [?document :ameba.document/type ~doctype]
         [(fulltext $ ~text-attr ?term) [[?content]]]
         [ancestor ?content ?document]]]))

(defmacro doc-attr-search-rule
  "Macro defining a rule for fulltext search on a document of the specified type, searching the specified attribute."
  ([doctype text-attr]
     `'[[(find-document ?document ?term)
         [?document :ameba.document/type ~doctype]
         [(fulltext $ ~text-attr ?term) [[?document]]]]]))

