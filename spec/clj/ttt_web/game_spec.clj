(ns ttt-web.game-spec
  (:require [speclj.core :refer [describe it  should=]]
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

  (it "starts a new game against the ai"
    (should= 2 (:mode sut/new-game))
    (should= 3 (:first-ai-level sut/new-game))
    (should= :web (:ui sut/new-game)))

  (it "plays the ai move and hands the turn back"
    (let [result (sut/ai-move {:board          [:x 2 3 4 5 6 7 8 9]
                               :player         :o
                               :game-state     :in-progress
                               :mode           2
                               :first-ai-level 3
                               :ui             :web})]
      (should= [:x 2 3 4 :o 6 7 8 9] (:board result))
      (should= :x (:player result))
      (should= :in-progress (:game-state result))))

  (it "ignores moves after the game is over"
    (let [state {:board [:x :x :x :o :o 6 7 8 9] :player :o :game-state "X wins!"}]
      (should= state (sut/play state 5)))))
