(ns ameba.schema
    (:use [datomic.api :only [db q] :as d]
        [ameba.datomic :only [conn]]))

(def document-dtm "resources/schemas/document.dtm")
(def structure-dtm "resources/schemas/structure.dtm")
(def content-dtm "resources/schemas/content.dtm")
(def comments-dtm "resources/schemas/comments.dtm")
(def notes-dtm "resources/schemas/notes.dtm")
(def user-dtm "resources/schemas/user.dtm")
(def tags-dtm "resources/schemas/tags.dtm")

(defn load-schema [& files] (vec (flatten (map #(read-string (slurp %)) files))))

(defn init-schema
  "Initializes the datomic database with the schema definitions in the given sequence of files.
'files' is one or more file paths. To use ameba default schema, use the *-dtm vars defined in this namespace. For example, to initialize document, structure, and content attributes: (init-schema document-dtm structure-dtm content-dtm)."
  [& files]
  @(d/transact conn (apply load-schema files)))


(defn init-schema-all
  "Initializes the datomic database with all of the default attribute definitions."
  []
  (init-schema document-dtm structure-dtm content-dtm comments-dtm notes-dtm user-dtm tags-dtm))




