(ns ttt-web.game-spec
  (:require [speclj.core :refer [describe it should=]]
            [ttt-web.game :as sut]))

(describe "Game"
  (it "plays the current player's mark and switches player"
    (let [result (sut/play {:board [1 2 3 4 5 6 7 8 9] :player :o :game-state :in-progress} 4)]
      (should= [1 2 3 4 :o 6 7 8 9] (:board result))
      (should= :x (:player result))))

  (it "ignores a move onto an occupied cell"
    (should= {:board [:o 2 3 4 5 6 7 8 9] :player :x}
             (sut/play {:board [:o 2 3 4 5 6 7 8 9] :player :x} 0)))

  (it "scores the board after a move"
    (should= "X wins!" (:game-state (sut/play {:board [:x :x 3 :o :o 6 7 8 9]
                                               :player :x
                                               :game-state :in-progress} 2))))

  (it "starts a new game in progress"
    (should= :in-progress (:game-state sut/new-game)))

  (it "ignores moves after the game is over"
    (let [state {:board [:x :x :x :o :o 6 7 8 9] :player :o :game-state "X wins!"}]
      (should= state (sut/play state 5)))))
