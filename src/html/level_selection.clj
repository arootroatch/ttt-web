(ns html.level-selection
  (:require [hiccup2.core :as h]
            [render-html :refer :all]
            [tic-tac-toe.tui.print-utils :as print-utils]))

(defmethod render-html :level-selection [{:keys [mode first-ai-level]}]
  (str (h/html [:html
                [:div {:style "margin:0 auto;width: 400px;text-align: center;"}
                 [:h1 "Tic-Tac-Toe"]
                 [:p (if (or (= 2 mode) (and (= 4 mode) (some? first-ai-level)))
                       (second print-utils/level-prompt)
                       (first print-utils/level-prompt))]
                 [:form {:method "POST" :action "/ttt"}
                  [:select {:name (if (and (= mode 4) (some? first-ai-level))
                                    "second-ai-level"
                                    "first-ai-level")}
                   [:option {:value 1} (nth print-utils/level-prompt 3)]
                   [:option {:value 2} (nth print-utils/level-prompt 4)]
                   [:option {:value 3} (nth print-utils/level-prompt 6)]]
                  [:input {:type "submit"}]]]])))
