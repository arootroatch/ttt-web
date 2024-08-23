(ns ttt
  (:require [hiccup2.core :as h])
  (:require [hiccup.form :as form])
  (:import MyServer.Route))

(defn db-selection []
  (str (h/html [:html])
       (h/html [:p "Please select your database"])
       (h/html [:select {:name "db"}])
       )
  )

(deftype TTT []
  Route
  (serve [this connData outputStream]
    (.write outputStream (.getBytes (db-selection))))
  )