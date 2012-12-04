(ns ameba.structure
    "Functions, queries, and rules related to document structure"
  (:use [ameba.datomic :only [id-to-entity query-for-entities e entities]]
        [datomic.api :only [q] :as d]
        [clojure.set :only [difference]]))

(def parent-rule '[[(parent ?ent ?parent)
                    [?parent :ameba.structure/child ?ent]]])

(def ancestor-rule '[[(ancestor ?ent ?ancestor)
                      [parent ?ent ?ancestor]]
                     [(ancestor ?ent ?ancestor)
                      [parent ?ent ?p]
                      [ancestor ?p ?ancestor]]])

(def rules (concat parent-rule ancestor-rule))



(defn head
  "Given a collection of entities comprising the elements of a 'list' linked via the
   :ameba.structure/next attribute, this function evaluates to the head of that list.
   It evaluates to nil if the collection is empty or nil. If the collection does not
   comprise the elements of a single list linked via :ameba.structure/next, the result is
   undefined."
  [entities]
  (first (reduce  #(disj % (:ameba.structure/next %2)) entities entities)))


(defn first-child
  "Given an entity with linked-list children, returns the head of that list"
  ([ent]
     (head (:ameba.structure/child ent)))
  ([ent children-attr]
     (head (get ent children-attr))))


(defn first-child_alternative
  ([db ent] (first-child db ent :ameba.structure/child))
  ([db ent attr]
     (first (difference
              (query-for-entities db '[:find ?child :in $ ?parent ?children-attr
                   :where
                   [?parent ?children-attr ?child]] (e ent) attr)
              (query-for-entities db '[:find ?child :in $ ?parent ?children-attr
                             :where
                             [?parent ?children-attr ?child]
                                       [_ :ameba.structure/next ?child]] (e ent) attr)))))

(defn as-seq
  [elem]
  (lazy-seq
   (cons elem
         (if (nil? (:ameba.structure/next elem))
           nil
           (as-seq (:ameba.structure/next elem)))))
  
  ;; ([elem] (as-seq elem :ameba.structure/next))
  ;; ([elem next-attr]
  ;;    (lazy-seq
  ;;     (cons elem
  ;;           (if (nil? (get elem next-attr))
  ;;             nil
  ;;             (as-seq (get elem next-attr) next-attr)))))
  )
  


(defn children-seq
  ([ent] (as-seq (first-child ent)))
  ([ent child-attr] (as-seq (first-child ent child-attr))))



  
 