(ns ttt-web.view-spec
  (:require [speclj.core :refer [context describe it should=]]
          [tic-tac-toe.board-options :as board-options]
          [ttt-web.view :as sut]))

(def base-btn-options {:class "board-square" :hx-swap "outerHTML" :hx-target "#board-wrapper"})

(defn- in-progress [board] {:board board :game-state :in-progress})

(describe "view"

  (context "square"
    (it "posts its move and replaces itself"
      (should= [:button (assoc base-btn-options :id "cell-0" :hx-post "/move/0")]
               (sut/square (in-progress board-options/initial-3x3-board) 0)))

    (it "marks token on square"
      (should= "X" (last (sut/square (in-progress [:x 2 3 4 5 6 7 8 9]) 0))))

    (it "posts its own index"
      (should= [:button (assoc base-btn-options :id "cell-4" :hx-post "/move/4")]
               (sut/square (in-progress board-options/initial-3x3-board) 4)))

    (it "button is disabled when move is taken"
      (should= :disabled (:disabled (second (sut/square (in-progress [1 2 3 4 :x 6 7 8 9]) 4)))))

    (it "empty squares are disabled when the game is over"
      (should= :disabled
               (:disabled (second (sut/square {:board      [:x :x :x :o :o 6 7 8 9]
                                               :game-state "X wins!"} 5))))))

  (context "board"
    (it "renders a square per cell"
      (should= (into [:div {:id "board" :class "three-grid"}]
                 (map #(sut/square (in-progress board-options/initial-3x3-board) %) (range 9)))
               (sut/board (in-progress board-options/initial-3x3-board))))

    (it "renders a 16 square board"
      (should= (into [:div {:id "board" :class "four-grid"}]
                 (map #(sut/square (in-progress board-options/initial-4x4-board) %) (range 16)))
               (sut/board (in-progress board-options/initial-4x4-board))))

    (it "renders proper token per cell"
      (should= (into [:div {:id "board" :class "three-grid"}]
                 (map #(sut/square (in-progress [:x :o :x :o :x :o :x :o :x]) %) (range 9)))
               (sut/board (in-progress [:x :o :x :o :x :o :x :o :x])))))

  (context "heading"
    (it "announces whose turn it is"
      (should= [:h2 {:id "play-heading"} "X's turn!"]
               (sut/heading {:player :x :game-state :in-progress})))

    (it "announces the result when the game is over"
      (should= [:h2 {:id "play-heading"} "X wins!"]
               (sut/heading {:player :o :game-state "X wins!"}))))

  (context "board-wrapper"
    (it "offers a restart when the game is over"
      (should= [:button {:id        "restart"
                         :hx-post   "/restart"
                         :hx-swap   "outerHTML"
                         :hx-target "#board-wrapper"}
                "Restart"]
               (last (sut/board-wrapper {:board      [:x :x :x :o :o 6 7 8 9]
                                         :player     :o
                                         :game-state "X wins!"}))))))
