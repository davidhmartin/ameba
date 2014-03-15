(ns ameba.schema
    (:use [datomic.api :only [db q] :as d]
          [clojure.java.io :only [resource]]))

(def ameba-dtm "schemas/ameba.edn")
(def document-dtm "schemas/document.edn")
(def structure-dtm "schemas/structure.edn")
(def content-dtm "schemas/content.edn")
(def comments-dtm "schemas/comments.edn")
(def notes-dtm "schemas/notes.edn")
(def user-dtm "schemas/user.edn")
(def tags-dtm "schemas/tags.edn")

(def all-default-dtms [ameba-dtm document-dtm structure-dtm content-dtm comments-dtm notes-dtm user-dtm tags-dtm])

(defn load-schema-files [& files] (vec (flatten (map #(read-string (slurp (resource %))) files))))

(defn init-schema
  "Initializes the datomic database with the schema definitions in the given sequence of files.
'files' is one or more file paths. To use ameba default schema, use the *-dtm vars defined in this namespace. For example, to initialize document, structure, and content attributes: (init-schema document-dtm structure-dtm content-dtm)."
  [connection & files]
  @(d/transact connection (apply load-schema-files files)))


(defn init-schema-all
  "Initializes the datomic database with all of the default attribute definitions."
  [connection]
  (apply init-schema (cons connection all-default-dtms)))




