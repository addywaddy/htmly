(ns htmly.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [alandipert.storage-atom :refer [local-storage]]))

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
                               :columns [[{:background ["white"]}
                                          {:image [""]}
                                          {:title ["Deine Name"]}
                                          {:paragraph ["Etwas text über dich, der dich grob beschreibt. Es muss nicht all zu lang sein, aber genug um einen Eindruck von dir zu bekommen."]}
                                          {:table {:items [
                                                          [["Alter"] ["10"]]
                                                          [["Große"] ["1,30m"]]
                                                          [["Haarfarbe"] ["Braun"]]
                                                          [["Augenfarbe"] ["Blau"]]]}}]

                                         [{:thumbs-up {
                                                  :icon "thumbs-up"
                                                  :title ["Daumen Hoch"]
                                                  :intro ["Diese Sachen finde ich cool:"]
                                                  :items [
                                                         ["One"]
                                                         ["Two"]
                                                         ["Three"]]}}
                                          {:thumbs-down {
                                                  :icon "thumbs-down"
                                                  :title ["Daumen Runter"]
                                                  :intro ["Diese Sachen finde ich schlecht:"]
                                                  :items [
                                                         ["One"]
                                                         ["Two"]
                                                         ["Three"]]}}
                                          {:music {
                                                  :icon "music"
                                                  :title ["Mein Top Lieder"]
                                                  :intro ["Diese Songs finde ich der Hammer:"]
                                                  :items [
                                                         ["One"]
                                                         ["Two"]
                                                         ["Three"]]}}
                                          {:film {
                                                  :icon "film"
                                                  :title ["Mein Top Filme"]
                                                  :intro ["Diese Filme sind genial:"]
                                                  :items [
                                                         ["One"]
                                                         ["Two"]
                                                         ["Three"]]}}]

                                         [{:links {
                                                  :icon "globe"
                                                  :title ["Meine Top Webseiten"]
                                                  :intro ["Diese Seiten mag ich:"]
                                                  :items [
                                                         [["One"] ["http://www.youtube.com"]]
                                                         [["Two"] ["http://www.google.com"]]
                                                         [["Three"] ["http://www.kika.de"]]]}}

                                          {:form {:title ["Quiz"]
                                                  :intro ["Rate mal, was mein Lieblingstier ist"]
                                                  :items [
                                                          ["Elefant"]
                                                          ["Giraffe"]
                                                          ["Tiger"]
                                                          ["Schlange"]
                                                          ["Hai"]]
                                                  :answer 1
                                                  :button ["Raten"]}}]]})

(def app (no-local-storage (atom default-data)))

