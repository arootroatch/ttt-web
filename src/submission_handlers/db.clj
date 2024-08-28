(ns submission-handlers.db
  (:require [tic-tac-toe.game-logs.game-logs :as game-logs]
            [tic-tac-toe.game-logs.sql :as sql]
            [tic-tac-toe.game_logs.edn-logs :as edn]
            [helpers :as helpers]
            [handle-submission :refer :all]))

(defmethod handle-submission :db [post-key post-value outputStream state]
  (let [post-state (assoc state post-key post-value)
        game-id (game-logs/get-new-game-id {:db (:db post-state) :path helpers/edn-game-id-path :ds sql/ds})
        filepath (edn/create-new-filepath helpers/edn-in-progress-dir game-id)
        new-state (assoc post-state :current-screen (helpers/set-screen post-state))]
    (if (= :edn post-value)
      (helpers/write-output outputStream (assoc new-state :game-id game-id :filepath filepath))
      (helpers/write-output outputStream (assoc new-state :game-id game-id)))))
