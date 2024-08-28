(ns submission-handlers.move
  (:require [tic-tac-toe.eval-board :as eval]
            [tic-tac-toe.game-logs.game-logs :as game-logs]
            [tic-tac-toe.game-logs.sql :as sql]
            [tic-tac-toe.gui.play :as play]
            [tic-tac-toe.gui.utils :as utils]
            [helpers :as helpers]
            [handle-submission :refer :all]))

(defn- ai-move [outputStream state]
  (let [new-state (play/ai-turn state)]
    (when (not= :in-progress (:game-state new-state))
      (game-logs/log-completed-game {:ds sql/ds :log-file helpers/edn-logs-path :state new-state}))
    (helpers/write-output outputStream new-state)))

(defmethod handle-submission :move [_ post-value outputStream state]
  (if (= :in-progress (:game-state state))
    (let [{:keys [board player]} state
          new-board (assoc board post-value player)
          new-state (assoc state :board new-board
                                 :player (utils/switch-player player)
                                 :human? (if (not= 1 (:mode state)) false true)
                                 :game-state (eval/score new-board))]
      (game-logs/log-move {:ds sql/ds :state new-state})
      (if (not= :in-progress (:game-state new-state))
        (do (game-logs/log-completed-game {:ds sql/ds :log-file helpers/edn-logs-path :state new-state})
            (helpers/write-output outputStream new-state))
        (if (not= 1 (:mode state))
          (ai-move outputStream new-state)
          (helpers/write-output outputStream new-state))))
    (helpers/write-output outputStream state)))
