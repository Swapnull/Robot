(ns robot.core
  (:require [clojure.math.combinatorics :as combo] [io.aviso.ansi :as pretty]))


;;Load external data files
(load-file "src/robot/possibleMoves.clj") ;; contains all the possible moves
(load-file "src/robot/weights.clj") ;; contains any extra weight for vertices

;; HELPER FUNCTIONS
(defn in?
 "true if collection contains element"
  [collection element]
  (some #(= element %) collection))

(defn abs [n] (max n (- n)))

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
  (let [routes (getRoutes start target)]
   ;; (prn (str "Found " (count routes) " possible routes from " start " to " target ".") )
    (first (sort (zipmap (map #(min (cost? %)) routes) routes)))))

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
  (loop [t targets    
         c (first t)  
         l []]         
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
  ([targets] (let [routes (getDeliveryRoutes targets)]
               (first (sort (zipmap (map #(length? %) routes) (map #(flt %) routes))))))
  ([start targets] (let [routes (getDeliveryRoutes start targets)]
                     (first (sort (zipmap (map #(length? %) routes) (map #(flt %) routes)))))))

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

(defn collectPackages
  "get destination of any packages in the current location"
  [location packages]
  (map #(second %) (filter #(= location (first %)) packages)))

(defn routeData
  "Returns data for use in run function" 
  [start destinations]
  (let [route (getShortestRoute start destinations)]
    {
    :length (first (first (last route)))
    :firstPath (last (first (last route)))
    :firstStop (last (last (first (last route))))
    }))

(defn validateLocations
  [locations]
  (if (in? (map #(in? (keys possibleMoves) %) locations) nil)
    (throw (Exception. "Incorrect location supplied"))
    (prn (pretty/blue "Locations validated"))))

(defn strip [coll chars]
    (apply str (remove #((set chars) %) coll)))

(defn run 
  ([ptc ptd] (run ptc ptd "office"))
  ([ptc ptd start]
    (validateLocations (flatten (conj ptc ptd)))
    (prn (str "Detected " (+ (count ptc) (count ptd)) " package to collect and deliver")) 
    (prn (str "Starting at " (pretty/bold-green start)))
    (loop [d (flatten (conj (getDestinations ptc ptd) "office"))
         p ptc ;;packages to collect
         r []
         s start
         l 0]
      (if (in? (map #(first %) ptc) s)
        (prn (str "Collected a package from " (pretty/bold-green s) " for " (pretty/bold-red (second (get ptc (.indexOf (map #(first %) ptc) s))))))
        (if (or (in? ptd s) (and (and (<= (count d) 1) (= s "office")) (not (empty? r))))
          (prn (str "Delivered package to " (pretty/bold-green s)))
          nil))
      (if (or (empty? d) (and (<= (count d) 1) (= s "office")))
        (prn { 
             :finalDestination (pretty/blue s) 
             :path r
             :distance (pretty/magenta l) })
        (let [data (routeData s d)]
          (recur  
            (distinct (conj (flatten (conj (filter #(not (= (get data :firstStop) %)) d) (collectPackages (get data :firstStop) p))) "office"))
            (filter #(not (= (get data :firstStop) (first %))) p) 
            (conj r (get data :firstPath))
            (get data :firstStop)
            (+ l (get data :length))))))))
;;TASKS
;; Here are the tasks for the submission. They are commented as the REPL takes forever to load/timesout otherwise

;;(run [["r131" "office"]] []) ;;Task 1 Collect a parcel from the main office and deliver it to R131
;;(run [["r119" "office"]] []) ;;Task 2 Collect a parcel from the main office and deliver it to R119
;;(run [["r113" "r115"]] []) ;;Task 3 Collect a parcel from R113 and deliver it to room R115
;;(run [["r113" "r129"]] []) ;;Task 4 Collect a parcel from R113 and deliver it to room R129
;;(run [["r131" "office"]] ["r131"]) ;; Task 5 Take a parcel from office to R131. Collect another from R131 and deliver to office
;;(run [] ["r131" "r111"]) ;; Task 6 Take two parcels from the main office to rooms r131 and r111
;;(run [["r121" "office"]] ["r131" "r111"]) ;; Task 7 (and 8?) Take 2 parcels from main office to r131 and r111. Collect a parcel from r121 and bring to office

