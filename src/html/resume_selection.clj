(ns html.resume-selection
  (:require [hiccup2.core :as h]
            [tic-tac-toe.prompts :as prompts]
            [render-html :refer :all]))

(defmethod render-html :resume-selection [_]
  (str (h/html [:html
                [:div {:style "margin:0 auto;width: 400px;text-align: center;"}
                 [:h1 "Tic-Tac-Toe"]
                 [:p (first prompts/resume-prompt)]
                 [:form {:method "POST" :action "/ttt"}
                  [:select {:name "resume-selection"}
                   [:option {:value 1} (second prompts/resume-prompt)]
                   [:option {:value 2} (last prompts/resume-prompt)]]
                  [:input {:type "submit"}]]]])))
