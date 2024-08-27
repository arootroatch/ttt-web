(ns html-spec.resume-selection-spec
  (:require [clojure.test :refer :all]
            [speclj.core :refer :all]
            [tic-tac-toe.game-logs.game-logs :as game-logs]
            [tic-tac-toe.gui.resume-selection :as resume]
            [ttt-spec :as ttt-spec]))

(def sql-tui-resume-state
  {:mode            2
   :board           [1 2 3 4 :x 6 7 8 9]
   :first-ai-level  3
   :db              :sql
   :filepath        "spec/tic_tac_toe/game_logs/in_progress/game-4.edn"
   :game-id         4
   :second-ai-level nil
   :player          :o
   :human?          false
   :game-state      :in-progress
   :ui              :tui
   :current-screen  :play})

(def in-progress {:games/current_screen  "",
                  :games/game_state      ":in-progress",
                  :games/human           false,
                  :games/mode            2,
                  :games/first_ai_level  3,
                  :games/second_ai_level 0,
                  :games/board           "[1 2 3 4 5 6 7 8 9]",
                  :games/id              8,
                  :games/ui              ":tui",
                  :games/player          ":o",
                  :games/moves           "[1 2 3 4 :x 6 7 8 9][:o 2 3 4 :x 6 7 8 9]"})

(describe "resume-selection"
  (with-stubs)
  (before (.reset ttt-spec/out))

  (it "asks if user wants to resume previous game if in-progress"
    (with-redefs [game-logs/get-last-in-progress-game
                  (stub :last {:return {:game-id 8}})]
      (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/sql-post) ttt-spec/out)
      (should-contain (str "Set-Cookie: state=" ttt-spec/resume-state "\n\n")
                      (str ttt-spec/out))
      (should-contain "<p>There&apos;s an unfinished game! Would you like resume?</p>" (str ttt-spec/out))
      (should-contain "<form action=\"/ttt\" method=\"POST\">" (str ttt-spec/out))
      (should-contain
        "<select name=\"resume-selection\"><option value=\"1\">Yes</option><option value=\"2\">No</option></select>"
        (str ttt-spec/out))
      (should-contain "<input type=\"submit\" /></form></div>" (str ttt-spec/out))
      (should-contain #"<html>.*</html>" (str ttt-spec/out))
      (should-contain "<div style=\"margin:0 auto;width: 400px;text-align: center;\">" (str ttt-spec/out))
      (should-contain "<h1>Tic-Tac-Toe</h1>" (str ttt-spec/out))))

  (it "sets screen to play if resumed"
    (with-redefs [game-logs/get-last-in-progress-game (stub :last {:return in-progress})
                  resume/handle-resume (stub :handle-resume {:return sql-tui-resume-state})]
      (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/resume-post-1) ttt-spec/out)
      (should-contain ":current-screen :play" (str ttt-spec/out))))

  (it "set screen to mode-selection if not resumed"
    (with-redefs [game-logs/get-last-in-progress-game (stub :last {:return nil})
                  resume/handle-resume (stub :handle-resume {:return nil})]
      (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/resume-post-2) ttt-spec/out)
      (should-contain (str "Set-Cookie: state=" ttt-spec/mode-state)
                      (str ttt-spec/out)))))
