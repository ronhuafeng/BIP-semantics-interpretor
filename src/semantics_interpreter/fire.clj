(ns semantics-interpreter.fire
  (:use semantics-interpreter.data-structure)
  (:use semantics-interpreter.state-queries)
  (:use semantics-interpreter.dataset)
  (:use semantics-interpreter.firing-selection))

(defmulti fire-a-component-via-export
  "Fire a component via its 'export'port.
   Return type: boolean"
  (fn [component port] (:type component)))

(defmethod fire-a-component-via-export 'atomic
  [component port]
  (let [t (first (reveal-components-via-port component port))]
    (set-current component (:to t))))

(defmethod fire-a-component-via-export 'compound
  #_ ("Fire the component connected to the port.")
  [component port]
  (let [t (first
            (reveal-components-via-port component
              port))]
    (apply fire-a-component-via-export t)))

(defmethod fire-a-component-via-export 'interaction
  #_ ("Fire every component connected to this interaction
      via their corresponding port.")
  [component port]
  (let [tl
        (reveal-components-via-port component
          port)]
    (reduce #(and %1 %2)
      true
      (map (partial apply fire-a-component-via-export)
        tl))))

(defmulti fire-a-component
  "Fire a component and set it to a new state.
   Return type: boolean"
  (fn [component] (:type component)))

(defmethod fire-a-component 'atomic
  [component]
  #_ ("select a transition to fire.")
  (let [t (select-a-enabled-component
            (filter
              (fn [t]
                (and (attrs-equal? (get-current component)
                       (:from t)
                       :name )
                  (= false (:export? (:port t)))))
              (:transitions component)))]
    (if (not-empty t)
      #_ ("if new state set successfully, the function return true.")
      (set-current component (:to t))
      false)))
(defmethod fire-a-component 'compound
  [component]
  (fire-a-component
    (select-a-enabled-component
      (filter component-enable? (:components component)))))

(defmethod fire-a-component 'interaction
  [component]
  (reduce
    #(and %1 %2)
    true
    (map (fn [t]
           (fire-a-component-via-export
             (:source-component t)
             (:source t)))
      (:port-bindings component))))