(defn edit-title-and-intro [details]
  (dom/span nil
            (dom/span nil "<h3>\n  ")
            (om/build editable-input (:title details))
            (dom/span nil "\n<h3>\n")
            (dom/span nil "<p>\n  ")
            (om/build editable-textarea (:intro details))
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
  Deine Webseite muss nicht einen weissen Hintergrund haben. Klicke auf einer der folgenden Farben und sehe was passiert:
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
  Was wäre eine Webseite ohne Bilder? Langweilig. Und was wäre eine Webseite über dich ohne dein Bild? Dein Laptop hat einen eingebauten Kamera, die wir jetzt gleich verwenden werden.
</p>")

(def image-text-2 "
<p>
  Jetzt siehst du hoffentlich etwas. Man, sieht du gut aus heute! Sobald du bereit bist ...
</p>")

(def image-text-3"
<p>
 <em>(Pssst! Falls du noch nicht mit deinem Foto zufrieden bist, kannst du mehrmals probieren)</em>
</p>")

(defn image [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (dom/div nil
                 (raw-html image-text-1)
                 (dom/p nil (dom/button #js {:className "btn btn-primary" :onClick (fn [e] (.attach js/Webcam "#image-preview")) } "Kamera starten!"))
                 (raw-html image-text-2)
                 (dom/p nil (dom/button #js {:className "btn btn-primary" :onClick (fn [e] (.snap js/Webcam (fn [data-uri] (om/update! details 0, data-uri) (.reset js/Webcam)))) } "Selfie speichern!"))
                 (raw-html image-text-3))
        (dom/div #js {:style #js {:position "relative"}}
                 (dom/img #js {:className "img-rounded" :src (first details) :width "360px" :height "360px"})
                 (dom/div #js {:id "image-preview"}))))))

(def title-text "
<h3>
  Titeln
</h3>
<p>
  So. Farbe festgelegt, Bild eingestellt. Jetzt fangen wir an, endlich mal HTML zu schreiben! Siehst du das graue Box unten? So sieht HTML aus.
  'h1' steht für 'Header eins' und ist normalerweise für den Hauptitel eine Webseite gedacht. HTML ist meistens so gebaut. Es gibt einen öffnende 'Tag', also hier
<code>&lt;h1&gt;</code>, dann der Inhalt, und zum Schluss einen schliessende 'Tag', hier <code>&lt;&#8260;h1&gt;</code>.
</p>
<p>
  'Deine Name' steht da momentan, aber du heisst sicherlich anders. Probier mal jetzt diesen Text zu ändern und schaue, was auf der rechte Seite passiert!
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
  Der nächste HTML Tag, den wir anschauen ist der <code>p</code>-Tag. Warum <code>p</code>, wenn es sich um Absätze handelt? Alle HTML-Tags stehen für einen englischen Wort, und Absatz auf englisch lautet ... na, dass kannst du selber herausfinden :)
</p>
<p>
  Ändere den Text unten, und schreibe etwas über dich selbst.
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
  Kennst du bereits Excel? Dannn weisst du wahrscheinlich schon was eine Tabelle ist. Man kann auch Tabellen in HTML beschreiben, wie du unten siehst (auf der rechte Seite ist der Vorschau). Um eine Tabelle zu definieren braucht man mehrere verschachtelte HTML-Tags:
</p>
<ul>
  <li><code>table</code> : englisch für Tabelle</li>
  <li><code>tbody</code> : Table Body</li>
  <li><code>tr</code> : Table Row (Zeile)</li>
  <li><code>th</code> : Table Header</li>
  <li><code>td</code> : Table Data</li>
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
                   (om/build-all table-row-help (details :items)))
                  (dom/span nil "  </tbody>\n")
                  (dom/span nil "</table>")))
        (dom/table #js {:className "table table-bordered"}
                   (apply
                    dom/tbody nil
                    (om/build-all table-row (details :items))))))))

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
                       (om/build-all list-item-component (details :items)))
                      (dom/span nil (str "</" tag ">")))))

(defn icon-title [details]
  (dom/h3 nil
          (dom/span #js {:className (str "glyphicon glyphicon-" (:icon details))} "")
          (dom/span nil (str " " (-> details :title first)))))

(def ulist-text-1 "
<h3>
  Unsortierte Listen
</h3>
<p>
  Unsortierte Listen bestehen aus 2 HTML-Tags:
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
                 (if (= (details :function) :thumbs-up)
                   (raw-html ulist-text-1)
                   (raw-html ulist-text-2))
                 (source-code
                  (edit-title-and-intro details)
                  (list-help details "ul" list-item-help)))
        (dom/div nil
                 (icon-title details)
                 (dom/p nil (-> details :intro first))
                 (apply
                  dom/ul nil
                  (om/build-all list-item (-> details :items))))))))

(def olist-text-1 "
<h3>
  Sortierte Listen
</h3>
<p>
  Sortierte Listen sind wie unsortierte Listen, indem sie aus 2 HTML-Tags bestehen. Sie sind aber numeriert.
</p>
<p>
  Was sind deine Top Drei Lieblingslieder?
</p>")

(def olist-text-2 "
<p>
  Und deine Top Drei Lieblingsfilme?
</p>")

(defn olist [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (if (:help state)
        (dom/div nil
                 (if (= (details :function) :music)
                   (raw-html olist-text-1)
                   (raw-html olist-text-2))
                 (source-code
                  (edit-title-and-intro details)
                  (list-help details "ul" list-item-help)))
        (dom/div nil
                 (icon-title details)
                 (dom/p nil (-> details :intro first))
                 (apply
                  dom/ol nil
                  (om/build-all list-item (-> details :items))))))))

(def linklist-text "
<h3>
  Links
</h3>
<p>
  Ohne links wäre das Internet wie eine Straßennetz volle Sackgassen. Links ermöglichen es, dass man von eine Seite zur Nächste springen kann, in dem man auf sie einfach klickt. Der HTML-Tag für Links ist der <code>a</code>-Tag, und steht für 'Anchor', dass englische Wort für Anker. Ein <code>a</code>-Tag hat Inhalt, aber auch meistens einen Attribut, also eine Besonderheit, um festzulegen, wo er hinführen soll. Dieses Attribut heisst <code>href</code>, was für 'Hypertext Reference' steht.
</p>
<p>
  Hast du 3 Lieblingswebseiten? Dann trage sie hier ein, und versuche, den <code>href</code> korrekt zu setzen. Wenn alles richtig ist, solltest du einfach darauf klicken, und du wird auf die Seite landen. Und wie kommst du den wieder hierher? Anhand der 'Back Button' deines Browsers :)
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
                 (dom/p nil (-> details :intro first))
                 (apply
                  dom/ol nil
                  (om/build-all link-list-item (-> details :items))))))))

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
                       (om/build-all checkbox-help (details :items)))
                      (om/build button-help (:button details))
                      (dom/span nil (str "\n</form>")))))

