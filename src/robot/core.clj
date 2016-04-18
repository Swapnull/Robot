(ns robot.core
  (:require [clojure.math.combinatorics :as combo]))
;;  (:use [clojure.repl]
;;   [seesaw.core]))

;;Load external data files
(load-file "src/robot/possibleMoves.clj") ;; contains all the possible moves
(load-file "src/robot/weights.clj") ;; contains any extra weight for vertices

(def robotLocation ["storage"])
(def packagesToCollect [["r101" "r105"] ["r103" "office"]]) ;; [start target]
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
  ([targets] (first (sort (zipmap (map #(length? %) (getDeliveryRoutes targets)) (map #(flt %) (getDeliveryRoutes targets))))))
  ([start targets] (first (sort (zipmap (map #(length? %) (getDeliveryRoutes start targets)) (map #(flt %) (getDeliveryRoutes start targets)))))))

(getShortestRoute "r103" ["office" "r101"])


(defn getDestinations
  "gets all the destinations the robot needs to visit"
  []
  (if (empty? packagesToDeliver)
    (if (empty? packagesToCollect)
      []  
      (map #(first %) packagesToCollect))
    (if (empty? packagesToCollect)
      packagesToDeliver
      (conj (map #(first %) packagesToCollect) packagesToDeliver))))



(defn getFirstDestination
  ([d] (last (last (first (last (getShortestRoute d))))))
  ([s d] (last (last (first (last (getShortestRoute s d)))))))

(defn getFirstPath
  ([d] (last (first (last (getShortestRoute d)))))
  ([s d] (last (first (last (getShortestRoute s d))))))


(defn getFirstPathDistance
  ([d] (first (first (last (getShortestRoute d)))))
  ([s d] (first (first (last (getShortestRoute s d))))))

(defn collectPackages
  [d p]
  (map #(second %) (filter #(= (getFirstDestination d) (first %)) p)))

(defn run []
  (loop [d (getDestinations)
         p packagesToCollect
         r []
         s "office"
         l 0]
    (if (empty? d)
      {"Length" l "Route" r}
      (recur (distinct (flatten (conj (remove #{(getFirstDestination s d)} d) (collectPackages d p))))
             (filter #(not (= (getFirstDestination s d) (first %))) p) 
             (conj r (getFirstPath s d))
             (getFirstDestination s d)
             (+ l (getFirstPathDistance s d))))))


(run)
