(ns html.mode-selection
  (:require [hiccup2.core :as h]
            [tic-tac-toe.tui.print-utils :as print-utils]
            [render-html :refer :all]))

(defmethod render-html :mode-selection [_]
  (str (h/html [:html
                [:div {:style "margin:0 auto;width: 400px;text-align: center;"}
                 [:h1 "Tic-Tac-Toe"]
                 [:p (first print-utils/mode-prompt)]
                 [:form {:method "POST" :action "/ttt"}
                  [:select {:name "mode"}
                   [:option {:value 1} (second print-utils/mode-prompt)]
                   [:option {:value 2} (nth print-utils/mode-prompt 2)]
                   [:option {:value 3} (nth print-utils/mode-prompt 3)]
                   [:option {:value 4} (last print-utils/mode-prompt)]]
                  [:input {:type "submit"}]]]])))