(defn option-tag [details owner]
  (reify
    om/IRender
    (render [this]
      (dom/option #js {:value (first details)} (-> details last first)))))

(def form-text-1 "
<h3>Formulare</h3>
<p>
  Wir sind gleich fertig mit deine Webseite! Zum Schluss werden wir einen einfachen Formular bauen, im Form eines Quizes. Einen Formular im HTML kennst du sicherlich schon.
  Wenn du im internet dich irgendwo anmeldest oder suchst, schreibst du in einen Eingabefeld und klickst danach auf einen Button. In unsere Formular sind keine Eingabefelde vorhanden, dafür aber folgende:
</p>
<ul>
  <li><code>input</code></li>
  <li><code>button</code></li>
  <li><code>select</code></li>
</ul>
<p>
  Wir verwenden ein besonder Art von <code>input</code>s hier - sogenannte 'Radio Buttons'. Mit 'Radio Buttons' kann man aus eine Liste einen Auswahl treffen, was sich perfekt für unsere Quiz eignet.
</p>
<p>
  Worum handelt es sich bei deinem Quiz? Du kannst die Frage und möglichen Antworten jetzt anpassen:
</p>")

(def form-text-2 "
<p>
  Jetzt brauch deinen Quiz eine Lösung! Wähle jetzt der Antwort aus:
</p>")

(def form-text-3 "
<p>
  Probiere den jetzt aus! Funktioniert sie richtig?
</p>")

(defn form [details owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (let [indexed-details (map-indexed (fn [idx itm] [idx itm]) (-> details :items))]
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
                                             :value (-> details :answer)
                                             :onChange (fn [e] (om/transact! details :answer (fn [_] (.. e -target -value))))}
                             (om/build-all option-tag indexed-details)))
                   (raw-html form-text-3))
          (dom/form #js {:onSubmit js/runQuiz}
                    (dom/h3 nil (-> details :title first))
                    (dom/p nil (-> details :intro first))
                    (dom/input #js {:type "hidden" :name "quiz-answer" :value (details :answer)})
                    (apply
                     dom/fieldset nil
                     (om/build-all checkbox indexed-details))
                    (dom/button #js {:type "submit" :className "btn btn-default"} (-> details :button first))))))))

(def step-lookup
  {:background background
   :image image
   :title title
   :paragraph paragraph
   :table table
   :thumbs-up ulist
   :thumbs-down ulist
   :music olist
   :film olist
   :links linklist
   :form form})

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

(defn page-source-as-href [e]
  (let [a-tag (.. e -target)]
    (aset a-tag "href" (str "data:text/plain;charset=utf-8," (aset js/window "encodeURIComponent" (page->html)))))
  false)

(defn preview-or-tutorial [app]
  (dom/li nil
          (dom/a #js {:href "#" :onClick (fn [e] (om/transact! app :show-help (fn [bool] (not bool))) false)}
                 (if (app :show-help)
                   "Zur Webseite"
                   "Züruck zum Tutorial"))))

(defn download-menu-item []
  (dom/li nil
          (dom/a #js {:download "appp.html" :href "#" :onClick (fn [e] page-source-as-href)}
                 "Speichern")))

(defn menu-view [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/nav #js {:className "navbar navbar-default navbar-fixed-top"}
               (dom/div #js {:className "collapse navbar-collapse"}
                        (dom/div #js {:className "container"}
                                 (dom/ul #js {:className "nav navbar-nav"}
                                         (preview-or-tutorial app)
                                         (download-menu-item))))))))

(def intro-text "
<div class='row'>
  <div class='col-md-12'>
    <h1>Bau dir deine Website!</h1>
    <p class='lead'>
      Durch diesen Tutorial wirst du lernen, wie einfach es ist, eine Website zu bauen, und dass es gar nicht so langweilig ist, wie man denkt!
    </p>
    <p class='lead'>
      Wenn du ins Internet gehts, verwendest du einen bestimmte Art Program: einen <strong>Browser</strong>. Du kennst sie wahrscheinlich als <em>Firefox, Chrome, Safari</em> oder <em>Internet Explorer</em>. Dein Browser fragt zum Beispiel Google nach deren Startseite, und Google schickt einen Antwort zurück. Dieses Antwort beinhaltet
    </p>
    <ul class='lead'>
      <li>Einen Ja oder Nein, je nach dem ob die Seite überhaupt existiert; und</li>
      <li>Die Inhalte der Seite</li>
    </ul>
    <p class='lead'>
      Die Inhalte werden in eine bestimmte Sprache geschrieben, die HTML heisst. HTML steht für 'HyperText Markup Language', und sagt deinen Browser dass er z.B. einen Titel oder einen Bild anzeigen soll.
    </p>
    <p class='lead'>
      Du wirst gleich etwas HTML schreiben, aber zuerst muss du eine Farbe für deine Seite auswählen und einen Selfie machen.
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
      Deine Seite ist nun fertig. Du kannst die anhand der Link 'zur Webseite' anschauen und als vollständige Webseite herunterladen. Gratuliere!
    </p>
  </div>
</div>")

(defn tutorial-view [app owner]
  (reify
    om/IRender
    (render [this]
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
  (let [color (-> @app :columns first first first)]
    (change-body-color color))

  (om/root
    tutorial-view
    app
    {:target (. js/document (getElementById "app"))}))
