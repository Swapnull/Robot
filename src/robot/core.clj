(ns robot.core
  (:require [clojure.math.combinatorics :as combo]))

;; door is twice as expensive as going through a door.
;; doors cannot stay open
;; no cost of changing direction
;; 1 office length is not 1 unit some doors are offset so may need to use fraction of units
;; if stuck can redraw on a regular grid.
;; diagonal moves are 1 & a bit if needed.
;; maybe use http://quil.info/ for GUI if everything is working early.


;Load the file that contains all possible moves
(load-file "src/robot/possibleMoves.clj")
(load-file "src/robot/weights.clj")

(def robotLocation ["storage"])
(def packages [["r101" "r109" 0]]) ;; [start target collected?]

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
  "gets all nodes that are not in the path"
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

(defn getDeliveryRoutes
  "get all the valid delivery routes"
  ([targets] (getDeliveryRoutes "office" targets "office")) ;; if no start and end supplied, assume both as office
  ([start targets] (getDeliveryRoutes start targets "office")) ;;if no end supplied, assume office
  ([start targets end]
    (map #(getPossibleRoute (flatten [start % end])) (combo/permutations targets))))

(defn getShortestRoute 
  "gets all the valid routes and their costs and then returns the lowest cost"
  [targets]
  (first (sort (zipmap (map #(length? %) (getDeliveryRoutes targets)) (map #(flt %) (getDeliveryRoutes targets))))))

(defn -main
 "Wrapper to run program"
 [& args]
 (prn (getShortestVertex (first robotLocation) "o131")))
