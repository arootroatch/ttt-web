(ns ttt-web.handlers-spec
  (:require [ring.middleware.anti-forgery :as af]
            [speclj.core :refer [context describe it redefs-around should-contain should-not-contain should=]]
            [tic-tac-toe.board-options :as board-options]
            [ttt-web.game :as game]
            [ttt-web.handlers :as sut]))

(def a-game {:board [1 2 3 4 5 6 7 8 9] :player :x :game-state :in-progress :mode 1})

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
    (it "sends a new visitor to mode selection"
      (let [response (sut/home {:session {}})]
        (should= 302 (:status response))
        (should= "/mode" (get-in response [:headers "Location"]))))

    (it "sends a returning player back to the game"
      (let [response (sut/home {:session {:board [1 2 3 4 5 6 7 8 9] :mode 1}})]
        (should= 302 (:status response))
        (should= "/play" (get-in response [:headers "Location"])))))

  (context "play-screen"
    (redefs-around [af/*anti-forgery-token* "test-token"])

    (it "renders the board"
      (should= 9 (count (re-seq #"<button" (:body (sut/play-screen {:session a-game}))))))

    (it "renders the heading"
      (should-contain "X&apos;s turn!" (:body (sut/play-screen {:session a-game}))))

    (it "escapes rendered content"
      (should-contain "&lt;script&gt;"
                      (:body (sut/play-screen {:session (assoc a-game :game-state "<script>alert(1)</script>")}))))

    (it "renders the page shell"
      (let [response (sut/play-screen {:session a-game})]
        (should= 200 (:status response))
        (should= "text/html" (get-in response [:headers "Content-Type"]))
        (should-contain "<h1>Tic-Tac-Toe</h1>" (:body response))
        (should-contain "htmx.org" (:body response))))

    (it "sends a visitor with no game to mode selection"
      (let [response (sut/play-screen {:session {}})]
        (should= 302 (:status response))
        (should= "/mode" (get-in response [:headers "Location"]))))

    (it "sends a half-configured session back to the step it skipped"
      (should= "/board" (get-in (sut/play-screen {:session {:mode 2}}) [:headers "Location"]))
      (should= "/level" (get-in (sut/play-screen {:session {:mode  2
                                                            :board board-options/initial-3x3-board}})
                                [:headers "Location"]))))

  (context "screen guards"
    (it "sends a boardless session to board selection"
      (should= "/board" (get-in (sut/level-screen {:session {:mode 2}}) [:headers "Location"])))

    (it "sends a modeless session to mode selection"
      (should= "/mode" (get-in (sut/board-screen {:session {}}) [:headers "Location"])))

    (it "sends a ready session forward instead of asking again"
      (should= "/play" (get-in (sut/board-screen {:session a-game}) [:headers "Location"])))

    (it "skips level selection when two humans play"
      (should= "/play" (get-in (sut/level-screen {:session {:mode  1
                                                            :board board-options/initial-3x3-board}})
                               [:headers "Location"]))))

  (context "selection guards"
    (it "refuses a board choice before a mode is chosen"
      (should= "/mode" (get-in (sut/select-board {:params {:n "1"} :session {}})
                               [:headers "Location"])))

    (it "refuses a level choice before a board is chosen"
      (should= "/board" (get-in (sut/select-level {:params {:n "3"} :session {:mode 2}})
                                [:headers "Location"])))

    (it "refuses a level choice when two humans play"
      (should= "/play" (get-in (sut/select-level {:params  {:n "3"}
                                                  :session {:mode  1
                                                            :board board-options/initial-3x3-board}})
                               [:headers "Location"]))))

  (context "level-screen"
    (redefs-around [af/*anti-forgery-token* "test-token"])

    (it "renders level selection as a full page"
      (let [response (sut/level-screen {:session {:mode 2 :board board-options/initial-3x3-board}})]
        (should= 200 (:status response))
        (should-contain "<!DOCTYPE" (:body response))
        (should-contain "test-token" (:body response))
        (should-contain "id=\"level-1\"" (:body response))
        (should-contain "id=\"level-3\"" (:body response))))

    (it "asks for player O's level when the first is already chosen"
      (should-contain "player O"
                      (:body (sut/level-screen {:session {:mode           4
                                                         :first-ai-level 3
                                                         :board          board-options/initial-3x3-board}})))))

  (context "mode-screen"
    (redefs-around [af/*anti-forgery-token* "test-token"])

    (it "carries the csrf token and the stylesheet"
      (let [body (:body (sut/mode-screen {}))]
        (should-contain "test-token" body)
        (should-contain "/css/app.css" body)))

    (it "clears a finished game so the next one starts fresh"
      (let [response (sut/mode-screen {:session {:mode            4
                                                 :first-ai-level  3
                                                 :second-ai-level 2
                                                 :board           [:x :x :x :o :o 6 7 8 9]
                                                 :game-state      "X wins!"
                                                 :other           "keep-me"}})]
        (should= {:other "keep-me"} (:session response))))

    (it "returns a fragment to htmx and a full page to the browser"
      (should-not-contain "<!DOCTYPE" (:body (sut/mode-screen {:headers {"hx-request" "true"}})))
      (should-contain "<!DOCTYPE" (:body (sut/mode-screen {}))))

    (it "renders mode selection as a full page"
      (let [response (sut/mode-screen {})]
        (should= 200 (:status response))
        (should-contain "<!DOCTYPE" (:body response))
        (should-contain "htmx.org" (:body response))
        (should-contain "id=\"mode-1\"" (:body response))
        (should-contain "id=\"mode-4\"" (:body response)))))

  (context "board-screen"
    (redefs-around [af/*anti-forgery-token* "test-token"])

    (it "renders board selection as a full page"
      (let [response (sut/board-screen {:session {:mode 2}})]
        (should= 200 (:status response))
        (should-contain "<!DOCTYPE" (:body response))
        (should-contain "test-token" (:body response))
        (should-contain "id=\"board-1\"" (:body response))
        (should-contain "id=\"board-2\"" (:body response)))))

  (context "select-mode"
    (it "preserves session keys it does not own"
      (should= "keep-me"
               (get-in (sut/select-mode {:params {:n "2"} :session {:other "keep-me"}})
                       [:session :other])))

    (it "stores the mode and advances to board selection"
      (let [response (sut/select-mode {:params {:n "2"} :session {}})]
        (should= 200 (:status response))
        (should= 2 (get-in response [:session :mode]))
        (should= "/board" (get-in response [:headers "HX-Push-Url"]))
        (should-contain "id=\"board-1\"" (:body response))
        (should-not-contain "<!DOCTYPE" (:body response)))))

  (context "select-board"
    (it "preserves session keys it does not own"
      (should= "keep-me"
               (get-in (sut/select-board {:params {:n "1"} :session {:mode 1 :other "keep-me"}})
                       [:session :other])))

    (it "stores the chosen board and advances to level selection"
      (let [response (sut/select-board {:params {:n "2"} :session {:mode 2}})]
        (should= 200 (:status response))
        (should= board-options/initial-4x4-board (get-in response [:session :board]))
        (should= "/level" (get-in response [:headers "HX-Push-Url"]))
        (should-contain "id=\"level-1\"" (:body response))))

    (it "starts the game immediately when two humans are playing"
      (let [response (sut/select-board {:params {:n "1"} :session {:mode 1}})]
        (should= "/play" (get-in response [:headers "HX-Push-Url"]))
        (should= :x (get-in response [:session :player]))
        (should= :in-progress (get-in response [:session :game-state]))
        (should-contain "id=\"board-wrapper\"" (:body response))
        (should= 9 (count (re-seq #"board-square" (:body response))))
        (should-contain "X&apos;s turn!" (:body response))
        (should-not-contain "id=\"restart\"" (:body response))))

    (it "stores a 3x3 board when the first option is chosen"
      (should= board-options/initial-3x3-board
               (get-in (sut/select-board {:params {:n "1"} :session {:mode 2}})
                       [:session :board]))))

  (context "select-level"
    (it "preserves session keys it does not own"
      (should= "keep-me"
               (get-in (sut/select-level {:params  {:n "3"}
                                          :session {:mode 2 :other "keep-me"
                                                    :board board-options/initial-3x3-board}})
                       [:session :other])))

    (it "starts the game once the ai level is chosen"
      (let [response (sut/select-level {:params  {:n "3"}
                                        :session {:mode 2 :board board-options/initial-3x3-board}})]
        (should= 200 (:status response))
        (should= 3 (get-in response [:session :first-ai-level]))
        (should= :in-progress (get-in response [:session :game-state]))
        (should= "/play" (get-in response [:headers "HX-Push-Url"]))
        (should= 9 (count (re-seq #"board-square" (:body response))))))

    (it "asks for the second ai level when two ais play"
      (let [response (sut/select-level {:params  {:n "3"}
                                        :session {:mode 4 :board board-options/initial-3x3-board}})]
        (should= 3 (get-in response [:session :first-ai-level]))
        (should= "/level" (get-in response [:headers "HX-Push-Url"]))
        (should-contain "player O" (:body response))
        (should-not-contain "board-square" (:body response))))

    (it "starts the game once both ai levels are chosen"
      (let [response (sut/select-level {:params  {:n "2"}
                                        :session {:mode           4
                                                  :first-ai-level 3
                                                  :board          board-options/initial-3x3-board}})]
        (should= 3 (get-in response [:session :first-ai-level]))
        (should= 2 (get-in response [:session :second-ai-level]))
        (should= "/play" (get-in response [:headers "HX-Push-Url"]))
        (should= 9 (count (re-seq #"board-square" (:body response)))))))

  (context "restart"
    (it "preserves session keys it does not own"
      (should= "keep-me"
               (get-in (sut/restart {:session {:other      "keep-me"
                                               :board      [:x :x :x :o :o 6 7 8 9]
                                               :game-state "X wins!"}})
                       [:session :other])))

    (it "discards the finished game and starts a new one"
      (let [response (sut/restart {:session {:board      [:x :x :x :o :o 6 7 8 9]
                                             :player     :o
                                             :game-state "X wins!"}})]
        (should= 200 (:status response))
        (should= (game/new-game board-options/initial-3x3-board nil) (:session response))
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
