(ns ttt-web.game
  (:require [tic-tac-toe.board-options :as board-options]
            [tic-tac-toe.bot-moves]
            [tic-tac-toe.eval-board :as eval-board]
            [tic-tac-toe.player :as player]))

(def state-keys
  "Session keys this namespace owns. Everything else in the session belongs to
  middleware (the anti-forgery token, for one) and must survive a new game."
  [:board :player :game-state :ui :mode :first-ai-level :second-ai-level])

(defn next-level-key
  "Which AI level this session still has to choose, or nil when it needs none.
  Mode 1 has no AI; mode 4 has two."
  [{:keys [mode first-ai-level second-ai-level]}]
  (cond
    (= 1 mode) nil
    (nil? first-ai-level) :first-ai-level
    (and (= 4 mode) (nil? second-ai-level)) :second-ai-level))

(defn current-screen
  "The only screen this session is entitled to be on, given what it has chosen
  so far. Every screen redirects here when it isn't the answer."
  [{:keys [mode board] :as state}]
  (cond
    (nil? mode) :mode
    (nil? board) :board
    (next-level-key state) :level
    :else :play))

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

(defn new-game
  ([{:keys [board mode first-ai-level second-ai-level]}]
   (new-game board mode first-ai-level second-ai-level))
  ([board mode]
   (new-game board mode nil nil))
  ([board mode first-ai-level]
   (new-game board mode first-ai-level nil))
  ([board mode first-ai-level second-ai-level]
   (cond-> {:board (case (count board)
                     16 board-options/initial-4x4-board
                     board-options/initial-3x3-board)
            :player :x
            :game-state :in-progress
            :ui :web
            :mode mode}
     (some? first-ai-level) (assoc :first-ai-level first-ai-level)
     (some? second-ai-level) (assoc :second-ai-level second-ai-level))))
