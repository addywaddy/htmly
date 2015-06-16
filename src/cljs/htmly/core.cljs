(ns htmly.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cognitect.transit :as transit]
            [alandipert.storage-atom :refer [local-storage load-local-storage]]))

(enable-console-print!)

(defn console-log [obj]
  (.log js/console obj))

(defn raw-html [content]
  (om.dom/div #js {:dangerouslySetInnerHTML #js {:__html content}} nil))

(defn input-width [text]
  (let [char-count (aget (str text) "length")
        weighting 0.61
        padding 0.1]
    (if (> char-count 0)
      (+ padding (* weighting char-count))
      (+ padding weighting))))

(defn text-area-height [text]
  (let [rows (.ceil js/Math (/ (input-width text) 50))
        row-height 1.43]
    (* row-height rows)))

(defn editable-textarea [ctx owner]
  (reify
  om/IRender
  (render [this]
    (dom/textarea #js {:className ""
                       :onChange (fn [e] (om/transact! ctx [0], (fn [_] (.. e -target -value))))
                       :value (first ctx)
                       :style #js {:width "50em" :height (str (text-area-height (first ctx)) "em")}
                       }))))

(defn editable-input [ctx owner]
  (reify
    om/IRender
    (render [this]
      (dom/span nil
                (dom/input #js {:id (str "input-" root-node-id)
                                :className ""
                                :onChange (fn [e] (om/transact! ctx [0] (fn [_] (.. e -target -value))))
                                :value (first ctx)
                                :style #js {:width (str (input-width (first ctx)) "em")}})))))

(defn change-body-color [color]
  (let [body (aget js/document "body")]
    (aset body "className" color))
  false)

(defn no-local-storage [function]
  function)

(def default-data {:show-help true
                               :columns [[{"background" ["white"]}
                                          {"image" [""]}
                                          {"title" ["Deine Name"]}
                                          {"paragraph" ["Schreibe etwas über dich, das dich grob beschreibt. Es muss nicht allzu lang sein, aber genug um einen Eindruck von dir zu bekommen."]}
                                          {"table" {"items" [
                                                          [["Alter"] ["10"]]
                                                          [["Große"] ["1,30m"]]
                                                          [["Haarfarbe"] ["Braun"]]
                                                          [["Augenfarbe"] ["Blau"]]]}}]

                                         [{"thumbs-up" {
                                                  "icon" "thumbs-up"
                                                  "title" ["Daumen Hoch"]
                                                  "intro" ["Diese Sachen finde ich cool:"]
                                                  "items" [
                                                         ["One"]
                                                         ["Two"]
                                                         ["Three"]]}}
                                          {"thumbs-down" {
                                                  "icon" "thumbs-down"
                                                  "title" ["Daumen Runter"]
                                                  "intro" ["Diese Sachen finde ich schlecht:"]
                                                  "items" [
                                                         ["One"]
                                                         ["Two"]
                                                         ["Three"]]}}
                                          {"music" {
                                                  "icon" "music"
                                                  "title" ["Meine Lieblingslieder"]
                                                  "intro" ["Diese Songs sind der Hammer:"]
                                                  "items" [
                                                         ["One"]
                                                         ["Two"]
                                                         ["Three"]]}}
                                          {"film" {
                                                  "icon" "film"
                                                  "title" ["Meine Lieblingsfilme"]
                                                  "intro" ["Diese Filme sind genial:"]
                                                  "items" [
                                                         ["One"]
                                                         ["Two"]
                                                         ["Three"]]}}]

                                         [{"links" {
                                                  "icon" "globe"
                                                  "title" ["Meine Lieblings-Webseiten"]
                                                  "intro" ["Diese Seiten mag ich:"]
                                                  "items" [
                                                         [["One"] ["http://www.youtube.com"]]
                                                         [["Two"] ["http://www.google.com"]]
                                                         [["Three"] ["http://www.kika.de"]]]}}

                                          {"form" {"title" ["Quiz"]
                                                  "intro" ["Rate mal, was mein Lieblingstier ist!"]
                                                  "items" [
                                                          ["Elefant"]
                                                          ["Giraffe"]
                                                          ["Tiger"]
                                                          ["Schlange"]
                                                          ["Hai"]]
                                                  "answer" 1
                                                  "button" ["Raten"]}}]]})

(def app (local-storage (atom default-data) "data"))

(defn edit-title-and-intro [details]
  (dom/span nil
            (dom/span nil "<h3>\n  ")
            (om/build editable-input (details "title"))
            (dom/span nil "\n<h3>\n")
            (dom/span nil "<p>\n  ")
            (om/build editable-textarea (details "intro"))
            (dom/span nil "\n<p>\n")))

(defn source-code [& args]
  (apply dom/pre #js {:className "text-primary"} args))

(defn background-thumbnail [details classname]
  (dom/span #js {:className "background-thumbnails"}
                          (dom/a #js {:href "#" :className "thumbnail background"}
                                 (dom/div #js {:className classname :onClick (fn [_]
                                                                               (om/update! details 0, classname)
                                                                               (change-body-color classname)
                                                                               false)}))))

(def background-text "
<h3>Farben</h3>
<p>
  Der Hintergrund deiner Website muss nicht weiß sein. Klicke auf eine der folgenden Farben und sieh was passiert:
</p>")

(defn background [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (dom/div nil
                 (raw-html background-text)
                 (background-thumbnail details "pink")
                 (background-thumbnail details "green")
                 (background-thumbnail details "blue")
                 (background-thumbnail details "grey")
                 (background-thumbnail details "white"))))))

(def image-text-1 "
<h3>Bild</h3>
<p>
  Was wäre eine Webseite ohne Bilder? Langweilig. Und was wäre eine Webseite über dich ohne ein Bild von dir? Dein Laptop hat eine eingebaute Kamera, die wir jetzt gleich verwenden werden.
</p>")

(def image-text-2 "
<p>
  Jetzt siehst du hoffentlich etwas. Man, siehst du gut aus! Sobald du bereit bist ...
</p>")

(def image-text-3"
<p>
 <em>(Pssst! Falls du mit deinem Selfie noch nicht zufrieden bist, probiere es einfach nochmal.)</em>
</p>")

(defn take-selfie [details]
  (if (aget js/Webcam "loaded")
    (.snap js/Webcam (fn [data-uri] (om/update! details 0, data-uri) (.reset js/Webcam)))
    (.alert js/window "Du musst zuerst die Kamera starten!")
    )
  )

(defn image [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (dom/div nil
                 (raw-html image-text-1)
                 (dom/p nil (dom/button #js {:className "btn btn-primary" :onClick (fn [e] (.attach js/Webcam "#image-preview")) } "Kamera starten!"))
                 (raw-html image-text-2)
                 (dom/p nil (dom/button #js {:className "btn btn-primary" :onClick (fn [e] (take-selfie details)) } "Selfie speichern!"))
                 (raw-html image-text-3))
        (dom/div #js {:style #js {:position "relative"}}
                 (dom/img #js {:className "img-rounded" :src (first details) :width "360px" :height "360px"})
                 (dom/div #js {:id "image-preview"}))))))

(def title-text "
<h3>
  Titeln
</h3>
<p>
  So. Farbe festgelegt, Bild eingestellt. Jetzt fangen wir an, endlich mal HTML zu schreiben! Siehst du die graue Box unten? So sieht HTML aus.
  'h1' steht für 'heading eins' und wird normalerweise für den Hauptitel einer Webseite genutzt. So ist HTML meistens gebaut – es gibt einen öffnenden 'Tag' <code>&lt;h1&gt;</code> und einen schließenden Tag <code>&lt;&#8260;h1&gt;</code>. Zwischen diesen Tags befindet sich der Inhalt.
</p>
<p>
  Momentan steht da 'Deine Name', aber du heißt sicherlich anders. Probiere jetzt, diesen Text zu ändern und schaue, was auf der rechte Seite passiert!
</p>")

(defn title [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (dom/div nil
                 (raw-html title-text)
                 (source-code
                  (dom/span nil "<h1>\n  ")
                  (om/build editable-input details)
                  (dom/span nil "\n</h1>")))
        (dom/h2 nil (first details))))))

(def paragraph-text "
<h3>
  Absätze
</h3>
<p>
  Der nächste HTML Tag, den wir anschauen ist der <code>p</code>-Tag. Warum <code>p</code>, wenn es sich um Absätze handelt? Alle HTML-Tags stehen für ein englisches Wort, und Absatz heißt auf englisch … na, das kannst du bestimmt selbst herausfinden :)
</p>
<p>
  Ändere den Text unten und schreibe etwas über dich selbst.
</p>")

(defn paragraph [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (dom/div nil
                 (raw-html paragraph-text)
                 (source-code
                  (dom/span nil "<p>\n  ")
                  (om/build editable-textarea details)
                  (dom/span nil "\n</p>")))
        (dom/p #js {:className "lead"} (first details))
        (dom/p #js {:className "lead"} (first details))))))

(defn table-row [row owner]
  (reify
    om/IRender
    (render [this]
      (dom/tr nil
       (dom/th nil (-> row first first))
       (dom/td nil (-> row last first))))))

(defn table-row-help [row owner]
  (reify
    om/IRender
    (render [this]
      (dom/span nil
                (dom/span nil "    <tr>\n")
                (dom/span nil "      <th>")
                (om/build editable-input (first row))
                (dom/span nil "</th>\n")
                (dom/span nil "      <td>")
                (om/build editable-input (last row))
                (dom/span nil "</td>\n")
                (dom/span nil "    </tr>\n")))))

(def table-text "
<h3>
  Tabellen
</h3>
<p>
  Vielleicht kennst du schon das Programm 'Excel', dann weißt du wahrscheinlich auch schon was eine Tabelle ist. Du kannst Tabellen auch in HTML beschreiben, wie du unten siehst (auf der rechte Seite ist der Vorschau). Um eine Tabelle zu definieren braucht man mehrere verschachtelte HTML-Tags:
</p>
<ul>
  <li><code>table</code> : englisch für Tabelle</li>
  <li><code>tbody</code> : Table Body</li>
  <li><code>tr</code> : Table Row (Zeile)</li>
  <li><code>th</code> : Table Header (Tabellenkopf)</li>
  <li><code>td</code> : Table Data (Tabellenzelle)</li>
</ul>
<p>
  Passe die Tabelle nun mit Infos über dich an:
</p>")

(defn table [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (dom/div nil
                 (raw-html table-text)
                 (source-code
                  (dom/span nil "<table>\n")
                  (dom/span nil "  <tbody>\n")
                  (apply
                   dom/span nil
                   (om/build-all table-row-help (details "items")))
                  (dom/span nil "  </tbody>\n")
                  (dom/span nil "</table>")))
        (dom/table #js {:className "table table-bordered"}
                   (apply
                    dom/tbody nil
                    (om/build-all table-row (details "items"))))))))

(defn list-item [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/li nil (first details)))))

(defn link-list-item [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/li nil
              (dom/a #js {:href (-> details last first)} (-> details first first))))))

(defn list-item-help [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/span nil
                (dom/span nil "  <li>")
                (om/build editable-input details)
                (dom/span nil "</li>\n")))))

(defn link-list-item-help [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/span nil
                (dom/span nil "  <li>\n")
                (dom/span nil "    <a href='")
                (om/build editable-input (last details))
                (dom/span nil "'>")
                (om/build editable-input (first details))
                (dom/span nil "</a>\n")
                (dom/span nil "</li>\n")))))

(defn list-help [details tag list-item-component]
  (dom/span nil
            (dom/span nil (str "<" tag ">\n")
                      (apply
                       dom/span nil
                       (om/build-all list-item-component (details "items")))
                      (dom/span nil (str "</" tag ">")))))

(defn icon-title [details]
  (dom/h3 nil
          (dom/span #js {:className (str "glyphicon glyphicon-" (details "icon"))} "")
          (dom/span nil (str " " (-> (details "title") first)))))

(def ulist-text-1 "
<h3>
  Unsortierte Listen
</h3>
<p>
  Unsortierte Listen bestehen aus zwei HTML-Tags:
</p>
<ul>
  <li><code>ul</code> : 'Unsorted List'</li>
  <li><code>li</code> : 'List Item'</li>
</ul>
<p>
  Was findest du gut? Passe die Liste für dich an! Du kannst auch den Titel und Absatz ändern.
</p>")

(def ulist-text-2 "
<p>
  Und was findest du schlecht?
</p>")

(defn ulist [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (dom/div nil
                 (if (= (details "icon") "thumbs-up")
                   (raw-html ulist-text-1)
                   (raw-html ulist-text-2))
                 (source-code
                  (edit-title-and-intro details)
                  (list-help details "ul" list-item-help)))
        (dom/div nil
                 (icon-title details)
                 (dom/p nil (-> (details "intro") first))
                 (apply
                  dom/ul nil
                  (om/build-all list-item (details "items"))))))))

(def olist-text-1 "
<h3>
  Sortierte Listen
</h3>
<p>
  Sortierte Listen bestehen wie unsortierte Listen aus 2 HTML-Tags. Sie sind aber numeriert.
</p>
<p>
  Was sind deine drei Lieblingslieder?
</p>")

(def olist-text-2 "
<p>
  Und deine drei Lieblingsfilme?
</p>")

(defn olist [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (dom/div nil
                 (if (= (details "icon") "music")
                   (raw-html olist-text-1)
                   (raw-html olist-text-2))
                 (source-code
                  (edit-title-and-intro details)
                  (list-help details "ul" list-item-help)))
        (dom/div nil
                 (icon-title details)
                 (dom/p nil (-> (details "intro") first))
                 (apply
                  dom/ol nil
                  (om/build-all list-item (details "items"))))))))

(def linklist-text "
<h3>
  Links
</h3>
<p>
  Ohne links wäre das Internet wie ein Straßennetz voller Sackgassen. Links ermöglichen es, von einer Webseite zur nächsten zu springen. Ganz einfach, indem man auf sie klickt. Der HTML-Tag für Links ist der <code>a</code>-Tag und steht für 'Anchor', das englische Wort für Anker. Ein <code>a</code>-Tag hat Inhalt, aber meistens auch ein Attribut (eine Besonderheit), das festlegt, wo der Link hinführen soll. Dieses Attribut heisst <code>href</code>, was für 'Hypertext Reference' steht.
</p>
<p>
  Hast du 3 Lieblingswebseiten? Dann trage sie hier ein und versuche den <code>href</code> korrekt zu setzen. Wenn alles richtig ist, solltest du einfach auf den Link klicken können und dann auf der richtigen Seite landen. Und wie kommst du dann wieder hierher zurück? Mit dem 'Zürück-Button' (meistens ein Pfeil oben link im Browser-Fenster) deines Browsers :)
</p>")

(defn linklist [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (dom/div nil
                 (raw-html linklist-text)
                 (source-code
                  (edit-title-and-intro details)
                  (list-help details "ol" link-list-item-help)))
        (dom/div nil
                 (icon-title details)
                 (dom/p nil (-> (details "intro") first))
                 (apply
                  dom/ol nil
                  (om/build-all link-list-item (details "items"))))))))

(defn checkbox-help [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
                (dom/span nil "  <label>\n    <input type='radio' name='quiz'/>\n    ")
                (om/build editable-input details)
                (dom/span nil "\n  </label>\n")))))

(defn checkbox [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "radio"}
               (dom/label nil
                          (dom/input #js {:type "radio" :name "answer" :value (first details)})
                          (-> details last first))))))

(defn button-help [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/span nil
                (dom/span nil "  <button>")
                (om/build editable-input details)
                (dom/span nil "</button>")))))

(defn form-help [details]
  (dom/span nil
            (dom/span nil (str "<form>\n")
                      (apply
                       dom/span nil
                       (om/build-all checkbox-help (details "items")))
                      (om/build button-help (details "button"))
                      (dom/span nil (str "\n</form>")))))

(defn option-tag [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/option #js {:value (first details)} (-> details last first)))))

(def form-text-1 "
<h3>Formulare</h3>
<p>
  Wir sind gleich fertig mit deiner Webseite! Zum Schluss werden wir ein einfaches Quiz bauen. Dafür werden wir ein Formular verwenden. Ein Formular im HTML hast du bestimmt schon einmal gesehen, 
  wenn du im internet dich irgendwo anmeldest oder etwas suchst. Dann schreibst du in ein Eingabefeld und klickst danach auf einen Button (der z.B. die Suche startet). In unserem Formular gibt es keine Eingabefelder. Dafür aber folgendes:
</p>
<ul>
  <li><code>input</code></li>
  <li><code>button</code></li>
  <li><code>select</code></li>
</ul>
<p>
  Wir verwenden eine besondere Art von <code>input</code>s — sogenannte 'Radio Buttons'. Mit 'Radio Buttons' kann man aus einer Liste eine Auswahl treffen. Das eignet sich perfekt für unser Quiz.
</p>
<p>
  Worum handelt es sich bei deinem Quiz? Du kannst die Frage und möglichen Antworten jetzt anpassen:
</p>")

(def form-text-2 "
<p>
  Jetzt braucht dein Quiz eine Lösung! Wähle jetzt die Antwort aus:
</p>")

(def form-text-3 "
<p>
  Probiere dein Quiz gleich einmal aus! Funktioniert es richtig?
</p>")

(defn form [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (let [indexed-details (map-indexed (fn [idx itm] [idx itm]) (details "items"))]
        (if (:help state)
          (dom/div nil
                   (raw-html form-text-1)
                   (source-code
                    (edit-title-and-intro details)
                    (form-help details))

                   (raw-html form-text-2)
                   (dom/div #js {:className "form-group"}
                            (dom/label #js {:className "control-label"} "Die Lösung:")
                            (apply
                             dom/select #js {:className "form-control"
                                             :value (details "answer")
                                             :onChange (fn [e] (om/transact! details "answer" (fn [_] (.. e -target -value))))}
                             (om/build-all option-tag indexed-details)))
                   (raw-html form-text-3))
          (dom/form #js {:onSubmit js/runQuiz}
                    (dom/h3 nil (-> (details "title") first))
                    (dom/p nil (-> (details "intro") first))
                    (dom/input #js {:type "hidden" :name "quiz-answer" :value (details "answer")})
                    (apply
                     dom/fieldset nil
                     (om/build-all checkbox indexed-details))
                    (dom/button #js {:type "submit" :className "btn btn-default"} (-> (details "button") first))))))))

(def step-lookup
  {"background" background
   "image" image
   "title" title
   "paragraph" paragraph
   "table" table
   "thumbs-up" ulist
   "thumbs-down" ulist
   "music" olist
   "film" olist
   "links" linklist
   "form" form})

(defn step [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (let [step-name (-> details keys first)]
        (om/build (step-lookup step-name) (details step-name) {:init-state state})))))

(defn column [steps owner]
  (reify
    om/IRender
    (render [this]
      (apply
       dom/div #js {:className "col-md-4"}
       (om/build-all step steps)))))

(defn help-text [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil (raw-html (details :help))))))

(defn step-with-help [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "row section"}
               (dom/div #js {:className "col-md-8"}
                        (om/build step details {:init-state {:help true}}))
               (dom/div #js {:className "col-md-4 preview"}
                        (om/build step details))))))

(defn page->html []
  (aget (.getElementById js/document "page") "innerHTML"))

(def json-writer (transit/writer :json-verbose))

(defn download-href []
  (let [json (transit/write json-writer ((load-local-storage "data") :columns))]
    (str "data:application/json;charset=utf-8," (clojure.string/replace json " " "\u00a0"))))

(defn download-page [e]
  (let [a-tag (.. e -target)]
    (aset a-tag "download" "website.json")
    (aset a-tag "href" (download-href))))

(defn preview-or-tutorial [app]
  (dom/li nil
          (dom/button #js {:className "btn btn-success navbar-btn" :onClick (fn [e] (om/transact! app :show-help (fn [bool] (not bool))) false)}
                 (if (app :show-help)
                   "Website"
                   "Tutorial"))))

(defn download-menu-item []
  (dom/li nil
          (dom/a #js {:href "#" :className "btn-download btn btn-success navbar-btn" :onClick download-page}
                 "Herunterladen")))

(defn reset-page [e]
  (reset! app default-data)
  false
  )

(defn reset-menu-item []
  (dom/li nil
          (dom/button #js {:className "btn btn-success navbar-btn" :onClick reset-page}
                 "Zurücksetzen")))

(defn update-storage [json]
  (let [transit-reader (transit/reader :json)
        user-data (transit/read transit-reader (clojure.string/replace json "\u00a0" " "))
        new-data (merge default-data {:columns user-data})]
    (swap! app assoc :columns user-data)
    ))

(defn handle-upload [e]
  (let [file-field (.. e -target)
        file (aget file-field "files" 0)
        file-reader (js/FileReader.)
        content (.readAsText file-reader file)]
    (set! (.-onload file-reader) (fn [] (update-storage (aget file-reader "result"))))
    false))

(defn upload-menu-item []
  (dom/li nil
          (dom/div #js {:className "btn-file btn btn-success navbar-btn"}
                   (dom/form #js {:method "post" :encType "multipart/form-data"}
                             (dom/input #js {:type "file" :name "data" :className "file-input" :onChange handle-upload}))
                   (dom/span nil "Hochladen")
                   )))

(defn menu-view [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/nav #js {:className "navbar navbar-default navbar-fixed-top"}
               (dom/div #js {:className "collapse navbar-collapse"}
                        (dom/div #js {:className "container"}
                                 (dom/ul #js {:className "nav navbar-nav"}
                                         (preview-or-tutorial app)
                                         (download-menu-item)
                                         (upload-menu-item)
                                         (reset-menu-item))))))))

(def intro-text "
<div class='row'>
  <div class='col-md-12'>
    <h1>Bau dir deine eigene Website!</h1>
    <p class='lead'>
      Durch dieses Tutorial wirst du lernen, wie einfach es ist, eine Website zu bauen und dass es gar nicht so langweilig ist, wie du vielleicht denkst!
    </p>
    <p class='lead'>
      Wenn du ins Internet gehst, verwendest du eine bestimmte Art Programm: einen <strong>Browser</strong>. Du kennst wahrscheinlich <em>Firefox, Chrome, Safari</em> oder <em>Internet Explorer</em>.
      Dein Browser fragt zum Beispiel Google nach deren Startseite. Google schickt dann eine Antwort zurück. Diese Antwort beinhaltet
    </p>
    <ul class='lead'>
      <li>Ein Ja oder Nein, je nach dem ob die Seite überhaupt existiert und</li>
      <li>Die Inhalte der Seite</li>
    </ul>
    <p class='lead'>
      Die Inhalte sind in einer bestimmten Sprache geschrieben, die HTML heisst. HTML steht für 'HyperText Markup Language'. Diese Sprache sagt deinem Browser, dass er z.B. einen Titel oder ein Bild anzeigen soll.
    </p>
    <p class='lead'>
      Gleich wirst du selbst etwas HTML schreiben, aber zuerst muss du eine Farbe für deine Website auswählen und ein Selfie machen :)
    </p>
    <p class='lead'>
      <em>Los gehts!</em>
    </p>
  </div>
</div>")

(def outro-text "
<div class='row'>
  <div class='col-md-12'>
    <h1>Geschafft!</h1>
    <p class='lead'>
      Deine Seite ist nun fertig. Ganz oben in der Navigation sind 4 Buttons:
    </p>
    <ul class='lead'>
      <li>
        <strong>Website</strong>: um deine Website anzuschauen.
      </li>
      <li>
        <strong>Herunterladen</strong>: um deine Seite zu speichern.
      </li>
      <li>
        <strong>Hochladen</strong>: um deine gespeicherte Seite zu verwenden.
      </li>
      <li>
        <strong>Zurücksetzen</strong>: um deine Änderungen zu löschen, wenn du vielleicht noch einmal von vorne beginnen möchtest.
      </li>
    </p>
  </div>
</div>")

(defn set-background-color [app]
  (let [background ((-> app :columns first first) "background")
            color (first background)]
        (change-body-color color)))

(defn tutorial-view [app owner]
  (reify
    om/IRender
    (render [this]
      (set-background-color app)
      (let [current-step (:current-step app)
            cols (:columns app)
            steps (-> cols flatten vec)]
        (dom/div nil
                 (om/build menu-view app)
                 (if (:show-help app)
                   (dom/div #js {:className "tutorial"}
                            (raw-html intro-text)
                            (apply
                             dom/div nil
                             (om/build-all step-with-help steps))
                            (raw-html outro-text))
                   (apply
                    dom/div #js {:className "row site" :id "site"}
                    (om/build-all column (:columns app)))))))))

(defn main []


  (om/root
    tutorial-view
    app
    {:target (. js/document (getElementById "app"))}))
