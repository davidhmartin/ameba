(ns ameba.schema
    (:use [datomic.api :only [db q] :as d]
          [ameba.datomic :only [conn]]
          [clojure.java.io :only [resource]]))

(def document-dtm "schemas/document.dtm")
(def structure-dtm "schemas/structure.dtm")
(def content-dtm "schemas/content.dtm")
(def comments-dtm "schemas/comments.dtm")
(def notes-dtm "schemas/notes.dtm")
(def user-dtm "schemas/user.dtm")
(def tags-dtm "schemas/tags.dtm")

(defn load-schema [& files] (vec (flatten (map #(read-string (slurp (resource %))) files))))

(defn init-schema
  "Initializes the datomic database with the schema definitions in the given sequence of files.
'files' is one or more file paths. To use ameba default schema, use the *-dtm vars defined in this namespace. For example, to initialize document, structure, and content attributes: (init-schema document-dtm structure-dtm content-dtm)."
  [& files]
  @(d/transact conn (apply load-schema files)))


(defn init-schema-all
  "Initializes the datomic database with all of the default attribute definitions."
  []
  (init-schema document-dtm structure-dtm content-dtm comments-dtm notes-dtm user-dtm tags-dtm))




