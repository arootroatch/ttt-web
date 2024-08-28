(ns submission-handlers.replay
  (:require [tic-tac-toe.game-logs.game-logs :as game-logs]
            [tic-tac-toe.game-logs.sql :as sql]
            [handle-submission :refer :all]
            [helpers :as helpers]))

(defmethod handle-submission :replay [_ post-value outputStream state]
  (let [game-log (game-logs/get-game-log {:db (:db state) :filepath helpers/edn-logs-path :ds sql/ds :id post-value})
        new-state (assoc game-log :current-screen :replay :game-state :in-progress :replay? true :human? true :player :x)]
    (helpers/write-output outputStream new-state)))
