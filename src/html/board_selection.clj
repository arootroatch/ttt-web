(ns html.board-selection
  (:require [hiccup2.core :as h]
            [tic-tac-toe.tui.print-utils :as print-utils]
            [render-html :refer :all]))

(defmethod render-html :board-selection [_]
  (str (h/html [:html
                [:div {:style "margin:0 auto;width: 400px;text-align: center;"}
                 [:h1 "Tic-Tac-Toe"]
                 [:p (first print-utils/board-prompt)]
                 [:form {:method "POST" :action "/ttt"}
                  [:select {:name "board"}
                   [:option {:value 1} (second print-utils/board-prompt)]
                   [:option {:value 2} (nth print-utils/board-prompt 2)]]
                  [:input {:type "submit"}]]]])))
