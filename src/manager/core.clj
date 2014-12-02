;core.clj Overview:
;Contains a single function, 'main'.

(ns manager.core
  (:gen-class)
  (:require
   [seesaw.core :as seesaw]
   [manager.gui :as gui]))

(defn -main [& args]
  (seesaw/native!) ;;moved out here to support 'built-in' compatability for menubar in OSX.
                   ;;Note: menubar not currently implemented, but I left this for future reference.
  (gui/run))
