(ns ameba.tags
  "data (model-layer) functions for :ameba.tag entities"
  (:use [datomic.api :only [q] :as d]))


(defn get-all-tag-types [db]
  (map #(d/entity db %)
       (flatten (seq (q '[:find ?t ?n :where [?t :ameba.tag/name ?n]] db)))))

(defn get-tags
  "Gets tags with given type"
  [db type]
  (map #(d/entity db %)
       (flatten (seq
                 (q '[:find ?t
                      :in $ ?type
                      :where
                      [?t :ameba.tag/category ?type]]
                    db
                    type)))))

(defn get-tag-names
  [db type]
  (map :ameba.tag/name (get-tags db type)))

(defn sorted-tag-descrs [db type]
  (-> (map :ameba.tag/label (get-tags db :maureenslist.tag.category/meal)) flatten sort))

;; another way to do sorted-tag-descrs
(defn get-tag-descrs [db type]
  (sort (flatten (seq
                  (q '[:find ?d
                       :in $ ?type
                       :where
                       [?t :ameba.tag/label ?d]
                       [?t :ameba.tag/category ?type]]
                     db
                     type)))))

(defn sorted-tags [db type]
  (sort-by :ameba.tag/label
           (-> (get-tags db type) seq flatten)))

(defn tag-with-descr [db descr]
  (d/entity db
            (q '[:find ?t
                 :in $ ?d
                 :where
                 [?t :ameba.tag/label ?d]]
               db
               descr)))

(defn tags-with-descrs [db descrs]
  (flatten (seq
            (let [descrs (set descrs)]
              (q '[:find ?t
                   :in $ ?descrs
                   :where
                   [?t :ameba.tag/label ?d] [(contains? ?descrs ?d)]]
                 db
                 descrs)))))

(defn descr-for-name [db tag-name]
  (ffirst
   (q '[:find ?d
        :in $ ?n
        :where
        [?t :ameba.tag/name ?n]
        [?t :ameba.tag/label ?d]]
      db
      tag-name)))

(defn contains-type [db tag-type tag-names]
  (if (nil? tag-names)
    false
    (let [tags-of-type (set (get-tag-names db tag-type))]
      (some #(contains? tags-of-type %) tag-names))))

