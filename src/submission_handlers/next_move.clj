(ns submission-handlers.next-move
  (:require [tic-tac-toe.eval-board :as eval]
            [tic-tac-toe.gui.utils :as utils]
            [helpers :as helpers]
            [handle-submission :refer :all]))

(defmethod handle-submission :next-move [_ _ outputStream state]
  (if (= :in-progress (:game-state state))
    (helpers/write-output outputStream (assoc state :board (first (:moves state)) :moves (rest (:moves state))
                                                    :game-state (eval/score (first (:moves state)))
                                                    :player (utils/switch-player (:player state))))
    (helpers/write-output outputStream state)))
