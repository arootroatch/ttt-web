(ns ttt-web.server
  (:require [ring.adapter.jetty :as jetty]
            [ttt-web.routes :as routes]))

(defn -main [& _args]
  (jetty/run-jetty routes/app {:port 3000 :join? true}))
