(ns robot.core-test
  (:require [clojure.test :refer :all]
            [robot.core :refer :all]))

(deftest task-one
  (go ["r131" ["office"]]))

(deftest task-two
  (go ["r119" ["office"]]))

(deftest task-three
  (go ["r113" ["r115"]]))
