(ns html.resume-selection
  (:require [hiccup2.core :as h]
            [tic-tac-toe.tui.print-utils :as print-utils]
            [render-html :refer :all]))

(defmethod render-html :resume-selection [_]
  (str (h/html [:html
                [:div {:style "margin:0 auto;width: 400px;text-align: center;"}
                 [:h1 "Tic-Tac-Toe"]
                 [:p (first print-utils/resume-prompt)]
                 [:form {:method "POST" :action "/ttt"}
                  [:select {:name "resume-selection"}
                   [:option {:value 1} (second print-utils/resume-prompt)]
                   [:option {:value 2} (last print-utils/resume-prompt)]]
                  [:input {:type "submit"}]]]])))
