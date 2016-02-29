(ns robot.core
 (:gen-class))

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
(def toCollect [""])
(def carrying [""])

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
         i 0] 
    (if (empty? w)
      (+ (- (count data) i) t)
      (if (or (and (in? data (first c)) (in? data (second c))))
        (recur (rest w) (first (rest w)) (+ t (last c)) (inc i))
        (recur (rest w) (first (rest w)) t i)))))


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
          (flt (map #(flt (getRoutes % t (conj p s)))  (getNewNodes s (conj p s)))) ;; recur for every option
      ))))
  
(defn getShortestRoute 
  "Get the shortest vertex between two nodes"
  [start target]
  (first (sort (zipmap (map #(min (cost? %)) (getRoutes start target)) (getRoutes start target)))))


(defn deliveryRoute
  [data]
  (loop [d (conj data "office") r []]
    (if (= 1 (count d))
      r
      (recur (rest d) (conj r (getShortestRoute (first d) (second d)))))))

(defn -main
 "Wrapper to run program"
 [& args]
 (prn (getShortestRoute (first robotLocation) "o131")))



