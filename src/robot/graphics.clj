;; GRAPHICS 

(def startField (text ""))
(def endField (text ""))
(def mainFrame (frame :title "Beep doop beep, Im a robot!"))
(def startButton (button :text "start"
                         :listen [:action (fn [e] (prn (getShortestRoute ["o121" "o111"])))]))
(def addPackageButton (button :text "Add"
                        :listen [:action (fn [e] (addPackage (text startField) (text endField)))]))

(def top-panel
  "Used to select the world, dimension and anvil file to view"
  (horizontal-panel
     :items [(label :text "Start") startField 
             (label :text "Target") endField
              addPackageButton]))

(defn addToFrame 
  "Takes content and displays in it container"
  [container content]
  (config! container :content content))

;; Functions
(defn displaySetup 
  []
  (native!)
  (-> mainFrame pack! show!)
  (addToFrame mainFrame (border-panel :north top-panel
                                      :center (label :icon (clojure.java.io/resource "rsz_map.png")
                                                     :paint (fn [c g] (.fillRect g 210 180 15 15)))
                                      :vgap 5 :hgap 5 :border 5)))
