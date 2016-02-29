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

(def robotLocation ["r101"])

(defn in?
 "true if collection contains element"
 [collection element]
 (some #(= element %) collection))

(defn getPossibleMoves [start]
  (get (first (filter #(= (first %) start) possibleMoves)) 1)
)

(defn getNewMoves [node path]
  (filter #(not (in? path %)) (getPossibleMoves node))
)

(defn cost? 
  "returns the cost of the path"
  [path]
  (count path)
)

;; function taken from http://stackoverflow.com/a/35300641/2237577
(defn flt [s] 
  "flatten to one sequence regardless of uneven nesting"
  (mapcat #(if (every? coll? %) (flt %) (list %)) s)
)

(defn getRoutes
  ([s t] (getRoutes s t [])) ;; only start and target provided
  ([s t p]
    (if(= s t)
     (conj p t)
      (if (empty? (getNewMoves s p))
          nil  ;; no moves available, return nil to signal this
          (map #(flt (getRoutes % t (conj p s)))  (getNewMoves s (conj p s))) ;; recur for every option
      )
    )
  )
)
  


(defn -main
 "I don't do a whole lot ... yet."
 [& args]
 (prn (flt (getRoutes (first robotLocation) "o131"))))





