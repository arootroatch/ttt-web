(ns ttt-web.game
  (:require [tic-tac-toe.board-options :as board-options]
  [tic-tac-toe.eval-board :as eval-board]
  [tic-tac-toe.player :as player]))

(defn playable? [{:keys [game-state board]} n]
  (and (= :in-progress game-state) (number? (nth board n))))

(defn play [{:keys [board player] :as state} n]
  (let [new-board (assoc board n player)]
    (cond-> state
      (playable? state n) (assoc
                            :board new-board
                            :player (player/switch-player player)
                            :game-state (eval-board/score new-board)))))

(def new-game {:board board-options/initial-3x3-board
               :player :x
               :game-state :in-progress})
