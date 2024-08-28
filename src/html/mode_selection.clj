(ns html.mode-selection
  (:require [hiccup2.core :as h]
            [next.jdbc :as jdbc]
            [tic-tac-toe.game-logs.sql :as sql]
            [tic-tac-toe.game_logs.edn-logs :as edn]
            [tic-tac-toe.tui.print-utils :as print-utils]
            [render-html :refer :all]))

(def edn-logs-path "src/game_logs/game-logs.edn")

(defmulti get-game-ids :db)

(defmethod get-game-ids :sql [{:keys [ds]}]
  (let [query (jdbc/execute!
                ds ["SELECT id FROM games WHERE game_state = 'X wins!' OR game_state = 'O wins!' OR
                game_state = 'It is a tie!' AND
                NOT board = '[1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27]'
                ORDER BY id ASC"])]
    (map #(:games/id %) query)))

(defmethod get-game-ids :edn [{:keys [path]}]
  (let [logs (edn/read-edn-file path)]
    (->> logs
         (filter #(< (count (:board %)) 17))
         (map #(:game-id %))
         sort)))

(defn render-id-option [n]
  [:option {:value n} n])

(defn replay-form [db]
  [:form {:method "POST" :action "/ttt"}
   [:select {:name "replay"}
    (map #(render-id-option %) (get-game-ids {:db db :ds sql/ds :path edn-logs-path}))]
   [:input {:type "submit"}]])

(defn mode-form []
  [:form {:method "POST" :action "/ttt"}
   [:select {:name "mode"}
    [:option {:value 1} (second print-utils/mode-prompt)]
    [:option {:value 2} (nth print-utils/mode-prompt 2)]
    [:option {:value 3} (nth print-utils/mode-prompt 3)]
    [:option {:value 4} (last print-utils/mode-prompt)]]
   [:input {:type "submit"}]])

(defmethod render-html :mode-selection [{:keys [db]}]
  (str (h/html [:html
                [:div {:style "margin:0 auto;width: 400px;text-align: center;"}
                 [:h1 "Tic-Tac-Toe"]
                 [:p "Choose a game to replay"]
                 (replay-form db)
                 [:p "OR"]
                 [:p (first print-utils/mode-prompt)]
                 (mode-form)]])))
