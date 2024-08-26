(ns html-spec.mode-selection-spec
  (:require [clojure.test :refer :all]
            [speclj.core :refer :all]
            [ttt-spec :as ttt-spec]))

(describe "mode-selection"
  (before (.reset ttt-spec/out))

  (it "asks for mode-selection if game not resumed"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/resume-post) ttt-spec/out)
    (should-contain (str "Set-Cookie: state=" ttt-spec/mode-state)
                    (str ttt-spec/out))
    (should-contain "<p>Please select game mode (X always plays first):</p>" (str ttt-spec/out))
    (should-contain "<form action=\"/ttt\" method=\"POST\">" (str ttt-spec/out))
    (should-contain "<select name=\"mode\"><option value=\"1\">Human vs Human</option>" (str ttt-spec/out))
    (should-contain "<option value=\"2\">Human vs Computer (Human plays first)</option>" (str ttt-spec/out))
    (should-contain "<option value=\"3\">Computer vs Human (Computer plays first)</option>" (str ttt-spec/out))
    (should-contain "<option value=\"4\">Computer vs Computer</option></select>" (str ttt-spec/out))
    (should-contain "<input type=\"submit\" /></form></div>" (str ttt-spec/out))
    (should-contain #"<html>.*</html>" (str ttt-spec/out))
    (should-contain "<div style=\"margin:0 auto;width: 400px;text-align: center;\">" (str ttt-spec/out))
    (should-contain "<h1>Tic-Tac-Toe</h1>" (str ttt-spec/out))))
