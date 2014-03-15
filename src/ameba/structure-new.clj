(ns ameba.structure-new
    "Functions, queries, and rules related to document structure"
  (:use [ameba.datomic :only [id-to-entity query-for-entities e entities head-fn first-child-fn]]
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





(def head [entities] (head-fn ":ameba.structure/next"))
first-child [ent] (first-child-fn ":ameba.structure/next" ":ameba.structure/child")



  
 
