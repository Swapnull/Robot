(ns robot.core
  (:require [clojure.math.combinatorics :as combo]))

;;Load external data files
(load-file "src/robot/possibleMoves.clj") ;; contains all the possible moves
(load-file "src/robot/weights.clj") ;; contains any extra weight for vertices

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


(defn getRoutes
  "Get all the routes between two nodes"
  ([s t] (getRoutes s t [])) ;; only start and target provided
  ([s t p]
    (if(= s t)
      (conj p t)
      (if (empty? (getNewNodes s p))
          nil  ;; no moves available, return nil to signal this
          (flt (map #(flt (getRoutes % t (conj p s))) (getNewNodes s (conj p s))))))))
  
(defn getShortestVertex 
  "Get the shortest vertex between two nodes"
  [start target]
  (first (sort (zipmap (map #(min (cost? %)) (getRoutes start target)) (getRoutes start target)))))

(defn getPossibleRoute
  "build up a route from data"
  [data]
  (loop [d data r []]
    (if (= 1 (count d))
      r
      (recur (rest d) (conj r (getShortestVertex (first d) (second d)))))))

(defn buildList
  "builds a list of targets ensuring there is no duplicates"
  [start targets end]
  (loop [t targets    ;;list of targets
         c (first t)  ;;current item
         l []]        ;;output list 
    (if (empty? t)
      [start l end]
      (if (or (in? l c) (or (= start c) (= end c)))
        (recur (rest t) (second t) l)
        (recur (rest t) (second t) (conj l c))))))

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

(defn getDestinations
  "gets all the destinations the robot needs to visit"
  [ptc ptd]
  (if (empty? ptd)
    (if (empty? ptc)
      []  
      (map #(first %) ptc))
    (if (empty? ptc)
      ptd
      (conj (map #(first %) ptc) ptd))))

;;(defn getFirstDestination
;;  ([d] (last (last (first (last (getShortestRoute d))))))
;;  ([s d] (last (last (first (last (getShortestRoute s d)))))))
;;(defn getFirstPath
;;  ([d] (last (first (last (getShortestRoute d)))))
;;  ([s d] (last (first (last (getShortestRoute s d))))))
;;(defn getFirstPathDistance
;;  ([d] (first (first (last (getShortestRoute d)))))
;;  ([s d] (first (first (last (getShortestRoute s d))))))

(defn collectPackages
  [d p]
  (map #(second %) (filter #(= (getFirstDestination d) (first %)) p)))

(defn routeData 
  [start destinations]
  (let [route (getShortestRoute start destinations)]
    {
    :length (first (first (last route)))
    :firstPath (last (first (last route)))
    :firstStop (last (last (first (last route))))
    }))

(routeData "office" ["r113" "r129"])

(defn run 
  ([ptc ptd] (run ptc ptd "office"))
  ([ptc ptd start]
    (loop [d (conj (getDestinations ptc ptd) "office")
         p ptc ;;packages to collect
         r []
         s start
         l 0]
      (if (<= (count d) 1)
        {"Length" l "Route" r}
        (let [data (routeData s d)]
          (recur (conj (flatten (conj (remove #{(getFirstDestination s d)} d) (collectPackages d p))) "office" )
              (filter #(not (= (getFirstDestination s d) (first %))) p) 
              (conj r (getFirstPath s d))
              (getFirstDestination s d)
              (+ l (getFirstPathDistance s d))))))))



(getShortestRoute ["r131" "office"])
;;TASKS
;; Here are the tasks for the submission. They are commented as the REPL takes forever to load/timesout otherwise

;;(run [["r131" "office"]] []) ;;Task 1 Collect a parcel from the main office and deliver it to R131
;;(run [["r119" "office"]] []) ;;Task 2 Collect a parcel from the main office and deliver it to R119
;;(run [["r113" "r115"]] []) ;;Task 3 Collect a parcel from R113 and deliver it to room R115
;;(run [["r113" "r129"]] []) ;;Task 4 Collect a parcel from R113 and deliver it to room R129
;;(run [["office" "r113"] ["r113" "office"]] []) ;; Task 5 Take a parcel from office to R131. Collect another from R131 and deliver to office
;;(run [] ["r131" "r111"]) ;; Task 6 Take two parcels from the main office to rooms r131 and r111
;;(run [["r121" "office"]] ["r131" "r111"]) ;; Task 7 (and 8?) Take 2 parcels from main office to r131 and r111. Collect a parcel from r121 and bring to office

 
