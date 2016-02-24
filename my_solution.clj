(ns robot.core
  (:gen-class))

;; door is twice as expensive as going through a door.
;; doors cannot stay open
;; no cost of changing direction
;; 1 office length is not 1 unit some doors are offset so may need to use fraction of units
;; if stuck can redraw on a regular grid.
;; diagonal moves are 1 & a bit if needed.
;; maybe use http://quil.info/ for GUI if everything is working early.


(def possibleMoves
  {
      "r101" ["o101"], "r103" ["o103"], "r105" ["o105"], "r107" ["o107"], "r109" ["o109"], "r111" ["o111"],
      "r113" ["o113"], "r115" ["o115"], "r117" ["o117"], "r119" ["o119"], "r121" ["o121"], "r123" ["o123"],
      "r125" ["o125"], "r127" ["o127"], "r129" ["o129"], "r131" ["o131"], "o101" ["r101" "o103" "ts" "a3"],
      "o103" ["r103" "o101" "o105" "b3"], "o105" ["r105" "o103" "o107"], "o107" ["r107" "o105" "o109" "b4"],
      "o109" ["r109" "o105" "o111"], "o111" ["r111" "o193"], "o113" ["r113" "o111" "o109" ], "o115" ["r115" "o113" "o117"],
      "o117" ["r117" "o115" "o119" "storage"], "o119" ["r119" "o117" "o121" "storage"], "o121" ["r121" "o119" "o123"],
      "o123" ["r123" "o121" "o125" "c1"], "o125" ["r125" "o123" "o127" "d2"], "o127" ["r127" "o125" "o129"],
      "o129" ["r129" "o127" "o131" "d1"], "o131" ["r131" "mail"], "storage" ["o117" "o119"], "mail" ["ts" "o131" "office"],
      "office" ["mail"], "ts" ["mail" "o101" "a2"], "a1" ["a3" "b1" "d3"], "a2" ["a3" "ts"], "a3" ["a1" "a2" "o101"],
      "b1" ["a1" "b2" "b3" "c2"], "b2" ["b1" "b4" "c3"], "b3" ["b1" "b4" "o103"], "b4" ["b2" "b3" "o107"], "c1" ["c2" "o123"],
      "c2" ["b1" "c1" "c3"], "c3" ["b2" "c2"], "d1" ["d2" "o129"], "d2" ["d1" "d3" "o125"], "d3" ["a1" "d2"]
  }
)

(def robotLocation ["r105"])

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

(defn getRoutes [start target]
    (loop [s start
        t target
        p [start]
        l (getPossibleMoves s)
        r []],
        (prn p)
        (if (empty? (getNewMoves s p)) ;; no avilable moves
           (recur (first l) t (pop p) (getNewMoves (last p) (pop p)) r)
           (if (in? (getPossibleMoves s) t) ;; target is in collection
               (conj p target) ;;add target to path and return
               (recur (first (getNewMoves s p)) t (conj p (first (getNewMoves s p))) (rest (getNewMoves s p)) r)
           )
        )
    )
)

;;(defn lazySeqToInt [x]
;;    (read-string (apply str (map #(- (int %) 48) x)))
;;)


 ; => [:1 :3 :4 :2]

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ( G :1))
