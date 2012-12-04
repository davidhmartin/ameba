(ns ameba.hiccup.render)


(defmulti render
  "Given a content entity, produce a value, e.g. html"
  (fn [ent]
    (if-let [content-type (:ameba.content/type ent)]
      content-type
      (:ameba.document/type ent))))


(defmethod render :default [ent] (str "No renderer for " (:db/id ent)))