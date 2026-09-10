(ns ttt-web.handlers-spec
  (:require [ring.middleware.anti-forgery :as af]
            [speclj.core :refer [context  redefs-around describe  it  should-contain  should-not-contain  should=]]
            [ttt-web.game :as game]
            [ttt-web.handlers :as sut]))

(describe "handlers"

  (context "move"
    (it "rejects a move outside the board"
      (should= 400 (:status (sut/move {:params  {:n "99"}
                                       :session {:board [1 2 3 4 5 6 7 8 9] :player :x}}))))

    (it "responds 200 to a valid move"
      (should= 200 (:status (sut/move {:params  {:n "0"}
                                       :session {:board      [1 2 3 4 5 6 7 8 9]
                                                 :player     :x
                                                 :game-state :in-progress}}))))

    (it "returns the whole board, not just a square"
      (let [result (:body (sut/move {:params  {:n "0"}
                                     :session {:board [1 2 3 4 5 6 7 8 9] :player :x :game-state :in-progress}}))]
        (should= 9 (count (re-seq #"<button" result)))
        (should-contain ">X<" result)
        (should-not-contain ">O<" result)
        (should-not-contain "<!DOCTYPE" result)))

    (it "returns the updated heading with the board"
      (should-contain "O&apos;s turn!"
                      (:body (sut/move {:params  {:n "0"}
                                        :session {:board      [1 2 3 4 5 6 7 8 9]
                                                  :player     :x
                                                  :game-state :in-progress}})))))

  (context "home"
    (redefs-around [af/*anti-forgery-token* "test-token"])

    (it "starts a new game"
      (should= game/new-game
               (:session (sut/home {}))))

    (it "renders the board"
      (should= 9 (count (re-seq #"<button" (:body (sut/home {}))))))

    (it "renders the heading"
      (should-contain "X&apos;s turn!" (:body (sut/home {}))))

    (it "escapes rendered content"
      (should-contain "&lt;script&gt;"
                      (:body (sut/home {:session {:board      [1 2 3 4 5 6 7 8 9]
                                                  :player     :x
                                                  :game-state "<script>alert(1)</script>"}}))))

    (it "renders the page shell"
      (let [response (sut/home {})]
        (should= 200 (:status response))
        (should= "text/html" (get-in response [:headers "Content-Type"]))
        (should-contain "<h1>Tic-Tac-Toe</h1>" (:body response))
        (should-contain "htmx.org" (:body response))))

    (it "resumes a game in progress"
      (let [state {:board [:x 2 3 4 :o 6 7 8 9] :player :x}]
        (should= state (:session (sut/home {:session state}))))))

  (context "restart"
    (it "discards the finished game and starts a new one"
      (let [response (sut/restart {:session {:board      [:x :x :x :o :o 6 7 8 9]
                                             :player     :o
                                             :game-state "X wins!"}})]
        (should= 200 (:status response))
        (should= game/new-game (:session response))
        (should-contain "X&apos;s turn!" (:body response)))))

  (context "ai-move"
    (it "plays the ai's move and returns the updated board"
      (let [response (sut/ai-move {:session {:board          [:x 2 3 4 5 6 7 8 9]
                                             :player         :o
                                             :game-state     :in-progress
                                             :mode           2
                                             :first-ai-level 3
                                             :ui :web}})]
        (should= 200 (:status response))
        (should= [:x 2 3 4 :o 6 7 8 9] (get-in response [:session :board]))
        (should= :x (get-in response [:session :player]))
        (should-contain ">O<" (:body response))))

    (it "plays the ai move only when it's the ai's turn"
      (let [response (sut/ai-move {:session {:board          [1 2 3 4 5 6 7 8 9]
                                             :player         :x
                                             :game-state     :in-progress
                                             :mode           2
                                             :first-ai-level 3
                                             :ui :web}})]
        (should= 400 (:status response))
        (should= "It's the human's turn!" (:body response))))))
