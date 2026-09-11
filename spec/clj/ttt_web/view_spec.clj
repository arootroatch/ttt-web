(ns ttt-web.view-spec
  (:require [speclj.core :refer [context describe it should=]]
            [tic-tac-toe.board-options :as board-options]
            [tic-tac-toe.prompts :as prompts]
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

  (context "selection-button"
    (it "posts its selection and swaps the screen"
      (should= [:button {:id        "mode-2"
                         :class     "selection-btn"
                         :hx-post   "/mode/2"
                         :hx-target "#screen"
                         :hx-swap   "outerHTML"}
                "Human vs Computer"]
               (sut/selection-button "mode" 2 "Human vs Computer"))))

  (context "mode-selection"
    (it "offers the four modes"
      (should= (into [:div {:id "screen" :class "screen"}
                      [:h2 (first prompts/mode-prompt)]]
                     (for [n [1 2 3 4]]
                       (sut/selection-button "mode" n (nth prompts/mode-prompt n))))
               (sut/mode-selection))))

  (context "board-selection"
    (it "offers 3x3 and 4x4"
      (should= (into [:div {:id "screen" :class "screen"}
                      [:h2 (first prompts/board-prompt)]]
                     (for [n [1 2]]
                       (sut/selection-button "board" n (nth prompts/board-prompt n))))
               (sut/board-selection))))

  (context "level-selection"
    (it "offers easy, medium and unbeatable"
      (should= (into [:div {:id "screen" :class "screen"}
                      [:h2 (nth prompts/level-prompt 2)]]
                     (for [[level label] [[1 3] [2 4] [3 6]]]
                       (sut/selection-button "level" level (nth prompts/level-prompt label))))
               (sut/level-selection {:mode 2})))

    (it "asks for player X's level first when two ais play"
      (should= [:h2 (nth prompts/level-prompt 0)]
               (nth (sut/level-selection {:mode 4}) 2)))

    (it "asks for player O's level once player X's is chosen"
      (should= [:h2 (nth prompts/level-prompt 1)]
               (nth (sut/level-selection {:mode 4 :first-ai-level 3}) 2))))

  (context "board-wrapper"
    (it "asks for the ai move when it is the ai's turn"
      (should= {:id         "board-wrapper"
                :hx-post    "/ai-move"
                :hx-trigger "load delay:300ms"
                :hx-swap    "outerHTML"}
               (second (sut/board-wrapper {:board          [:x 2 3 4 5 6 7 8 9]
                                           :player         :o
                                           :game-state     :in-progress
                                           :mode           2
                                           :first-ai-level 3}))))

    (it "does not ask for an ai move on the human's turn"
      (should= {:id "board-wrapper"}
               (second (sut/board-wrapper {:board          [1 2 3 4 5 6 7 8 9]
                                           :player         :x
                                           :game-state     :in-progress
                                           :mode           2
                                           :first-ai-level 3}))))

    (it "does not ask for an ai move once the game is over"
      (should= {:id "board-wrapper"}
               (second (sut/board-wrapper {:board          [:x :x :x :o :o 6 7 8 9]
                                           :player         :o
                                           :game-state     "X wins!"
                                           :mode           2
                                           :first-ai-level 3}))))

    (it "offers a restart and a new game when the game is over"
      (let [over    {:board      [:x :x :x :o :o 6 7 8 9]
                     :player     :o
                     :game-state "X wins!"}
            buttons (take-last 2 (sut/board-wrapper over))]
        (should= [:button {:id        "restart"
                           :hx-post   "/restart"
                           :hx-swap   "outerHTML"
                           :hx-target "#board-wrapper"}
                  "Restart"]
                 (first buttons))
        (should= [:button {:id          "new-game"
                           :hx-get      "/mode"
                           :hx-swap     "outerHTML"
                           :hx-target   "#board-wrapper"
                           :hx-push-url "true"}
                  "New Game"]
                 (second buttons))))

    (it "offers neither button while the game is in progress"
      (should= [nil nil]
               (take-last 2 (sut/board-wrapper {:board      [1 2 3 4 5 6 7 8 9]
                                                :player     :x
                                                :game-state :in-progress}))))))
