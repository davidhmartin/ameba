(ns ameba.render)


(defmulti render
  "Given a content entity, produce a value, e.g. html"
  (fn [render-type entity]
    [render-type (:ameba.content/type entity)]))

