(ns robot.core
  (:require [clojure.math.combinatorics :as combo]))
;;  (:use [clojure.repl]
;;   [seesaw.core]))

;;Load external data files
(load-file "src/robot/possibleMoves.clj") ;; contains all the possible moves
(load-file "src/robot/weights.clj") ;; contains any extra weight for vertices

(def robotLocation ["storage"])
(def packagesToCollect [["r101" "r109"] ["r109" "r107"]]) ;; [start target]
(def packagesToDeliver [])

;; HELPER FUNCTIONS
(defn in?
 "true if collection contains element"
 [collection element]
 (some #(= element %) collection))

;; function taken from http://stackoverflow.com/a/35300641/2237577
(defn flt 
  "flatten to one sequence regardless of uneven nesting"
  [s] 
  (mapcat #(if (every? coll? %) (flt %) (list %)) s))

(defn cost?
  "Calculate the cost of a vertex"
  [data]
  (loop [w weights
         c (first w)
         t 0  
         i 1] 
    (if (empty? w)
      (+ (- (count data) i) t)
      (if (or (and (in? data (first c)) (in? data (second c))))
        (recur (rest w) (first (rest w)) (+ t (last c)) (inc i))
        (recur (rest w) (first (rest w)) t i)))))

(defn length? 
  "gets the length of a path"
  [data] 
  (reduce + (map #(first %) data)))

(defn getPossibleMoves 
  "get possible moves from the current node"
  [start]
  (get (first (filter #(= (first %) start) possibleMoves)) 1))

(defn getNewNodes 
  "gets all possible nodes that are not already in the path"
  [node path]
  (filter #(not (in? path %)) (getPossibleMoves node)))

(defn getVertex
  "Get all the routes between two nodes"
  ([s t] (getVertex s t [])) ;; only start and target provided
  ([s t p]
    (if(= s t)
     (conj p t)
      (if (empty? (getNewNodes s p))
          nil  ;; no moves available, return nil to signal this
          (flt (map #(flt (getVertex % t (conj p s)))  (getNewNodes s (conj p s)))) ;; recur for every option
      ))))
  
(defn getShortestVertex 
  "Get the shortest vertex between two nodes"
  [start target]
  (first (sort (zipmap (map #(min (cost? %)) (getVertex start target)) (getVertex start target)))))

(defn getPossibleRoute
  "build up a route from data"
  [data]
  (loop [d data r []]
    (if (= 1 (count d))
      r
      (recur (rest d) (conj r (getShortestVertex (first d) (second d)))))))

(defn buildList
  [start targets end]
  (loop [t targets
         l []]
    (if (empty? t)
      [start l end]
      (if (or (in? l (first t)) (or (= start (first t)) (= end (first t))))
        (recur (rest t) l)
        (recur (rest t) (conj l (first t)))
      )
    )
  )
)

(defn getDeliveryRoutes
  "get all the valid delivery routes"
  ([targets] (getDeliveryRoutes "office" targets "office")) ;; if no start and end supplied, assume both as office
  ([start targets] (getDeliveryRoutes start targets "office")) ;;if no end supplied, assume office
  ([start targets end]
    (pmap #(getPossibleRoute (flatten (buildList start % end))) (combo/permutations targets))))

(defn getShortestRoute 
  "gets all the valid routes and their costs and then returns the lowest cost"
  [targets]
  (first (sort (zipmap (map #(length? %) (getDeliveryRoutes targets)) (map #(flt %) (getDeliveryRoutes targets))))))


(defn getUncollectedPackages
  [location]
  (filter #(= location (first %)) packagesToCollect)
)

(defn getUncollectedPackages
  [location]
  (filter #(not (= location (first %))) packagesToCollect)
)

(defn go [packages]
  (loop [
    p packages
   r []]
    (if (empty? packages)
      r
      (recur ( p) (conj r (getShortestRoute (filter #(first %) p)))))))

(go ["r131" "r101"])
