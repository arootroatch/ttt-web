(ns html.db-selection
  (:require [hiccup2.core :as h]
            [render-html :refer :all]))

(defmethod render-html :db-selection [_]
  (str (h/html [:html
                [:div {:style "margin:0 auto;width: 400px;text-align: center;"}
                 [:h1 "Tic-Tac-Toe"]
                 [:p "Please select your database"]
                 [:form {:method "POST" :action "/ttt"}
                  [:select {:name "db"}
                   [:option {:value :edn} "EDN"]
                   [:option {:value :sql} "PostgreSQL"]]
                  [:input {:type "submit"}]]]])))