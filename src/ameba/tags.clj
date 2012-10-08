(ns ameba.tags
  "data (model-layer) functions for :ameba.tag entities"
  (:use [ameba.datomic :only [conn resolve-id resolve-ids query-for-entities]]
        [datomic.api :only [db q] :as d]))


(defn get-all-tag-types []
  (map #(d/entity (db conn) %)
       (flatten (seq (q '[:find ?t ?n :where [?t :ameba.tag/name ?n]] (db conn))))))

(defn get-tags
  "Gets tags with given type, e.g. :tag.type/diet or :tag.type.meal"
  [type]
  (map #(d/entity (db conn) %)
       (flatten (seq
                 (q '[:find ?t
                      :in $ ?type
                      :where
                      [?t :tag/type ?type]]
                    (db conn)
                    type)))))

(defn get-tag-names
  [type]
  (map :tag/name (get-tags type)))

(defn get-tag-descrs [type]
  (q '[:find ?d
       :in $ ?type
       :where
       [?t :tag/description ?d]
       [?t :tag/type ?type]]
     (db conn)
     type))

(defn sorted-tag-descrs [type]
  (-> (get-tags type) seq flatten sort))

(defn sorted-tags [type]
  (sort-by :tag/description
           (-> (get-tags type) seq flatten)))

(defn tag-with-descr [descr]
  (d/entity (db conn)
            (q '[:find ?t
                 :in $ ?d
                 :where
                 [?t :tag/description ?d]]
               (db conn)
               descr)))

(defn tags-with-descrs [descrs]
  (flatten (seq
            (let [descrs (set descrs)]
              (q '[:find ?t
                   :in $ ?descrs
                   :where
                   [?t :tag/description ?d] [(contains? ?descrs ?d)]]
                 (db conn)
                 descrs)))))

(defn descr-for-name [tag-name]
  (ffirst
   (q '[:find ?d
        :in $ ?n
        :where
        [?t :tag/name ?n]
        [?t :tag/description ?d]]
      (db conn)
      tag-name)))

(defn contains-type [tag-type tag-names]
  (if (nil? tag-names)
    false
    (let [tags-of-type (set (get-tag-names tag-type))]
      (some #(contains? tags-of-type %) tag-names))))

