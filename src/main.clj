(ns main
  (:require [ttt :as ttt])
  (:import (MyServer Main)))

(defn -main [& args]
  (Main/setRoute "/ttt" (ttt/->TTT))
  (Main/main (into-array String args)))