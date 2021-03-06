(ns semantics-interpreter.firing-selection
  (:use semantics-interpreter.data-structure)
  (:use semantics-interpreter.state-queries))

(defn select-a-enabled-component
  [enabled-components]
  {:pre [(pos? (count enabled-components))
         "Empty list of enabled components is not allowed!"]}
  (rand-nth enabled-components))

;; Should be multi-method. Digging into atomic/compound/interaction through
;; a port will meet different things.
(defmulti reveal-components-via-port
  "Reveal the inner parts of a component through the 'export' port."
  (fn [component port] (:type component)))

(defmethod reveal-components-via-port 'atomic
  [component port]
  {:pre [(= true (:export? port))]
   :post [(= 1 (count %))]}
  #_ ("Will generate a list of one transition.")
  (filter
    (fn [t]
      (and (attrs-equal? (get-current component)
             (:from t)
             :name )
        (attrs-equal? port (:port t) :name )))
    (:transitions component)))


(defmethod reveal-components-via-port 'compound
  #_ ("Generate a tuple (component/interaction, port) wrapped in a list.")
  [component port]
  {:post [(= 1 (count %))]}
  (let [tl (filter
             (fn [t]
               (attrs-equal?
                 port
                 (:target t)
                 :name ))
             (:port-bindings component))]
    (map (fn [t]
           [(:source-component t) (:source t)])
      tl)))
(defmethod reveal-components-via-port 'interaction
  #_ ("Generate a list of one tuple (component/interaction, port).")
  [component port]
  (map
    (fn [t]
      [(:source-component t) (:source t)])
    (:port-bindings component)))
