(ns robot.core
  (:gen-class))

;; door is twice as expensive as going through a door.
;; doors cannot stay open
;; no cost of changing direction
;; 1 office length is not 1 unit some doors are offset so may need to use fraction of units
;; if stuck can redraw on a regular grid.
;; diagonal moves are 1 & a bit if needed.
;; maybe use http://quil.info/ for GUI if everything is working early.

;; TSP IMPLEMENTATIONS
;; https://github.com/hdurer/clj-ga-salesman/blob/master/src/info/betareduction/dojo/holger_salesman.clj
;; https://github.com/bgianfo/traveling-salesman/blob/master/clojure/tsp-ants.clj
;; https://github.com/rrees/clj-ga-salesman/blob/master/src/salesman.clj


;Load the file that contains all possible moves
(load-file "possibleMoves.clj")

(def routes {
'(a b) 2,
'(b a) 3,
'(c b) 4,
'(b c) 4,
'(c a) 5,
'(a c) 2,
'(d c) 9,
'(c d) 7})

(defn targets[src]
    (map (comp second key)
         (filter
             (fn [keyval] (= 'a (first (key keyval))))
             routes)))

(defn score[route]
    (get routes route))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (prn "hello world"))
