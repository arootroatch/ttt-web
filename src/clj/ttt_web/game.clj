(ns ttt-web.game
  (:require [tic-tac-toe.board-options :as board-options]
  [tic-tac-toe.bot-moves]
  [tic-tac-toe.eval-board :as eval-board]
  [tic-tac-toe.player :as player]))

(defn playable? [{:keys [game-state board]} n]
  (and (= :in-progress game-state) (number? (nth board n))))

(defn ai-turn? [{:keys [mode player game-state]}]
  (and (= :in-progress game-state)
    (or (= 4 mode)
      (and (= 2 mode) (= player :o))
      (and (= 3 mode) (= player :x)))))

(defn ->new-state [{:keys [player] :as state} new-board]
  (assoc state
    :board new-board
    :player (player/switch-player player)
    :game-state (eval-board/score new-board)))

(defn play [{:keys [board player] :as state} n]
  (cond-> state
    (playable? state n) (->new-state (assoc board n player))))

(defn ai-move [state]
  (->new-state state (player/take-turn state)))

(def new-game {:board board-options/initial-3x3-board
               :player :x
               :game-state :in-progress
               :ui :web
               :mode 2
               :first-ai-level 3})
