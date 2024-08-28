(ns submission-handlers.second-ai-level
  (:require [tic-tac-toe.game-logs.game-logs :as game-logs]
            [tic-tac-toe.game-logs.sql :as sql]
            [tic-tac-toe.gui.play :as play]
            [helpers :as helpers]
            [handle-submission :refer :all]))

(defn- ai-game [state]
  (if (not= :in-progress (:game-state state))
    (do (game-logs/log-completed-game {:ds sql/ds :log-file helpers/edn-logs-path :state state}) state)
    (recur (play/ai-turn state))))

(defmethod handle-submission :second-ai-level [post-key post-value outputStream state]
  (let [post-state (assoc state post-key post-value)
        new-state (assoc post-state :current-screen (helpers/set-screen post-state))]
    (helpers/write-output outputStream (ai-game new-state))))
