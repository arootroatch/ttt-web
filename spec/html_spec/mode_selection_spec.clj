(ns html-spec.mode-selection-spec
  (:require [clojure.test :refer :all]
            [speclj.core :refer :all]
            [tic-tac-toe.game-logs.game-logs :as game-logs]
            [tic-tac-toe.gui.resume-selection :as resume]
            [ttt-spec :as ttt-spec]
            [html-spec.resume-selection-spec :as resume-spec]))

(describe "mode-selection"
  (with-stubs)
  (before (.reset ttt-spec/out))

  (it "asks for mode-selection"
    (with-redefs [game-logs/get-last-in-progress-game (stub :last)
                  resume/handle-resume (stub :handle-resume [:return resume-spec/sql-tui-resume-state])]
      (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/resume-post-2) ttt-spec/out))
    (should-contain "<p>Please select game mode (X always plays first):</p>" (str ttt-spec/out))
    (should-contain "<form action=\"/ttt\" method=\"POST\">" (str ttt-spec/out))
    (should-contain "<select name=\"mode\"><option value=\"1\">Human vs Human</option>" (str ttt-spec/out))
    (should-contain "<option value=\"2\">Human vs Computer (Human plays first)</option>" (str ttt-spec/out))
    (should-contain "<option value=\"3\">Computer vs Human (Computer plays first)</option>" (str ttt-spec/out))
    (should-contain "<option value=\"4\">Computer vs Computer</option></select>" (str ttt-spec/out))
    (should-contain "<input type=\"submit\" /></form></div>" (str ttt-spec/out))
    (should-contain #"<html>.*</html>" (str ttt-spec/out))
    (should-contain "<div style=\"margin:0 auto;width: 400px;text-align: center;\">" (str ttt-spec/out))
    (should-contain "<h1>Tic-Tac-Toe</h1>" (str ttt-spec/out)))

  (it "sets human? to true if mode is 1"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/mode-post-1) ttt-spec/out)
    (should-contain "human? true" (str ttt-spec/out)))

  (it "sets human? to true if mode is 2"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/mode-post-2) ttt-spec/out)
    (should-contain "human? true" (str ttt-spec/out)))

  (it "sets human? to false if mode is 3"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/mode-post-3) ttt-spec/out)
    (should-contain "human? false" (str ttt-spec/out)))

  (it "sets human? to false if mode is 4"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/mode-post-4) ttt-spec/out)
    (should-contain "human? false" (str ttt-spec/out)))
  )
